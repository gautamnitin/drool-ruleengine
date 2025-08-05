package com.drool.ruleengine.service;

import com.drool.ruleengine.model.Rule;
import com.drool.ruleengine.model.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kie.api.runtime.KieSession;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service that provides access to rule engine functionality.
 * This service delegates to the appropriate rule engine implementation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RuleEngineService {
    private final RuleEngine ruleEngine;
    
    /**
     * Process a single transaction against the rules for a specific customer.
     * 
     * @param transaction The transaction to process
     * @param customerId The ID of the customer whose rules should be applied
     * @return The processed transaction with category assigned
     */
    public Transaction processTransaction(Transaction transaction, String customerId) {
        return ruleEngine.processTransaction(transaction, customerId);
    }
    
    /**
     * Process a batch of transactions against the rules for a specific customer.
     * 
     * @param transactions The list of transactions to process
     * @param customerId The ID of the customer whose rules should be applied
     * @return The processed transactions with categories assigned
     */
    public List<Transaction> processTransactions(List<Transaction> transactions, String customerId) {
        return ruleEngine.processTransactions(transactions, customerId);
    }
    
    /**
     * Get the rules for a specific customer.
     * 
     * @param customerId The ID of the customer
     * @return List of rules for the customer
     */
    public List<Rule> getRulesForCustomer(String customerId) {
        return ruleEngine.getRulesForCustomer(customerId);
    }
    
    /**
     * For backward compatibility with existing code.
     * This method will be deprecated in future versions.
     * 
     * @param customerId The ID of the customer
     * @return KieSession for the customer
     */
    public KieSession getSessionWithRules(String customerId) {
        if (ruleEngine instanceof DroolsRuleEngine) {
            log.warn("Direct access to KieSession is deprecated. Use processTransaction or processTransactions instead.");
            return ((DroolsRuleEngine) ruleEngine).getSessionWithRules(customerId);
        } else {
            throw new UnsupportedOperationException("This rule engine implementation does not support direct KieSession access");
        }
    }
}
