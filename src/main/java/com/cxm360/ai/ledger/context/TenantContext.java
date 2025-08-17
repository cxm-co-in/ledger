package com.cxm360.ai.ledger.context;

import java.util.UUID;

/**
 * Thread-local context for storing tenant information.
 * This allows services to access tenant context without passing it as a parameter.
 */
public class TenantContext {
    
    private static final ThreadLocal<UUID> currentTenant = new ThreadLocal<>();
    
    /**
     * Set the current tenant ID for the current thread.
     * 
     * @param tenantId The tenant ID to set
     */
    public static void setCurrentTenant(UUID tenantId) {
        currentTenant.set(tenantId);
    }
    
    /**
     * Get the current tenant ID for the current thread.
     * 
     * @return The current tenant ID, or null if not set
     */
    public static UUID getCurrentTenant() {
        return currentTenant.get();
    }
    
    /**
     * Clear the current tenant context for the current thread.
     * This should be called in cleanup to prevent memory leaks.
     */
    public static void clear() {
        currentTenant.remove();
    }
    
    /**
     * Check if a tenant context is currently set.
     * 
     * @return true if tenant context is set, false otherwise
     */
    public static boolean hasCurrentTenant() {
        return currentTenant.get() != null;
    }
}
