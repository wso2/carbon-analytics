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
package org.wso2.carbon.event.output.adapter.jms.internal.util;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.transport.base.BaseConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.output.adapter.core.exception.ConnectionUnavailableException;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterRuntimeException;
import org.wso2.carbon.event.output.adapter.jms.JMSEventAdapter;

import javax.jms.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Performs the actual sending of a JMS message, and the subsequent committing of a JTA transaction
 * (if requested) or the local session transaction, if used. An instance of this class is unique
 * to a single message send out operation and will not be shared.
 */
public class JMSMessageSender {

    private static final Log log = LogFactory.getLog(JMSMessageSender.class);

    /**
     * Should this sender use JMS 1.1 ? (if false, defaults to 1.0.2b)
     */
    private boolean jmsSpec11 = true;

    /**
     * Are we sending to a Queue ?
     */
    private Boolean isQueue = null;

    /**
     * Factory used by the connection pool.
     */
    private JMSConnectionFactory jmsConnectionFactory;

    /**
     * Create a JMSSender using a JMSConnectionFactory and target EPR
     *
     * @param jmsConnectionFactory the JMSConnectionFactory
     */
    public JMSMessageSender(JMSConnectionFactory jmsConnectionFactory) {
        this.jmsSpec11 = jmsConnectionFactory.isJmsSpec11();
        this.jmsConnectionFactory = jmsConnectionFactory;
    }

