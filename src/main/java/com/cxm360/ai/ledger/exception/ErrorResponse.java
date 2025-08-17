package com.cxm360.ai.ledger.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standardized error response structure for API errors.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    
    /**
     * Timestamp when the error occurred.
     */
    private LocalDateTime timestamp;
    
    /**
     * HTTP status code.
     */
    private int status;
    
    /**
     * Error type/category.
     */
    private String error;
    
    /**
     * Human-readable error message.
     */
    private String message;
    
    /**
     * Request path that caused the error.
     */
    private String path;
}
