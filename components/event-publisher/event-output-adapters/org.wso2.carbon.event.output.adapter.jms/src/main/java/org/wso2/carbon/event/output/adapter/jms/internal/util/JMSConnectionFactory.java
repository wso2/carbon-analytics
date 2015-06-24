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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterRuntimeException;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Hashtable;

/**
 * Encapsulate a JMS Connection factory definition within an Axis2.xml
 * <p/>
 * JMS Connection Factory definitions, allows JNDI properties as well as other service
 * level parameters to be defined, and re-used by each service that binds to it
 * <p/>
 * When used for sending messages out, the JMSConnectionFactory'ies are able to cache
 * a Connection, Session or Producer
 */
public class JMSConnectionFactory {

    private static final Log log = LogFactory.getLog(JMSConnectionFactory.class);

    /**
     * The list of parameters from the axis2.xml definition
     */
    private Hashtable<String, String> parameters = new Hashtable<String, String>();
    private String name;

    /**
     * The cached InitialContext reference
     */
    private Context context = null;
    /**
     * The JMS ConnectionFactory this definition refers to
     */
    private ConnectionFactory conFactory = null;

    /**
     * The Shared Destination
     */
    private Destination sharedDestination = null;

    private int maxConnections;

    private GenericObjectPool connectionPool;

    private String destinationName;

    /**
     * Digest a JMS CF definition from an axis2.xml 'Parameter' and construct
     */
    public JMSConnectionFactory(Hashtable<String, String> parameters, String name, String destination, int maxConcurrentConnections) {
        this.parameters = parameters;
        this.name = name;
        this.destinationName = destination;

        if (maxConcurrentConnections > 1) {
            this.maxConnections = maxConcurrentConnections;
        }

        try {
            context = new InitialContext(parameters);
            conFactory = JMSUtils.lookup(context, ConnectionFactory.class,
                    parameters.get(JMSConstants.PARAM_CONFAC_JNDI_NAME));
            log.info("JMS ConnectionFactory : " + name + " initialized");

        } catch (NamingException e) {
            throw new OutputEventAdapterRuntimeException("Cannot acquire JNDI context, JMS Connection factory : " +
                    parameters.get(JMSConstants.PARAM_CONFAC_JNDI_NAME) +
                    " or default destinationName : " +
                    parameters.get(JMSConstants.PARAM_DESTINATION) +
                    " for JMS CF : " + name + " using : " + parameters, e);
        }

        createConnectionPool();
    }

    // need to initialize
    private void createConnectionPool() {
        GenericObjectPool.Config poolConfig = new GenericObjectPool.Config();
        poolConfig.minEvictableIdleTimeMillis = 3000;
        poolConfig.maxWait = 3000;
        poolConfig.maxActive = maxConnections;
        poolConfig.maxIdle = maxConnections;
        poolConfig.minIdle = 0;
        poolConfig.numTestsPerEvictionRun = Math.max(1, maxConnections / 10);
        poolConfig.timeBetweenEvictionRunsMillis = 5000;
        this.connectionPool = new GenericObjectPool(new PoolableJMSConnectionFactory(), poolConfig);

    }


