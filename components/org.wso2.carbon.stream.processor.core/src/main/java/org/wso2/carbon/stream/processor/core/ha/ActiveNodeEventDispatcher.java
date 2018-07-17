package org.wso2.carbon.stream.processor.core.ha;

import org.apache.log4j.Logger;
import org.wso2.carbon.stream.processor.core.event.queue.EventQueue;
import org.wso2.carbon.stream.processor.core.event.queue.QueuedEvent;
import org.wso2.carbon.stream.processor.core.ha.transport.TCPNettyClient;
import org.wso2.carbon.stream.processor.core.util.BinaryEventConverter;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Queue;

public class ActiveNodeEventDispatcher implements Runnable {
    private static final Logger log = Logger.getLogger(ActiveNodeEventDispatcher.class);
    private TCPNettyClient tcpNettyClient = new TCPNettyClient();
    private String host;
    private int port;
    private EventQueue<QueuedEvent> eventQueue;

    public ActiveNodeEventDispatcher(EventQueue<QueuedEvent> eventQueue, String host, int port) {
        this.eventQueue = eventQueue;
        this.host = host;
        this.port = port;
    }

    @Override
    public void run() {
        try {
            if (!tcpNettyClient.isActive()) {
                tcpNettyClient.connect(host, port);
            }
        } catch (ConnectionUnavailableException e) {
            log.error("Error in connecting to " + host + ":" + port + ". Will retry in the next iteration");
            return;
        }

        QueuedEvent queuedEvent = eventQueue.dequeue();
        while (queuedEvent != null) {
            try {
                ByteBuffer byteBuffer = BinaryEventConverter.convertToBinaryMessage(queuedEvent);
                tcpNettyClient.send("aa", byteBuffer.array());
            } catch (ConnectionUnavailableException e) {
                log.error("Error in sending events to " + host + ":" + port + ".Will retry in the next iteration");
            } catch (IOException e) {
                log.error("Error in converting events to binary message.Will retry in the next iteration");
            }
            log.info("Sent - " + queuedEvent.getstreamId() + "  |   " + queuedEvent.getEvent().toString());
            queuedEvent = eventQueue.dequeue();
        }
    }
}
