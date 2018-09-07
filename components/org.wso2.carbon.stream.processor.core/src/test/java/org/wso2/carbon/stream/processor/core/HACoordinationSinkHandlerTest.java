/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.wso2.carbon.stream.processor.core;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.stream.processor.core.ha.HACoordinationSinkHandler;
import org.wso2.carbon.stream.processor.core.ha.HACoordinationSinkHandlerManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.output.sink.SinkHandlerCallback;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.mock;

public class HACoordinationSinkHandlerTest extends PowerMockTestCase {

    private static final String SINK_1 = "sink-1";
    private SinkHandlerCallback sinkHandlerCallback;

    @BeforeTest
    public void setDebugLogLevel() {
        Logger.getLogger(HACoordinationSinkHandler.class.getName()).setLevel(Level.DEBUG);
    }
    @Test
    public void testPassiveNodeEventQueue() {

        HACoordinationSinkHandler haCoordinationSinkHandler = (HACoordinationSinkHandler)
                new HACoordinationSinkHandlerManager(10).generateSinkHandler();

        sinkHandlerCallback = mock(SinkHandlerCallback.class);
        doNothing().when(sinkHandlerCallback).mapAndSend(Mockito.any(Event.class));
        haCoordinationSinkHandler.init(SINK_1, new StreamDefinition(), sinkHandlerCallback);

        Whitebox.setInternalState(haCoordinationSinkHandler, "sinkHandlerCallback", sinkHandlerCallback);

        Event event = new Event();
        Event eventTwo = new Event();
        Event eventThree = new Event();
        Event eventFour = new Event();
        event.setTimestamp(1L);
        eventTwo.setTimestamp(2L);
        eventThree.setTimestamp(3L);
        eventFour.setTimestamp(4L);

        haCoordinationSinkHandler.handle(event);
        haCoordinationSinkHandler.handle(eventTwo);
        haCoordinationSinkHandler.handle(eventThree);
        haCoordinationSinkHandler.handle(eventFour);
        Assert.assertEquals(haCoordinationSinkHandler.getPassiveNodeProcessedEvents().size(), 4);
        Assert.assertEquals(haCoordinationSinkHandler.getPassiveNodeProcessedEvents().peek().getTimestamp(), 1L);

        //Active Node Published Event at TS=2
        haCoordinationSinkHandler.trimPassiveNodeEventQueue(2L);
        Assert.assertEquals(haCoordinationSinkHandler.getPassiveNodeProcessedEvents().size(), 2);
        Assert.assertEquals(haCoordinationSinkHandler.getPassiveNodeProcessedEvents().peek().getTimestamp(), 3L);

        haCoordinationSinkHandler.setAsActive();
        Assert.assertEquals(haCoordinationSinkHandler.getPassiveNodeProcessedEvents().size(), 0);

    }

    @Test
    public void testActiveNodeTSUpdate() {

        HACoordinationSinkHandler haCoordinationSinkHandler = (HACoordinationSinkHandler)
                new HACoordinationSinkHandlerManager(10).generateSinkHandler();

        sinkHandlerCallback = mock(SinkHandlerCallback.class);
        doNothing().when(sinkHandlerCallback).mapAndSend(Mockito.any(Event.class));

        haCoordinationSinkHandler.init(SINK_1, new StreamDefinition(), sinkHandlerCallback);

        Whitebox.setInternalState(haCoordinationSinkHandler, "sinkHandlerCallback", sinkHandlerCallback);

        Event event = new Event();
        Event eventTwo = new Event();
        Event eventThree = new Event();
        Event eventFour = new Event();
        event.setTimestamp(1L);
        eventTwo.setTimestamp(2L);
        eventThree.setTimestamp(3L);
        eventFour.setTimestamp(4L);

        haCoordinationSinkHandler.handle(event);
        haCoordinationSinkHandler.handle(eventTwo);

        //Passive Node Should Not Save the Last Published Timestamp
        Assert.assertEquals(haCoordinationSinkHandler.getActiveNodeLastPublishedTimestamp(), 0L);

        haCoordinationSinkHandler.setAsActive();

        //Active Node Should Now Save Last Published Timestamp
        haCoordinationSinkHandler.handle(eventThree);
        haCoordinationSinkHandler.handle(eventFour);
        Assert.assertEquals(haCoordinationSinkHandler.getActiveNodeLastPublishedTimestamp(), 4L);
    }

