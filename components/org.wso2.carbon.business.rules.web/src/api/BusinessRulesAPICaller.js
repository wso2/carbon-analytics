/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

import React from 'react';
import axios from 'axios';
// Auth Utils
import AuthManager from '../utils/AuthManager';

/**
 * App context.
 */
const appContext = window.contextPath;

/**
 * Used to call APIs, related to Business Rules
 */
class BusinessRulesAPICaller {
    constructor(url) {
        this.url = url;
    }

    /**
     * Returns the axios http client
     */
    getHTTPClient() {
        return axios.create({
            baseURL: this.url + appContext,
            timeout: 30000,
            headers: {"Authorization": "Bearer " + AuthManager.getUser().SDID}
        });
    }

    /**
     * Returns available template groups
     */
    getTemplateGroups() {
        return this.getHTTPClient().get('/template-groups');
    }

    /**
     * Returns the template group that has the given ID
     * @param templateGroupID
     */
    getTemplateGroup(templateGroupID) {
        return this.getHTTPClient().get('/template-groups/' + templateGroupID);
    }

    /**
     * Returns rule templates available under the given template group
     * @param templateGroupID
     */
    getRuleTemplates(templateGroupID) {
        return this.getHTTPClient().get('/template-groups/' + templateGroupID + '/templates');
    }

    /**
     * Returns the rule template that has the given ruleTemplateID and belongs to the template group
     * with the given templateGroupID
     *
     * @param templateGroupID
     * @param ruleTemplateID
     */
    getRuleTemplate(templateGroupID, ruleTemplateID) {
        return this.getHTTPClient().get('/template-groups/' + templateGroupID + '/templates/' + ruleTemplateID);
    }

    /**
     * Creates a business rule with the given business rule JSON.
     * Deploying enabled or disabled according to the given deployStatus
     *
     * @param businessRuleJSON
     * @param deployStatus Deploy if true, Do not deploy if false
     * @returns {AxiosPromise}
     */
    createBusinessRule(businessRuleJSON, deployStatus) {
        let formData = new FormData();
        formData.append("businessRule", (businessRuleJSON));
        return this.getHTTPClient().post('/instances?deploy=' + deployStatus, formData,
            {headers: {'Content-Type': 'multipart/form-data'}});
    }

    /**
     * Returns available business rules
     */
    getBusinessRules() {
        return this.getHTTPClient().get('/instances');
    }

    /**
     * Returns the business rule with the given ID
     * @param businessRuleID
     */
    getBusinessRule(businessRuleID) {
        return this.getHTTPClient().get('/instances/' + businessRuleID);
    }

    /**
     * Gets deployment info of the business rule with the given ID
     * @param businessRuleID
     */
    getDeploymentInfo(businessRuleID) {
        return this.getHTTPClient().get('/instances/' + businessRuleID + '/deployment-info');
    }

    /**
     * Updates the business rule with the given ID, with the given JSON of a business rule; with deployment status
     * as specified
     *
     * @param businessRuleID
     * @param businessRuleJSON
     * @param deployStatus
     * @returns {AxiosPromise}
     */
    updateBusinessRule(businessRuleID, businessRuleJSON, deployStatus) {
        return this.getHTTPClient().put('/instances/' + businessRuleID + '?deploy=' + deployStatus,
            businessRuleJSON, {headers: {'Content-Type': 'application/json'}});
    }

    /**
     * Deletes the business rule with the given ID.
     * Un-deploys SiddhiApps of the business rule only if force deletion status is false
     *
     * @param businessRuleID
     * @param forceDeleteStatus
     */
    deleteBusinessRule(businessRuleID, forceDeleteStatus) {
        return this.getHTTPClient()
            .delete('/instances/' + businessRuleID + '?force-delete=' + forceDeleteStatus);
    }

    /**
     * Re deploys the artifacts of the business rule, identified by the given ID
     *
     * @param businessRuleID
     */
    redeployBusinessRule(businessRuleID) {
        return this.getHTTPClient().post('/instances/' + businessRuleID);
    }
}

export default BusinessRulesAPICaller;
