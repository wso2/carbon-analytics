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

import axios from 'axios';
// Auth Utils
import AuthManager from '../utils/AuthManager';

/**
 * App context.
 */
const appContext = window.contextPath;

/**
 * Business Rules Manager API client
 */
export default class BusinessRulesAPI {
    constructor(url) {
        this.url = url;
    }

    /**
     * Returns an Axios HTTP Client
     * @returns {AxiosInstance}     Axios HTTP Client
     */
    getHTTPClient() {
        return axios.create({
            baseURL: this.url + appContext,
            timeout: 30000,
            headers: { Authorization: 'Bearer ' + AuthManager.getUser().SDID },
        });
    }

    /**
     * Returns available Template Groups
     * @returns {AxiosPromise}      Available Template Groups
     */
    getTemplateGroups() {
        return this.getHTTPClient().get('/template-groups');
    }

    /**
     * Returns the Template Group that has the given ID
     * @param {String} templateGroupID      UUID of the Template Group
     * @returns {AxiosPromise}              Requested Template Group
     */
    getTemplateGroup(templateGroupID) {
        return this.getHTTPClient().get('/template-groups/' + templateGroupID);
    }

    /**
     * Returns Rule Templates available under the Template Group, which has the given ID
     * @param {String} templateGroupID      UUID of the Template Group
     * @returns {AxiosPromise}              Available Rule Templates
     */
    getRuleTemplates(templateGroupID) {
        return this.getHTTPClient().get('/template-groups/' + templateGroupID + '/templates');
    }

    /**
     * Returns the Rule Template with the given ID, which belongs to the Template Group that has the given ID
     * @param {String} templateGroupID      UUID of the Template Group
     * @param {String} ruleTemplateID       UUID of the Rule Template
     * @returns {AxiosPromise}              Requested Rule Template
     */
    getRuleTemplate(templateGroupID, ruleTemplateID) {
        return this.getHTTPClient().get('/template-groups/' + templateGroupID + '/templates/' + ruleTemplateID);
    }

    /**
     * Creates and saves a business rule with the given business rule JSON,
     * Deployment is done according to the given deployStatus
     * @param {Object} businessRuleJSON     Business rule object
     * @param {boolean} deployStatus        Whether to Deploy the business rule or not
     * @returns {AxiosPromise}              Response after saving the business rule
     */
    createBusinessRule(businessRuleJSON, deployStatus) {
        const formData = new FormData();
        formData.append('businessRule', (businessRuleJSON));
        return this.getHTTPClient().post('/instances?deploy=' + deployStatus, formData,
            { headers: { 'Content-Type': 'multipart/form-data' } });
    }

    /**
     * Returns available business rules from the database
     * @returns {AxiosPromise}      Existing business rules
     */
    getBusinessRules() {
        return this.getHTTPClient().get('/instances');
    }

    /**
     * Returns the business rule, that has the given ID
     * @param {String} businessRuleID       ID of the business rule
     * @returns {AxiosPromise}              Business rule
     */
    getBusinessRule(businessRuleID) {
        return this.getHTTPClient().get('/instances/' + businessRuleID);
    }

    /**
     * Gets deployment info of the business rule, that has the given ID
     * @param {String} businessRuleID       ID of the business rule
     * @returns {AxiosPromise}              Deployment info of the business rule
     */
    getDeploymentInfo(businessRuleID) {
        return this.getHTTPClient().get('/instances/' + businessRuleID + '/deployment-info');
    }

    /**
     * Updates the business rule that has the given ID, with the given JSON of a business rule
     * @param {String} businessRuleID       ID of the business rule
     * @param {Object} businessRuleJSON     Business rule object
     * @param {boolean} deployStatus        Whether to deploy the business rule or not
     * @returns {AxiosPromise}              Response after updating the business rule
     */
    updateBusinessRule(businessRuleID, businessRuleJSON, deployStatus) {
        return this.getHTTPClient().put('/instances/' + businessRuleID + '?deploy=' + deployStatus,
            businessRuleJSON, { headers: { 'Content-Type': 'application/json' } });
    }

    /**
     * Deletes the business rule, that has the given ID
     * @param {String} businessRuleID       ID of the business rule
     * @param {boolean} forceDeleteStatus   Whether to force delete the business rule or not
     * @returns {AxiosPromise}              Response after deleting the business rule
     */
    deleteBusinessRule(businessRuleID, forceDeleteStatus) {
        return this.getHTTPClient()
            .delete('/instances/' + businessRuleID + '?force-delete=' + forceDeleteStatus);
    }

    /**
     * Re-deploys the business rule, that has the given ID
     * @param {String} businessRuleID       ID of the business rule
     * @returns {AxiosPromise}              Response after re-deploying the business rule
     */
    redeployBusinessRule(businessRuleID) {
        return this.getHTTPClient().post('/instances/' + businessRuleID);
    }
}
