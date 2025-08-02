package com.drool.ruleengine.model;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Entity
@Table(indexes = {
    @Index(name = "idx_rule_customer_id", columnList = "customerId")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rule {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private String drlCondition;
    private int priority;
    private boolean isActive;
    private String customerId;


    @ManyToOne(fetch = jakarta.persistence.FetchType.EAGER)
    private Category category;
}