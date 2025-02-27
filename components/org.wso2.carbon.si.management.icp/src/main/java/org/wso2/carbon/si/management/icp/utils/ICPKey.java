/*
 *  Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.wso2.carbon.si.management.icp.utils;

public enum ICPKey {
    NAME(Constants.NAME),
    TYPE(Constants.TYPE),
    APP_NAME(Constants.APP_NAME),
    STATUS(Constants.STATUS),
    INPUT_STREAM(Constants.INPUT_STREAM),
    OUTPUT_STREAM(Constants.OUTPUT_STREAM),
    QUERY(Constants.QUERY),
    AGE(Constants.AGE),
    STATS_ENABLED(Constants.STATS_ENABLED);

    private final String value;

    ICPKey(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
