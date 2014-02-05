/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.event.input.adaptor.jms.internal.util;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.input.adaptor.core.InputEventAdaptorListener;
import org.wso2.carbon.event.input.adaptor.core.exception.InputEventAdaptorEventProcessingException;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class JMSMessageListener implements MessageListener {
    private static final Log log = LogFactory.getLog(JMSMessageListener.class);
    private InputEventAdaptorListener eventAdaptorListener = null;
    private final int tenantId;
    private final String tenantDomain;

    public JMSMessageListener(InputEventAdaptorListener eventAdaptorListener, AxisConfiguration axisConfiguration) {
        this.eventAdaptorListener = eventAdaptorListener;
        tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true);
    }

    public void onMessage(Message message) {

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain);

            if (message instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) message;
                // Send the text of the message. Conversion to any type (XML,JSON) should
                // not happen here since this message will be built later.
                try {
                    String msgText = textMessage.getText();
                    eventAdaptorListener.onEventCall(msgText);
                } catch (JMSException e) {
                    if (log.isErrorEnabled()) {
                        log.error("Failed to get text from " + textMessage, e);
                    }
                } catch (InputEventAdaptorEventProcessingException e) {
                    if (log.isErrorEnabled()) {
                        log.error(e);
                    }
                }
            } else if (message instanceof MapMessage) {
                MapMessage mapMessage = (MapMessage) message;
                Map event = new HashMap();
                try {
                    Enumeration names = mapMessage.getMapNames();
                    Object name;
                    while (names.hasMoreElements()) {
                        name = names.nextElement();
                        event.put(name, mapMessage.getObject((String) name));
                    }
                    eventAdaptorListener.onEventCall(event);

                } catch (JMSException e) {
                    log.error("Can not read the map message ", e);
                } catch (InputEventAdaptorEventProcessingException e) {
                    log.error("Can not send the message to broker ", e);
                }
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }
}
