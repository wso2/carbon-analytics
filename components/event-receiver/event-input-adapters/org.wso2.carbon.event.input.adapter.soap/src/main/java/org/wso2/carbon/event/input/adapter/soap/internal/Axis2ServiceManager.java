/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.event.input.adapter.soap.internal;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.InOnlyAxisOperation;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.receivers.AbstractInMessageReceiver;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.event.input.adapter.core.exception.InputEventAdapterRuntimeException;
import org.wso2.carbon.event.input.adapter.soap.SOAPEventAdapter;
import org.wso2.carbon.event.input.adapter.soap.internal.util.SOAPEventAdapterConstants;
import org.wso2.carbon.event.statistics.internal.Constants;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class Axis2ServiceManager {
    public static Map<String, List<SOAPEventAdapter>> ADAPTER_MAP = new ConcurrentHashMap<String, List<SOAPEventAdapter>>();

    private Axis2ServiceManager() {

    }

    public static synchronized void registerService(String serviceName, SOAPEventAdapter soapEventAdapter,
                                                    AxisConfiguration axisConfiguration) throws AxisFault {


//        TenantManager.getTenantAxisConfiguration()
//        TenantAxisUtils.getTenantAxisConfiguration()
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        String id = tenantId + SOAPEventAdapterConstants.SEPARATOR + serviceName;

        List<SOAPEventAdapter> adapterList = ADAPTER_MAP.get(id);
        if (adapterList == null) {
            adapterList = new CopyOnWriteArrayList<SOAPEventAdapter>();
            adapterList.add(soapEventAdapter);
            ADAPTER_MAP.put(id, adapterList);

            AxisService axisService = axisConfiguration.getService(serviceName);
            if (axisService == null) {
                // create a new axis service
                axisService = new AxisService(serviceName);

                List<String> transports = axisService.getExposedTransports();
                transports.clear();
                transports.add("http");
                transports.add("https");
                transports.add("local");
                axisService.setExposedTransports(transports);

                axisConfiguration.addService(axisService);
                axisService.getAxisServiceGroup().addParameter(CarbonConstants.DYNAMIC_SERVICE_PARAM_NAME, "true");

                AxisOperation axisOperation = axisService.getOperation(new QName("",
                        SOAPEventAdapterConstants.OPERATION_NAME));
                if (axisOperation == null) {
                    axisOperation = new InOnlyAxisOperation(new QName("", SOAPEventAdapterConstants.OPERATION_NAME));
                    axisOperation.setMessageReceiver(new SubscriptionMessageReceiver(serviceName,
                            SOAPEventAdapterConstants.OPERATION_NAME, tenantId));
                    axisOperation.setSoapAction("urn:" + SOAPEventAdapterConstants.OPERATION_NAME);
                    axisConfiguration.getPhasesInfo().setOperationPhases(axisOperation);
                    axisService.addOperation(axisOperation);
                }
            }

        } else {
            adapterList.add(soapEventAdapter);
        }
    }

    public static void unregisterService(String serviceName, SOAPEventAdapter soapEventAdapter,
                                         AxisConfiguration axisConfiguration) throws AxisFault {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        String id = tenantId + SOAPEventAdapterConstants.SEPARATOR + serviceName + SOAPEventAdapterConstants.SEPARATOR;
        List<SOAPEventAdapter> soapEventAdapters = ADAPTER_MAP.get(id);
        soapEventAdapters.remove(soapEventAdapter);
        if (soapEventAdapters.size() == 0) {
            ADAPTER_MAP.remove(id);

            AxisService axisService = axisConfiguration.getService(serviceName);

            if (axisService == null) {
                throw new AxisFault("There is no service with the name " + serviceName);
            }

            AxisOperation axisOperation = axisService.getOperation(new QName("",
                    SOAPEventAdapterConstants.OPERATION_NAME));
            if (axisOperation == null) {
                throw new AxisFault("There is no operation with the name " + SOAPEventAdapterConstants.OPERATION_NAME);
            }
            SubscriptionMessageReceiver messageReceiver =
                    (SubscriptionMessageReceiver) axisOperation.getMessageReceiver();
            if (messageReceiver == null) {
                throw new AxisFault("There is no message receiver for operation with name "
                        + SOAPEventAdapterConstants.OPERATION_NAME);
            }

            axisService.removeOperation(new QName("", SOAPEventAdapterConstants.OPERATION_NAME));
        }

    }


    static class SubscriptionMessageReceiver extends AbstractInMessageReceiver {
        private final String serviceName;
        private final String operationName;
        private final int tenantId;
        private final String id;

        public SubscriptionMessageReceiver(String serviceName, String operationName, int tenantId) {
            this.serviceName = serviceName;
            this.operationName = operationName;
            this.tenantId = tenantId;
            id = tenantId + SOAPEventAdapterConstants.SEPARATOR + serviceName + SOAPEventAdapterConstants.SEPARATOR
                    + operationName;
        }

        protected void invokeBusinessLogic(MessageContext messageContext) throws AxisFault {

            SOAPEnvelope soapEnvelope = messageContext.getEnvelope();
            OMElement bodyElement = soapEnvelope.getBody().getFirstElement();

            if (log.isDebugEnabled()) {
                log.debug("Event received in Soap Input Event Adaptor - " + bodyElement);
            }
            if (bodyElement != null) {
                try {
                    List<SOAPEventAdapter> soapEventAdapterList = ADAPTER_MAP.get(id);
                    if (soapEventAdapterList != null) {
                        for (SOAPEventAdapter eventAdaptor : soapEventAdapterList) {
                            eventAdaptor.getEventAdaptorListener().onEvent(bodyElement);
                        }
                    }
                } catch (InputEventAdapterRuntimeException e) {
                    log.error("Can not process the received event ", e);
                }
            } else {
                log.warn("Dropping the empty/null event received through soap adaptor service " + serviceName
                        + " for the operation " + operationName + " & tenant " + tenantId);
            }
        }
    }
}
