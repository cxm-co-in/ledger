package com.cxm360.ai.ledger.service.impl;

import com.cxm360.ai.ledger.context.TenantContext;
import com.cxm360.ai.ledger.dto.CreateJournalEntryRequest;
import com.cxm360.ai.ledger.dto.JournalEntryDto;
import com.cxm360.ai.ledger.mapper.JournalEntryMapper;
import com.cxm360.ai.ledger.model.JournalEntry;
import com.cxm360.ai.ledger.model.JournalLine;
import com.cxm360.ai.ledger.model.enums.JournalEntryStatus;
import com.cxm360.ai.ledger.repository.AccountRepository;
import com.cxm360.ai.ledger.repository.JournalEntryRepository;
import com.cxm360.ai.ledger.repository.PostingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JournalEntryServiceImpl implements com.cxm360.ai.ledger.service.JournalEntryService {

    private final JournalEntryRepository journalEntryRepository;
    private final PostingRepository postingRepository;
    private final AccountRepository accountRepository;
    private final JournalEntryMapper journalEntryMapper;
    private final com.cxm360.ai.ledger.repository.TenantRepository tenantRepository;

    @Override
    @Transactional
    public JournalEntryDto createJournalEntry(UUID ledgerId, CreateJournalEntryRequest request) {
        // Get tenant from context
        UUID currentTenantId = TenantContext.getCurrentTenant();
        if (currentTenantId == null) {
            throw new IllegalStateException("Tenant context not set");
        }

        // TODO: Validate that ledgerId exists and tenantId matches context

        JournalEntry journalEntry = journalEntryMapper.toEntity(request);
        journalEntry.setLedgerId(ledgerId);
        // Set tenant from context
        com.cxm360.ai.ledger.model.Tenant tenant = tenantRepository.findById(currentTenantId)
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
        // TODO: Add proper error handling for not found
        JournalEntry journalEntry = journalEntryRepository.findById(journalEntryId)
                .orElseThrow(() -> new IllegalArgumentException("Journal Entry not found"));

        if (journalEntry.getStatus() != JournalEntryStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT entries can be posted.");
        }

        // 1. VALIDATE THE JOURNAL ENTRY
        // TODO: Check that all accounts on the lines exist and are active for the given tenant.
        // TODO: Check that the accounting date is in an OPEN period.
        // TODO: Perform currency conversion for all lines to the ledger's functional currency.
        // TODO: Verify the entry balances (functional debits must equal functional credits).

        // 2. CREATE POSTINGS
        // TODO: For each JournalLine, create a corresponding Posting record.
        //  - posting.setAccountId(line.getAccount().getId());
        //  - posting.setAmountMinorSigned( ... ); // positive for DEBIT, negative for CREDIT
        //  - ... and so on, copying all relevant data.
        //  - postingRepository.save(posting);

        // 3. UPDATE JOURNAL ENTRY STATUS
        journalEntry.setStatus(JournalEntryStatus.POSTED);
        // TODO: Set sequence number and postedAt timestamp
        JournalEntry savedEntry = journalEntryRepository.save(journalEntry);

        return journalEntryMapper.toDto(savedEntry);
    }


}
