/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.stream.processor.core;

import org.mockito.Mockito;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.stream.processor.core.ha.HACoordinationRecordTableHandler;
import org.wso2.carbon.stream.processor.core.ha.HACoordinationRecordTableHandlerManager;
import org.wso2.carbon.stream.processor.core.ha.RecordTableData;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.table.record.RecordTableHandlerCallback;
import org.wso2.siddhi.query.api.definition.TableDefinition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

public class HACoordinationRecordTableHandlerTest extends PowerMockTestCase {

    @Test
    public void testPassiveNodeEventQueue() throws ConnectionUnavailableException {
        HACoordinationRecordTableHandlerManager recordTableHandlerManager = new HACoordinationRecordTableHandlerManager
                (10);
        HACoordinationRecordTableHandler recordTableHandler = (HACoordinationRecordTableHandler)
                recordTableHandlerManager.generateRecordTableHandler();
        recordTableHandler.init("element-id", TableDefinition.id("id"));

        RecordTableHandlerCallback recordTableHandlerCallback = mock(RecordTableHandlerCallback.class);
        doNothing().when(recordTableHandlerCallback).add(Mockito.any());
        doNothing().when(recordTableHandlerCallback).delete(Mockito.any(), Mockito.any());
        doNothing().when(recordTableHandlerCallback).update(Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any());
        doNothing().when(recordTableHandlerCallback).updateOrAdd(Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any());
        when(recordTableHandlerCallback.find(Mockito.any(), Mockito.any())).thenReturn(null);
        when(recordTableHandlerCallback.contains(Mockito.any(), Mockito.any())).thenReturn(false);

        //Events should be queued since passive node
        recordTableHandler.contains(1L, new HashMap<>(), null, recordTableHandlerCallback);
        recordTableHandler.find(2L, new HashMap<>(), null, recordTableHandlerCallback);
        recordTableHandler.add(3L, new ArrayList<>(), recordTableHandlerCallback);
        recordTableHandler.delete(4L, new ArrayList<>(), null, recordTableHandlerCallback);
        recordTableHandler.update(5L, null, new ArrayList<>(), new LinkedHashMap<>(), new ArrayList<>(),
                recordTableHandlerCallback);
        recordTableHandler.updateOrAdd(6L, null, new ArrayList<>(), new LinkedHashMap<>(), new ArrayList<>(),
                new ArrayList<>(), recordTableHandlerCallback);

        Assert.assertEquals(recordTableHandler.getEventQueue().size(), 4);
        Assert.assertEquals(recordTableHandler.getEventQueue().peek().getTimestamp(), 3L);
        Assert.assertEquals(recordTableHandler.getEventQueue().poll().getEventType(), RecordTableData.EventType.ADD);
        Assert.assertEquals(recordTableHandler.getEventQueue().poll().getEventType(),
                RecordTableData.EventType.DELETE);
        Assert.assertEquals(recordTableHandler.getEventQueue().poll().getEventType(),
                RecordTableData.EventType.UPDATE);
        Assert.assertEquals(recordTableHandler.getEventQueue().poll().getEventType(), RecordTableData.EventType.
                UPDATE_OR_ADD);
    }

