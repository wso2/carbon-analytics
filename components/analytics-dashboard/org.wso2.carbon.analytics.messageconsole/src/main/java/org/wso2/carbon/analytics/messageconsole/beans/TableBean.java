/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.analytics.messageconsole.beans;

import java.io.Serializable;

/**
 * This class represent meta information about message console table.
 */
public class TableBean implements Serializable {

    private static final long serialVersionUID = 5363807857116507258L;

    private String name;
    private ColumnBean[] columns = new ColumnBean[0];

    public TableBean() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ColumnBean[] getColumns() {
        return columns;
    }

    public void setColumns(ColumnBean[] columns) {
        this.columns = columns;
    }
}
