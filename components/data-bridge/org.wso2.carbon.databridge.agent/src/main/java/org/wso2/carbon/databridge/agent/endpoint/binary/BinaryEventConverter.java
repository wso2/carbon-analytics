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
package org.wso2.carbon.databridge.agent.endpoint.binary;

import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.binary.BinaryMessageConstants;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This is a Util class which does the Binary message transformation for publish, login, logout operations.
 */
public class BinaryEventConverter {
    public static String createBinaryLoginMessage(String userName, String password) {
        StringBuilder message = new StringBuilder();
        message.append(BinaryMessageConstants.START_MESSAGE).append("\n");
        message.append(BinaryMessageConstants.LOGIN_OPERATION).append("\n");
        message.append(userName).append(BinaryMessageConstants.PARAMS_SEPARATOR).append(password).append("\n");
        message.append(BinaryMessageConstants.END_MESSAGE).append("\n");
        return message.toString();
    }

    public static String createBinaryLogoutMessage(String sessionId) {
        StringBuilder message = new StringBuilder();
        message.append(BinaryMessageConstants.START_MESSAGE).append("\n");
        message.append(BinaryMessageConstants.LOGOUT_OPERATION).append("\n");
        message.append(BinaryMessageConstants.SESSION_ID_PREFIX).append(sessionId).append("\n");
        message.append(BinaryMessageConstants.END_MESSAGE).append("\n");
        return message.toString();
    }

    public static String createBinaryPublishMessage(List<Event> events, String sessionId) {
        StringBuilder message = new StringBuilder();
        message.append(BinaryMessageConstants.START_MESSAGE).append("\n");
        message.append(BinaryMessageConstants.SESSION_ID_PREFIX).append(sessionId).append("\n");
        message.append(BinaryMessageConstants.PUBLISH_OPERATION).append("\n");
        for (Event event : events) {
            appendBinaryMessageBody(event, message);
        }
        message.append(BinaryMessageConstants.END_MESSAGE).append("\n");
        return message.toString();
    }


    private static void appendBinaryMessageBody(Event event, StringBuilder message) {
        message.append(BinaryMessageConstants.START_EVENT).
                append("\n");
        message.append(BinaryMessageConstants.STREAM_ID_PREFIX).
                append(event.getStreamId()).append("\n");
        message.append(BinaryMessageConstants.TIME_STAMP_PREFIX).
                append(event.getTimeStamp()).append("\n");
        if (event.getMetaData() != null && event.getMetaData().length != 0) {
            message.append(BinaryMessageConstants.START_META_DATA).append("\n");
            for (Object aMetaData : event.getMetaData()) {
                message.append(aMetaData.toString()).append("\n");
            }
            message.append(BinaryMessageConstants.END_META_DATA).append("\n");
        }
        if (event.getCorrelationData() != null && event.getCorrelationData().length != 0) {
            message.append(BinaryMessageConstants.START_CORRELATION_DATA).append("\n");
            for (Object aCorrelationData : event.getCorrelationData()) {
                message.append(aCorrelationData.toString()).append("\n");
            }
            message.append(BinaryMessageConstants.END_CORRELATION_DATA).append("\n");
        }
        if (event.getPayloadData() != null && event.getPayloadData().length != 0) {
            message.append(BinaryMessageConstants.START_PAYLOAD_DATA).append("\n");
            for (Object aPayloadData : event.getPayloadData()) {
                message.append(aPayloadData.toString()).append("\n");
            }
            message.append(BinaryMessageConstants.END_PAYLOAD_DATA).append("\n");
        }
        if (event.getArbitraryDataMap() != null && !event.getArbitraryDataMap().isEmpty()) {
            message.append(BinaryMessageConstants.START_ARBITRARY_DATA).append("\n");
            Iterator<String> eventProps = event.getArbitraryDataMap().keySet().iterator();
            Map<String, String> properties = event.getArbitraryDataMap();
            while (eventProps.hasNext()) {
                String propertyName = eventProps.next();
                message.append(propertyName).append(BinaryMessageConstants.PARAMS_SEPARATOR)
                        .append(properties.get(propertyName));
                message.append("\n");
            }
            message.append(BinaryMessageConstants.END_ARBITRARY_DATA).append("\n");
        }
        message.append(BinaryMessageConstants.END_EVENT).append("\n");
    }

}
