package com.drool.ruleengine.service;

import com.drool.ruleengine.model.Rule;
import com.drool.ruleengine.model.Transaction;

import java.util.List;

/**
 * Interface for rule engine implementations.
 * This abstraction allows for different rule engine implementations
 * to be used interchangeably.
 */
public interface RuleEngine {
    
    /**
     * Process a single transaction against the rules for a specific customer.
     * 
     * @param transaction The transaction to process
     * @param customerId The ID of the customer whose rules should be applied
     * @return The processed transaction with category assigned
     */
    Transaction processTransaction(Transaction transaction, String customerId);
    
    /**
     * Process a batch of transactions against the rules for a specific customer.
     * 
     * @param transactions The list of transactions to process
     * @param customerId The ID of the customer whose rules should be applied
     * @return The processed transactions with categories assigned
     */
    List<Transaction> processTransactions(List<Transaction> transactions, String customerId);
    
    /**
     * Get the rules for a specific customer.
     * 
     * @param customerId The ID of the customer
     * @return List of rules for the customer
     */
    List<Rule> getRulesForCustomer(String customerId);
}