    @Test
    public void testQueueLimit() {
        HACoordinationSinkHandler haCoordinationSinkHandler = (HACoordinationSinkHandler)
                new HACoordinationSinkHandlerManager(5).generateSinkHandler();

        sinkHandlerCallback = mock(SinkHandlerCallback.class);
        doNothing().when(sinkHandlerCallback).mapAndSend(Mockito.any(Event.class));

        haCoordinationSinkHandler.init(SINK_1, new StreamDefinition(), sinkHandlerCallback);

        Whitebox.setInternalState(haCoordinationSinkHandler, "sinkHandlerCallback", sinkHandlerCallback);

        Event event = new Event();
        Event eventTwo = new Event();
        Event eventThree = new Event();
        Event eventFour = new Event();
        Event eventFive = new Event();
        Event eventSix = new Event();
        event.setTimestamp(1L);
        eventTwo.setTimestamp(2L);
        eventThree.setTimestamp(3L);
        eventFour.setTimestamp(4L);
        eventFive.setTimestamp(5L);
        eventSix.setTimestamp(6L);

        haCoordinationSinkHandler.handle(event);
        haCoordinationSinkHandler.handle(eventTwo);
        haCoordinationSinkHandler.handle(eventThree);
        haCoordinationSinkHandler.handle(eventFour);
        haCoordinationSinkHandler.handle(eventFive);
        haCoordinationSinkHandler.handle(eventSix);

        Assert.assertEquals(haCoordinationSinkHandler.getPassiveNodeProcessedEvents().peek().getTimestamp(), 2L);

    }

    @Test
    public void testPassiveNodeArrayOfEventsQueue() {

        HACoordinationSinkHandler haCoordinationSinkHandler = (HACoordinationSinkHandler)
                new HACoordinationSinkHandlerManager(10).generateSinkHandler();

        sinkHandlerCallback = mock(SinkHandlerCallback.class);
        doNothing().when(sinkHandlerCallback).mapAndSend(Mockito.any(Event.class));
        haCoordinationSinkHandler.init(SINK_1, new StreamDefinition(), sinkHandlerCallback);

        Whitebox.setInternalState(haCoordinationSinkHandler, "sinkHandlerCallback", sinkHandlerCallback);

        Event event = new Event();
        Event eventTwo = new Event();
        Event eventThree = new Event();
        Event eventFour = new Event();
        event.setTimestamp(1L);
        eventTwo.setTimestamp(2L);
        eventThree.setTimestamp(3L);
        eventFour.setTimestamp(4L);

        Event[] events = {event, eventTwo, eventThree, eventFour};

        haCoordinationSinkHandler.handle(events);
        Assert.assertEquals(haCoordinationSinkHandler.getPassiveNodeProcessedEvents().size(), 4);
        Assert.assertEquals(haCoordinationSinkHandler.getPassiveNodeProcessedEvents().peek().getTimestamp(), 1L);

        //Active Node Published Event at TS=2
        haCoordinationSinkHandler.trimPassiveNodeEventQueue(2L);
        Assert.assertEquals(haCoordinationSinkHandler.getPassiveNodeProcessedEvents().size(), 2);
        Assert.assertEquals(haCoordinationSinkHandler.getPassiveNodeProcessedEvents().peek().getTimestamp(), 3L);

        haCoordinationSinkHandler.setAsActive();
        Assert.assertEquals(haCoordinationSinkHandler.getPassiveNodeProcessedEvents().size(), 0);

    }

    @Test
    public void testActiveNodeArrayOfEventsTSUpdate() {

        HACoordinationSinkHandler haCoordinationSinkHandler = (HACoordinationSinkHandler)
                new HACoordinationSinkHandlerManager(10).generateSinkHandler();

        sinkHandlerCallback = mock(SinkHandlerCallback.class);
        doNothing().when(sinkHandlerCallback).mapAndSend(Mockito.any(Event.class));

        haCoordinationSinkHandler.init(SINK_1, new StreamDefinition(), sinkHandlerCallback);

        Whitebox.setInternalState(haCoordinationSinkHandler, "sinkHandlerCallback", sinkHandlerCallback);

        Event event = new Event();
        Event eventTwo = new Event();
        Event eventThree = new Event();
        Event eventFour = new Event();
        event.setTimestamp(1L);
        eventTwo.setTimestamp(2L);
        eventThree.setTimestamp(3L);
        eventFour.setTimestamp(4L);
        Event[] events = {event, eventTwo, eventThree, eventFour};

        haCoordinationSinkHandler.setAsActive();

        haCoordinationSinkHandler.handle(events);
        Assert.assertEquals(haCoordinationSinkHandler.getActiveNodeLastPublishedTimestamp(), 4L);
    }
}
