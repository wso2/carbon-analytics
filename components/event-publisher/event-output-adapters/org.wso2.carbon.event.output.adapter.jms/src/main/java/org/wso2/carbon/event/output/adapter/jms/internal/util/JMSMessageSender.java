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

import javax.jms.*;
import java.util.Map;

/**
 * Performs the actual sending of a JMS message, and the subsequent committing of a JTA transaction
 * (if requested) or the local session transaction, if used. An instance of this class is unique
 * to a single message send out operation and will not be shared.
 */
public class JMSMessageSender {

    private static final Log log = LogFactory.getLog(JMSMessageSender.class);

    /**
     * The Connection to be used to send out
     */
    private Connection connection = null;
    /**
     * The Session to be used to send out
     */
    private Session session = null;
    /**
     * The MessageProducer used
     */
    private MessageProducer producer = null;
    /**
     * Target Destination
     */
    private Destination destination = null;
    /**
     * The level of cachability for resources
     */
    private int cacheLevel = JMSConstants.CACHE_CONNECTION;
    /**
     * Should this sender use JMS 1.1 ? (if false, defaults to 1.0.2b)
     */
    private boolean jmsSpec11 = true;
    /**
     * Are we sending to a Queue ?
     */
    private Boolean isQueue = null;

    /**
     * This is a low-end method to support the one-time sends using JMS 1.0.2b
     *
     * @param connection  the JMS Connection
     * @param session     JMS Session
     * @param producer    the MessageProducer
     * @param destination the JMS Destination
     * @param cacheLevel  cacheLevel - None | Connection | Session | Producer
     * @param jmsSpec11   true if the JMS 1.1 API should be used
     * @param isQueue     posting to a Queue?
     */
//    public JMSMessageSender(Connection connection, Session session, MessageProducer producer,
//                            Destination destination, int cacheLevel, boolean jmsSpec11, Boolean isQueue) {
//
//        this.connection = connection;
//        this.session = session;
//        this.producer = producer;
//        this.destination = destination;
//        this.cacheLevel = cacheLevel;
//        this.jmsSpec11 = jmsSpec11;
//        this.isQueue = isQueue;
//    }

    /**
     * Create a JMSSender using a JMSConnectionFactory and target EPR
     *
     * @param jmsConnectionFactory the JMSConnectionFactory
     */
    public JMSMessageSender(JMSConnectionFactory jmsConnectionFactory,
                            Map<String, String> messageProperties) {

        this.cacheLevel = jmsConnectionFactory.getCacheLevel();
        this.jmsSpec11 = jmsConnectionFactory.isJmsSpec11();
        this.connection = jmsConnectionFactory.getConnection();
        this.session = jmsConnectionFactory.getSession(connection);
        this.destination =
                jmsConnectionFactory.getSharedDestination() == null ?
                        jmsConnectionFactory.getDestination(messageProperties.get(JMSConstants.PARAM_DESTINATION)) :
                        jmsConnectionFactory.getSharedDestination();
        this.producer = jmsConnectionFactory.getMessageProducer(connection, session, destination);
    }

