package com.drool.ruleengine.component;

import com.drool.ruleengine.model.Category;
import com.drool.ruleengine.model.Rule;
import com.drool.ruleengine.repository.CategoryRepository;
import com.drool.ruleengine.repository.RuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final CategoryRepository categoryRepo;
    private final RuleRepository ruleRepo;

    @Override
    public void run(String... args) {
        Category electronics = categoryRepo.save(new Category(null, "High Electronics", "Expensive electronics purchases"));

        ruleRepo.save(new Rule(null, "HighElectro",
                "amount > 500 && merchantType == \"ELECTRONICS\"",
                10, true, "ngautam", electronics));

        System.out.println("<=========================Rules initialized===========================================>");
        System.out.println("Rules:" + ruleRepo.findAll());
    }
}