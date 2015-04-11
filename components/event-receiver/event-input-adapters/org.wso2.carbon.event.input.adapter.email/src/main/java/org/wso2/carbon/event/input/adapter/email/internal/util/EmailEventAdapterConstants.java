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
package org.wso2.carbon.event.input.adapter.email.internal.util;


public final class EmailEventAdapterConstants {

    private EmailEventAdapterConstants() {
    }

    public static final String ADAPTER_TYPE_EMAIL = "email";

    public static final String ADAPTER_CONF_RECEIVING_EMAIL_ADDRESS = "transport.mail.Address";
    public static final String ADAPTER_CONF_RECEIVING_EMAIL_ADDRESS_HINT = "transport.mail.Address.hint";
    public static final String ADAPTER_CONF_RECEIVING_EMAIL_PROTOCOL = "transport.mail.Protocol";
    public static final String ADAPTER_CONF_RECEIVING_EMAIL_PROTOCOL_HINT = "transport.mail.Protocol.hint";
    public static final String ADAPTER_CONF_RECEIVING_EMAIL_POLL_INTERVAL = "transport.PollInterval";
    public static final String ADAPTER_CONF_RECEIVING_EMAIL_POLL_INTERVAL_HINT = "transport.PollInterval.hint";
    public static final String ADAPTER_CONF_RECEIVING_EMAIL_PROTOCOL_HOST = "mail.protocol.host";
    public static final String ADAPTER_CONF_RECEIVING_EMAIL_PROTOCOL_HOST_HINT = "mail.protocol.host.hint";
    public static final String ADAPTER_CONF_RECEIVING_EMAIL_PROTOCOL_PORT = "mail.protocol.port";
    public static final String ADAPTER_CONF_RECEIVING_EMAIL_PROTOCOL_PORT_HINT = "mail.protocol.port.hint";
    public static final String ADAPTER_CONF_RECEIVING_EMAIL_USERNAME = "mail.protocol.user";
    public static final String ADAPTER_CONF_RECEIVING_EMAIL_USERNAME_HINT = "mail.protocol.user.hint";
    public static final String ADAPTER_CONF_RECEIVING_EMAIL_PASSWORD = "mail.protocol.password";
    public static final String ADAPTER_CONF_RECEIVING_EMAIL_PASSWORD_HINT = "mail.protocol.password.hint";
    public static final String ADAPTER_CONF_RECEIVING_EMAIL_SOCKET_FACTORY_CLASS = "mail.protocol.socketFactory.class";
    public static final String ADAPTER_CONF_RECEIVING_EMAIL_SOCKET_FACTORY_FALLBACK =
            "mail.protocol.socketFactory.fallback";
    public static final String ADAPTER_CONF_RECEIVING_EMAIL_TRANSPORT_NAME = "mailto";
    public static final String ADAPTER_MESSAGE_RECEIVING_EMAIL_SUBJECT = "email.in.subject";
    public static final String ADAPTER_MESSAGE_RECEIVING_EMAIL_SUBJECT_HINT = "email.in.subject.hint";

    public static final String BROKER_CONF_EMAIL_PROTOCOL = "transport.mail.Protocol";
    public static final int AXIS_TIME_INTERVAL_IN_MILLISECONDS = 10000;


}
