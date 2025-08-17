package com.cxm360.ai.ledger.controller;

import com.cxm360.ai.ledger.dto.AccountDto;
import com.cxm360.ai.ledger.dto.CreateAccountRequest;
import com.cxm360.ai.ledger.dto.UpdateAccountRequest;
import com.cxm360.ai.ledger.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/ledgers/{ledgerId}/accounts")
    public ResponseEntity<AccountDto> createAccount(
            @PathVariable UUID ledgerId,
            @Valid @RequestBody CreateAccountRequest request) {
        AccountDto createdAccount = accountService.createAccount(ledgerId, request);
        return new ResponseEntity<>(createdAccount, HttpStatus.CREATED);
    }

    @GetMapping("/accounts/{accountId}")
    public ResponseEntity<AccountDto> getAccountById(@PathVariable UUID accountId) {
        return accountService.getAccountById(accountId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/ledgers/{ledgerId}/accounts")
    public ResponseEntity<List<AccountDto>> getAccountsByLedgerId(@PathVariable UUID ledgerId) {
        List<AccountDto> accounts = accountService.getAccountsByLedgerId(ledgerId);
        return ResponseEntity.ok(accounts);
    }

    @PutMapping("/accounts/{accountId}")
    public ResponseEntity<AccountDto> updateAccount(
            @PathVariable UUID accountId,
            @Valid @RequestBody UpdateAccountRequest request) {
        AccountDto updatedAccount = accountService.updateAccount(accountId, request);
        return ResponseEntity.ok(updatedAccount);
    }
}
