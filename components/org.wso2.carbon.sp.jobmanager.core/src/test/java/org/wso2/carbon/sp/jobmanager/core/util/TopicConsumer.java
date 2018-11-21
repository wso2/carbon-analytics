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
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;

/**
 * Consumes JMS topic messages.
 */
public class TopicConsumer implements Runnable {
    private final ResultContainer resultContainer;
    private TopicConnectionFactory topicConnectionFactory;
    private String topicName;
    private boolean active = true;
    private static Log log = LogFactory.getLog(TopicConsumer.class);

    public TopicConsumer(TopicConnectionFactory topicConnectionFactory, String topicName,
                         ResultContainer resultContainer) {
        this.topicConnectionFactory = topicConnectionFactory;
        this.topicName = topicName;
        this.resultContainer = resultContainer;
    }

    public void run() {
        // create topic connection
        TopicConnection topicConnection = null;
        try {
            topicConnection = topicConnectionFactory.createTopicConnection();
            topicConnection.start();
        } catch (JMSException e) {
            log.error("Can not create topic connection." + e.getMessage(), e);
            return;
        }
        Session session = null;
        try {
            session = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createTopic(topicName);
            MessageConsumer consumer = session.createConsumer(destination);
            log.info("Listening for messages");
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
                        log.info("Received Map Message : " + map);
                    } else if (message instanceof TextMessage) {
                        resultContainer.eventReceived(message.toString());
                        log.info("Received Text Message : " + ((TextMessage) message).getText());
                    } else if (message instanceof BytesMessage) {
                        byte[] byteData = null;
                        byteData = new byte[(int) ((BytesMessage) message).getBodyLength()];
                        ((BytesMessage) message).readBytes(byteData);
                        ((BytesMessage) message).reset();
                        resultContainer.eventReceived(new String(byteData));
                    } else {
                        resultContainer.eventReceived(message.toString());
                        log.info("Received message : " + message.toString());
                    }
                }
            }
            log.info("Finished listening for messages.");
            session.close();
            topicConnection.stop();
            topicConnection.close();
        } catch (JMSException e) {
            log.error("Can not subscribe." + e.getMessage(), e);
        }
    }

    public void shutdown() {
        active = false;
    }
}

