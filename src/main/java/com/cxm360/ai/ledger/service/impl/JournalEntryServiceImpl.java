package com.cxm360.ai.ledger.service.impl;

import com.cxm360.ai.ledger.context.TenantContext;
import com.cxm360.ai.ledger.dto.CreateJournalEntryRequest;
import com.cxm360.ai.ledger.dto.JournalEntryDto;
import com.cxm360.ai.ledger.exception.CurrencyConversionException;
import com.cxm360.ai.ledger.exception.JournalEntryBalancingException;
import com.cxm360.ai.ledger.exception.ResourceNotFoundException;
import com.cxm360.ai.ledger.mapper.JournalEntryMapper;
import com.cxm360.ai.ledger.model.*;
import com.cxm360.ai.ledger.model.enums.JournalEntryStatus;
import com.cxm360.ai.ledger.model.enums.NormalSide;
import com.cxm360.ai.ledger.repository.*;
import com.cxm360.ai.ledger.validation.BasicValidationResult;
import com.cxm360.ai.ledger.validation.FunctionalUtils;
import com.cxm360.ai.ledger.validation.JournalEntryValidator;
import io.vavr.control.Option;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JournalEntryServiceImpl implements com.cxm360.ai.ledger.service.JournalEntryService {

    private final JournalEntryRepository journalEntryRepository;
    private final PostingRepository postingRepository;
    private final AccountRepository accountRepository;
    private final JournalEntryMapper journalEntryMapper;
    private final TenantRepository tenantRepository;
    private final LedgerRepository ledgerRepository;
    private final PeriodRepository periodRepository;
    private final FxRateRepository fxRateRepository;
    private final BalanceSnapshotRepository balanceSnapshotRepository;

    @Override
    @Transactional
    public JournalEntryDto createJournalEntry(UUID ledgerId, CreateJournalEntryRequest request) {
        // Get tenant from context using functional approach
        UUID currentTenantId = FunctionalUtils.requireNonNull(
            TenantContext.getCurrentTenant(), 
            "Tenant context not set"
        );

        // Validate that ledgerId exists and tenantId matches context using functional approach
        Ledger ledger = Option.ofOptional(ledgerRepository.findById(ledgerId))
                .getOrElseThrow(() -> new ResourceNotFoundException("Ledger", ledgerId.toString()));
        
        FunctionalUtils.requireTrue(
            ledger.getTenant().getId().equals(currentTenantId),
            () -> new IllegalArgumentException("Ledger does not belong to current tenant")
        );

        JournalEntry journalEntry = journalEntryMapper.toEntity(request);
        journalEntry.setLedger(ledger);
        
        // Set tenant from context using functional approach
        Tenant tenant = Option.ofOptional(tenantRepository.findById(currentTenantId))
                .getOrElseThrow(() -> new IllegalArgumentException("Tenant not found: " + currentTenantId));
        journalEntry.setTenant(tenant);

        // Set the bidirectional relationship
        for (JournalLine line : journalEntry.getJournalLines()) {
            line.setJournalEntry(journalEntry);
            // Set tenant on lines as well
            line.setTenant(tenant);
        }

        // Validate the journal entry using the validation framework
        BasicValidationResult<JournalEntry> validationResult = 
                JournalEntryValidator.validateCreation(journalEntry, currentTenantId);
        
        FunctionalUtils.requireTrue(
            validationResult.isSuccess(),
            () -> new IllegalArgumentException("Journal entry validation failed: " + String.join(", ", validationResult.getErrorsAsList()))
        );

        JournalEntry savedEntry = journalEntryRepository.save(journalEntry);
        return journalEntryMapper.toDto(savedEntry);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<JournalEntryDto> getJournalEntryById(UUID journalEntryId) {
        return journalEntryRepository.findById(journalEntryId).map(journalEntryMapper::toDto);
    }

    @Override
    @Transactional
    public JournalEntryDto postJournalEntry(UUID journalEntryId) {
        // Get tenant from context using functional approach
        UUID currentTenantId = FunctionalUtils.requireNonNull(
            TenantContext.getCurrentTenant(), 
            "Tenant context not set"
        );

        JournalEntry journalEntry = Option.ofOptional(journalEntryRepository.findById(journalEntryId))
                .getOrElseThrow(() -> new ResourceNotFoundException("Journal Entry", journalEntryId.toString()));

        // Validate that journal entry can be posted using validation framework
        BasicValidationResult<JournalEntry> postValidation = 
                JournalEntryValidator.validateCanPost(journalEntry, currentTenantId);
        
        FunctionalUtils.requireTrue(
            postValidation.isSuccess(),
            () -> new IllegalStateException(postValidation.getErrorsAsList().get(0))
        );

        // 1. VALIDATE THE JOURNAL ENTRY using validation framework
        // Validate journal lines
        BasicValidationResult<List<JournalLine>> linesValidation = 
                JournalEntryValidator.validateJournalLines(journalEntry.getJournalLines(), currentTenantId);
        
        FunctionalUtils.requireTrue(
            linesValidation.isSuccess(),
            () -> new IllegalArgumentException(linesValidation.getErrorsAsList().get(0))
        );

        // Validate currency constraints
        BasicValidationResult<List<JournalLine>> currencyValidation = 
                JournalEntryValidator.validateCurrencyConstraints(journalEntry.getJournalLines());
        
        FunctionalUtils.requireTrue(
            currencyValidation.isSuccess(),
            () -> new IllegalArgumentException(currencyValidation.getErrorsAsList().get(0))
        );

        // Check that the accounting date is in an OPEN period using validation framework
        Period period = periodRepository.findPeriodContainingDate(currentTenantId, journalEntry.getLedger().getId(), journalEntry.getAccountingDate())
                .orElse(null);
        
        BasicValidationResult<Period> periodValidation = 
                JournalEntryValidator.validateAccountingDate(period, journalEntry.getAccountingDate());
        
        FunctionalUtils.requireTrue(
            periodValidation.isSuccess(),
            () -> new IllegalArgumentException(periodValidation.getErrorsAsList().get(0))
        );

        // Perform currency conversion for all lines to the ledger's functional currency
        String functionalCurrencyCode = journalEntry.getLedger().getFunctionalCurrency().getCode();
        long totalFunctionalDebits = 0;
        long totalFunctionalCredits = 0;

        for (JournalLine line : journalEntry.getJournalLines()) {
            if (line.getCurrencyCode().equals(functionalCurrencyCode)) {
                line.setFxRate(BigDecimal.ONE);
                line.setFunctionalAmountMinor(line.getAmountMinor());
            } else {
                // Get FX rate for the line currency to functional currency
                FxRate fxRate = fxRateRepository.findMostRecentRate(line.getCurrencyCode(), functionalCurrencyCode, journalEntry.getAccountingDate())
                        .orElseThrow(() -> new CurrencyConversionException(line.getCurrencyCode(), functionalCurrencyCode, "No FX rate found as of " + journalEntry.getAccountingDate()));
                
                line.setFxRate(fxRate.getRate());
                
                // Convert amount to functional currency (using BigDecimal for precision)
                BigDecimal amount = BigDecimal.valueOf(line.getAmountMinor());
                BigDecimal convertedAmount = amount.multiply(fxRate.getRate());
                line.setFunctionalAmountMinor(convertedAmount.longValue());
            }

            // Accumulate functional amounts for balancing
            if (line.getDirection() == NormalSide.DEBIT) {
                totalFunctionalDebits += line.getFunctionalAmountMinor();
            } else {
                totalFunctionalCredits += line.getFunctionalAmountMinor();
            }
        }

        // Verify the entry balances using validation framework
        final long finalTotalDebits = totalFunctionalDebits;
        final long finalTotalCredits = totalFunctionalCredits;
        
        BasicValidationResult<JournalEntry> balancingValidation = 
                JournalEntryValidator.validateBalancing(journalEntry, finalTotalDebits, finalTotalCredits);
        
        FunctionalUtils.requireTrue(
            balancingValidation.isSuccess(),
            () -> new JournalEntryBalancingException(finalTotalDebits, finalTotalCredits)
        );

        // 2. CREATE POSTINGS
        List<Posting> postings = new ArrayList<>();
        for (JournalLine line : journalEntry.getJournalLines()) {
            Posting posting = Posting.builder()
                    .tenant(journalEntry.getTenant())
                    .ledger(journalEntry.getLedger())
                    .journalEntry(journalEntry)
                    .journalLine(line)
                    .account(line.getAccount())
                    .party(line.getParty())
                    .accountingDate(journalEntry.getAccountingDate())
                    .currencyCode(line.getCurrencyCode())
                    .amountMinorSigned(line.getDirection() == NormalSide.DEBIT ? line.getAmountMinor() : -line.getAmountMinor())
                    .postedAt(OffsetDateTime.now())
                    .build();
            
            postings.add(posting);
        }
        
        // Save all postings
        postingRepository.saveAll(postings);

        // 3. UPDATE JOURNAL ENTRY STATUS
        journalEntry.setStatus(JournalEntryStatus.POSTED);
        journalEntry.setPostedAt(OffsetDateTime.now());
        
        // Assign sequence number (get next available for this ledger)
        Long nextSequence = journalEntryRepository.findNextSequenceNumber(currentTenantId, journalEntry.getLedger().getId())
                .orElse(1L);
        journalEntry.setSequenceNo(nextSequence);
        
        JournalEntry savedEntry = journalEntryRepository.save(journalEntry);

        return journalEntryMapper.toDto(savedEntry);
    }


}
