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
package org.wso2.carbon.databridge.commons.binary;

public class BinaryMessageConstants {
    public static final String START_MESSAGE = "START";
    public static final String END_MESSAGE = "END";
    public static final String START_EVENT = "START_EVENT";
    public static final String END_EVENT = "END_EVENT";
    public static final String PARAMS_SEPARATOR = "#::#";

    public static final String PUBLISH_OPERATION = "publish";
    public static final String LOGIN_OPERATION = "login";
    public static final String LOGOUT_OPERATION = "logout";

    public static final String STREAM_ID_PREFIX = "STREAM_ID__";
    public static final String TIME_STAMP_PREFIX = "TIME_STAMP__";
    public static final String START_META_DATA = "START_META_DATA";
    public static final String START_CORRELATION_DATA = "START_CORRELATION_DATA";
    public static final String START_PAYLOAD_DATA = "START_PAYLOAD";
    public static final String START_ARBITRARY_DATA = "START_ARBITRARY";
    public static final String END_ARBITRARY_DATA = "END_ARBITRARY";
    public static final String END_META_DATA = "END_META_DATA";
    public static final String END_CORRELATION_DATA = "END_CORRELATION_DATA";
    public static final String END_PAYLOAD_DATA = "END_PAYLOAD";

    public static final String SESSION_ID_PREFIX = "SESSION_ID__";

    public static final String OK_RESPONSE = "OK";
    public static final String ERROR_RESPONSE = "ERROR";

    public static final String ERROR_MESSAGE = "ERROR_MSG__";
    public static final String ERROR_EXCEPTION_CLASS = "ERROR_EXCEPTION_CLASS__";
    public static final String ERROR_EXCEPTION_TRACE = "ERROR_EXCEPTION_TRACE__";

}
