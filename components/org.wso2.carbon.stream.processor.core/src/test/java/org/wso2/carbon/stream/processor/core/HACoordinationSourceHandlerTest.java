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
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.stream.processor.core.ha.HACoordinationSourceHandler;
import org.wso2.carbon.stream.processor.core.ha.util.CoordinationConstants;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.Map;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.spy;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.mock;

public class HACoordinationSourceHandlerTest extends PowerMockTestCase {

    @BeforeTest
    public void setDebugLogLevel() {
        Logger.getLogger(HACoordinationSourceHandler.class.getName()).setLevel(Level.DEBUG);
    }

    private static final String SOURCE_1 = "source-1";

    @Test
    public void testActiveNodeProcessing() throws InterruptedException {

        HACoordinationSourceHandler haCoordinationSourceHandler = spy(new HACoordinationSourceHandler());
        doNothing().when(haCoordinationSourceHandler).sendEvent(Mockito.any(Event.class), Mockito.any());

        InputHandler inputHandler = mock(InputHandler.class);
        doNothing().when(inputHandler).send(any(Event.class));
        haCoordinationSourceHandler.setInputHandler(inputHandler);
        haCoordinationSourceHandler.setAsActive();
        haCoordinationSourceHandler.setPassiveNodeAdded(false);

        haCoordinationSourceHandler.init("A", null, SOURCE_1, new StreamDefinition());

        Event event = new Event();
        Event eventTwo = new Event();
        Event eventThree = new Event();
        Event eventFour = new Event();
        event.setTimestamp(1L);
        eventTwo.setTimestamp(2L);
        eventThree.setTimestamp(3L);
        eventFour.setTimestamp(4L);

        haCoordinationSourceHandler.sendEvent(event, null, inputHandler);
        haCoordinationSourceHandler.sendEvent(eventTwo, null, inputHandler);

        Map<String, Object> stateObject = haCoordinationSourceHandler.currentState();
        Assert.assertEquals((long) stateObject.get(CoordinationConstants.ACTIVE_PROCESSED_LAST_TIMESTAMP), 2L);

        haCoordinationSourceHandler.sendEvent(eventThree, null, inputHandler);
        haCoordinationSourceHandler.sendEvent(eventFour, null, inputHandler);

        stateObject = haCoordinationSourceHandler.currentState();
        Assert.assertEquals((long) stateObject.get(CoordinationConstants.ACTIVE_PROCESSED_LAST_TIMESTAMP), 4L);

    }

    @Test
    public void testActiveNodeArrayOfEventsProcessing() throws InterruptedException {

        HACoordinationSourceHandler haCoordinationSourceHandler = spy(new HACoordinationSourceHandler());
        doNothing().when(haCoordinationSourceHandler).sendEvent(Mockito.any(Event.class), Mockito.any());

        InputHandler inputHandler = mock(InputHandler.class);
        doNothing().when(inputHandler).send(any(Event.class));
        haCoordinationSourceHandler.setInputHandler(inputHandler);
        haCoordinationSourceHandler.setAsActive();
        haCoordinationSourceHandler.setPassiveNodeAdded(false);

        haCoordinationSourceHandler.init("A", null, SOURCE_1, new StreamDefinition());

        Event event = new Event();
        Event eventTwo = new Event();
        Event eventThree = new Event();
        Event eventFour = new Event();
        event.setTimestamp(1L);
        eventTwo.setTimestamp(2L);
        eventThree.setTimestamp(3L);
        eventFour.setTimestamp(4L);
        Event[] events = {event, eventTwo, eventThree, eventFour};

        haCoordinationSourceHandler.sendEvent(events, null, inputHandler);

        Map<String, Object> stateObject = haCoordinationSourceHandler.currentState();
        Assert.assertEquals((long) stateObject.get(CoordinationConstants.ACTIVE_PROCESSED_LAST_TIMESTAMP), 4L);

    }
}
