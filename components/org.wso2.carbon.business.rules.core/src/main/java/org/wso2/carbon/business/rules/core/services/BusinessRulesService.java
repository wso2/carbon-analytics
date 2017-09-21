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

import org.wso2.carbon.business.rules.core.exceptions.TemplateManagerException;
import org.wso2.carbon.business.rules.core.bean.BusinessRule;
import org.wso2.carbon.business.rules.core.services.businessRulesFromScratch.BusinessRulesFromScratch;
import org.wso2.carbon.business.rules.core.services.businessRulesFromTemplate.BusinessRulesFromTemplate;

public interface BusinessRulesService extends BusinessRulesFromScratch, BusinessRulesFromTemplate {
    /**
     * Gives the Business Rule from Template instance that has the given UUID
     *
     * @param businessRuleUUID
     * @return
     * @throws TemplateManagerException
     */
    BusinessRule findBusinessRuleFromTemplate(String businessRuleUUID) throws TemplateManagerException;

    /**
     * Deletes the Business Rule that has the given UUID
     * and Undeploys the Templates belonging to the Business Rule
     *
     * @param uuid UUID of the saved Business Rule definition
     */
    void deleteBusinessRule(String uuid) throws TemplateManagerException;
}
