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

package org.wso2.carbon.event.input.adaptor.email.internal.util;


public final class EmailEventAdaptorConstants {

    private EmailEventAdaptorConstants(){}

    public static final String ADAPTOR_TYPE_EMAIL = "email";

    public static final String ADAPTOR_CONF_RECEIVING_EMAIL_ADDRESS = "transport.mail.Address";
    public static final String ADAPTOR_CONF_RECEIVING_EMAIL_ADDRESS_HINT = "transport.mail.Address.hint";
    public static final String ADAPTOR_CONF_RECEIVING_EMAIL_PROTOCOL = "transport.mail.Protocol";
    public static final String ADAPTOR_CONF_RECEIVING_EMAIL_PROTOCOL_HINT = "transport.mail.Protocol.hint";
    public static final String ADAPTOR_CONF_RECEIVING_EMAIL_POLL_INTERVAL = "transport.PollInterval";
    public static final String ADAPTOR_CONF_RECEIVING_EMAIL_POLL_INTERVAL_HINT = "transport.PollInterval.hint";
    public static final String ADAPTOR_CONF_RECEIVING_EMAIL_PROTOCOL_HOST = "mail.protocol.host" ;
    public static final String ADAPTOR_CONF_RECEIVING_EMAIL_PROTOCOL_PORT = "mail.protocol.port";
    public static final String ADAPTOR_CONF_RECEIVING_EMAIL_USERNAME = "mail.protocol.user";
    public static final String ADAPTOR_CONF_RECEIVING_EMAIL_PASSWORD = "mail.protocol.password";
    public static final String ADAPTOR_CONF_RECEIVING_EMAIL_SOCKET_FACTORY_CLASS = "mail.protocol.socketFactory.class" ;
    public static final String ADAPTOR_CONF_RECEIVING_EMAIL_SOCKET_FACTORY_FALLBACK = "mail.protocol.socketFactory.fallback" ;
    public static final String ADAPTOR_MESSAGE_RECEIVING_EMAIL_SUBJECT = "email.in.subject" ;
    public static final String ADAPTOR_MESSAGE_RECEIVING_EMAIL_SUBJECT_HINT = "email.in.subject.hint" ;

    public static final String BROKER_CONF_EMAIL_PROTOCOL = "transport.mail.Protocol";


}
