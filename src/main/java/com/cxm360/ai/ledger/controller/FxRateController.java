package com.cxm360.ai.ledger.controller;

import com.cxm360.ai.ledger.model.FxRate;
import com.cxm360.ai.ledger.service.FxRateService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@RestController
@RequestMapping("${api.base-path}/fx/rates")
@RequiredArgsConstructor
public class FxRateController {

    private final FxRateService fxRateService;

    /**
     * Create or update an exchange rate.
     */
    @PostMapping
    public ResponseEntity<FxRate> upsertFxRate(
            @RequestParam String baseCode,
            @RequestParam String quoteCode,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOf,
            @RequestParam BigDecimal rate,
            @RequestParam(required = false) String source) {
        
        FxRate fxRate = fxRateService.upsertFxRate(baseCode, quoteCode, asOf, rate, source != null ? source : "MANUAL");
        return ResponseEntity.ok(fxRate);
    }

    /**
     * Get the most recent exchange rate for a currency pair as of a specific date.
     */
    @GetMapping
    public ResponseEntity<FxRate> getMostRecentRate(
            @RequestParam String baseCode,
            @RequestParam String quoteCode,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOf) {
        
        Optional<FxRate> rate = fxRateService.getMostRecentRate(baseCode, quoteCode, asOf);
        return rate.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get an exchange rate for a currency pair on a specific date.
     */
    @GetMapping("/{baseCode}/{quoteCode}/{asOf}")
    public ResponseEntity<FxRate> getRate(
            @PathVariable String baseCode,
            @PathVariable String quoteCode,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOf) {
        
        Optional<FxRate> rate = fxRateService.getRate(baseCode, quoteCode, asOf);
        return rate.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Convert an amount from one currency to another.
     */
    @GetMapping("/convert")
    public ResponseEntity<BigDecimal> convertAmount(
            @RequestParam BigDecimal amount,
            @RequestParam String fromCurrency,
            @RequestParam String toCurrency,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOf) {
        
        try {
            BigDecimal convertedAmount = fxRateService.convertAmount(amount, fromCurrency, toCurrency, asOf);
            return ResponseEntity.ok(convertedAmount);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
