/*
*  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.event.input.adaptor.email.internal;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.InOnlyAxisOperation;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisEvent;
import org.apache.axis2.transport.mail.MailConstants;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.event.input.adaptor.core.InputEventAdaptorListener;
import org.wso2.carbon.event.input.adaptor.email.internal.util.EmailEventAdaptorConstants;
import org.wso2.carbon.event.input.adaptor.core.config.InputEventAdaptorConfiguration;
import org.wso2.carbon.event.input.adaptor.core.message.config.InputEventAdaptorMessageConfiguration;

import javax.xml.namespace.QName;
import java.util.List;

public class Axis2Util {

    private Axis2Util(){}

    public static AxisService registerAxis2EmailService(InputEventAdaptorMessageConfiguration inputEventAdaptorMessageConfiguration, InputEventAdaptorListener eventAdaptorListener, InputEventAdaptorConfiguration inputEventAdaptorConfiguration,
                                                        AxisConfiguration axisConfiguration, String subscriptionId) throws AxisFault {
        //first create an Axis2 service to receive the messages to this broker
        //operation name can not have
        String axisServiceName = inputEventAdaptorConfiguration.getName() + "Service";
        AxisService axisService = axisConfiguration.getService(axisServiceName);
        if (axisService == null) {
            // create a new axis service
            axisService = new AxisService(axisServiceName);
            axisService.addParameter(EmailEventAdaptorConstants.ADAPTOR_CONF_RECEIVING_EMAIL_ADDRESS, inputEventAdaptorConfiguration.getInputProperties().get(EmailEventAdaptorConstants.ADAPTOR_CONF_RECEIVING_EMAIL_ADDRESS));
            axisService.addParameter(EmailEventAdaptorConstants.ADAPTOR_CONF_RECEIVING_EMAIL_PROTOCOL, inputEventAdaptorConfiguration.getInputProperties().get(EmailEventAdaptorConstants.ADAPTOR_CONF_RECEIVING_EMAIL_PROTOCOL));
            axisService.addParameter(EmailEventAdaptorConstants.ADAPTOR_CONF_RECEIVING_EMAIL_POLL_INTERVAL, inputEventAdaptorConfiguration.getInputProperties().get(EmailEventAdaptorConstants.ADAPTOR_CONF_RECEIVING_EMAIL_POLL_INTERVAL));
            axisService.addParameter("mail." + inputEventAdaptorConfiguration.getInputProperties().get(EmailEventAdaptorConstants.BROKER_CONF_EMAIL_PROTOCOL) + ".host", inputEventAdaptorConfiguration.getInputProperties().get(EmailEventAdaptorConstants.ADAPTOR_CONF_RECEIVING_EMAIL_PROTOCOL_HOST));
            axisService.addParameter("mail." + inputEventAdaptorConfiguration.getInputProperties().get(EmailEventAdaptorConstants.BROKER_CONF_EMAIL_PROTOCOL) + ".port", inputEventAdaptorConfiguration.getInputProperties().get(EmailEventAdaptorConstants.ADAPTOR_CONF_RECEIVING_EMAIL_PROTOCOL_PORT));
            axisService.addParameter("mail." + inputEventAdaptorConfiguration.getInputProperties().get(EmailEventAdaptorConstants.BROKER_CONF_EMAIL_PROTOCOL) + ".user", inputEventAdaptorConfiguration.getInputProperties().get(EmailEventAdaptorConstants.ADAPTOR_CONF_RECEIVING_EMAIL_USERNAME));
            axisService.addParameter("mail." + inputEventAdaptorConfiguration.getInputProperties().get(EmailEventAdaptorConstants.BROKER_CONF_EMAIL_PROTOCOL) + ".password", inputEventAdaptorConfiguration.getInputProperties().get(EmailEventAdaptorConstants.ADAPTOR_CONF_RECEIVING_EMAIL_PASSWORD));
            axisService.addParameter("mail." + inputEventAdaptorConfiguration.getInputProperties().get(EmailEventAdaptorConstants.BROKER_CONF_EMAIL_PROTOCOL) + ".socketFactory.class", inputEventAdaptorConfiguration.getInputProperties().get(EmailEventAdaptorConstants.ADAPTOR_CONF_RECEIVING_EMAIL_SOCKET_FACTORY_CLASS));
            axisService.addParameter("mail." + inputEventAdaptorConfiguration.getInputProperties().get(EmailEventAdaptorConstants.BROKER_CONF_EMAIL_PROTOCOL) + ".socketFactory.fallback", inputEventAdaptorConfiguration.getInputProperties().get(EmailEventAdaptorConstants.ADAPTOR_CONF_RECEIVING_EMAIL_SOCKET_FACTORY_FALLBACK));
            axisService.addParameter("transport.mail.ContentType", "text/plain");

//            axisService = new AxisService(axisServiceName);
//            axisService.addParameter("transport.mail.Address", "wso2cep.demo@gmail.com");
//            axisService.addParameter("transport.mail.Protocol", "pop3");
//            axisService.addParameter("transport.PollInterval", "5");
//            axisService.addParameter("mail.pop3.host", "pop.gmail.com");
//            axisService.addParameter("mail.pop3.port", "995");
//            axisService.addParameter("mail.pop3.user", "suhothayan");
//            axisService.addParameter("mail.pop3.password", "hhjhkjjlkj");
//            axisService.addParameter("mail.pop3.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
//            axisService.addParameter("mail.pop3.socketFactory.fallback", "false");
////            axisService.addParameter("transport.mail.ContentType","application/xml");
//            axisService.addParameter("transport.mail.ContentType", "text/plain");
////            axisService.setEPRs(new String[]{"wso2cep.demo@gmail.com"});

//            <parameter name="transport.mail.Address">synapse.demo.1@gmail.com</parameter>
//            <parameter name="transport.mail.Protocol">pop3</parameter>
//            <parameter name="transport.PollInterval">5</parameter>
//            <parameter name="mail.pop3.host">pop.gmail.com</parameter>
//            <parameter name="mail.pop3.port">995</parameter>
//            <parameter name="mail.pop3.user">synapse.demo.1</parameter>
//            <parameter name="mail.pop3.password">mailpassword</parameter>
//            <parameter name="mail.pop3.socketFactory.class">javax.net.ssl.SSLSocketFactory</parameter>
//            <parameter name="mail.pop3.socketFactory.fallback">false</parameter>
//            <parameter name="mail.pop3.socketFactory.port">995</parameter>
//            <parameter name="transport.mail.ContentType">application/xml</parameter>


            axisConfiguration.addService(axisService);
            axisService.getAxisServiceGroup().addParameter(CarbonConstants.DYNAMIC_SERVICE_PARAM_NAME, "true");
        }

        String operationName = inputEventAdaptorMessageConfiguration.getInputMessageProperties().get(EmailEventAdaptorConstants.ADAPTOR_MESSAGE_RECEIVING_EMAIL_SUBJECT);
        AxisOperation axisOperation = axisService.getOperation(new QName("", operationName));
        if (axisOperation == null) {
            axisOperation = new InOnlyAxisOperation(new QName("", operationName));
            axisOperation.setMessageReceiver(new SubscriptionEmailMessageReceiver());
            axisOperation.setSoapAction("urn:" + operationName);

            axisConfiguration.getPhasesInfo().setOperationPhases(axisOperation);
            axisService.addOperation(axisOperation);
        }
        List<String> transports = axisService.getExposedTransports();
        transports.clear();
        transports.add(MailConstants.TRANSPORT_NAME);
        axisService.setExposedTransports(transports);

        SubscriptionEmailMessageReceiver messageReceiver =
                (SubscriptionEmailMessageReceiver) axisOperation.getMessageReceiver();
        messageReceiver.addEventAdaptorListener(subscriptionId, eventAdaptorListener);

//        AxisEvent serviceUpdateEvent = new AxisEvent(AxisEvent.SERVICE_DEPLOY,axisService);
        axisConfiguration.notifyObservers(new AxisEvent(AxisEvent.SERVICE_REMOVE, axisService), axisService);
        axisConfiguration.notifyObservers(new AxisEvent(AxisEvent.SERVICE_DEPLOY, axisService), axisService);

        return axisService;
    }

    /**
     * removes the operation from the Axis service.
     *
     *
     * @param inputEventAdaptorMessageConfiguration
     * @param inputEventAdaptorConfiguration
     * @param axisConfiguration
     * @param subscriptionId
     * @throws AxisFault
     */
    public static void removeEmailServiceOperation(InputEventAdaptorMessageConfiguration inputEventAdaptorMessageConfiguration,
                                                   InputEventAdaptorConfiguration inputEventAdaptorConfiguration,
                                                   AxisConfiguration axisConfiguration, String subscriptionId) throws AxisFault {


        String axisServiceName = inputEventAdaptorConfiguration.getName() + "Service";
        AxisService axisService = axisConfiguration.getService(axisServiceName);

        if (axisService == null) {
            throw new AxisFault("There is no service with the name ==> " + axisServiceName);
        }
        String operationName = inputEventAdaptorMessageConfiguration.getInputMessageProperties().get(EmailEventAdaptorConstants.ADAPTOR_MESSAGE_RECEIVING_EMAIL_SUBJECT);
        AxisOperation axisOperation = axisService.getOperation(new QName("", operationName));
        if (axisOperation == null) {
            throw new AxisFault("There is no operation with the name ==> " + operationName);
        }
        SubscriptionEmailMessageReceiver messageReceiver =
                (SubscriptionEmailMessageReceiver) axisOperation.getMessageReceiver();
        if (messageReceiver == null) {
            throw new AxisFault("There is no message receiver for operation with name ==> " + operationName);
        }
        if (messageReceiver.removeEventAdaptorListener(subscriptionId)) {
            axisService.removeOperation(new QName("", operationName));
        }

        AxisEvent serviceUpdateEvent = new AxisEvent(AxisEvent.SERVICE_DEPLOY,axisService);
        axisConfiguration.notifyObservers(serviceUpdateEvent ,axisService);

    }

}
