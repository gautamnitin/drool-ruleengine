package com.drool.ruleengine.config;

import com.drool.ruleengine.service.DroolsRuleEngine;
import com.drool.ruleengine.service.RuleEngine;
import com.drool.ruleengine.service.SimpleRuleEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuration class for rule engine selection.
 * This allows switching between different rule engine implementations
 * based on configuration properties.
 */
@Configuration
@Slf4j
public class RuleEngineConfig {

    /**
     * The type of rule engine to use.
     * Possible values: "drools", "simple"
     */
    @Value("${ruleengine.type:drools}")
    private String ruleEngineType;
    
    /**
     * Configure the primary rule engine based on configuration.
     * 
     * @param droolsRuleEngine The Drools rule engine implementation
     * @param simpleRuleEngine The simple rule engine implementation
     * @return The selected rule engine implementation
     */
    @Bean
    @Primary
    public RuleEngine ruleEngine(DroolsRuleEngine droolsRuleEngine, SimpleRuleEngine simpleRuleEngine) {
        log.info("Configuring rule engine of type: {}", ruleEngineType);
        
        switch (ruleEngineType.toLowerCase()) {
            case "simple":
                log.info("Using SimpleRuleEngine implementation");
                return simpleRuleEngine;
            case "drools":
            default:
                log.info("Using DroolsRuleEngine implementation");
                return droolsRuleEngine;
        }
    }
}