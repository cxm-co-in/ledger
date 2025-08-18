package com.cxm360.ai.ledger.model;

import com.cxm360.ai.ledger.model.Tenant;
import com.cxm360.ai.ledger.model.enums.AccountType;
import com.cxm360.ai.ledger.model.enums.CurrencyMode;
import com.cxm360.ai.ledger.model.enums.NormalSide;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Represents an account in the chart of accounts for a specific ledger.
 * An account is where financial transactions are recorded.
 */
@Entity
@Table(name = "account")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    /**
     * The unique identifier for the account.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * The tenant this account belongs to, used for data isolation.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    /**
     * The ledger this account is a part of.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ledger_id", nullable = false)
    private Ledger ledger;

    /**
     * A unique code for the account (e.g., "1010", "6050").
     */
    @Column(nullable = false, unique = true)
    private String code;

    /**
     * The human-readable name of the account (e.g., "Cash on Hand", "Salaries Expense").
     */
    @Column(nullable = false)
    private String name;

    /**
     * The financial type of the account (e.g., ASSET, LIABILITY, REVENUE).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType type;

    /**
     * The normal balance side of the account (DEBIT or CREDIT).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "normal_side", nullable = false)
    private NormalSide normalSide;

    /**
     * The currency constraint for the account (SINGLE or MULTI).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "currency_mode", nullable = false)
    private CurrencyMode currencyMode;

    /**
     * The specific currency code (e.g., "USD") if the currency_mode is SINGLE.
     */
    @Column(name = "currency_code")
    private String currencyCode;

    /**
     * Whether the account is active and can be posted to.
     */
    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    /**
     * A self-referencing relationship for creating a hierarchical chart of accounts.
     * This links an account to its parent account.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_account_id")
    private Account parentAccount;

    // Note: A @ManyToOne relationship to the Ledger entity will be added later
    // once the Ledger entity itself has been created.
    
    /**
     * Set the ledger for this account.
     * This method is needed for service layer operations.
     */
    public void setLedger(Ledger ledger) {
        this.ledger = ledger;
    }
    
    /**
     * Set the parent account for this account.
     * This method is needed for service layer operations.
     */
    public void setParentAccount(Account parentAccount) {
        this.parentAccount = parentAccount;
    }
    
    /**
     * Set the active status for this account.
     * This method is needed for service layer operations.
     */
    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }
}
