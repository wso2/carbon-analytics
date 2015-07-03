/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.analytics.api;

/**
 * This class holds the constants used within analytics API client side implementation.
 */
public class AnalyticsDataConstants {
    private AnalyticsDataConstants() {
    }
    public static final String ANALYTICS_DATA_CONFIGURATION_FILE_NAME = "analytics-data-config.xml";
    public static final String ANALYTICS_CONFIG_DIR = "analytics";
    public static final String ANALYTICS_API_CONF_PASSWORD_ALIAS = "Analytics.Data.Config.Password";
    public static final String ANALYTICS_API_TRUST_STORE_PASSWORD_ALIAS = "Analytics.Data.Config.TrustStorePassword";
    public static final String SSL_TRUST_STORE_SYS_PROP = "javax.net.ssl.trustStore";
    public static final String SSL_TRUST_STORE_PASSWORD_SYS_PROP = "javax.net.ssl.trustStorePassword";
    public static final String HTTP_PROTOCOL = "http";
    public static final String TRUST_STORE_CARBON_CONFIG = "Security.TrustStore.Location";
    public static final String TRUST_STORE_PASSWORD_CARBON_CONFIG = "Security.TrustStore.Password";
    public static final int MAXIMUM_NUMBER_OF_RETRY = 5;
}