    public void returnPooledConnection(JMSPooledConnectionHolder pooledConnection) {
        try {
            this.connectionPool.returnObject(pooledConnection);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Create a new MessageProducer
     *
     * @param session     Session to be used
     * @param destination Destination to be used
     * @return a new MessageProducer
     */
    private MessageProducer createProducer(Session session, Destination destination) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Creating a new JMS MessageProducer from JMS CF : " + name);
            }

            return JMSUtils.createProducer(
                    session, destination, isQueue(), isJmsSpec11());

        } catch (JMSException e) {
            handleException("Error creating JMS producer from JMS CF : " + name, e);
        }
        return null;
    }


    /**
     * Get cached InitialContext
     *
     * @return cache InitialContext
     */
    public Context getContext() {
        return context;
    }

    /**
     * Lookup a Destination using this JMS CF definitions and JNDI name
     *
     * @return JMS Destination for the given JNDI name or null
     */
    public synchronized Destination getDestination() {
        try {
            if (sharedDestination == null) {
                sharedDestination = JMSUtils.lookupDestination(context, destinationName, parameters.get(JMSConstants.PARAM_DEST_TYPE));
            }
            return sharedDestination;
        } catch (NamingException e) {
            handleException("Error looking up the JMS destinationName with name " + destinationName
                    + " of type " + parameters.get(JMSConstants.PARAM_DEST_TYPE), e);
        }

        // never executes but keeps the compiler happy
        return null;
    }

    /**
     * Get the reply Destination from the PARAM_REPLY_DESTINATION parameter
     *
     * @return reply destinationName defined in the JMS CF
     */
    public String getReplyToDestination() {
        return parameters.get(JMSConstants.PARAM_REPLY_DESTINATION);
    }

    /**
     * Get the reply destinationName type from the PARAM_REPLY_DEST_TYPE parameter
     *
     * @return reply destinationName defined in the JMS CF
     */
    public String getReplyDestinationType() {
        return parameters.get(JMSConstants.PARAM_REPLY_DEST_TYPE) != null ?
                parameters.get(JMSConstants.PARAM_REPLY_DEST_TYPE) :
                JMSConstants.DESTINATION_TYPE_GENERIC;
    }

    private void handleException(String msg, Exception e) {
        log.error(msg, e);
        throw new OutputEventAdapterRuntimeException(msg, e);
    }

    /**
     * Should the JMS 1.1 API be used? - defaults to yes
     *
     * @return true, if JMS 1.1 api should  be used
     */
    public boolean isJmsSpec11() {
        return parameters.get(JMSConstants.PARAM_JMS_SPEC_VER) == null ||
                "1.1".equals(parameters.get(JMSConstants.PARAM_JMS_SPEC_VER));
    }

    /**
     * Return the type of the JMS CF Destination
     *
     * @return TRUE if a Queue, FALSE for a Topic and NULL for a JMS 1.1 Generic Destination
     */
    public Boolean isQueue() {
        if (parameters.get(JMSConstants.PARAM_CONFAC_TYPE) == null &&
                parameters.get(JMSConstants.PARAM_DEST_TYPE) == null) {
            return null;
        }

        if (parameters.get(JMSConstants.PARAM_CONFAC_TYPE) != null) {
            if ("queue".equalsIgnoreCase(parameters.get(JMSConstants.PARAM_CONFAC_TYPE))) {
                return true;
            } else if ("topic".equalsIgnoreCase(parameters.get(JMSConstants.PARAM_CONFAC_TYPE))) {
                return false;
            } else {
                throw new OutputEventAdapterRuntimeException("Invalid " + JMSConstants.PARAM_CONFAC_TYPE + " : " +
                        parameters.get(JMSConstants.PARAM_CONFAC_TYPE) + " for JMS CF : " + name);
            }
        } else {
            if ("queue".equalsIgnoreCase(parameters.get(JMSConstants.PARAM_DEST_TYPE))) {
                return true;
            } else if ("topic".equalsIgnoreCase(parameters.get(JMSConstants.PARAM_DEST_TYPE))) {
                return false;
            } else {
                throw new OutputEventAdapterRuntimeException("Invalid " + JMSConstants.PARAM_DEST_TYPE + " : " +
                        parameters.get(JMSConstants.PARAM_DEST_TYPE) + " for JMS CF : " + name);
            }
        }
    }

    /**
     * Is a session transaction requested from users of this JMS CF?
     *
     * @return session transaction required by the clients of this?
     */
    private boolean isSessionTransacted() {
        return parameters.get(JMSConstants.PARAM_SESSION_TRANSACTED) != null &&
                Boolean.valueOf(parameters.get(JMSConstants.PARAM_SESSION_TRANSACTED));
    }

    private boolean isDurable() {
        if (parameters.get(JMSConstants.PARAM_SUB_DURABLE) != null) {
            return Boolean.valueOf(parameters.get(JMSConstants.PARAM_SUB_DURABLE));
        }
        return false;
    }

    private String getClientId() {
        return parameters.get(JMSConstants.PARAM_DURABLE_SUB_CLIENT_ID);
    }


    public Connection createConnection() {

        Connection connection = null;
        try {
            connection = JMSUtils.createConnection(
                    conFactory,
                    parameters.get(JMSConstants.PARAM_JMS_USERNAME),
                    parameters.get(JMSConstants.PARAM_JMS_PASSWORD),
                    isJmsSpec11(), isQueue(), isDurable(), getClientId());

            if (log.isDebugEnabled()) {
                log.debug("New JMS Connection from JMS CF : " + name + " created");
            }
        } catch (JMSException e) {
            handleException("Error acquiring a Connection from the JMS CF : " + name +
                    " using properties : " + parameters, e);
        }
        return connection;
    }

    public JMSPooledConnectionHolder getConnectionFromPool() {
        try {
            return (JMSPooledConnectionHolder) this.connectionPool.borrowObject();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }


    public synchronized void close() {

        try {
            connectionPool.close();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        if (context != null) {
            try {
                context.close();
            } catch (NamingException e) {
                log.warn("Error while closing the InitialContext of factory : " + name, e);
            }
        }
    }

    /**
     * Holder class for connections and its session/producer.
     */
    public static class JMSPooledConnectionHolder {
        private Connection connection;
        private Session session;
        private MessageProducer producer;

        public Connection getConnection() {
            return connection;
        }

        public void setConnection(Connection connection) {
            this.connection = connection;
        }

        public Session getSession() {
            return session;
        }

        public void setSession(Session session) {
            this.session = session;
        }

        public MessageProducer getProducer() {
            return producer;
        }

        public void setProducer(MessageProducer producer) {
            this.producer = producer;
        }


    }


    /**
     * JMSConnectionFactory used by the connection pool.
     */
    private class PoolableJMSConnectionFactory implements PoolableObjectFactory {

        int count = 0;

        @Override
        public Object makeObject() throws Exception {
            Connection con = createConnection();
            try {
                Session session = JMSUtils.createSession(
                        con, isSessionTransacted(), Session.AUTO_ACKNOWLEDGE, isJmsSpec11(), isQueue());

                MessageProducer producer = createProducer(session, getDestination());

                JMSPooledConnectionHolder entry = new JMSPooledConnectionHolder();
                entry.setConnection(con);
                entry.setSession(session);
                entry.setProducer(producer);
                return entry;
            } catch (JMSException e) {
                log.error(e.getMessage(), e);
                return null;
            }

        }

        @Override
        public void destroyObject(Object o) throws Exception {

            JMSPooledConnectionHolder entry = (JMSPooledConnectionHolder) o;
            entry.getProducer().close();
            entry.getSession().close();
            entry.getConnection().close();

        }

        @Override
        public boolean validateObject(Object o) {
            return false;
        }

        @Override
        public void activateObject(Object o) throws Exception {

        }

        @Override
        public void passivateObject(Object o) throws Exception {

        }

    }


}
