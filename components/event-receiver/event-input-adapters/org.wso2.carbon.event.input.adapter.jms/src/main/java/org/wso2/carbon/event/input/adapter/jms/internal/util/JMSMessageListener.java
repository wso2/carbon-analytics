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
package org.wso2.carbon.event.input.adapter.jms.internal.util;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterListener;
import org.wso2.carbon.event.input.adapter.core.exception.InputEventAdapterRuntimeException;

import javax.jms.*;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class JMSMessageListener implements MessageListener {
    private static final Log log = LogFactory.getLog(JMSMessageListener.class);
    private InputEventAdapterListener eventAdaptorListener = null;
    private final int tenantId;
    private final String tenantDomain;

    public JMSMessageListener(InputEventAdapterListener eventAdaptorListener) {
        this.eventAdaptorListener = eventAdaptorListener;
        tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true);
    }

    public void onMessage(Message message) {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain);
            if (message != null) {

                if (log.isDebugEnabled()) {
                    log.debug("Event received in JMS Event Adaptor - " + message);
                }

                if (message instanceof TextMessage) {
                    TextMessage textMessage = (TextMessage) message;
                    // Send the text of the message. Conversion to any type (XML,JSON) should
                    // not happen here since this message will be built later.
                    try {
                        String msgText = textMessage.getText();
                        eventAdaptorListener.onEvent(msgText);
                    } catch (JMSException e) {
                        if (log.isErrorEnabled()) {
                            log.error("Failed to get text from " + textMessage, e);
                        }
                    } catch (InputEventAdapterRuntimeException e) {
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
                        eventAdaptorListener.onEvent(event);

                    } catch (JMSException e) {
                        log.error("Can not read the map message ", e);
                    } catch (InputEventAdapterRuntimeException e) {
                        log.error("Can not send the message to broker ", e);
                    }
                } else if (message instanceof BytesMessage) {
                    BytesMessage bytesMessage = (BytesMessage) message;
                    byte[] bytes;
                    bytes = new byte[(int) bytesMessage.getBodyLength()];
                    bytesMessage.readBytes(bytes);
                    eventAdaptorListener.onEvent(new String(bytes, "UTF-8"));
                } else {
                    log.warn("Event dropped due to unsupported message type");
                }
            } else {
                log.warn("Dropping the empty/null event received through jms adaptor");
            }
        } catch (JMSException e) {
            log.error(e);
        } catch (UnsupportedEncodingException e) {
            log.error(e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }
}