    /**
     * Perform actual send of JMS message to the Destination selected
     *
     * @param messageProperties the Axis2 MessageContext
     */
    public void send(Message jmsMessage, Map<String, String> messageProperties) {

        Boolean jtaCommit = getBooleanProperty(messageProperties, BaseConstants.JTA_COMMIT_AFTER_SEND);
        Boolean rollbackOnly = getBooleanProperty(messageProperties, BaseConstants.SET_ROLLBACK_ONLY);
        Boolean persistent = getBooleanProperty(messageProperties, JMSConstants.JMS_DELIVERY_MODE);
        Integer priority = getIntegerProperty(messageProperties, JMSConstants.JMS_PRIORITY);
        Integer timeToLive = getIntegerProperty(messageProperties, JMSConstants.JMS_TIME_TO_LIVE);

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


        boolean sendingSuccessful = false;
        // perform actual message sending
        try {
            if (jmsSpec11 || isQueue == null) {
                producer.send(jmsMessage);

            } else {
                if (isQueue) {
                    ((QueueSender) producer).send(jmsMessage);

                } else {
                    ((TopicPublisher) producer).publish(jmsMessage);
                }
            }

//            // set the actual MessageID to the message context for use by any others down the line
            String msgId = null;
            try {
                msgId = jmsMessage.getJMSMessageID();
//                if (msgId != null) {
//                    msgCtx.setProperty(JMSConstants.JMS_MESSAGE_ID, msgId);
//                }
            } catch (JMSException ignore) {
            }

            sendingSuccessful = true;

            if (log.isDebugEnabled()) {
                log.debug(
                        " with JMS Message ID : " + msgId +
                                " to destination : " + producer.getDestination());
            }

        } catch (JMSException e) {
            handleConnectionException("Error sending message to destination : " + destination, e);

        } finally {

            if (jtaCommit != null) {

//                UserTransaction ut = (UserTransaction) msgCtx.getProperty(BaseConstants.USER_TRANSACTION);
//                if (ut != null) {
//
//                    try {
//                        if (sendingSuccessful && jtaCommit) {
//                            ut.commit();
//                        } else {
//                            ut.rollback();
//                        }
//                        msgCtx.removeProperty(BaseConstants.USER_TRANSACTION);
//
//                        if (log.isDebugEnabled()) {
//                            log.debug((sendingSuccessful ? "Committed" : "Rolled back") +
//                                " JTA Transaction");
//                        }
//
//                    } catch (Exception e) {
//                        handleException("Error committing/rolling back JTA transaction after " +
//                            "sending of message "+//with MessageContext ID : " + msgCtx.getMessageID() +
//                            " to destination : " + destination, e);
//                    }
//                }
//
//            } else {
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

                } catch (JMSException e) {
                    handleConnectionException("Error committing/rolling back local (i.e. session) " +
                            "transaction after sending of message "//with MessageContext ID : " +
                            + " to destination : " + destination, e);
                }
            }
        }
    }

    public Message convertToJMSMessage(Object messageObj, Map<String, String> messageProperties) {
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
        if (producer != null && cacheLevel < JMSConstants.CACHE_PRODUCER) {
            try {
                producer.close();
            } catch (JMSException e) {
                log.error("Error closing JMS MessageProducer after send", e);
            } finally {
                producer = null;
            }
        }

        if (session != null && cacheLevel < JMSConstants.CACHE_SESSION) {
            try {
                session.close();
            } catch (JMSException e) {
                log.error("Error closing JMS Session after send", e);
            } finally {
                session = null;
            }
        }

        if (connection != null && cacheLevel < JMSConstants.CACHE_CONNECTION) {
            try {
                connection.close();
            } catch (JMSException e) {
                log.error("Error closing JMS Connection after send", e);
            } finally {
                connection = null;
            }
        }
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
        Object o = messageProperties.get(name);
        if (o != null) {
            if (o instanceof Boolean) {
                return (Boolean) o;
            } else if (o instanceof String) {
                return Boolean.valueOf((String) o);
            }
        }
        return null;
    }

    private Integer getIntegerProperty(Map<String, String> messageProperties, String name) {
        Object o = messageProperties.get(name);
        if (o != null) {
            if (o instanceof Integer) {
                return (Integer) o;
            } else if (o instanceof String) {
                return Integer.parseInt((String) o);
            }
        }
        return null;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public void setProducer(MessageProducer producer) {
        this.producer = producer;
    }

    public void setCacheLevel(int cacheLevel) {
        this.cacheLevel = cacheLevel;
    }

    public int getCacheLevel() {
        return cacheLevel;
    }

    public Connection getConnection() {
        return connection;
    }

    public MessageProducer getProducer() {
        return producer;
    }

    public Session getSession() {
        return session;
    }
}
