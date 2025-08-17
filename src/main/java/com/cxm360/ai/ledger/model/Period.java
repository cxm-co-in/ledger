package com.cxm360.ai.ledger.model;

import com.cxm360.ai.ledger.model.Tenant;
import com.cxm360.ai.ledger.model.enums.PeriodStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Represents a financial period (e.g., a month or a quarter) for a ledger.
 * Periods can be opened or closed to control transaction posting.
 */
@Entity
@Table(name = "periods")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Period {

    /**
     * The unique identifier for the period.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * The tenant this period belongs to.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    /**
     * The identifier for the ledger this period is associated with.
     */
    @Column(name = "ledger_id", nullable = false)
    private UUID ledgerId;

    /**
     * The start date of the financial period (inclusive).
     */
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /**
     * The end date of the financial period (inclusive).
     */
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    /**
     * The status of the period (e.g., OPEN, CLOSED, LOCKED).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PeriodStatus status;

}
