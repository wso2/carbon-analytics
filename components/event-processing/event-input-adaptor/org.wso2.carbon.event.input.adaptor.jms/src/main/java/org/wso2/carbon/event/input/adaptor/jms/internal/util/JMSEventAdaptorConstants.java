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

package org.wso2.carbon.event.input.adaptor.jms.internal.util;


public final class JMSEventAdaptorConstants {


    private JMSEventAdaptorConstants() {
    }

    public static final String ADAPTOR_TYPE_JMS = "jms";

    public static final String JNDI_INITIAL_CONTEXT_FACTORY_CLASS = "java.naming.factory.initial";
    public static final String JNDI_INITIAL_CONTEXT_FACTORY_CLASS_HINT = "java.naming.factory.initial.hint";
    public static final String JAVA_NAMING_PROVIDER_URL = "java.naming.provider.url";
    public static final String JAVA_NAMING_PROVIDER_URL_HINT = "java.naming.provider.url.hint";
    public static final String ADAPTOR_JMS_USERNAME = "transport.jms.UserName";
    public static final String ADAPTOR_JMS_PASSWORD = "transport.jms.Password";
    public static final String ADAPTOR_JMS_CONNECTION_FACTORY_JNDINAME = "transport.jms.ConnectionFactoryJNDIName";
    public static final String ADAPTOR_JMS_CONNECTION_FACTORY_JNDINAME_HINT = "transport.jms.ConnectionFactoryJNDIName.hint";
    public static final String ADAPTOR_JMS_DURABLE_SUBSCRIBER_NAME = "transport.jms.DurableSubscriberName";
    public static final String ADAPTOR_JMS_DURABLE_SUBSCRIBER_NAME_HINT = "transport.jms.DurableSubscriberName.hint";
    public static final String ADAPTOR_JMS_SUBSCRIPTION_DURABLE = "transport.jms.SubscriptionDurable";
    public static final String ADAPTOR_JMS_SUBSCRIPTION_DURABLE_HINT = "transport.jms.SubscriptionDurable.hint";
    public static final String ADAPTOR_JMS_DESTINATION_TYPE = "transport.jms.DestinationType";
    public static final String ADAPTOR_JMS_DESTINATION_TYPE_HINT = "transport.jms.DestinationType.hint";

    public static final String ADAPTOR_JMS_DESTINATION = "transport.jms.Destination";
    public static final String ADAPTOR_JMS_DESTINATION_HINT = "transport.jms.Destination.hint";


}
