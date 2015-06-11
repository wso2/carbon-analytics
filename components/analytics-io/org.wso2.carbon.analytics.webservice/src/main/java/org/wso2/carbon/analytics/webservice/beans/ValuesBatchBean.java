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

package org.wso2.carbon.analytics.webservice.beans;

import java.io.Serializable;

/**
 * This class represents the primary key values batch
 */
public class ValuesBatchBean implements Serializable {

    private static final long serialVersionUID = -8260715251012715139L;
    private RecordValueEntryBean[] keyValues;

    public ValuesBatchBean() {
    }

    public RecordValueEntryBean[] getKeyValues() {
        return keyValues;
    }

    public void setKeyValues(RecordValueEntryBean[] keyValues) {
        this.keyValues = keyValues;
    }
}
