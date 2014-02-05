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

package org.wso2.carbon.event.input.adaptor.wsevent.local.internal.util;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.receivers.AbstractInMessageReceiver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.input.adaptor.core.InputEventAdaptorListener;
import org.wso2.carbon.event.input.adaptor.core.exception.InputEventAdaptorEventProcessingException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class SubscriptionMessageReceiver extends AbstractInMessageReceiver {

    private static final Log log = LogFactory.getLog(SubscriptionMessageReceiver.class);

    private List<InputEventAdaptorListener> eventAdaptorListeners;
    private ConcurrentHashMap<String, InputEventAdaptorListener> eventAdaptorListenerMap;

    public SubscriptionMessageReceiver() {
        this.eventAdaptorListenerMap = new ConcurrentHashMap<String, InputEventAdaptorListener>();
        this.eventAdaptorListeners = new ArrayList<InputEventAdaptorListener>();
    }

    public void addEventAdaptorListener(String subscriptionId,
                                        InputEventAdaptorListener eventAdaptorListener) {
        if (null == eventAdaptorListenerMap.putIfAbsent(subscriptionId, eventAdaptorListener)) {
            this.eventAdaptorListeners = new ArrayList<InputEventAdaptorListener>(eventAdaptorListenerMap.values());
        }
    }

    public boolean removeEventAdaptorListener(String subscriptionId) {
        if (null != eventAdaptorListenerMap.remove(subscriptionId)) {
            this.eventAdaptorListeners = new ArrayList<InputEventAdaptorListener>(eventAdaptorListenerMap.values());
        }
        if (eventAdaptorListeners.size() == 0) {
            return true;
        }
        return false;
    }

    protected void invokeBusinessLogic(MessageContext messageContext) throws AxisFault {

        SOAPEnvelope soapEnvelope = messageContext.getEnvelope();
        OMElement bodyElement = soapEnvelope.getBody().getFirstElement();

        try {
            for (InputEventAdaptorListener eventAdaptorListener : this.eventAdaptorListeners) {
                eventAdaptorListener.onEventCall(bodyElement);
            }
        } catch (InputEventAdaptorEventProcessingException e) {
            log.error("Can not process the received event ", e);
        }
    }
}