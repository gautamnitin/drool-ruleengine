package com.drool.ruleengine.service;

import com.drool.ruleengine.model.Rule;
import com.drool.ruleengine.model.Transaction;
import com.drool.ruleengine.repository.RuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Primary;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A simple rule engine implementation using Spring Expression Language (SpEL).
 * This is an alternative to the Drools implementation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SimpleRuleEngine implements RuleEngine {
    private final RuleRepository ruleRepo;
    
    // Cache for compiled expressions
    private final Map<String, Expression> expressionCache = new ConcurrentHashMap<>();
    
    // Parser for SpEL expressions
    private final ExpressionParser parser = new SpelExpressionParser();

    @Override
    public Transaction processTransaction(Transaction transaction, String customerId) {
        log.debug("Processing transaction for customer: {}", customerId);
        
        // Get rules for this customer
        List<Rule> rules = getRulesForCustomer(customerId);
        
        // Apply rules to the transaction
        applyRules(transaction, rules);
        
        return transaction;
    }

    @Override
    public List<Transaction> processTransactions(List<Transaction> transactions, String customerId) {
        log.debug("Processing {} transactions for customer: {}", transactions.size(), customerId);
        
        // Get rules for this customer
        List<Rule> rules = getRulesForCustomer(customerId);
        
        // Apply rules to each transaction
        for (Transaction transaction : transactions) {
            applyRules(transaction, rules);
        }
        
        return transactions;
    }

    @Override
    @Cacheable(value = "customerRules", key = "#customerId")
    public List<Rule> getRulesForCustomer(String customerId) {
        return ruleRepo.findByCustomerId(customerId);
    }
    
    /**
     * Apply rules to a transaction.
     * 
     * @param transaction The transaction to process
     * @param rules The rules to apply
     */
    private void applyRules(Transaction transaction, List<Rule> rules) {
        // Create evaluation context with the transaction
        StandardEvaluationContext context = new StandardEvaluationContext(transaction);
        
        // Apply rules in priority order
        rules.stream()
            .filter(Rule::isActive)
            .sorted((r1, r2) -> Integer.compare(r2.getPriority(), r1.getPriority())) // Higher priority first
            .forEach(rule -> {
                try {
                    // Get or compile the expression
                    Expression expression = getOrCompileExpression(rule.getDrlCondition());
                    
                    // Evaluate the condition
                    Boolean result = expression.getValue(context, Boolean.class);
                    
                    // If condition is true, set the category
                    if (Boolean.TRUE.equals(result)) {
                        transaction.setCategory(rule.getCategory().getName());
                        log.debug("Rule '{}' matched for transaction {}", rule.getName(), transaction.getId());
                        return; // Stop processing rules for this transaction
                    }
                } catch (Exception e) {
                    log.error("Error evaluating rule '{}': {}", rule.getName(), e.getMessage());
                }
            });
    }
    
    /**
     * Get a compiled expression from cache or compile it.
     * 
     * @param spelExpression The SpEL expression string
     * @return The compiled expression
     */
    private Expression getOrCompileExpression(String spelExpression) {
        // Convert Drools syntax to SpEL syntax
        String spelConverted = convertDroolsToSpel(spelExpression);
        
        // Get from cache or compile
        return expressionCache.computeIfAbsent(spelConverted, parser::parseExpression);
    }
    
    /**
     * Convert Drools syntax to SpEL syntax.
     * This is a simplified conversion and may not handle all Drools syntax.
     * 
     * @param droolsExpression The Drools expression
     * @return The SpEL expression
     */
    private String convertDroolsToSpel(String droolsExpression) {
        // This is a simplified conversion that handles basic conditions
        // In a real implementation, you would need a more sophisticated parser
        
        // Replace common operators
        String spel = droolsExpression
                .replace(" == ", " eq ")
                .replace(" != ", " ne ")
                .replace(" > ", " gt ")
                .replace(" < ", " lt ")
                .replace(" >= ", " ge ")
                .replace(" <= ", " le ")
                .replace(" && ", " and ")
                .replace(" || ", " or ");
        
        // Handle contains for strings
        spel = spel.replaceAll("(\\w+)\\.contains\\(([^)]+)\\)", "$2.contains($1)");
        
        return spel;
    }
}