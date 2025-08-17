package com.cxm360.ai.ledger.filter;

import com.cxm360.ai.ledger.context.TenantContext;
import io.vavr.control.Option;
import io.vavr.control.Try;
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
 * Uses Vavr functional style for improved error handling and composition.
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
            extractAndSetTenantContext(request);
            chain.doFilter(request, response);
        } finally {
            // Always clear the context to prevent memory leaks
            TenantContext.clear();
        }
    }

    /**
     * Extracts tenant ID from request and sets it in context using functional composition.
     */
    private void extractAndSetTenantContext(ServletRequest request) {
        Option.of(request)
                .filter(HttpServletRequest.class::isInstance)
                .map(req -> (HttpServletRequest) req)
                .flatMap(this::extractTenantHeader)
                .flatMap(this::parseTenantId)
                .peek(this::setTenantContext)
                .onEmpty(() -> log.debug("No tenant ID header found, skipping tenant context"));
    }

    /**
     * Extracts tenant header value from HTTP request.
     */
    private Option<String> extractTenantHeader(HttpServletRequest httpRequest) {
        return Option.of(httpRequest.getHeader(TENANT_HEADER))
                .filter(StringUtils::hasText);
    }

    /**
     * Parses tenant ID string to UUID using Try for safe conversion.
     */
    private Option<UUID> parseTenantId(String tenantIdHeader) {
        return Try.of(() -> UUID.fromString(tenantIdHeader))
                .onSuccess(tenantId -> log.debug("Successfully parsed tenant ID: {}", tenantId))
                .onFailure(e -> log.warn("Invalid tenant ID format in header: {}", tenantIdHeader))
                .toOption();
    }

    /**
     * Sets the tenant context and logs the action.
     */
    private void setTenantContext(UUID tenantId) {
        TenantContext.setCurrentTenant(tenantId);
        log.debug("Set tenant context: {}", tenantId);
    }
}
