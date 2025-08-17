package com.cxm360.ai.ledger.model;

import com.cxm360.ai.ledger.config.JsonNodeConverter;
import com.fasterxml.jackson.databind.JsonNode;
import com.cxm360.ai.ledger.model.Tenant;
import com.cxm360.ai.ledger.model.enums.PartyType;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Represents a party involved in financial transactions, such as a customer, vendor, or employee.
 */
@Entity
@Table(name = "parties")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Party {

    /**
     * The unique identifier for the party.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * The tenant this party belongs to.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    /**
     * The name of the party.
     */
    @Column(nullable = false)
    private String name;

    /**
     * The type of the party (e.g., CUSTOMER, VENDOR).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PartyType type;

    /**
     * An optional external identifier, for linking to other systems like a CRM or payroll system.
     */
    @Column(name = "external_id")
    private String externalId;

    /**
     * JSON object containing contact details like address, phone number, etc.
     * Using JsonNode for type safety and validation.
     */
    @Column(name = "contact_details", columnDefinition = "jsonb") // For PostgreSQL
    @Convert(converter = JsonNodeConverter.class)
    private JsonNode contactDetails;

}
