/*
*  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.databridge.commons.utils;

public class DataBridgeCommonsUtils {

    public static final String STREAM_NAME_VERSION_SPLITTER = ":";

    public static String generateStreamId(String streamName, String streamVersion) {
        return streamName + STREAM_NAME_VERSION_SPLITTER + streamVersion;
    }

    public static String getStreamNameFromStreamId(String streamId) {
        if (streamId == null) {
            return null;
        }
        return streamId.split(STREAM_NAME_VERSION_SPLITTER)[0];
    }

    public static String getStreamVersionFromStreamId(String streamId) {
        if (streamId == null) {
            return null;
        }
        return streamId.split(STREAM_NAME_VERSION_SPLITTER)[1];
    }

}
