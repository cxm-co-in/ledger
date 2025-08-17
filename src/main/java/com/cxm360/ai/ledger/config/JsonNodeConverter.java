package com.cxm360.ai.ledger.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

/**
 * Hibernate converter for JsonNode fields to handle JSON storage in the database.
 * This converter automatically converts JsonNode objects to/from JSON strings for database storage.
 */
@Converter
@Slf4j
public class JsonNodeConverter implements AttributeConverter<JsonNode, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(JsonNode attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            log.error("Error converting JsonNode to JSON string", e);
            throw new RuntimeException("Error converting JsonNode to JSON string", e);
        }
    }

    @Override
    public JsonNode convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readTree(dbData);
        } catch (JsonProcessingException e) {
            log.error("Error converting JSON string to JsonNode", e);
            throw new RuntimeException("Error converting JSON string to JsonNode", e);
        }
    }
}
