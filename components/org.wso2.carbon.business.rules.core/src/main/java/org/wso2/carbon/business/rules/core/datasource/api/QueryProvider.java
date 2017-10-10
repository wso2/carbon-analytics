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
package org.wso2.carbon.business.rules.core.datasource.api;

import org.wso2.carbon.business.rules.core.exceptions.BusinessRulesDatasourceException;

import java.sql.Blob;
import java.sql.PreparedStatement;
/**
 *  Query Provider interface
 * **/
public interface QueryProvider {
    public PreparedStatement getInsertQuery(String businessRuleUUID, Blob businessRule, boolean deploymentStatus)
            throws BusinessRulesDatasourceException;

    public PreparedStatement getDeleteQuery(String businessRuleUUID) throws BusinessRulesDatasourceException;

    public PreparedStatement getUpdateBusinessRuleQuery(String businessRuleUUID, Blob newBusinessRule,
                                                        boolean deploymentStatus)
            throws BusinessRulesDatasourceException;

    public PreparedStatement getUpdateDeploymentStatus(String businessRuleUUID, boolean deploymentStatus)
            throws BusinessRulesDatasourceException;

    public PreparedStatement getRetrieveBusinessRule(String businessRuleUUID) throws BusinessRulesDatasourceException;
}
