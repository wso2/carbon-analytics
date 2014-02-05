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

package org.wso2.carbon.event.output.adaptor.email;


import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.base.BaseConstants;
import org.apache.axis2.transport.mail.MailConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.agent.thrift.internal.utils.AgentConstants;
import org.wso2.carbon.event.output.adaptor.core.AbstractOutputEventAdaptor;
import org.wso2.carbon.event.output.adaptor.core.MessageType;
import org.wso2.carbon.event.output.adaptor.core.Property;
import org.wso2.carbon.event.output.adaptor.core.config.OutputEventAdaptorConfiguration;
import org.wso2.carbon.event.output.adaptor.core.message.config.OutputEventAdaptorMessageConfiguration;
import org.wso2.carbon.event.output.adaptor.email.internal.ds.EmailEventAdaptorServiceValueHolder;
import org.wso2.carbon.event.output.adaptor.email.internal.util.EmailEventAdaptorConstants;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class EmailEventAdaptorType extends AbstractOutputEventAdaptor {

    private static final Log log = LogFactory.getLog(EmailEventAdaptorType.class);

    private static EmailEventAdaptorType emailEventAdaptor = new EmailEventAdaptorType();
    private ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(EmailEventAdaptorConstants.MIN_THREAD, EmailEventAdaptorConstants.MAX_THREAD, AgentConstants.DEFAULT_KEEP_ALIVE_TIME, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(1000));
    private ConcurrentHashMap<OutputEventAdaptorMessageConfiguration, EmailSenderConfiguration> emailSenderConfigurationMap = new ConcurrentHashMap<OutputEventAdaptorMessageConfiguration, EmailSenderConfiguration>();
    private ResourceBundle resourceBundle;

    private EmailEventAdaptorType() {

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
    public static EmailEventAdaptorType getInstance() {
        return emailEventAdaptor;
    }

    /**
     * @return name of the Email event adaptor
     */
    @Override
    protected String getName() {
        return EmailEventAdaptorConstants.ADAPTOR_TYPE_EMAIL;
    }

    /**
     * Initialises the resource bundle
     */
    @Override
    protected void init() {
        resourceBundle = ResourceBundle.getBundle("org.wso2.carbon.event.output.adaptor.email.i18n.Resources", Locale.getDefault());
    }


    /**
     * @return output adaptor configuration property list
     */
    @Override
    public List<Property> getOutputAdaptorProperties() {

        List<Property> propertyList = new ArrayList<Property>();

        // set default outgoing mail subject
        Property subjectProperty = new Property(EmailEventAdaptorConstants.ADAPTOR_CONF_EMAIL_DEFAULT_SUBJECT);
        subjectProperty.setDisplayName(
                resourceBundle.getString(EmailEventAdaptorConstants.ADAPTOR_CONF_EMAIL_DEFAULT_SUBJECT));
        subjectProperty.setRequired(true);
        subjectProperty.setHint(resourceBundle.getString(EmailEventAdaptorConstants.ADAPTOR_CONF_EMAIL_HINT_DEFAULT_SUBJECT));

        propertyList.add(subjectProperty);

        return propertyList;

    }

    /**
     * @return output message configuration property list
     */
    @Override
    public List<Property> getOutputMessageProperties() {
        List<Property> propertyList = new ArrayList<Property>();

        // set email address
        Property emailAddress = new Property(EmailEventAdaptorConstants.ADAPTOR_MESSAGE_EMAIL_ADDRESS);
        emailAddress.setDisplayName(
                resourceBundle.getString(EmailEventAdaptorConstants.ADAPTOR_MESSAGE_EMAIL_ADDRESS));
        emailAddress.setRequired(true);


        // set email subject
        Property subject = new Property(EmailEventAdaptorConstants.ADAPTOR_MESSAGE_EMAIL_SUBJECT);
        subject.setDisplayName(
                resourceBundle.getString(EmailEventAdaptorConstants.ADAPTOR_MESSAGE_EMAIL_SUBJECT));
        subject.setRequired(true);

        propertyList.add(emailAddress);
        propertyList.add(subject);

        return propertyList;
    }

    /**
     * @param outputEventMessageConfiguration
     *                 - outputEventMessageConfiguration to publish messages
     * @param message
     * @param outputEventAdaptorConfiguration
     *
     * @param tenantId
     */
    public void publish(
            OutputEventAdaptorMessageConfiguration outputEventMessageConfiguration,
            Object message,
            OutputEventAdaptorConfiguration outputEventAdaptorConfiguration, int tenantId) {

        EmailSenderConfiguration emailSenderConfiguration = emailSenderConfigurationMap.get(outputEventMessageConfiguration);
        if (emailSenderConfiguration == null) {
            emailSenderConfiguration = new EmailSenderConfiguration(outputEventMessageConfiguration, outputEventAdaptorConfiguration.getOutputProperties().get(EmailEventAdaptorConstants.ADAPTOR_CONF_EMAIL_DEFAULT_SUBJECT));
            emailSenderConfigurationMap.putIfAbsent(outputEventMessageConfiguration, emailSenderConfiguration);
        }

        String[] emailIds = emailSenderConfiguration.getEmailIds();
        if (emailIds != null) {
            for (String email : emailIds) {
                threadPoolExecutor.submit(new EmailSender(email, emailSenderConfiguration.getSubject(), message.toString()));
            }
        }
    }


    @Override
    public void testConnection(
            OutputEventAdaptorConfiguration outputEventtAdaptorConfiguration, int tenantId) {
        // no test
    }


    class EmailSender implements Runnable {
        String to;
        String subject;
        String body;

        EmailSender(String to, String subject, String body) {
            this.to = to;
            this.subject = subject;
            this.body = body;
        }

        @Override
        public void run() {
            Map<String, String> headerMap = new HashMap<String, String>();
            headerMap.put(MailConstants.MAIL_HEADER_SUBJECT, subject);
            OMElement payload = OMAbstractFactory.getOMFactory().createOMElement(
                    BaseConstants.DEFAULT_TEXT_WRAPPER, null);
            payload.setText(body);

            try {
                ServiceClient serviceClient;
                ConfigurationContext configContext = EmailEventAdaptorServiceValueHolder.getConfigurationContextService().getClientConfigContext();
                if (configContext != null) {
                    serviceClient = new ServiceClient(configContext, null);
                } else {
                    serviceClient = new ServiceClient();
                }
                Options options = new Options();
                options.setProperty(Constants.Configuration.ENABLE_REST, Constants.VALUE_TRUE);
                options.setProperty(MessageContext.TRANSPORT_HEADERS, headerMap);
                options.setProperty(MailConstants.TRANSPORT_MAIL_FORMAT,
                        MailConstants.TRANSPORT_FORMAT_TEXT);
                options.setTo(new EndpointReference("mailto:" + to));


                serviceClient.setOptions(options);
                serviceClient.fireAndForget(payload);
                log.debug("Sending confirmation mail to " + to);
            } catch (AxisFault e) {
                String msg = "Error in delivering the message, " +
                        "subject: " + subject + ", to: " + to + ".";
                log.error(msg);
            } catch (Throwable t){
                String msg = "Error in delivering the message, " +
                             "subject: " + subject + ", to: " + to + ".";
                log.error(msg);
                log.error(t);
            }
        }
    }

    private final class EmailSenderConfiguration {


        private String subject;
        private String[] emailIds;

        private EmailSenderConfiguration(
                OutputEventAdaptorMessageConfiguration outputEventAdaptorMessageConfiguration,
                String defaultSubject) {
            if (defaultSubject != null) {
                subject = defaultSubject;
            } else {
                subject = "";
            }
            String emailIdString = null;
            if (outputEventAdaptorMessageConfiguration.getOutputMessageProperties().size() == 2) {
                subject = outputEventAdaptorMessageConfiguration.getOutputMessageProperties().get(EmailEventAdaptorConstants.ADAPTOR_MESSAGE_EMAIL_SUBJECT);
                emailIdString = outputEventAdaptorMessageConfiguration.getOutputMessageProperties().get(EmailEventAdaptorConstants.ADAPTOR_MESSAGE_EMAIL_ADDRESS);
            } else if (outputEventAdaptorMessageConfiguration.getOutputMessageProperties().size() == 1) {
                emailIdString = outputEventAdaptorMessageConfiguration.getOutputMessageProperties().get(EmailEventAdaptorConstants.ADAPTOR_MESSAGE_EMAIL_ADDRESS);
                log.info("Subject is empty");
            } else {
                log.error("Doesn't contains E-mail ids hence no message will be sent");
            }
            emailIds = null;
            if (emailIdString != null) {
                emailIds = emailIdString.replaceAll(" ", "").split(",");
            }
        }

        public String getSubject() {
            return subject;
        }

        public String[] getEmailIds() {
            return emailIds;
        }
    }

}
