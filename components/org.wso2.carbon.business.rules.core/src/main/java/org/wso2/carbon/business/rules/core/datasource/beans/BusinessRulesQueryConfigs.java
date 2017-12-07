/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */

package org.wso2.carbon.business.rules.core.datasource.beans;

import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;
import org.wso2.carbon.database.query.manager.config.Queries;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Configuration bean class for business rules manager query configurations.
 */
@Configuration(namespace = "wso2.business.rules.manager", description = "WSO2 Business Rules Manager Query Provider")
public class BusinessRulesQueryConfigs {
    @Element(description = "Database query map")
    private ArrayList<Queries> queries;
    private String datasource;
    private String username;
    private String password;
    private ArrayList<HashMap<String, ArrayList<String>>> deployment_configs;
    private HashMap<String, Object> roles;

    public BusinessRulesQueryConfigs() {
    }

    public ArrayList<Queries> getQueries() {
        return queries;
    }

    public void setQueries(ArrayList<Queries> queries) {
        this.queries = queries;
    }

    public String getDatasource() {
        return datasource;
    }

    public void setDatasource(String datasource) {
        this.datasource = datasource;
    }

    public ArrayList<HashMap<String, ArrayList<String>>> getDeployment_configs() {
        return deployment_configs;
    }

    public void setDeployment_configs(ArrayList<HashMap<String, ArrayList<String>>> deployment_configs) {
        this.deployment_configs = deployment_configs;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public HashMap<String, Object> getRoles() {
        return roles;
    }

    public void setRoles(HashMap<String, Object> roles) {
        this.roles = roles;
    }
}
