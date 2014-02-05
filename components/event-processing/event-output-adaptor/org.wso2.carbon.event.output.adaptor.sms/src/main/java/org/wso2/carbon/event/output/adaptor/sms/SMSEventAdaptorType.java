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

package org.wso2.carbon.event.output.adaptor.sms;


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
import org.wso2.carbon.event.output.adaptor.core.AbstractOutputEventAdaptor;
import org.wso2.carbon.event.output.adaptor.core.MessageType;
import org.wso2.carbon.event.output.adaptor.core.Property;
import org.wso2.carbon.event.output.adaptor.core.config.OutputEventAdaptorConfiguration;
import org.wso2.carbon.event.output.adaptor.core.message.config.OutputEventAdaptorMessageConfiguration;
import org.wso2.carbon.event.output.adaptor.sms.internal.ds.SMSEventAdaptorServiceValueHolder;
import org.wso2.carbon.event.output.adaptor.sms.internal.util.SMSEventAdaptorConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

public final class SMSEventAdaptorType extends AbstractOutputEventAdaptor {

    private static final Log log = LogFactory.getLog(SMSEventAdaptorType.class);

    private static SMSEventAdaptorType SMSEventAdaptor = new SMSEventAdaptorType();
    private ConcurrentHashMap<OutputEventAdaptorMessageConfiguration, List<String>> smsSenderConfigurationMap = new ConcurrentHashMap<OutputEventAdaptorMessageConfiguration, List<String>>();
    private ResourceBundle resourceBundle;

    private SMSEventAdaptorType() {

    }

    @Override
    protected List<String> getSupportedOutputMessageTypes() {
        List<String> supportOutputMessageTypes = new ArrayList<String>();
        supportOutputMessageTypes.add(MessageType.XML);
        supportOutputMessageTypes.add(MessageType.JSON);
        supportOutputMessageTypes.add(MessageType.TEXT);
        return supportOutputMessageTypes;
    }

    /**
     * @return Email event adaptor instance
     */
    public static SMSEventAdaptorType getInstance() {
        return SMSEventAdaptor;
    }

    /**
     * @return name of the Email event adaptor
     */
    @Override
    protected String getName() {
        return SMSEventAdaptorConstants.ADAPTOR_TYPE_SMS;
    }

    /**
     * Initialises the resource bundle
     */
    @Override
    protected void init() {
        resourceBundle = ResourceBundle.getBundle("org.wso2.carbon.event.output.adaptor.sms.i18n.Resources", Locale.getDefault());
    }


    /**
     * @return output adaptor configuration property list
     */
    @Override
    public List<Property> getOutputAdaptorProperties() {
        return null;
    }

    /**
     * @return output message configuration property list
     */
    @Override
    public List<Property> getOutputMessageProperties() {
        List<Property> propertyList = new ArrayList<Property>();

        // set sms address
        Property phoneNo = new Property(SMSEventAdaptorConstants.ADAPTOR_MESSAGE_SMS_NO);
        phoneNo.setDisplayName(
                resourceBundle.getString(SMSEventAdaptorConstants.ADAPTOR_MESSAGE_SMS_NO));
        phoneNo.setHint(resourceBundle.getString(SMSEventAdaptorConstants.ADAPTOR_CONF_SMS_HINT_NO));
        phoneNo.setRequired(true);

        propertyList.add(phoneNo);
        return propertyList;
    }

    /**
     * @param outputEventAdaptorMessageConfiguration
     *                - outputEventAdaptorMessageConfiguration to publish messages
     * @param message
     * @param outputEventAdaptorConfiguration
     * @param tenantId
     */
    public void publish(
            OutputEventAdaptorMessageConfiguration outputEventAdaptorMessageConfiguration,
            Object message,
            OutputEventAdaptorConfiguration outputEventAdaptorConfiguration, int tenantId) {

        List<String> smsList = smsSenderConfigurationMap.get(outputEventAdaptorMessageConfiguration.getOutputMessageProperties().get(SMSEventAdaptorConstants.ADAPTOR_MESSAGE_SMS_NO));
        if (smsList == null) {
            smsList = new ArrayList<String>();
            smsList.add(outputEventAdaptorMessageConfiguration.getOutputMessageProperties().get(SMSEventAdaptorConstants.ADAPTOR_MESSAGE_SMS_NO));
            smsSenderConfigurationMap.putIfAbsent(outputEventAdaptorMessageConfiguration, smsList);
        }

        String[] smsNOs = smsList.toArray(new String[0]);
        if (smsNOs != null) {
            for (String smsNo : smsNOs) {
                OMElement payload = OMAbstractFactory.getOMFactory().createOMElement(
                        BaseConstants.DEFAULT_TEXT_WRAPPER, null);
                payload.setText((String) message);

                try {
                    ServiceClient serviceClient;
                    ConfigurationContext configContext = SMSEventAdaptorServiceValueHolder.getConfigurationContextService().getClientConfigContext();
                    if (configContext != null) {
                        serviceClient = new ServiceClient(configContext, null);
                    } else {
                        serviceClient = new ServiceClient();
                    }
                    Options options = new Options();
                    options.setProperty(Constants.Configuration.ENABLE_REST, Constants.VALUE_TRUE);
                    options.setTo(new EndpointReference("sms://" + smsNo));
                    serviceClient.setOptions(options);
                    serviceClient.fireAndForget(payload);

                } catch (AxisFault axisFault) {
                    smsSenderConfigurationMap.remove(outputEventAdaptorMessageConfiguration.getOutputMessageProperties().get(SMSEventAdaptorConstants.ADAPTOR_MESSAGE_SMS_NO));
                    String msg = "Error in delivering the message, " +
                                 "message: " + message + ", to: " + smsNo + ".";
                    log.error(msg, axisFault);
                }catch (Exception ex){
                    String msg = "Error in delivering the message, " +
                                 "message: " + message + ", to: " + smsNo + ".";
                    log.error(msg, ex);
                }
            }
        }
    }


    @Override
    public void testConnection(
            OutputEventAdaptorConfiguration outputEventAdaptorConfiguration, int tenantId) {
        // no test
    }

}
