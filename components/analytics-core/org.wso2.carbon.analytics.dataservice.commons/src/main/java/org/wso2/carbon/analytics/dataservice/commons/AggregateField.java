/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.analytics.dataservice.commons;

import java.io.Serializable;

/**
 * This class represents the bean class to hold the field name, alias and the aggregate function.
 * Aggregate function will be calculated over the given field name.
 */
public class AggregateField implements Serializable {

    private static final long serialVersionUID = 3077504068553108030L;
    private String[] aggregateVariables;
    private String aggregateFunction;
    private String alias;

    public AggregateField() {

    }

    public AggregateField(String[] aggregateVariables, String aggregateFunction, String alias) {
        this.aggregateVariables = aggregateVariables;
        this.alias = alias;
        this.aggregateFunction = aggregateFunction.toUpperCase();
    }

    public String[] getAggregateVariables() {
        return aggregateVariables;
    }

    public void setAggregateVariables(String[] aggregateVariables) {
        this.aggregateVariables = aggregateVariables;
    }

    public String getAggregateFunction() {
        return aggregateFunction;
    }

    public void setAggregateFunction(String aggregateFunction) {
        this.aggregateFunction = aggregateFunction.toUpperCase();
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}

