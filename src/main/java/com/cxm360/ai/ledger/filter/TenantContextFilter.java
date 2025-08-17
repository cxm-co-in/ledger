package com.cxm360.ai.ledger.filter;

import com.cxm360.ai.ledger.context.TenantContext;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter to automatically set tenant context from HTTP headers.
 * This filter extracts the X-Tenant-ID header and sets it in the thread-local context.
 */
@Component
@Order(1)
@Slf4j
public class TenantContextFilter implements Filter {

    private static final String TENANT_HEADER = "X-Tenant-ID";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        try {
            if (request instanceof HttpServletRequest httpRequest) {
                String tenantIdHeader = httpRequest.getHeader(TENANT_HEADER);
                
                if (StringUtils.hasText(tenantIdHeader)) {
                    try {
                        UUID tenantId = UUID.fromString(tenantIdHeader);
                        TenantContext.setCurrentTenant(tenantId);
                        log.debug("Set tenant context: {}", tenantId);
                    } catch (IllegalArgumentException e) {
                        log.warn("Invalid tenant ID format in header: {}", tenantIdHeader);
                    }
                } else {
                    log.debug("No tenant ID header found, skipping tenant context");
                }
            }
            
            chain.doFilter(request, response);
        } finally {
            // Always clear the context to prevent memory leaks
            TenantContext.clear();
        }
    }
}
