/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.event.output.adapter.sms;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.transport.base.BaseConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapter;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;
import org.wso2.carbon.event.output.adapter.core.exception.TestConnectionNotSupportedException;
import org.wso2.carbon.event.output.adapter.sms.internal.ds.SMSEventAdapterServiceValueHolder;
import org.wso2.carbon.event.output.adapter.sms.internal.util.SMSEventAdapterConstants;

import java.util.Map;

public final class SMSEventAdapter implements OutputEventAdapter {

    private static final Log log = LogFactory.getLog(SMSEventAdapter.class);
    private OutputEventAdapterConfiguration eventAdapterConfiguration;
    private Map<String, String> globalProperties;

    private ServiceClient serviceClient;

    public SMSEventAdapter(OutputEventAdapterConfiguration eventAdapterConfiguration, Map<String, String> globalProperties) {
        this.eventAdapterConfiguration = eventAdapterConfiguration;
        this.globalProperties = globalProperties;
    }

    @Override
    public void init() throws OutputEventAdapterException {
        String SMSNo = eventAdapterConfiguration.getStaticProperties().get(SMSEventAdapterConstants.ADAPTER_MESSAGE_SMS_NO);
        ConfigurationContext configContext = SMSEventAdapterServiceValueHolder.getConfigurationContextService().getClientConfigContext();
        try{
            if (configContext != null) {
                serviceClient = new ServiceClient(configContext, null);
            } else {
                serviceClient = new ServiceClient();
            }
            Options options = new Options();
            options.setProperty(Constants.Configuration.ENABLE_REST, Constants.VALUE_TRUE);
            options.setTo(new EndpointReference("sms://" + SMSNo));
            serviceClient.setOptions(options);
        } catch (AxisFault axisFault){
            String systemType = serviceClient.getAxisConfiguration().getTransportOut("sms").getParameter("systemType").toString();
            String systemID = serviceClient.getAxisConfiguration().getTransportOut("sms").getParameter("systemId").toString();
            String password = serviceClient.getAxisConfiguration().getTransportOut("sms").getParameter("password").toString();
            String host = serviceClient.getAxisConfiguration().getTransportOut("sms").getParameter("host").toString();
            String port = serviceClient.getAxisConfiguration().getTransportOut("sms").getParameter("port").toString();
            String phoneNumber = serviceClient.getAxisConfiguration().getTransportOut("sms").getParameter("phoneNumber").toString();
            String msg = "Error in creating client, " +
                    "with configuration parameters systemType: " + systemType + ", systemID: " + systemID + ", password: " + password
                    + ", host: " + host + ", port: " + port + ", phoneNumber: " + phoneNumber
                    + ", to: " + eventAdapterConfiguration.getStaticProperties().get(SMSEventAdapterConstants.ADAPTER_MESSAGE_SMS_NO) + ".";
            throw new OutputEventAdapterException(msg, axisFault);
        }
    }

    @Override
    public void testConnect() throws TestConnectionNotSupportedException {
        throw new TestConnectionNotSupportedException("not-available");
    }

    @Override
    public void connect() {
        //not applicable.
    }

    @Override
    public void publish(Object message, Map<String, String> dynamicProperties) {
        OMElement payload = OMAbstractFactory.getOMFactory().createOMElement(
                BaseConstants.DEFAULT_TEXT_WRAPPER, null);
        payload.setText(message.toString());
        try {
            serviceClient.fireAndForget(payload);
        } catch (AxisFault axisFault) {
            String msg = "Error in delivering the message, " +
                    "message: " + message + ", to: " + eventAdapterConfiguration.getStaticProperties().get(SMSEventAdapterConstants.ADAPTER_MESSAGE_SMS_NO) + ".";
            log.error(msg, axisFault);
        } catch (Exception ex) {
            String msg = "Error in delivering the message, " +
                    "message: " + message + ", to: " + eventAdapterConfiguration.getStaticProperties().get(SMSEventAdapterConstants.ADAPTER_MESSAGE_SMS_NO) + ".";
            log.error(msg, ex);
        }
    }

    @Override
    public void disconnect() {
        //Not applicable.
    }

    @Override
    public void destroy() {
        //Not required.
    }
}
