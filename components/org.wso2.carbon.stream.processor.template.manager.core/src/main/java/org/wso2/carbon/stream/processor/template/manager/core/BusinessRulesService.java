package org.wso2.carbon.stream.processor.template.manager.core;


import org.wso2.carbon.stream.processor.template.manager.core.util.BusinessRule;
import org.wso2.carbon.stream.processor.template.manager.core.util.RuleCollection;

import java.util.Collection;

public interface BusinessRulesService {
    /**
     * Finds the specified RuleTemplate
     * Derives Templates by replacing templated elements with given values
     * Deploys Templates in corresponding
     * Saves provided values map to the database
     *
     * @param businessRule Given BusinessRule object, which has RuleTemplate name and provided values
     */
    public void createbusinessRuleFromTemplate(BusinessRule businessRule);

    /**
     * Returns available BusinessRules
     *
     * @return Available Business Rules
     */
    public Collection<BusinessRule> listBusinessRules();

    /**
     * Finds the specified RuleTemplate
     * Derive Templates by replacing templated elements with newly given values
     * Deploys Templates in corresponding formats
     * Updates existing values map in the database, with the new one
     *
     * @param businessRule Given BusinessRule object, which has RuleTemplate name and newly provided values
     */
    public void editBusinessRule(BusinessRule businessRule);


    /**
     * Deletes the given values map from the database
     * Undeploy the templates
     *
     * @param businessRule
     */
    public void deleteBusinessRule(BusinessRule businessRule);

    /**
     * Returns available RuleCollections from the directory
     *
     * @return Available RuleCollections
     */
    public Collection<RuleCollection> loadRuleCollections();
}
