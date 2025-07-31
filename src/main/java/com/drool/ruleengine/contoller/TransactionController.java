package com.drool.ruleengine.contoller;

import com.drool.ruleengine.model.Transaction;
import com.drool.ruleengine.repository.TransactionRepository;
import com.drool.ruleengine.service.RuleEngineService;
import lombok.RequiredArgsConstructor;
import org.kie.api.runtime.KieSession;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transactions")
public class TransactionController {
    private final RuleEngineService ruleEngineService;
    private final TransactionRepository txRepo;

    @PostMapping("/classify")
    public Transaction classify(@RequestBody Transaction tx, @RequestParam String customerId) {
        KieSession session = ruleEngineService.getSessionWithRules(customerId);
        session.insert(tx);
        session.fireAllRules();
        session.dispose();
        return txRepo.save(tx);
    }
}
