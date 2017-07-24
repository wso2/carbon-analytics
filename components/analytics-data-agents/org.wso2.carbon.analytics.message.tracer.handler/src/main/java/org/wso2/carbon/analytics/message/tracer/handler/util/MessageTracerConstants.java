/**
 * Copyright (c) 2005 - 2013, WSO2 Inc. (http://www.wso2.com) All Rights Reserved.
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
package org.wso2.carbon.analytics.message.tracer.handler.util;

import org.wso2.carbon.core.RegistryResources;

public class MessageTracerConstants {

    private MessageTracerConstants() {
    }

    public static final String ACTIVITY_REG_PATH = RegistryResources.COMPONENTS
                                                   + "org.wso2.carbon.analytics.message.tracer.handler/tracing/";

    public static final String CLOUD_DEPLOYMENT_PROP = "IsCloudDeployment";

    public static final String SERVER_CONFIG_BAM_URL = "BamServerURL";

    public static final String DEFAULT_BAM_SERVER_URL = "tcp://127.0.0.1:7611";

    public static final String ANALYTICS_SERVICE_MESSAGE_TRACER_MODULE_NAME = "wso2analyticsmessagetracerservice";

    public static final String ENABLE_TRACE = "EnableMessageTrace";

    public static final String ENABLE_PUBLISH_TO_BAM = "EnablePublishToBAM";

    public static final String ENABLE_DUMP_MESSAGE_BODY = "EnableDumpMessageBody";

    public static final String ENABLE_LOGGING = "EnableLogging";

    public static final String ACTIVITY_ID = "activityID";

    public static final String EMPTY_STRING = "";

    public static final String SYNAPSE_SERVICE = "__SynapseService";

    public static final String ORG_APACHE_SYNAPSE_TRANSPORT_PASSTHRU_UTIL_RELAY_UTILS_CLASS_NAME = "org.apache.synapse.transport.passthru.util.RelayUtils";

    public static final String BUILD_MESSAGE_METHOD_NAME = "buildMessage";

    public static final String TENANT_ID = "tenantId";

    public static final String STATUS_FAULT = "fault";

    public static final String STATUS_SUCCESS = "success";

    public static final String TRANSPORT_IN_URL = "TransportInURL";
    public static final String SOAP_BODY = "soap_body";
    public static final String HOST = "host";
    public static final String REQUEST_URL = "request_url";
    public static final String SERVICE_NAME = "service_name";
    public static final String OPERATION_NAME = "operation_name";
    public static final String TIMESTAMP = "timestamp";
    public static final String MSG_DIRECTION = "message_direction";
    public static final String SOAP_HEADER = "soap_header";
    public static final String IN_DIRECTION = "IN";
    public static final String OUT_DIRECTION = "OUT";
    public static final String USERNAME = "username";

    public static final String AUTHENTICATION_ADMIN = "AuthenticationAdmin";

    public static final String MESSAGE_DIRECTION = "in";

    public static final String AUTHENTICATION_OPERATION = "login";
}
