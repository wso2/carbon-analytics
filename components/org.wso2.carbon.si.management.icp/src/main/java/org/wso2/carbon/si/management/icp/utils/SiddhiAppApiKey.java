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

public enum SiddhiAppApiKey {
    OUTPUT_STREAM_ID(Constants.OUTPUT_STREAM_ID),
    INPUT_STREAM_ID(Constants.INPUT_STREAM_ID),
    APP_NAME(Constants.APP_NAME),
    IS_ACTIVE(Constants.IS_ACTIVE),
    QUERY_NAME(Constants.QUERY_NAME),
    QUERY(Constants.QUERY),
    TABLE_ID(Constants.TABLE_ID),
    WINDOW_ID(Constants.WINDOW_ID),
    STATUS(Constants.STATUS),
    AGE(Constants.AGE),
    IS_STAT_ENABLED(Constants.IS_STAT_ENABLED);

    private final String value;

    SiddhiAppApiKey(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
