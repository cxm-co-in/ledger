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
        // Get tenant from context
        UUID currentTenantId = TenantContext.getCurrentTenant();
        if (currentTenantId == null) {
            throw new IllegalStateException("Tenant context not set");
        }

        // Validate that ledgerId exists and tenantId matches context
        Ledger ledger = ledgerRepository.findById(ledgerId)
                .orElseThrow(() -> new ResourceNotFoundException("Ledger", ledgerId.toString()));
        
        if (!ledger.getTenant().getId().equals(currentTenantId)) {
            throw new IllegalArgumentException("Ledger does not belong to current tenant");
        }

        JournalEntry journalEntry = journalEntryMapper.toEntity(request);
        journalEntry.setLedger(ledger);
        // Set tenant from context
        Tenant tenant = tenantRepository.findById(currentTenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + currentTenantId));
        journalEntry.setTenant(tenant);

        // Set the bidirectional relationship
        for (JournalLine line : journalEntry.getJournalLines()) {
            line.setJournalEntry(journalEntry);
            // Set tenant on lines as well
            line.setTenant(tenant);
        }

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
        // Get tenant from context
        UUID currentTenantId = TenantContext.getCurrentTenant();
        if (currentTenantId == null) {
            throw new IllegalStateException("Tenant context not set");
        }

        JournalEntry journalEntry = journalEntryRepository.findById(journalEntryId)
                .orElseThrow(() -> new ResourceNotFoundException("Journal Entry", journalEntryId.toString()));

        if (journalEntry.getStatus() != JournalEntryStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT entries can be posted.");
        }

        // 1. VALIDATE THE JOURNAL ENTRY
        // Check that all accounts on the lines exist and are active for the given tenant
        for (JournalLine line : journalEntry.getJournalLines()) {
            Account account = accountRepository.findById(line.getAccount().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Account", line.getAccount().getId().toString()));
            
            if (!account.getTenant().getId().equals(currentTenantId)) {
                throw new IllegalArgumentException("Account does not belong to current tenant");
            }
            
            if (!account.isActive()) {
                throw new IllegalArgumentException("Account is not active: " + account.getCode());
            }
            
            // Validate currency constraint if account is single currency
            if (account.getCurrencyMode() == com.cxm360.ai.ledger.model.enums.CurrencyMode.SINGLE) {
                if (!line.getCurrencyCode().equals(account.getCurrencyCode())) {
                    throw new IllegalArgumentException("Account " + account.getCode() + " only accepts currency: " + account.getCurrencyCode());
                }
            }
        }

        // Check that the accounting date is in an OPEN period
        Period period = periodRepository.findPeriodContainingDate(currentTenantId, journalEntry.getLedger().getId(), journalEntry.getAccountingDate())
                .orElseThrow(() -> new IllegalArgumentException("No period found for accounting date: " + journalEntry.getAccountingDate()));
        
        if (period.getStatus() != com.cxm360.ai.ledger.model.enums.PeriodStatus.OPEN) {
            throw new IllegalArgumentException("Period is not open for accounting date: " + journalEntry.getAccountingDate());
        }

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

        // Verify the entry balances (functional debits must equal functional credits)
        if (totalFunctionalDebits != totalFunctionalCredits) {
            throw new JournalEntryBalancingException(totalFunctionalDebits, totalFunctionalCredits);
        }

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
