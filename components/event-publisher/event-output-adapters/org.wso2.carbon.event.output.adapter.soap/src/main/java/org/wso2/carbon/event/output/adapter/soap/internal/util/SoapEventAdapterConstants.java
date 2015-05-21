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
    public static final String ADAPTER_CONF_SOAP_HEADERS = "soapHeaders";
    public static final String ADAPTER_CONF_SOAP_HEADERS_HINT = "soapHeaders.hint";
    public static final String AXIS2_CLIENT_CONF_FILE = "/axis2/axis2_client.xml";
    public static final String SERVER_CLIENT_DEPLOYMENT_DIR = "/repository/deployment/client/";

    public static final int ADAPTER_MIN_THREAD_POOL_SIZE = 8;
    public static final int ADAPTER_MAX_THREAD_POOL_SIZE = 100;
    public static final int ADAPTER_EXECUTOR_JOB_QUEUE_SIZE = 2000;
    public static final long DEFAULT_KEEP_ALIVE_TIME_IN_MILLIS = 20000;

    public static final String ADAPTER_MIN_THREAD_POOL_SIZE_NAME = "minThread";
    public static final String ADAPTER_MAX_THREAD_POOL_SIZE_NAME = "maxThread";
    public static final String ADAPTER_KEEP_ALIVE_TIME_NAME = "keepAliveTimeInMillis";
    public static final String ADAPTER_EXECUTOR_JOB_QUEUE_SIZE_NAME = "jobQueueSize";
    public static final String ADAPTER_CONF_HTTP_HEADERS = "httpHeaders";
    public static final String ADAPTER_CONF_HTTP_HEADERS_HINT = "httpHeaders.hint";

    public static final String HEADER_SEPARATOR = ",";
    public static final String ENTRY_SEPARATOR = ":";

    public static final String AXIS2_CLIENT_CONNECTION_TIMEOUT = "axis2ClientConnectionTimeout";
    public static final String AXIS2_REUSE_HTTP_CLIENT = "reuseHTTPClient";
    public static final String AXIS2_AUTO_RELEASE_CONNECTION = "autoReleaseConnection";
    public static final String AXIS2_MAX_CONNECTION_PER_HOST = "maxConnectionsPerHost";

    public static final boolean IS_DEFAULT_AXIS2_AUTO_RELEASE_CONNECTION = true;
    public static final boolean IS_DEFAULT_AXIS2_REUSE_HTTP_CLIENT = true;
    public static final int DEFAULT_AXIS2_CLIENT_CONNECTION_TIMEOUT = 4 * 60 * 60 * 1000;
    public static final int DEFAULT_AXIS2_MAX_CONNECTION_PER_HOST = 50;


}
