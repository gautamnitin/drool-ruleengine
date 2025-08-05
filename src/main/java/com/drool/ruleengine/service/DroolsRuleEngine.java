package com.drool.ruleengine.service;

import com.drool.ruleengine.model.Rule;
import com.drool.ruleengine.model.Transaction;
import com.drool.ruleengine.repository.RuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Drools implementation of the RuleEngine interface.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DroolsRuleEngine implements RuleEngine {
    private final RuleRepository ruleRepo;
    
    // Cache for KieSessions by customerId
    private final Map<String, KieContainer> kieContainerCache = new ConcurrentHashMap<>();
    // Cache for last rule update timestamp by customerId
    private final Map<String, Long> ruleUpdateTimestamps = new ConcurrentHashMap<>();

    @Override
    public Transaction processTransaction(Transaction transaction, String customerId) {
        KieSession session = getSessionWithRules(customerId);
        try {
            session.insert(transaction);
            session.fireAllRules();
            return transaction;
        } finally {
            session.dispose();
        }
    }

    @Override
    public List<Transaction> processTransactions(List<Transaction> transactions, String customerId) {
        KieSession session = getSessionWithRules(customerId);
        try {
            for (Transaction tx : transactions) {
                session.insert(tx);
            }
            session.fireAllRules();
            return transactions;
        } finally {
            session.dispose();
        }
    }

    @Override
    @Cacheable(value = "customerRules", key = "#customerId")
    public List<Rule> getRulesForCustomer(String customerId) {
        return ruleRepo.findByCustomerId(customerId);
    }

    /**
     * Generates DRL content from a list of rules
     * Optimized for performance with StringBuilder capacity pre-allocation
     */
    private String generateDRL(List<Rule> rules) {
        // Pre-allocate StringBuilder capacity based on estimated size
        // This avoids multiple resizing operations during append
        int estimatedSize = 100 + (rules.size() * 200); // Base size + estimated size per rule
        StringBuilder drl = new StringBuilder(estimatedSize);
        
        // Add package and imports
        drl.append("package com.rules;\n");
        drl.append("import com.drool.ruleengine.model.Transaction;\n\n");
        
        // Filter active rules first to avoid checking in the loop
        rules.stream()
            .filter(Rule::isActive)
            .forEach(r -> {
                drl.append("rule \"").append(r.getCustomerId()).append("-").append(r.getName()).append("\"\n")
                   .append("    salience ").append(r.getPriority()).append("\n")
                   .append("    when\n")
                   .append("        t : Transaction(").append(r.getDrlCondition()).append(")\n")
                   .append("    then\n")
                   .append("        t.setCategory(\"").append(r.getCategory().getName()).append("\");\n")
                   .append("        update(t);\n")
                   .append("end\n\n");
            });
            
        return drl.toString();
    }
    
    /**
     * Builds and returns a Drools KieSession from the generated DRL
     * Uses caching to avoid rebuilding KieSessions for the same customer rules
     * 
     * @param customerId The ID of the customer
     * @return KieSession for the customer
     */
    public KieSession getSessionWithRules(String customerId) {
        long startTime = System.currentTimeMillis();
        log.debug("Starting getSessionWithRules for customer: {}", customerId);
        
        try {
            // Check if we have a cached KieContainer for this customer
            KieContainer kieContainer = kieContainerCache.get(customerId);
            boolean cacheHit = kieContainer != null;
            
            // Get the rules for this customer (this call is cached via @Cacheable)
            List<Rule> rules = getRulesForCustomer(customerId);
            log.debug("Found {} rules for customer: {}", rules.size(), customerId);
            
            // If no cached container or rules have been updated, rebuild
            if (!cacheHit || hasRulesChanged(customerId, rules)) {
                log.debug("Cache miss or rules changed for customer: {}, rebuilding KieContainer", customerId);
                long drlStartTime = System.currentTimeMillis();
                String drlContent = generateDRL(rules);
                log.debug("DRL generation took: {} ms", System.currentTimeMillis() - drlStartTime);
                
                // Use KieServices instead of KieHelper for better performance with caching
                long buildStartTime = System.currentTimeMillis();
                KieServices ks = KieServices.Factory.get();
                KieFileSystem kfs = ks.newKieFileSystem();
                kfs.write("src/main/resources/rules/customer_" + customerId + ".drl", 
                        ks.getResources().newByteArrayResource(drlContent.getBytes(StandardCharsets.UTF_8)));
                
                KieBuilder kb = ks.newKieBuilder(kfs).buildAll();
                kieContainer = ks.newKieContainer(ks.getRepository().getDefaultReleaseId());
                log.debug("KieContainer build took: {} ms", System.currentTimeMillis() - buildStartTime);
                
                // Update the cache
                kieContainerCache.put(customerId, kieContainer);
                updateRuleTimestamp(customerId);
            } else {
                log.debug("Using cached KieContainer for customer: {}", customerId);
            }
            
            // Create a new session from the cached container
            KieSession session = kieContainer.newKieSession();
            log.debug("KieSession created for customer: {}", customerId);
            return session;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("getSessionWithRules completed for customer: {} in {} ms", customerId, duration);
        }
    }
    
    /**
     * Check if rules for a customer have changed since last cache update
     */
    private boolean hasRulesChanged(String customerId, List<Rule> currentRules) {
        // For simplicity, we're just checking if we have a timestamp
        // In a real app, you'd compare rule versions or modification timestamps
        return !ruleUpdateTimestamps.containsKey(customerId);
    }
    
    /**
     * Update the timestamp for when rules were last cached
     */
    private void updateRuleTimestamp(String customerId) {
        ruleUpdateTimestamps.put(customerId, System.currentTimeMillis());
    }
}