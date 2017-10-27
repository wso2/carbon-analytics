/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.business.rules.core.services;

import org.wso2.carbon.business.rules.core.bean.BusinessRule;
import org.wso2.carbon.business.rules.core.bean.template.BusinessRuleFromTemplate;
import org.wso2.carbon.business.rules.core.bean.scratch.BusinessRuleFromScratch;
import org.wso2.carbon.business.rules.core.exceptions.BusinessRuleNotFoundException;
import org.wso2.carbon.business.rules.core.exceptions.RuleTemplateScriptException;
import org.wso2.carbon.business.rules.core.exceptions.SiddhiAppsApiHelperException;
import org.wso2.carbon.business.rules.core.exceptions.TemplateManagerServiceException;

/**
 * Business Rules related services.
 **/
public interface BusinessRulesService {
    /**
     * Gives the Business Rule from Template instance that has the given UUID
     *
     * @param businessRuleUUID
     * @return
     * @throws TemplateManagerServiceException
     */
    BusinessRule findBusinessRule(String businessRuleUUID) throws TemplateManagerServiceException, BusinessRuleNotFoundException;

    /**
     * Deletes the Business Rule that has the given UUID
     * and Undeploys the Templates belonging to the Business Rule
     *
     * @param uuid UUID of the saved Business Rule definition
     */
    int deleteBusinessRule(String uuid, Boolean forceDeleteEnabled) throws SiddhiAppsApiHelperException, BusinessRuleNotFoundException, TemplateManagerServiceException;

    /**
     * Creates a Business Rule instance from the specifications of the given Business Rule
     * and Deploys the Templates belonging to the Business Rule
     *
     * @param businessRuleFromTemplate business rule object
     */
    int createBusinessRuleFromTemplate(BusinessRuleFromTemplate businessRuleFromTemplate, Boolean shouldDeploy)
            throws TemplateManagerServiceException, RuleTemplateScriptException;

    /**
     * Overwrites the Business Rule which has the given UUID, with the given Business Rule
     * and Updates the deployed Templates belonging to the Business Rule
     *
     * @param uuid                     UUID of the saved Business Rule definition
     * @param businessRuleFromTemplate business rule from template object
     */
    int editBusinessRuleFromTemplate(String uuid, BusinessRuleFromTemplate businessRuleFromTemplate,
                                     Boolean shouldDeploy) throws TemplateManagerServiceException, RuleTemplateScriptException;

    int createBusinessRuleFromScratch(BusinessRuleFromScratch businessRuleFromScratch, Boolean toDeploy) throws
            TemplateManagerServiceException, RuleTemplateScriptException;

    int editBusinessRuleFromScratch(String uuid, BusinessRuleFromScratch businessRuleFromScratch, Boolean toDeploy)
            throws TemplateManagerServiceException, RuleTemplateScriptException;
}
