/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.streaming.integrator.core.siddhi.error.handler.util;

/**
 * Class that holds the Database Queries required for the error store.
 */
public class ExecutionInfo {

    private String preparedCheckTableExistenceStatement;
    private String preparedCreateTableStatement;
    private String preparedInsertStatement;
    private String preparedSelectStatement;
    private String preparedMinimalSelectStatement;
    private String preparedSelectSingleStatement;
    private String preparedSelectWithLimitOffsetStatement;
    private String preparedMinimalSelectWithLimitOffsetStatement;
    private String preparedSelectCountStatement;
    private String preparedSelectCountBySiddhiAppNameStatement;
    private String preparedDeleteStatement;
    private String preparedDeleteBySiddhiAppNameStatement;
    private String preparedPurgeStatement;

    private boolean tableExist = false;

    public String getPreparedInsertStatement() {
        return preparedInsertStatement;
    }

    public void setPreparedInsertStatement(String insertStatementPrefix) {
        this.preparedInsertStatement = insertStatementPrefix;
    }

    public String getPreparedCreateTableStatement() {
        return preparedCreateTableStatement;
    }

    public void setPreparedCreateTableStatement(String preparedCreateTableStatement) {
        this.preparedCreateTableStatement = preparedCreateTableStatement;
    }

    public String getPreparedCheckTableExistenceStatement() {
        return preparedCheckTableExistenceStatement;
    }

    public void setPreparedCheckTableExistenceStatement(String preparedCheckTableExistenceStatement) {
        this.preparedCheckTableExistenceStatement = preparedCheckTableExistenceStatement;
    }

    public String getPreparedSelectStatement() {
        return preparedSelectStatement;
    }

    public void setPreparedSelectStatement(String preparedSelectStatement) {
        this.preparedSelectStatement = preparedSelectStatement;
    }

    public String getPreparedMinimalSelectStatement() {
        return preparedMinimalSelectStatement;
    }

    public void setPreparedMinimalSelectStatement(String preparedMinimalSelectStatement) {
        this.preparedMinimalSelectStatement = preparedMinimalSelectStatement;
    }

    public String getPreparedSelectSingleStatement() {
        return preparedSelectSingleStatement;
    }

    public void setPreparedSelectSingleStatement(String preparedSelectSingleStatement) {
        this.preparedSelectSingleStatement = preparedSelectSingleStatement;
    }

    public String getPreparedSelectWithLimitOffsetStatement() {
        return preparedSelectWithLimitOffsetStatement;
    }

    public void setPreparedSelectWithLimitOffsetStatement(String preparedSelectWithLimitOffsetStatement) {
        this.preparedSelectWithLimitOffsetStatement = preparedSelectWithLimitOffsetStatement;
    }

    public String getPreparedMinimalSelectWithLimitOffsetStatement() {
        return preparedMinimalSelectWithLimitOffsetStatement;
    }

    public void setPreparedMinimalSelectWithLimitOffsetStatement(String preparedMinimalSelectWithLimitOffsetStatement) {
        this.preparedMinimalSelectWithLimitOffsetStatement = preparedMinimalSelectWithLimitOffsetStatement;
    }

    public String getPreparedSelectCountStatement() {
        return preparedSelectCountStatement;
    }

    public void setPreparedSelectCountStatement(String preparedSelectCountStatement) {
        this.preparedSelectCountStatement = preparedSelectCountStatement;
    }

    public String getPreparedSelectCountBySiddhiAppNameStatement() {
        return preparedSelectCountBySiddhiAppNameStatement;
    }

    public void setPreparedSelectCountBySiddhiAppNameStatement(String preparedSelectCountBySiddhiAppNameStatement) {
        this.preparedSelectCountBySiddhiAppNameStatement = preparedSelectCountBySiddhiAppNameStatement;
    }

    public String getPreparedDeleteStatement() {
        return preparedDeleteStatement;
    }

    public void setPreparedDeleteStatement(String preparedDeleteStatement) {
        this.preparedDeleteStatement = preparedDeleteStatement;
    }

    public String getPreparedPurgeStatement() {
        return preparedPurgeStatement;
    }

    public void setPreparedPurgeStatement(String preparedPurgeStatement) {
        this.preparedPurgeStatement = preparedPurgeStatement;
    }

    public String getPreparedDeleteBySiddhiAppNameStatement() {
        return preparedDeleteBySiddhiAppNameStatement;
    }

    public void setPreparedDeleteBySiddhiAppNameStatement(String preparedDeleteBySiddhiAppNameStatement) {
        this.preparedDeleteBySiddhiAppNameStatement = preparedDeleteBySiddhiAppNameStatement;
    }

    public boolean isTableExist() {
        return tableExist;
    }

    public void setTableExist(boolean tableExist) {
        this.tableExist = tableExist;
    }

}
