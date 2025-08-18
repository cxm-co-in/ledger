package com.cxm360.ai.ledger.controller;

import com.cxm360.ai.ledger.dto.CreateTenantRequest;
import com.cxm360.ai.ledger.dto.TenantDto;
import com.cxm360.ai.ledger.dto.UpdateTenantRequest;
import com.cxm360.ai.ledger.service.TenantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("${api.base-path}")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    @PostMapping("/tenants")
    public ResponseEntity<TenantDto> createTenant(@Valid @RequestBody CreateTenantRequest request) {
        TenantDto createdTenant = tenantService.createTenant(request);
        return new ResponseEntity<>(createdTenant, HttpStatus.CREATED);
    }

    @GetMapping("/tenants/{tenantId}")
    public ResponseEntity<TenantDto> getTenantById(@PathVariable UUID tenantId) {
        return tenantService.getTenantById(tenantId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/tenants")
    public ResponseEntity<List<TenantDto>> getAllTenants() {
        List<TenantDto> tenants = tenantService.getAllTenants();
        return ResponseEntity.ok(tenants);
    }

    @PutMapping("/tenants/{tenantId}")
    public ResponseEntity<TenantDto> updateTenant(
            @PathVariable UUID tenantId,
            @Valid @RequestBody UpdateTenantRequest request) {
        TenantDto updatedTenant = tenantService.updateTenant(tenantId, request);
        return ResponseEntity.ok(updatedTenant);
    }
}
