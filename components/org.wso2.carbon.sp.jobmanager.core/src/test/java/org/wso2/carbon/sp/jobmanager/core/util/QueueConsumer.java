/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.sp.jobmanager.core.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.Session;
import javax.jms.TextMessage;

public class QueueConsumer implements Runnable {

    private final ResultContainer resultContainer;
    private QueueConnectionFactory queueConnectionFactory;
    private String queueName;
    private boolean active = true;
    private static Log log = LogFactory.getLog(QueueConsumer.class);

    public QueueConsumer(QueueConnectionFactory queueConnectionFactory, String queueName,
                         ResultContainer resultContainer) {
        this.queueConnectionFactory = queueConnectionFactory;
        this.queueName = queueName;
        this.resultContainer = resultContainer;
    }

    public void run() {
        // create queue connection
        QueueConnection queueConnection = null;
        try {
            queueConnection = queueConnectionFactory.createQueueConnection();
            queueConnection.start();
        } catch (JMSException e) {
            log.error("Can not create queue connection." + e.getMessage(), e);
            return;
        }
        Session session;
        try {
            session = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createQueue(queueName);
            MessageConsumer consumer = session.createConsumer(destination);
            log.info("Listening for messages at " + queueName);
            while (active) {
                Message message = consumer.receive(1000);
                if (message != null) {
                    if (message instanceof MapMessage) {
                        MapMessage mapMessage = (MapMessage) message;
                        Map<String, Object> map = new HashMap<String, Object>();
                        Enumeration enumeration = mapMessage.getMapNames();
                        while (enumeration.hasMoreElements()) {
                            String key = (String) enumeration.nextElement();
                            map.put(key, mapMessage.getObject(key));
                        }
                        resultContainer.eventReceived(map.toString());
                        log.info("Received Map Message: " + map);
                    } else if (message instanceof TextMessage) {
                        resultContainer.eventReceived(message.toString());
                        log.info("Received Text Message: " + ((TextMessage) message).getText());
                    } else if (message instanceof BytesMessage) {
                        byte[] byteData = null;
                        byteData = new byte[(int) ((BytesMessage) message).getBodyLength()];
                        ((BytesMessage) message).readBytes(byteData);
                        ((BytesMessage) message).reset();
                        resultContainer.eventReceived(new String(byteData));
                    } else {
                        resultContainer.eventReceived(message.toString());
                        log.info("Received message: " + message.toString());
                    }
                }
            }
            log.info("Finished listening for messages.");
            session.close();
            queueConnection.stop();
            queueConnection.close();
        } catch (JMSException e) {
            log.error("Can not subscribe." + e.getMessage(), e);
        }
    }

    public void shutdown() {
        active = false;
    }

}