    @Test
    public void testActiveNodeEventQueue() throws ConnectionUnavailableException {
        HACoordinationRecordTableHandlerManager recordTableHandlerManager = new HACoordinationRecordTableHandlerManager
                (10);
        HACoordinationRecordTableHandler recordTableHandler = (HACoordinationRecordTableHandler)
                recordTableHandlerManager.generateRecordTableHandler();
        recordTableHandler.init("element-id", TableDefinition.id("id"));

        RecordTableHandlerCallback recordTableHandlerCallback = mock(RecordTableHandlerCallback.class);
        doNothing().when(recordTableHandlerCallback).add(Mockito.any());
        doNothing().when(recordTableHandlerCallback).delete(Mockito.any(), Mockito.any());
        doNothing().when(recordTableHandlerCallback).update(Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any());
        doNothing().when(recordTableHandlerCallback).updateOrAdd(Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any());
        when(recordTableHandlerCallback.find(Mockito.any(), Mockito.any())).thenReturn(null);
        when(recordTableHandlerCallback.contains(Mockito.any(), Mockito.any())).thenReturn(false);

        //Events should be queued since passive node
        recordTableHandler.add(1L, new ArrayList<>(), recordTableHandlerCallback);
        recordTableHandler.delete(2L, new ArrayList<>(), null, recordTableHandlerCallback);
        recordTableHandler.update(3L, null, new ArrayList<>(), new LinkedHashMap<>(), new ArrayList<>(),
                recordTableHandlerCallback);
        recordTableHandler.updateOrAdd(4L, null, new ArrayList<>(), new LinkedHashMap<>(), new ArrayList<>(),
                new ArrayList<>(), recordTableHandlerCallback);

        //Events older than this timestamp should be removed from queue
        recordTableHandler.trimRecordTableEventQueue(2L);

        Assert.assertEquals(recordTableHandler.getEventQueue().size(), 2);
        Assert.assertEquals(recordTableHandler.getEventQueue().peek().getEventType(),
                RecordTableData.EventType.UPDATE);

        recordTableHandler.add(5L, new ArrayList<>(), recordTableHandlerCallback);
        recordTableHandler.delete(6L, new ArrayList<>(), null, recordTableHandlerCallback);
        //All queued events should be handled
        recordTableHandler.setAsActive();

        //Timestamp of latest event should be updated
        recordTableHandler.add(7L, new ArrayList<>(), recordTableHandlerCallback);
        recordTableHandler.delete(8L, new ArrayList<>(), null, recordTableHandlerCallback);
        recordTableHandler.update(9L, null, new ArrayList<>(), new LinkedHashMap<>(), new ArrayList<>(),
                recordTableHandlerCallback);
        recordTableHandler.updateOrAdd(10L, null, new ArrayList<>(), new LinkedHashMap<>(), new ArrayList<>(),
                new ArrayList<>(), recordTableHandlerCallback);
        recordTableHandler.find(11L, new HashMap<>(), null, recordTableHandlerCallback);
        recordTableHandler.contains(12L, new HashMap<>(), null, recordTableHandlerCallback);

        Assert.assertEquals(recordTableHandler.getEventQueue().size(), 0);
        Assert.assertEquals(recordTableHandler.getActiveNodeLastOperationTimestamp(), 12L);
    }

    @Test
    public void testQueueCapacity() throws ConnectionUnavailableException {
        HACoordinationRecordTableHandlerManager recordTableHandlerManager = new HACoordinationRecordTableHandlerManager
                (5);
        HACoordinationRecordTableHandler recordTableHandler = (HACoordinationRecordTableHandler)
                recordTableHandlerManager.generateRecordTableHandler();
        recordTableHandler.init("element-id", TableDefinition.id("id"));

        RecordTableHandlerCallback recordTableHandlerCallback = mock(RecordTableHandlerCallback.class);
        doNothing().when(recordTableHandlerCallback).add(Mockito.any());
        doNothing().when(recordTableHandlerCallback).delete(Mockito.any(), Mockito.any());
        doNothing().when(recordTableHandlerCallback).update(Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any());
        doNothing().when(recordTableHandlerCallback).updateOrAdd(Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any());
        when(recordTableHandlerCallback.find(Mockito.any(), Mockito.any())).thenReturn(null);
        when(recordTableHandlerCallback.contains(Mockito.any(), Mockito.any())).thenReturn(false);

        //Filling up queue
        recordTableHandler.add(1L, new ArrayList<>(), recordTableHandlerCallback);
        recordTableHandler.add(2L, new ArrayList<>(), recordTableHandlerCallback);
        recordTableHandler.add(3L, new ArrayList<>(), recordTableHandlerCallback);
        recordTableHandler.add(4L, new ArrayList<>(), recordTableHandlerCallback);
        recordTableHandler.add(5L, new ArrayList<>(), recordTableHandlerCallback);

        Assert.assertEquals(recordTableHandler.getEventQueue().size(), 5);

        recordTableHandler.add(1L, new ArrayList<>(), recordTableHandlerCallback);
        Assert.assertEquals(recordTableHandler.getEventQueue().size(), 5);

        recordTableHandler.find(2L, new HashMap<>(), null, recordTableHandlerCallback);
        Assert.assertEquals(recordTableHandler.getEventQueue().size(), 5);

        recordTableHandler.add(3L, new ArrayList<>(), recordTableHandlerCallback);
        Assert.assertEquals(recordTableHandler.getEventQueue().size(), 5);

        recordTableHandler.delete(4L, new ArrayList<>(), null, recordTableHandlerCallback);
        Assert.assertEquals(recordTableHandler.getEventQueue().size(), 5);

        recordTableHandler.update(5L, null, new ArrayList<>(), new LinkedHashMap<>(),
                new ArrayList<>(), recordTableHandlerCallback);
        Assert.assertEquals(recordTableHandler.getEventQueue().size(), 5);

        recordTableHandler.updateOrAdd(6L, null, new ArrayList<>(), new LinkedHashMap<>(),
                new ArrayList<>(), new ArrayList<>(), recordTableHandlerCallback);
        Assert.assertEquals(recordTableHandler.getEventQueue().size(), 5);
    }
}
