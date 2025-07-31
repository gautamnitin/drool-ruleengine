package com.drool.ruleengine.model;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Entity
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


    @ManyToOne
    private Category category;
}