package org.wso2.carbon.stream.processor.template.manager.core.util;

import java.util.Collection;

/**
 * Represents a Rule Collection, which consists of one or more RuleTemplates
 */
public class RuleCollection {
    private String name;
    private String description;
    private Collection<RuleTemplate> ruleTemplates;

    public RuleCollection(String name, String description, Collection<RuleTemplate> ruleTemplates) {
        this.name = name;
        this.description = description;
        this.ruleTemplates = ruleTemplates;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Collection<RuleTemplate> getRuleTemplates() {
        return ruleTemplates;
    }

    public void setRuleTemplates(Collection<RuleTemplate> ruleTemplates) {
        this.ruleTemplates = ruleTemplates;
    }

    @Override
    public String toString() {
        return "RuleCollection{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", ruleTemplates=" + ruleTemplates +
                '}';
    }
}
