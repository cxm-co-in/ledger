package com.cxm360.ai.ledger.context;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TenantContextTest {

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void testSetAndGetCurrentTenant() {
        UUID tenantId = UUID.randomUUID();
        TenantContext.setCurrentTenant(tenantId);
        
        assertEquals(tenantId, TenantContext.getCurrentTenant());
    }

    @Test
    void testHasCurrentTenant() {
        assertFalse(TenantContext.hasCurrentTenant());
        
        UUID tenantId = UUID.randomUUID();
        TenantContext.setCurrentTenant(tenantId);
        
        assertTrue(TenantContext.hasCurrentTenant());
    }

    @Test
    void testClear() {
        UUID tenantId = UUID.randomUUID();
        TenantContext.setCurrentTenant(tenantId);
        
        assertTrue(TenantContext.hasCurrentTenant());
        
        TenantContext.clear();
        
        assertFalse(TenantContext.hasCurrentTenant());
        assertNull(TenantContext.getCurrentTenant());
    }

    @Test
    void testMultipleTenants() {
        UUID tenant1 = UUID.randomUUID();
        UUID tenant2 = UUID.randomUUID();
        
        TenantContext.setCurrentTenant(tenant1);
        assertEquals(tenant1, TenantContext.getCurrentTenant());
        
        TenantContext.setCurrentTenant(tenant2);
        assertEquals(tenant2, TenantContext.getCurrentTenant());
    }
}
