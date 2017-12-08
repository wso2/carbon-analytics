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
package org.wso2.carbon.data.provider.bean;

/**
 * Model class of the data object to be sent.
 */
public class DataModel {

    private final DataSetMetadata metadata;
    private final Object[][] data;
    private final int lastRow;
    private final String topic;

    public DataModel(DataSetMetadata metadata, Object[][] data, int lastRow, String topic) {
        this.metadata = metadata;
        this.data = data;
        this.lastRow = lastRow;
        this.topic = topic;
    }

    public DataSetMetadata getMetadata() {
        return metadata;
    }

    public Object[][] getData() {
        return data;
    }

    public int getLastRow() {
        return lastRow;
    }

    public String getTopic() {
        return topic;
    }
}
