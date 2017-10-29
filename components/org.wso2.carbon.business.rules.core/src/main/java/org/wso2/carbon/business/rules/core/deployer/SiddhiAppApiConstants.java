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
package org.wso2.carbon.business.rules.core.deployer;

/**
 * Consists of constants related to Siddhi App Api
 */

class SiddhiAppApiConstants {
    static final String HTTP = "http";
    static final String PATH_SIDDHI_APPS = "siddhi-apps/";
    static final String PATH_STATUS = "/status";

    static final String STATUS = "status";

    /*Headers*/
    static final String CONTENT_TYPE = "content-type";
    static final String TEXT_PLAIN = "text/plain";
    static final String APPLICATION_JSON = "application/json";

    /*Authentication*/
    static final String DEFAULT_USER = "admin";
    static final String DEFAULT_PASSWORD = "admin";
}
