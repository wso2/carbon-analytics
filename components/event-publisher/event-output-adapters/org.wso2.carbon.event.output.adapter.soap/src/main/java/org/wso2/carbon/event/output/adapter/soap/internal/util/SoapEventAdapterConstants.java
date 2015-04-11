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
package org.wso2.carbon.event.output.adapter.soap.internal.util;

public class SoapEventAdapterConstants {

    public static final String ADAPTER_TYPE_SOAP = "soap";

    public static final String ADAPTER_CONF_SOAP_URL = "url";
    public static final String ADAPTER_CONF_SOAP_URL_HINT = "url.hint";
    public static final String ADAPTER_CONF_SOAP_USERNAME = "username";
    public static final String ADAPTER_CONF_SOAP_PASSWORD = "password";
    public static final String ADAPTER_CONF_SOAP_HEADERS = "headers";
    public static final String AXIS2_CLIENT_CONF_FILE = "/axis2/axis2_client.xml";
    public static final String SERVER_CLIENT_DEPLOYMENT_DIR = "/repository/deployment/client/";

    public static final int ADAPTER_MIN_THREAD_POOL_SIZE = 8;
    public static final int ADAPTER_MAX_THREAD_POOL_SIZE = 100;
    public static final int ADAPTER_EXECUTOR_JOB_QUEUE_SIZE = 2000;
    public static final long DEFAULT_KEEP_ALIVE_TIME = 20;

    public static final String ADAPTER_MIN_THREAD_POOL_SIZE_NAME = "minThread";
    public static final String ADAPTER_MAX_THREAD_POOL_SIZE_NAME = "maxThread";
    public static final String DEFAULT_KEEP_ALIVE_TIME_NAME = "defaultKeepAliveTime";
    public static final String ADAPTER_EXECUTOR_JOB_QUEUE_SIZE_NAME = "jobQueueSize";

    public static final String HEADER_SEPARATOR = ",";
    public static final String ENTRY_SEPARATOR = ":";

}
