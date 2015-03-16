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
package org.wso2.carbon.databridge.receiver.binary.internal;

import org.wso2.carbon.databridge.commons.binary.BinaryMessageConstants;
import org.wso2.carbon.databridge.commons.binary.BinaryMessageConverterUtil;
import org.wso2.carbon.databridge.commons.exception.MalformedEventException;
import org.wso2.carbon.databridge.core.DataBridgeReceiverService;
import org.wso2.carbon.databridge.receiver.binary.BinaryEventConverter;

import java.util.Arrays;

public class RequestProcessor {
    private StringBuilder stream;
    private boolean isPublishMessage;
    private boolean isLoginMessage;
    private boolean isLogoutMessage;
    private boolean isMessageEnded;
    private String[] nextExpectedElement;
    private String sessionId;
    private String userName;
    private String password;
    private DataBridgeReceiverService dataBridgeReceiverService;

    public RequestProcessor(DataBridgeReceiverService dataBridgeReceiverService) {
        this.stream = new StringBuilder();
        this.dataBridgeReceiverService = dataBridgeReceiverService;
    }


    /**
     * Read the message and validate it, and put the string binary bundle to queue.
     * and pass the event converter for binary message
     *
     * @param messageLine One line of binary message received.
     * @return returns response that needs to be sent back to the client.
     */
    public String consume(String messageLine) throws
            Exception {
        if (stream.length() == 0) {
            if (!messageLine.equals(BinaryMessageConstants.START_MESSAGE)) {
                throw new MalformedEventException("The binary message should start with " +
                        BinaryMessageConstants.START_MESSAGE + " clause, but found : " + messageLine);
            }
            stream.append(messageLine).append("\n");
            nextExpectedElement = new String[]{
                    BinaryMessageConstants.SESSION_ID_PREFIX,
                    BinaryMessageConstants.LOGIN_OPERATION,
                    BinaryMessageConstants.LOGOUT_OPERATION};
        } else {
            if (isPublishMessage) {
                consumePublishMessage(messageLine);
            } else if (isLoginMessage) {
                consumeLoginMessage(messageLine);
            } else if (isLogoutMessage) {
                consumeLogoutMessage(messageLine);
            } else if (messageLine.equals(BinaryMessageConstants.LOGIN_OPERATION)) {
                isLoginMessage = true;
                consumeLoginMessage(messageLine);
            } else if (messageLine.equals(BinaryMessageConstants.LOGOUT_OPERATION)) {
                isLogoutMessage = true;
                consumeLogoutMessage(messageLine);
            } else {
                isPublishMessage = true;
                consumePublishMessage(messageLine);
            }
        }
        if (isMessageEnded) {
            return processStream();
        } else {
            return null;
        }
    }

    private void consumePublishMessage(String messageLine) throws
            Exception {
        String matchedElement = validateMessageLine(messageLine);
        if (matchedElement.equals(BinaryMessageConstants.SESSION_ID_PREFIX)) {
            sessionId = messageLine.replace(BinaryMessageConstants.SESSION_ID_PREFIX, "");
            nextExpectedElement = new String[]{BinaryMessageConstants.PUBLISH_OPERATION};
        } else if (messageLine.equals(BinaryMessageConstants.PUBLISH_OPERATION)) {
            nextExpectedElement = new String[]{BinaryMessageConstants.START_EVENT};
        } else if (messageLine.equals(BinaryMessageConstants.START_EVENT)) {
            nextExpectedElement = new String[]{BinaryMessageConstants.STREAM_ID_PREFIX};
        } else if (matchedElement.equals(BinaryMessageConstants.STREAM_ID_PREFIX)) {
            nextExpectedElement = new String[]{BinaryMessageConstants.TIME_STAMP_PREFIX};
        } else if (matchedElement.equals(BinaryMessageConstants.TIME_STAMP_PREFIX)) {
            nextExpectedElement = new String[]{BinaryMessageConstants.START_META_DATA,
                    BinaryMessageConstants.START_CORRELATION_DATA,
                    BinaryMessageConstants.START_PAYLOAD_DATA,
                    BinaryMessageConstants.START_ARBITRARY_DATA};
        } else if (messageLine.equals(BinaryMessageConstants.START_META_DATA)) {
            nextExpectedElement = new String[0];
        } else if (messageLine.equals(BinaryMessageConstants.START_CORRELATION_DATA)) {
            nextExpectedElement = new String[0];
        } else if (messageLine.equals(BinaryMessageConstants.START_PAYLOAD_DATA)) {
            nextExpectedElement = new String[0];
        } else if (messageLine.equals(BinaryMessageConstants.START_ARBITRARY_DATA)) {
            nextExpectedElement = new String[0];
        } else if (messageLine.equals(BinaryMessageConstants.END_META_DATA)) {
            nextExpectedElement = new String[]{
                    BinaryMessageConstants.START_CORRELATION_DATA,
                    BinaryMessageConstants.START_PAYLOAD_DATA,
                    BinaryMessageConstants.START_ARBITRARY_DATA,
                    BinaryMessageConstants.END_EVENT,
                    BinaryMessageConstants.END_MESSAGE};
        } else if (messageLine.equals(BinaryMessageConstants.END_CORRELATION_DATA)) {
            nextExpectedElement = new String[]{
                    BinaryMessageConstants.START_META_DATA,
                    BinaryMessageConstants.START_PAYLOAD_DATA,
                    BinaryMessageConstants.START_ARBITRARY_DATA,
                    BinaryMessageConstants.END_EVENT,
                    BinaryMessageConstants.END_MESSAGE};
        } else if (messageLine.equals(BinaryMessageConstants.END_PAYLOAD_DATA)) {
            nextExpectedElement = new String[]{
                    BinaryMessageConstants.START_META_DATA,
                    BinaryMessageConstants.START_CORRELATION_DATA,
                    BinaryMessageConstants.START_ARBITRARY_DATA,
                    BinaryMessageConstants.END_EVENT,
                    BinaryMessageConstants.END_MESSAGE};
        } else if (messageLine.equals(BinaryMessageConstants.END_ARBITRARY_DATA)) {
            nextExpectedElement = new String[]{
                    BinaryMessageConstants.START_META_DATA,
                    BinaryMessageConstants.START_CORRELATION_DATA,
                    BinaryMessageConstants.START_PAYLOAD_DATA,
                    BinaryMessageConstants.END_EVENT,
                    BinaryMessageConstants.END_MESSAGE};
        } else if (messageLine.equals(BinaryMessageConstants.END_EVENT)) {
            nextExpectedElement = new String[]{BinaryMessageConstants.START_EVENT,
                    BinaryMessageConstants.END_MESSAGE};
        } else if (messageLine.equals(BinaryMessageConstants.END_MESSAGE)) {
            nextExpectedElement = new String[0];
            isMessageEnded = true;
        }
        stream.append(messageLine).append("\n");
    }

