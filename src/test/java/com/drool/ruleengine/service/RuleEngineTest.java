package com.drool.ruleengine.service;

import com.drool.ruleengine.model.Category;
import com.drool.ruleengine.model.Rule;
import com.drool.ruleengine.model.Transaction;
import com.drool.ruleengine.repository.RuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RuleEngineTest {

    @Mock
    private RuleRepository ruleRepository;

    @InjectMocks
    private DroolsRuleEngine droolsRuleEngine;
    
    @InjectMocks
    private SimpleRuleEngine simpleRuleEngine;
    
    private List<Rule> testRules;
    private Transaction testTransaction;
    private final String customerId = "test-customer";
    
    @BeforeEach
    void setUp() {
        // Create test categories
        Category foodCategory = new Category();
        foodCategory.setId(1L);
        foodCategory.setName("Food");
        
        Category travelCategory = new Category();
        travelCategory.setId(2L);
        travelCategory.setName("Travel");
        
        // Create test rules
        Rule foodRule = new Rule();
        foodRule.setId(1L);
        foodRule.setName("Food Rule");
        foodRule.setDrlCondition("merchantType == \"RESTAURANT\"");
        foodRule.setPriority(10);
        foodRule.setActive(true);
        foodRule.setCustomerId(customerId);
        foodRule.setCategory(foodCategory);
        
        Rule travelRule = new Rule();
        travelRule.setId(2L);
        travelRule.setName("Travel Rule");
        travelRule.setDrlCondition("merchantType == \"AIRLINE\"");
        travelRule.setPriority(5);
        travelRule.setActive(true);
        travelRule.setCustomerId(customerId);
        travelRule.setCategory(travelCategory);
        
        testRules = Arrays.asList(foodRule, travelRule);
        
        // Create test transaction
        testTransaction = new Transaction();
        testTransaction.setId(1L);
        testTransaction.setMerchant("Test Restaurant");
        testTransaction.setMerchantType("RESTAURANT");
        testTransaction.setAmount(100.0);
        testTransaction.setLocation("New York");
    }
    
    @Test
    void testDroolsRuleEngine() {
        // Mock repository response
        when(ruleRepository.findByCustomerId(customerId)).thenReturn(testRules);
        
        // Process transaction
        Transaction result = droolsRuleEngine.processTransaction(testTransaction, customerId);
        
        // Verify category was set correctly
        assertEquals("Food", result.getCategory());
    }
    
    @Test
    void testSimpleRuleEngine() {
        // Mock repository response
        when(ruleRepository.findByCustomerId(customerId)).thenReturn(testRules);
        
        // Process transaction
        Transaction result = simpleRuleEngine.processTransaction(testTransaction, customerId);
        
        // Verify category was set correctly
        assertEquals("Food", result.getCategory());
    }
    
    @Test
    void testBatchProcessing() {
        // Mock repository response
        when(ruleRepository.findByCustomerId(customerId)).thenReturn(testRules);
        
        // Create a second transaction
        Transaction airlineTransaction = new Transaction();
        airlineTransaction.setId(2L);
        airlineTransaction.setMerchant("Test Airline");
        airlineTransaction.setMerchantType("AIRLINE");
        airlineTransaction.setAmount(500.0);
        airlineTransaction.setLocation("New York");
        
        List<Transaction> transactions = Arrays.asList(testTransaction, airlineTransaction);
        
        // Process transactions with Drools engine
        List<Transaction> droolsResults = droolsRuleEngine.processTransactions(transactions, customerId);
        
        // Verify categories were set correctly
        assertEquals("Food", droolsResults.get(0).getCategory());
        assertEquals("Travel", droolsResults.get(1).getCategory());
        
        // Reset categories
        testTransaction.setCategory(null);
        airlineTransaction.setCategory(null);
        
        // Process transactions with Simple engine
        List<Transaction> simpleResults = simpleRuleEngine.processTransactions(transactions, customerId);
        
        // Verify categories were set correctly
        assertEquals("Food", simpleResults.get(0).getCategory());
        assertEquals("Travel", simpleResults.get(1).getCategory());
    }
}