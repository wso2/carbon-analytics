/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.event.output.adaptor.manager.core.internal.util;

public final class OutputEventAdaptorManagerConstants {

    public static final String DEFAULT_THRIFT_RECEIVER_URL = "tcp://localhost:7661";
    public static final String DEFAULT_THRIFT_AUTHENTICATOR_URL = "ssl://localhost:7761";
    public static final String DEFAULT_THRIFT_USERNAME = "admin";
    public static final String DEFAULT_THRIFT_PASSWORD = "admin";

    private OutputEventAdaptorManagerConstants() {
    }

    public static final String OEA_CONF_NS = "http://wso2.org/carbon/eventadaptormanager";
    public static final String OEA_ELE_ROOT_ELEMENT = "outputEventAdaptor";
    public static final String OEA_ELE_DIRECTORY = "outputeventadaptors";

    public static final String OEA_ELE_PROPERTY = "property";

    public static final String OEA_ATTR_NAME = "name";
    public static final String OEA_ATTR_TYPE = "type";

    public static final String OEA_ATTR_TRACING = "trace";
    public static final String OEA_ATTR_STATISTICS = "statistics";
    public static final String OEA_VALUE_ENABLE ="enable";
    public static final String OEA_VALUE_DISABLE ="disable";

    public static final String OEA_CONFIG_FILE_EXTENSION_WITH_DOT = ".xml";

    public static final String DEFAULT_WSO2EVENT_OUTPUT_ADAPTOR = "DefaultWSO2EventOutputAdaptor";
    public static final String ADAPTOR_TYPE_WSO2EVENT = "wso2event";

    public static final String ADAPTOR_MESSAGE_RECEIVER_URL = "receiverURL";
    public static final String ADAPTOR_MESSAGE_AUTHENTICATOR_URL = "authenticatorURL";
    public static final String ADAPTOR_MESSAGE_USERNAME = "username";
    public static final String ADAPTOR_MESSAGE_PASSWORD = "password";

}