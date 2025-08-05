package com.drool.ruleengine.contoller;

import com.drool.ruleengine.model.Transaction;
import com.drool.ruleengine.repository.TransactionRepository;
import com.drool.ruleengine.service.RuleEngineService;
import lombok.RequiredArgsConstructor;
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
        // Process the transaction using the rule engine
        Transaction processedTx = ruleEngineService.processTransaction(tx, customerId);
        
        // Save the transaction with the updated category
        return txRepo.save(processedTx);
    }
    
    /**
     * Batch classify multiple transactions for better performance
     */
    @PostMapping("/classify-batch")
    public List<Transaction> classifyBatch(@RequestBody List<Transaction> transactions, @RequestParam String customerId) {
        // Process all transactions using the rule engine
        List<Transaction> processedTransactions = ruleEngineService.processTransactions(transactions, customerId);
        
        // Save all transactions
        return txRepo.saveAll(processedTransactions);
    }
}
