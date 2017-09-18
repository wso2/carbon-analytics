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
package org.wso2.carbon.business.rules.core.internal.services.businessRulesFromScratch;/*

/**
 * Consists of methods related to Business Rules from scratch
 */

import org.wso2.carbon.business.rules.core.internal.bean.businessRulesFromScratch.BusinessRuleFromScratch;

public interface BusinessRulesFromScratch {
    /**
     * Creates a Business Rule instance from the specifications of the given Business Rule
     * and Deploys the Templates belonging to the Business Rule
     *
     * @param businessRuleFromScratch
     */
    void createBusinessRuleFromScratch(BusinessRuleFromScratch businessRuleFromScratch);
    /**
     * Overwrites the Business Rule which has the given UUID, with the given Business Rule
     * and Updates the deployed Templates belonging to the Business Rule
     *
     * @param uuid                     UUID of the saved Business Rule definition
     * @param businessRuleFromScratch
     */
    void editBusinessRuleFromScratch(String uuid, BusinessRuleFromScratch businessRuleFromScratch);
    /**
     * Deploys the Templates belonging to the given BusinessRuleFromTemplate, that is denoted by the given UUID
     *
     * @param businessRuleFromScratch
     */
    void deployTemplates(BusinessRuleFromScratch businessRuleFromScratch);
}
