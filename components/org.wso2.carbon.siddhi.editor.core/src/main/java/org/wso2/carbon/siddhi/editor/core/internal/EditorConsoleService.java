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
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.editor.log.appender.DataHolder;
import org.wso2.carbon.editor.log.appender.internal.CircularBuffer;
import org.wso2.carbon.editor.log.appender.internal.ConsoleLogEvent;
import org.wso2.msf4j.websocket.WebSocketEndpoint;

import java.io.IOException;
import java.util.List;
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
    private static final int SCHEDULER_INITIAL_DELAY = 1000;
    private static final int SCHEDULER_TERMINATION_DELAY = 50;
    private Session session;
    private CircularBuffer<ConsoleLogEvent> circularBuffer = DataHolder.getBuffer();
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @OnOpen
    public void onOpen(Session session) {
        LOGGER.info("Connected with user : " + session.getId());
        this.session = session;
        scheduler.scheduleWithFixedDelay(new LogPublisherTask(), SCHEDULER_INITIAL_DELAY, SCHEDULER_TERMINATION_DELAY,
                TimeUnit.MILLISECONDS);
    }

    @OnMessage
    public void onMessage(String text, Session session) {
        try {
            session.getBasicRemote().sendText("Welcome to Stream Processor Studio");
            LOGGER.info("Received message : " + text);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }

    @OnClose
    public void onClose(Session session) {
        if (scheduler != null) {
            try {
                scheduler.shutdown();
                scheduler.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                // (Re-)Cancel if current thread also interrupted
                scheduler.shutdownNow();
                // Preserve interrupt status
                Thread.currentThread().interrupt();
                LogLog.error("Interrupted while awaiting for Schedule Executor termination" + e.getMessage(), e);
            }
        }
        if (session.isOpen()) {
            try {
                session.getBasicRemote().sendText("Console client connection is closing !. "
                        + "Refresh editor to reconnect.");
                session.close();
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
            }
        }
        scheduler.shutdown();
    }

    private final class LogPublisherTask implements Runnable {
        public void run() {
            try {
                List<ConsoleLogEvent> logEvents = circularBuffer.get(circularBuffer.getAmount());
                if (!logEvents.isEmpty()) {
                    broadcastConsoleOutput(logEvents);
                    circularBuffer.clear();
                }
            } catch (Exception e) {
                // Preserve interrupt status
                Thread.currentThread().interrupt();
                LogLog.error("LogEventAppender cannot publish log events, " + e.getMessage(), e);
            }
        }

    }

    private void broadcastConsoleOutput(List<ConsoleLogEvent> event) {
        for (ConsoleLogEvent logEvent : event) {
            if (session.isOpen()) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    String json = mapper.writeValueAsString(logEvent);
                    session.getBasicRemote().sendText(json);
                } catch (IOException e) {
                    LogLog.error("Editor Console Appender cannot publish log event, " + e.getMessage(), e);
                }
            }
        }
    }
}

