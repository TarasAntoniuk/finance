package com.tarasantoniuk.finance.banking.bankpayment.enums;

/**
 * Types of bank payment transactions
 */
public enum PaymentType {
    /**
     * Payment to supplier for goods/services
     */
    SUPPLIER_PAYMENT,

    /**
     * Salary payment to employee
     */
    SALARY,

    /**
     * Tax payment to government
     */
    TAX_PAYMENT,

    /**
     * Loan repayment to bank
     */
    LOAN_REPAYMENT,

    /**
     * Payment to contractor/freelancer
     */
    CONTRACTOR_PAYMENT,

    /**
     * Utility payment (electricity, water, gas, etc.)
     */
    UTILITY_PAYMENT,

    /**
     * Rent payment
     */
    RENT,

    /**
     * Refund to customer
     */
    REFUND,

    /**
     * Transfer between own accounts
     */
    INTERNAL_TRANSFER,

    /**
     * Other payment type
     */
    OTHER
}