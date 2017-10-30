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

import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.stream.processor.core.ha.HACoordinationSourceHandler;
import org.wso2.carbon.stream.processor.core.ha.HACoordinationSourceHandlerManager;
import org.wso2.carbon.stream.processor.core.ha.util.CoordinationConstants;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.input.source.InputEventHandler;
import org.wso2.siddhi.core.stream.input.source.PassThroughSourceMapper;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.Map;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.mock;

@PrepareForTest({PassThroughSourceMapper.class, InputEventHandler.class})
public class HACoordinationSourceHandlerTest extends PowerMockTestCase {

    private static final String SOURCE_1 = "source-1";

    @Test
    public void testPassiveNodeEventQueue() throws InterruptedException {

        HACoordinationSourceHandler haCoordinationSourceHandler =
                (HACoordinationSourceHandler) new HACoordinationSourceHandlerManager(10).generateSourceHandler();

        InputHandler inputHandler = mock(InputHandler.class);
        doNothing().when(inputHandler).send(any(Event.class));

        haCoordinationSourceHandler.init(SOURCE_1, new StreamDefinition());

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

        haCoordinationSourceHandler.sendEvent(event, inputHandler);
        haCoordinationSourceHandler.sendEvent(eventTwo, inputHandler);

        //Source should stop events from processing and start collecting
        haCoordinationSourceHandler.collectEvents(true);
        haCoordinationSourceHandler.sendEvent(eventThree, inputHandler);
        haCoordinationSourceHandler.sendEvent(eventFour, inputHandler);

        Assert.assertEquals(haCoordinationSourceHandler.getPassiveNodeBufferedEvents().size(), 2);
        Assert.assertEquals(haCoordinationSourceHandler.getPassiveNodeBufferedEvents().peek().getTimestamp(), 3L);

    }

    @Test
    public void testPassiveNodeEventSending() throws InterruptedException {

        HACoordinationSourceHandler haCoordinationSourceHandler = spy(new HACoordinationSourceHandler(10));
        doNothing().when(haCoordinationSourceHandler).sendEvent(Mockito.any(Event.class));

        InputHandler inputHandler = mock(InputHandler.class);
        doNothing().when(inputHandler).send(any(Event.class));
        when(haCoordinationSourceHandler.getInputHandler()).thenReturn(inputHandler);

        haCoordinationSourceHandler.init(SOURCE_1, new StreamDefinition());

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

        haCoordinationSourceHandler.sendEvent(event, inputHandler);
        haCoordinationSourceHandler.sendEvent(eventTwo, inputHandler);

        //Source should stop events from processing and start collecting
        haCoordinationSourceHandler.collectEvents(true);
        haCoordinationSourceHandler.sendEvent(eventThree, inputHandler);
        haCoordinationSourceHandler.sendEvent(eventFour, inputHandler);

        Assert.assertEquals(haCoordinationSourceHandler.getPassiveNodeBufferedEvents().size(), 2);
        Assert.assertEquals(haCoordinationSourceHandler.getPassiveNodeBufferedEvents().peek().getTimestamp(), 3L);

        haCoordinationSourceHandler.sendEvent(eventFive, inputHandler);
        haCoordinationSourceHandler.sendEvent(eventSix, inputHandler);

        //Passive node should remove events that active node processed before snapshot was taken
        //and process all events after that
        haCoordinationSourceHandler.processBufferedEvents(4L);

        Assert.assertEquals(haCoordinationSourceHandler.getPassiveNodeBufferedEvents().size(), 0);

    }

    @Test
    public void testActiveNodeProcessing() throws Exception {

        HACoordinationSourceHandler haCoordinationSourceHandler = spy(new HACoordinationSourceHandler(10));
        doNothing().when(haCoordinationSourceHandler).sendEvent(Mockito.any(Event.class));

        InputHandler inputHandler = mock(InputHandler.class);
        doNothing().when(inputHandler).send(any(Event.class));
        haCoordinationSourceHandler.setInputHandler(inputHandler);

        haCoordinationSourceHandler.init(SOURCE_1, new StreamDefinition());

        Event event = new Event();
        Event eventTwo = new Event();
        Event eventThree = new Event();
        Event eventFour = new Event();
        event.setTimestamp(1L);
        eventTwo.setTimestamp(2L);
        eventThree.setTimestamp(3L);
        eventFour.setTimestamp(4L);

        haCoordinationSourceHandler.setAsActive();

        //Active Should Send Events for Processing and Update the Last Published Events TS
        haCoordinationSourceHandler.sendEvent(event, inputHandler);
        haCoordinationSourceHandler.sendEvent(eventTwo, inputHandler);


        Map<String, Object> stateObject = haCoordinationSourceHandler.currentState();
        Assert.assertEquals((long) stateObject.get(CoordinationConstants.ACTIVE_PROCESSED_LAST_TIMESTAMP), 2L);

        haCoordinationSourceHandler.sendEvent(eventThree, inputHandler);
        haCoordinationSourceHandler.sendEvent(eventFour, inputHandler);

        stateObject = haCoordinationSourceHandler.currentState();
        Assert.assertEquals((long) stateObject.get(CoordinationConstants.ACTIVE_PROCESSED_LAST_TIMESTAMP), 4L);

    }
}
