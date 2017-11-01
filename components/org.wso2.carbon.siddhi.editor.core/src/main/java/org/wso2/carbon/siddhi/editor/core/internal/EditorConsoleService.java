/*
 * Copyright (c) 2017 WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.siddhi.editor.core.internal;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.helpers.LogLog;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.editor.log.appender.internal.CircularBuffer;
import org.wso2.carbon.editor.log.appender.internal.ConsoleLogEvent;
import org.wso2.carbon.editor.log.appender.DataHodlder;
import org.wso2.msf4j.websocket.WebSocketEndpoint;

import java.io.IOException;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 * EditorConsoleService Websocket - Client connect to this
 */
@Component(
        name = "editor-console-service",
        service = WebSocketEndpoint.class,
        immediate = true
)
@ServerEndpoint(value = "/console")
public class EditorConsoleService implements WebSocketEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(EditorConsoleService.class);
    private static Queue<Session> queue = new ConcurrentLinkedQueue<Session>();
    private CircularBuffer<ConsoleLogEvent> circularBuffer = DataHodlder.getBuffer();
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
    private int schedulerInitialDelay = 1000;
    private int schedulerTerminationDelay = 10;
    private int publishedIndex = -1;


    @OnOpen
    public void onOpen(Session session) {
        LOGGER.info("Connected with user : " + session.getId());
        queue.add(session);
        scheduler.scheduleWithFixedDelay(new LogPublisherTask(), schedulerInitialDelay, schedulerTerminationDelay,
                TimeUnit.MILLISECONDS);
    }

    @OnMessage
    public void onMessage(String text, Session session) {
        try {
            session.getBasicRemote().sendText("Welcome to Stream Processor Studio");
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }

    @OnClose
    public void onClose(Session session) {
        if (session.isOpen()) {
            try {
                session.close();
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
            }
        }
    }

    private final class LogPublisherTask implements Runnable {
        public void run() {
            try {
                while (circularBuffer.getHead() != publishedIndex) {
                    /*if (circularBuffer.getHead() != publishedIndex) {
                        broadcastConsoleOutput(circularBuffer.get(circularBuffer.getHead() - publishedIndex));
                    }*/
                    broadcastConsoleOutput(circularBuffer.get(circularBuffer.getHead() - publishedIndex));
                }
            } catch (Exception e) {
                // Preserve interrupt status
                Thread.currentThread().interrupt();
                LogLog.error("LogEventAppender Cannot publish log events, " + e.getMessage(), e);
            }
        }
    }

    private void broadcastConsoleOutput(List<ConsoleLogEvent> event) {
        for (ConsoleLogEvent logEvent : event) {
            for (Session session : queue) {
                if (session.isOpen()) {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        String json = mapper.writeValueAsString(logEvent);
                        session.getBasicRemote().sendText(json);
                        publishedIndex++;
                    } catch (IOException e) {
                        LogLog.error("Editor Console Appender cannot publish log event, " + e.getMessage(), e);
                    }
                }
            }
        }
    }

    /**
     * This is the activation method of ServiceComponent. This will be called when it's references are fulfilled
     *
     * @throws Exception this will be thrown if an issue occurs while executing the activate method
     */
    @Activate
    protected void start() throws Exception {

    }

    /**
     * This is the deactivation method of ServiceComponent. This will be called when this component
     * is being stopped or references are satisfied during runtime.
     *
     * @throws Exception this will be thrown if an issue occurs while executing the de-activate method
     */
    @Deactivate
    protected void stop() throws Exception {

    }
}