    private void consumeLoginMessage(String messageLine) {
        validateMessageLine(messageLine);
        if (messageLine.equals(BinaryMessageConstants.LOGIN_OPERATION)) {
            nextExpectedElement = new String[0];
        } else if (messageLine.equals(BinaryMessageConstants.END_MESSAGE)) {
            isMessageEnded = true;
        } else {
            String[] userNamePw = messageLine.split(BinaryMessageConstants.PARAMS_SEPARATOR);
            this.userName = userNamePw[0];
            this.password = userNamePw[1];
        }
        stream.append(messageLine).append("\n");
    }

    private void consumeLogoutMessage(String messageLine) {
        String matchedContent = validateMessageLine(messageLine);
        if (messageLine.equals(BinaryMessageConstants.LOGOUT_OPERATION)) {
            nextExpectedElement = new String[]{BinaryMessageConstants.SESSION_ID_PREFIX};
        } else if (matchedContent.equals(BinaryMessageConstants.SESSION_ID_PREFIX)) {
            nextExpectedElement = new String[]{BinaryMessageConstants.END_MESSAGE};
            sessionId = messageLine.replace(BinaryMessageConstants.SESSION_ID_PREFIX, "");
        } else if (messageLine.equals(BinaryMessageConstants.END_MESSAGE)) {
            nextExpectedElement = new String[0];
            isMessageEnded = true;
        } else {
            throw new MalformedEventException("Expected elements are : "
                    + Arrays.toString(nextExpectedElement) + " but found :" + messageLine);
        }
        stream.append(messageLine).append("\n");
    }

    private String validateMessageLine(String messageLine) {
        for (String aNextElement : nextExpectedElement) {
            if (messageLine.startsWith(aNextElement))
                return aNextElement;
        }
        if (nextExpectedElement.length != 0) {
            throw new MalformedEventException("Unexpected message content :" + messageLine
                    + " found where it is expected to start as : " + Arrays.toString(nextExpectedElement));
        } else {
            return "";
        }
    }

    public boolean isMessageEnded() {
        return isMessageEnded;
    }

    public String processStream() throws
            Exception {
        if (this.isPublishMessage) {
            this.dataBridgeReceiverService.publish(this.stream.toString(), this.sessionId, BinaryEventConverter.getConverter());
            return BinaryMessageConverterUtil.getPublishSuccessResponse();
        } else if (this.isLoginMessage) {
            String sessionId = this.dataBridgeReceiverService.login(this.userName, this.password);
            return BinaryMessageConverterUtil.getLoginSuccessResponse(sessionId);
        } else if (this.isLogoutMessage) {
            this.dataBridgeReceiverService.logout(this.sessionId);
            return BinaryMessageConverterUtil.getLogoutSuccessResponse();
        } else {
            return null;
        }
    }
}
