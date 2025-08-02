package com.drool.ruleengine.contoller;

import com.drool.ruleengine.model.Transaction;
import com.drool.ruleengine.repository.TransactionRepository;
import com.drool.ruleengine.service.RuleEngineService;
import lombok.RequiredArgsConstructor;
import org.kie.api.runtime.KieSession;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transactions")
public class TransactionController {
    private final RuleEngineService ruleEngineService;
    private final TransactionRepository txRepo;

    @PostMapping("/classify")
    public Transaction classify(@RequestBody Transaction tx, @RequestParam String customerId) {
        // Get a session from the cached container
        KieSession session = ruleEngineService.getSessionWithRules(customerId);
        try {
            // Set focus on the agenda group if needed for better performance
            // session.getAgenda().getAgendaGroup("main").setFocus();
            
            // Insert the fact and fire rules
            session.insert(tx);
            session.fireAllRules();
            
            // Save the transaction with the updated category
            return txRepo.save(tx);
        } finally {
            // Always dispose the session to prevent memory leaks
            session.dispose();
        }
    }
    
    /**
     * Batch classify multiple transactions for better performance
     */
    @PostMapping("/classify-batch")
    public List<Transaction> classifyBatch(@RequestBody List<Transaction> transactions, @RequestParam String customerId) {
        // Get a session from the cached container
        KieSession session = ruleEngineService.getSessionWithRules(customerId);
        try {
            // Insert all transactions
            for (Transaction tx : transactions) {
                session.insert(tx);
            }
            
            // Fire all rules once for all transactions
            session.fireAllRules();
            
            // Save all transactions
            return txRepo.saveAll(transactions);
        } finally {
            // Always dispose the session
            session.dispose();
        }
    }
}
