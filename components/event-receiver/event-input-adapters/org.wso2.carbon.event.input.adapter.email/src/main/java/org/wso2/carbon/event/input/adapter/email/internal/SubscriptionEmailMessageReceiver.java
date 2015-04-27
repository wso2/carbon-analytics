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
package org.wso2.carbon.event.input.adapter.email.internal;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.receivers.AbstractInMessageReceiver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterListener;
import org.wso2.carbon.event.input.adapter.core.exception.InputEventAdapterRuntimeException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class SubscriptionEmailMessageReceiver extends AbstractInMessageReceiver {

    private static final Log log = LogFactory.getLog(SubscriptionEmailMessageReceiver.class);

    private List<InputEventAdapterListener> eventAdaptorListeners;
    private ConcurrentHashMap<String, InputEventAdapterListener> eventAdaptorListenerMap;

    public SubscriptionEmailMessageReceiver() {
        this.eventAdaptorListenerMap = new ConcurrentHashMap<String, InputEventAdapterListener>();
        this.eventAdaptorListeners = new ArrayList<InputEventAdapterListener>();
    }

    public void addEventAdaptorListener(String subscriptionId,
                                        InputEventAdapterListener eventAdaptorListener) {
        if (null == eventAdaptorListenerMap.putIfAbsent(subscriptionId, eventAdaptorListener)) {
            this.eventAdaptorListeners = new ArrayList<InputEventAdapterListener>(eventAdaptorListenerMap.values());
        }
    }

    public boolean removeEventAdaptorListener(String subscriptionId) {
        if (null != eventAdaptorListenerMap.remove(subscriptionId)) {
            this.eventAdaptorListeners = new ArrayList<InputEventAdapterListener>(eventAdaptorListenerMap.values());
        }
        if (eventAdaptorListeners.size() == 0) {
            return true;
        }
        return false;
    }

    protected void invokeBusinessLogic(MessageContext messageContext) throws AxisFault {

        SOAPEnvelope soapEnvelope = messageContext.getEnvelope();
        OMElement bodyElement = soapEnvelope.getBody().getFirstElement();
        String emailBody = bodyElement.getText();
        try {
            for (InputEventAdapterListener eventAdaptorListener : this.eventAdaptorListeners) {
                if (emailBody.trim() == null) {
                    log.warn("Dropping the empty/null event received through email adaptor");
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Event received in email Event Adaptor - " + bodyElement.getText());
                    }
                    eventAdaptorListener.onEvent(bodyElement.getText());
                }
            }
        } catch (InputEventAdapterRuntimeException e) {
            log.error("Can not process the received event ", e);
        }
    }
}