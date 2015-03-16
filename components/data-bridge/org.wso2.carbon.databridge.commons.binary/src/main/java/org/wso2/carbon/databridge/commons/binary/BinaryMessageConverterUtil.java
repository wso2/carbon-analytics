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

import org.wso2.carbon.databridge.commons.Event;

public class BinaryMessageConverterUtil {

    public static String getCompleteError(String message, Exception ex) {
        StringBuilder error = new StringBuilder();
        error.append(BinaryMessageConstants.ERROR_RESPONSE).append("\n");
        error.append(BinaryMessageConstants.ERROR_EXCEPTION_CLASS).append(ex.getClass().getCanonicalName()).append("\n");
        error.append(BinaryMessageConstants.ERROR_MESSAGE).append(message).append("\n");
        error.append(BinaryMessageConstants.ERROR_EXCEPTION_TRACE).append(ex.getStackTrace().toString()).append("\n");
        return error.toString();
    }

    public static String getLoginSuccessResponse(String sessionId) {
        StringBuilder response = new StringBuilder();
        response.append(BinaryMessageConstants.OK_RESPONSE).append("\n");
        response.append(BinaryMessageConstants.SESSION_ID_PREFIX).append(sessionId).append("\n");
        response.append(BinaryMessageConstants.END_MESSAGE).append("\n");
        return response.toString();
    }

    public static String getLogoutSuccessResponse() {
        StringBuilder response = new StringBuilder();
        response.append(BinaryMessageConstants.OK_RESPONSE).append("\n");
        response.append(BinaryMessageConstants.END_MESSAGE).append("\n");
        return response.toString();
    }

    public static String getPublishSuccessResponse() {
        StringBuilder response = new StringBuilder();
        response.append(BinaryMessageConstants.OK_RESPONSE).append("\n");
        response.append(BinaryMessageConstants.END_MESSAGE).append("\n");
        return response.toString();
    }
}
