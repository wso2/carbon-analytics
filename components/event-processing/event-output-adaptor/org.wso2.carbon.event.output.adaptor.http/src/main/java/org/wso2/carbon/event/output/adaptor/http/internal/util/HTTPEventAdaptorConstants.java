/*
 * Copyright 2013 The Apache Software Foundation.
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
package org.wso2.carbon.event.output.adaptor.http.internal.util;

/**
 * This class contains the constants related to HTTP output event adaptor.
 */
public class HTTPEventAdaptorConstants {

    public static final String ADAPTER_TYPE_HTTP = "http";
    
    public static final String ADAPTER_MESSAGE_URL = "http.url";
    
    public static final String ADAPTER_MESSAGE_URL_HINT = "http.url.hint";
    
    public static final int ADAPTER_MIN_THREAD_POOL_SIZE = 8;

    public static final int ADAPTER_MAX_THREAD_POOL_SIZE = 100;


    
    public static final int ADAPTER_EXECUTOR_JOB_QUEUE_SIZE = 2000;
    
    public static final String ADAPTER_PROXY_HOST = "http.proxy.host";
    
    public static final String ADAPTER_PROXY_HOST_HINT = "http.proxy.host.hint";
    
    public static final String ADAPTER_PROXY_PORT = "http.proxy.port";
    
    public static final String ADAPTER_PROXY_PORT_HINT = "http.proxy.port.hint";
    
    public static final String ADAPTER_USERNAME = "http.username";
    
    public static final String ADAPTER_USERNAME_HINT = "http.username.hint";
    
    public static final String ADAPTER_PASSWORD = "http.password";
    
    public static final String ADAPTER_PASSWORD_HINT = "http.password.hint";
    
    public static final String ADAPTER_HEADERS = "http.headers";
    
    public static final String ADAPTER_HEADERS_HINT = "http.headers.hint";
    
}
