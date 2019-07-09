/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.streaming.integrator.core.internal.util;


/**
 * Class which contains public constants
 */
public class SiddhiAppProcessorConstants {


    public static final String ANNOTATION_NAME_NAME = "name";
    public static final String SIDDHI_APP_FILES_DIRECTORY = "siddhi-files";
    public static final String SIDDHI_APP_FILE_EXTENSION = ".siddhi";
    public static final String SIDDHI_APP_REST_PREFIX = "siddhi-apps";
    public static final String SYSTEM_PROP_RUN_FILE = "file";
    public static final String SIDDHI_APP_DEPLOYMENT_DIRECTORY = "deployment";
    public static final String SIDDHI_APP_STATUS_ACTIVE = "active";
    public static final String SIDDHI_APP_STATUS_INACTIVE = "inactive";
    public static final String WSO2_ARTIFACT_DEPLOYMENT_NS = "wso2.artifact.deployment";
    public static final String WSO2_ARTIFACT_DEPLOYMENT_REPOSITORY_LOCATION = "repositoryLocation";

    public static final String ANALYTICS_SOLUTIONS = "analytics.solutions";

    public static final String WSO2_SERVER_TYPE = "type";
    public static final String WSO2_SERVER_TYPE_SP = "wso2-si";
    public static final String WSO2_SERVER_TYPE_APIM_ANALYTICS = "wso2-apim-analytics";
    public static final String WSO2_SERVER_TYPE_IS_ANALYTICS = "wso2-is-analytics";
    public static final String WSO2_SERVER_TYPE_EI_ANALYTICS = "wso2-ei-analytics";

    public static final String APIM_ANALYTICS_ENABLED = "APIM-analytics.enabled";
    public static final String APIM_ALETRS_ENABLED = "APIM-alerts.enabled";
    public static final String IS_ANALYTICS_ENABLED = "IS-analytics.enabled";
    public static final String EI_ANALYTICS_ENABLED = "EI-analytics.enabled";

    public static final String APIM_ALERT_SIDDHI_APP_PREFIX = "APIM_ALERT";
    public static final String APIM_SIDDHI_APP_PREFIX = "APIM_";
    public static final String IS_SIDDHI_APP_PREFIX = "IS_";
    public static final String EI_SIDDHI_APP_PREFIX = "EI_";

    public static final String PERSISTENCE_STORE_CLEAR_ENABLED = "persistenceStoreClearEnabled";
    public static final String SIDDHI_APP = "siddhiApp";

    public static final String HA_METRICS_PREFIX = "org.wso2.ha";
    public static final String HA_METRICS_SENDING_THROUGHPUT = "sending.throughput";
    public static final String HA_METRICS_RECEIVING_THROUGHPUT = "receiving.throughput";

    /**
     * Runtime modes of Stream Processor engine
     */
    public enum RuntimeMode {
        // Run File Mode.
        RUN_FILE,
        // Run Stream Processor Server Mode.
        SERVER,
        // Represents VALIDATION_ERROR Condition.
        ERROR
    }

    private SiddhiAppProcessorConstants() {
        // Prevents instantiation.
    }
}
