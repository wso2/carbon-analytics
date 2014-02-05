/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.analytics.hive;

public class HiveConstants {
    
    public static final String HIVE_METASTORE_RSS_INSTANCE = "HIVE_RSS";
    public static final String HIVE_METASTORE_DB = "metastore_db";
    public static final String HIVE_SCRIPT_BASE_PATH= "/repository/hive/scripts/";
    public static final String HIVE_SCRIPT_EXT = ".hiveql";
    public static final String HIVE_CONNECTION_CONF_PATH="/repository/hive/conf/";
    public static final String HIVE_DRIVER_KEY = "driver";
    public static final String HIVE_URL_KEY = "url";
    public static final String HIVE_USERNAME_KEY = "username";
    public static final String HIVE_PASSWORD_KEY = "password";
    public static final String HIVE_SCRIPT_NAME = "scriptName";
    public static final String SCRIPT_TRIGGER_CRON = "cron";
    public static final String DEFAULT_TRIGGER_CRON = "1 * * * * ? *";

    public static final String TENANT_TRACKER_PATH = "/repository/hive/tenants";
    public static final String TENANTS_PROPERTY = "Tenants";

    public static final String HIVE_DEFAULT_TASK_CLASS = "org.wso2.carbon.analytics.hive.task.HiveScriptExecutorTask";
    public static final String HIVE_TASK = "HIVE_TASK";

    public static final String TASK_TENANT_ID_KEY = "__TENANT_ID_PROP__";

    public static final String ENABLE_PROFILE_PROPERTY = "profile.analyzer";
    public static final String HIVE_RSS_CONFIG_FILE_PATH = "advanced/hive-rss-config.xml";
    public static final String HIVE_RSS_CONFIG_SERVER_URL = "rss-server-url";
    public static final String HIVE_RSS_CONFIG_USERNAME = "rss-server-admin-userName";
    public static final String HIVE_RSS_CONFIG_PASSWORD = "rss-server-admin-password";
    public static final String HIVE_RSS_CONFIG_DEFAULT_USERNAME = "admin";
    public static final String HIVE_RSS_CONFIG_DEFAULT_PASSWORD = "admin";
    public static final String HIVE_RSS_CONFIG_DEFAULT_SERVER_URL = "services/RSSAdmin";
    public static final String DEFAULT_SERVER_URL = "https://127.0.0.1:9443/";

    public static final String ANALYZER_CONFIG_XML = "advanced/analyzer-config.xml";
    public static final String ANALYTICS_NAMESPACE = "http://wso2.org/carbon/analytics";
    public static final String ANALYZERS_ELEMENT = "analyzers";
    public static final String ANALYZER_ELEMENT = "analyzer";
    public static final String ANALYZER_NAME_ELEMENT = "name";
    public static final String ANALYZER_CLASS_ELEMENT = "class";
    public static final String ANALYZER_PARAMS_ELEMENT = "parameters";
    public static final String ANALYZER_KEY = "analyzer";

    public static final String INCREMENTAL_ANNOTATION = "@Incremental";

}
