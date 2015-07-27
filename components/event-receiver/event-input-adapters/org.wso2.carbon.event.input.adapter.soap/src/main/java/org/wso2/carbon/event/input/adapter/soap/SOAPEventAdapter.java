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

package org.wso2.carbon.event.input.adapter.soap;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.InOnlyAxisOperation;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.input.adapter.core.EventAdapterUtil;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapter;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterConfiguration;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterListener;
import org.wso2.carbon.event.input.adapter.core.exception.InputEventAdapterException;
import org.wso2.carbon.event.input.adapter.core.exception.InputEventAdapterRuntimeException;
import org.wso2.carbon.event.input.adapter.core.exception.TestConnectionNotSupportedException;
import org.wso2.carbon.event.input.adapter.soap.internal.SubscriptionMessageReceiver;
import org.wso2.carbon.event.input.adapter.soap.internal.util.SOAPEventAdapterConstants;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class SOAPEventAdapter implements InputEventAdapter {

    private static final Log log = LogFactory.getLog(SOAPEventAdapter.class);
    private final InputEventAdapterConfiguration eventAdapterConfiguration;
    private final Map<String, String> globalProperties;
    private InputEventAdapterListener eventAdaptorListener;
    private final String id = UUID.randomUUID().toString();

    public SOAPEventAdapter(InputEventAdapterConfiguration eventAdapterConfiguration,
                            Map<String, String> globalProperties) {
        this.eventAdapterConfiguration = eventAdapterConfiguration;
        this.globalProperties = globalProperties;
    }

    @Override
    public void init(InputEventAdapterListener eventAdaptorListener) throws InputEventAdapterException {
        this.eventAdaptorListener = eventAdaptorListener;
    }

    @Override
    public void testConnect() throws TestConnectionNotSupportedException {
        throw new TestConnectionNotSupportedException("not-supported");
    }

    @Override
    public void connect() {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        try {
            registerService(eventAdaptorListener, eventAdapterConfiguration.getName(),
                    EventAdapterUtil.getAxisConfiguration());
        } catch (AxisFault axisFault) {
            throw new InputEventAdapterRuntimeException("Cannot register Input Adapter " +
                    eventAdapterConfiguration.getName() + " on tenant " + tenantId, axisFault);
        }
    }

    @Override
    public void disconnect() {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        try {
            unregisterService(eventAdapterConfiguration.getName(),
                    EventAdapterUtil.getAxisConfiguration());
        } catch (AxisFault axisFault) {
            throw new InputEventAdapterRuntimeException("Cannot un-register Input Adapter " +
                    eventAdapterConfiguration.getName() + " on tenant " + tenantId, axisFault);
        }
    }

    @Override
    public void destroy() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SOAPEventAdapter)) return false;

        SOAPEventAdapter that = (SOAPEventAdapter) o;

        return id.equals(that.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean isEventDuplicatedInCluster() {
        return false;
    }

    @Override
    public boolean isPolling() {
        return false;
    }

    private void registerService(InputEventAdapterListener eventAdaptorListener, String serviceName,
                                 AxisConfiguration axisConfiguration) throws AxisFault {


        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        AxisService axisService = axisConfiguration.getService(serviceName);
        if (axisService != null) {
            axisConfiguration.removeService(serviceName);
        }

        // create a new axis service
        axisService = new AxisService(serviceName);
        AxisOperation axisOperation = new InOnlyAxisOperation(new QName(SOAPEventAdapterConstants.OPERATION_NAME));
        axisOperation.setMessageReceiver(new SubscriptionMessageReceiver(eventAdaptorListener, serviceName,
                SOAPEventAdapterConstants.OPERATION_NAME, tenantId));
        axisService.addOperation(axisOperation);

        List<String> transports = axisService.getExposedTransports();
        transports.clear();
        String exposedTransports = eventAdapterConfiguration.getProperties().get(SOAPEventAdapterConstants.EXPOSED_TRANSPORTS);
        if (exposedTransports.equalsIgnoreCase(SOAPEventAdapterConstants.ALL)) {
            transports.add("http");
            transports.add("https");
            transports.add("local");
        } else {
            transports.add(SOAPEventAdapterConstants.EXPOSED_TRANSPORTS);
        }
        axisService.setExposedTransports(transports);
        axisConfiguration.addService(axisService);

        axisOperation.setSoapAction("urn:" + SOAPEventAdapterConstants.OPERATION_NAME);
        axisConfiguration.getPhasesInfo().setOperationPhases(axisOperation);
        axisOperation.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE).setName("in");

    }

    private void unregisterService(String serviceName,
                                   AxisConfiguration axisConfiguration) throws AxisFault {
        AxisService axisService = axisConfiguration.getService(serviceName);
        try{
            if (axisService == null) {
                throw new AxisFault("There is no service with the name " + serviceName);
            }
            AxisOperation axisOperation = axisService.getOperation(new QName(SOAPEventAdapterConstants.OPERATION_NAME));
            if (axisOperation == null) {
                throw new AxisFault("There is no operation with the name " + SOAPEventAdapterConstants.OPERATION_NAME);
            }
            SubscriptionMessageReceiver messageReceiver =
                    (SubscriptionMessageReceiver) axisOperation.getMessageReceiver();
            if (messageReceiver == null) {
                throw new AxisFault("There is no message receiver for operation with name "
                        + SOAPEventAdapterConstants.OPERATION_NAME);
            }
        }finally{
            if(axisService != null){
                axisConfiguration.removeService(serviceName);
            }
        }
    }

}