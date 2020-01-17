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

package io.siddhi.distribution.editor.core.internal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.siddhi.distribution.editor.log.appender.appender.DataHolder;
import io.siddhi.distribution.editor.log.appender.appender.internal.CircularBuffer;
import io.siddhi.distribution.editor.log.appender.appender.internal.ConsoleLogEvent;
import org.apache.log4j.helpers.LogLog;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.msf4j.websocket.WebSocketEndpoint;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.server.ServerEndpoint;

/**
 * EditorConsoleService Websocket - Client connect to this.
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
    private SimpleDateFormat timeFormatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss_SSS");
    private WebSocketConnection webSocketConnection;
    private ScheduledExecutorService scheduler;
    private CircularBuffer<ConsoleLogEvent> circularBuffer = DataHolder.getBuffer();

    @OnOpen
    public void onOpen(WebSocketConnection webSocketConnection) {

        if (this.webSocketConnection != null) {
            onClose(this.webSocketConnection);
        }
        this.webSocketConnection = webSocketConnection;
        this.scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleWithFixedDelay(new LogPublisherTask(), SCHEDULER_INITIAL_DELAY, SCHEDULER_TERMINATION_DELAY,
                TimeUnit.MILLISECONDS);
        LOGGER.info("Connected with user : " + webSocketConnection.getChannelId());
    }

    @OnMessage
    public void onMessage(String text, WebSocketConnection webSocketConnection) {

        if (webSocketConnection.isOpen()) {
            webSocketConnection.pushText("Welcome to Siddhi Editor");
            LOGGER.info("Received message : " + text);
        }
    }

    @OnClose
    public void onClose(WebSocketConnection webSocketConnection) {

        if (scheduler != null) {
            try {
                scheduler.shutdown();
                scheduler.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                // (Re-)Cancel if current thread also interrupted
                scheduler.shutdownNow();
                // Preserve interrupt status
                Thread.currentThread().interrupt();
                LOGGER.error("Interrupted while awaiting for Schedule Executor termination" + e.getMessage(), e);
            }
        }
        if (webSocketConnection.isOpen()) {
            try {
                ConsoleLogEvent clientCloseEvent = new ConsoleLogEvent();
                clientCloseEvent.setMessage("Connection closed (Possibly due to opening the editor in a "
                        + "new Tab/Window)! Refresh to reconnect the console.");
                clientCloseEvent.setLevel("ERROR");
                clientCloseEvent.setFqcn(EditorConsoleService.class.getCanonicalName());
                String timeString = timeFormatter.format(System.currentTimeMillis());
                clientCloseEvent.setTimeStamp(timeString);
                String jsonString = getJsonString(clientCloseEvent);
                webSocketConnection.pushText(jsonString);
                webSocketConnection.terminateConnection();
            } catch (IOException e) {
                LOGGER.error("Inturrupted while awaiting for Session termination" + e.getMessage(), e);
            }
        }
    }

    private void broadcastConsoleOutput(List<ConsoleLogEvent> event) {

        for (ConsoleLogEvent logEvent : event) {
            if (webSocketConnection.isOpen()) {
                try {
                    String jsonString = getJsonString(logEvent);
                    webSocketConnection.pushText(jsonString);
                } catch (IOException e) {
                    LogLog.error("Editor Console Appender cannot publish log event, " + e.getMessage(), e);
                }
            }
        }
    }

    private String getJsonString(ConsoleLogEvent logEvent) throws JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(logEvent);
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
}