    /**
     * Perform actual send of JMS message to the Destination selected
     */
    public void send(Object message, JMSEventAdapter.PublisherDetails publisherDetails, String jmsHeaders) {

        Map<String, String> messageProperties = publisherDetails.getMessageConfig();

        Boolean jtaCommit = getBooleanProperty(messageProperties, BaseConstants.JTA_COMMIT_AFTER_SEND);
        Boolean rollbackOnly = getBooleanProperty(messageProperties, BaseConstants.SET_ROLLBACK_ONLY);
        Boolean persistent = getBooleanProperty(messageProperties, JMSConstants.JMS_DELIVERY_MODE);
        Integer priority = getIntegerProperty(messageProperties, JMSConstants.JMS_PRIORITY);
        Integer timeToLive = getIntegerProperty(messageProperties, JMSConstants.JMS_TIME_TO_LIVE);

        MessageProducer producer = null;
        Destination destination = null;
        Session session = null;
        boolean sendingSuccessful = false;
        JMSConnectionFactory.JMSPooledConnectionHolder pooledConnection = null;
        try {

            pooledConnection = jmsConnectionFactory.getConnectionFromPool();

            producer = pooledConnection.getProducer();
            session = pooledConnection.getSession();
            Message jmsMessage = convertToJMSMessage(message, publisherDetails.getMessageConfig(), session);
            setJMSTransportHeaders(jmsMessage, jmsHeaders);

            // Do not commit, if message is marked for rollback
            if (rollbackOnly != null && rollbackOnly) {
                jtaCommit = Boolean.FALSE;
            }

            if (persistent != null) {
                try {
                    producer.setDeliveryMode(DeliveryMode.PERSISTENT);
                } catch (JMSException e) {
                    handleConnectionException("Error setting JMS Producer for PERSISTENT delivery", e);
                }
            }
            if (priority != null) {
                try {
                    producer.setPriority(priority);
                } catch (JMSException e) {
                    handleConnectionException("Error setting JMS Producer priority to : " + priority, e);
                }
            }
            if (timeToLive != null) {
                try {
                    producer.setTimeToLive(timeToLive);
                } catch (JMSException e) {
                    handleConnectionException("Error setting JMS Producer TTL to : " + timeToLive, e);
                }
            }

            // perform actual message sending
//        try {
            if (jmsSpec11 || isQueue == null) {
                producer.send(jmsMessage);

            } else {
                if (isQueue) {
                    ((QueueSender) producer).send(jmsMessage);

                } else {
                    ((TopicPublisher) producer).publish(jmsMessage);
                }
            }
            sendingSuccessful = true;

            if (log.isDebugEnabled()) {

//            // set the actual MessageID to the message context for use by any others down the line
                String msgId = null;
                try {
                    msgId = jmsMessage.getJMSMessageID();
                } catch (JMSException jmse) {
                    log.error(jmse.getMessage(), jmse);
                }

                log.debug(
                        " with JMS Message ID : " + msgId +
                                " to destination : " + producer.getDestination());
            }

        } catch (JMSException e) {
            handleConnectionException("Error sending message to destination : " + destination, e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            if (jtaCommit != null) {

                try {
                    if (session.getTransacted()) {
                        if (sendingSuccessful && (rollbackOnly == null || !rollbackOnly)) {
                            session.commit();
                        } else {
                            session.rollback();
                        }
                    }

                    if (log.isDebugEnabled()) {
                        log.debug((sendingSuccessful ? "Committed" : "Rolled back") +
                                " local (JMS Session) Transaction");
                    }

                } catch (Exception e) {
                    handleConnectionException("Error committing/rolling back local (i.e. session) " +
                            "transaction after sending of message "//with MessageContext ID : " +
                            + " to destination : " + destination, e);
                }
            }
            if (pooledConnection != null) {
                jmsConnectionFactory.returnPooledConnection(pooledConnection);
            }
        }
    }

    private Message setJMSTransportHeaders(Message message, String headerProperty) {

        Map<String, String> messageConfiguration = new HashMap<String, String>();

        if (headerProperty != null && message != null) {
            String[] headers = headerProperty.split(JMSEventAdapterConstants.HEADER_SEPARATOR);

            if (headers.length > 0) {
                for (String header : headers) {
                    try {
                        String[] headerPropertyWithValue = header.split(JMSEventAdapterConstants.ENTRY_SEPARATOR, 2);
                        messageConfiguration.put(headerPropertyWithValue[0], headerPropertyWithValue[1]);
                    } catch (Exception e) {
                        log.warn("Header property \" " + header + " \" is not defined in the correct format", e);
                    }
                }
            }

            try {
                return JMSUtils.setTransportHeaders(messageConfiguration, message);
            } catch (JMSException e) {
                throw new OutputEventAdapterRuntimeException(e);
            }
        }

        return message;
    }

    public Message convertToJMSMessage(Object messageObj, Map<String, String> messageProperties, Session session) {
        Message jmsMessage = null;
        try {
            if (messageObj instanceof OMElement) {
                jmsMessage = session.createTextMessage(messageObj.toString());
            } else if (messageObj instanceof String) {
                jmsMessage = session.createTextMessage((String) messageObj);
            } else if (messageObj instanceof Map) {
                MapMessage mapMessage = session.createMapMessage();
                Map sourceMessage = (Map) messageObj;
                for (Object key : sourceMessage.keySet()) {
                    mapMessage.setObject((String) key, sourceMessage.get(key));
                }
                jmsMessage = mapMessage;
            }
        } catch (JMSException e) {
            handleException("Failed to publish to topic:" + messageProperties.get(JMSConstants.PARAM_DESTINATION), e);
        }

        return jmsMessage;
    }

    /**
     * Close non-shared producer, session and connection if any
     */

    public void close() {
        jmsConnectionFactory.close();
    }

    private void handleException(String message, Exception e) {
        log.error(message, e);
        throw new OutputEventAdapterRuntimeException(message, e);
    }

    private void handleConnectionException(String message, Exception e) {
        log.error(message, e);
        throw new ConnectionUnavailableException(message, e);
    }

    private Boolean getBooleanProperty(Map<String, String> messageProperties, String name) {
        String o = messageProperties.get(name);
        if (o != null) {
            return Boolean.valueOf(o);
        }
        return null;
    }

    private Integer getIntegerProperty(Map<String, String> messageProperties, String name) {
        String o = messageProperties.get(name);
        if (o != null) {
            return Integer.parseInt(o);
        }
        return null;
    }

}
