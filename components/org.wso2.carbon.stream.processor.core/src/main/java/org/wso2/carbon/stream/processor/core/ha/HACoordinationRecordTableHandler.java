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

package org.wso2.carbon.stream.processor.core.ha;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.table.record.RecordTableHandler;
import org.wso2.siddhi.core.table.record.RecordTableHandlerCallback;
import org.wso2.siddhi.core.util.collection.operator.CompiledCondition;
import org.wso2.siddhi.core.util.collection.operator.CompiledExpression;
import org.wso2.siddhi.query.api.definition.TableDefinition;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.wso2.carbon.stream.processor.core.ha.RecordTableData.EventType;

/**
 * Implementation of {@link RecordTableHandler} used for two node minimum HA
 */
public class HACoordinationRecordTableHandler extends RecordTableHandler {

    private boolean isActiveNode;
    private long lastEventChunkTimestamp;
    private Queue<RecordTableData> eventQueue;
    private int queueCapacity;
    private TableDefinition tableDefinition;
    private static final Logger log = Logger.getLogger(HACoordinationRecordTableHandler.class);


    public HACoordinationRecordTableHandler(int queueCapacity) {
        this.queueCapacity = queueCapacity;
    }

    @Override
    public void init(String elementId, TableDefinition tableDefinition) {
        eventQueue = new LinkedBlockingQueue<>(queueCapacity);
        this.tableDefinition = tableDefinition;
    }

    @Override
    public void add(long timestamp, List<Object[]> records, RecordTableHandlerCallback recordTableHandlerCallback)
            throws ConnectionUnavailableException {
        if (isActiveNode) {
            lastEventChunkTimestamp = timestamp;
            recordTableHandlerCallback.add(records);
            if (log.isDebugEnabled()) {
                log.debug("Last Timestamp for Record Table Add " + timestamp);
            }
        } else {
            if (eventQueue.size() == queueCapacity) {
                eventQueue.remove();
            }
            eventQueue.add(new RecordTableData(timestamp, EventType.ADD, recordTableHandlerCallback, records));
        }
    }

    @Override
    public void delete(long timestamp, List<Map<String, Object>> deleteConditionParameterMaps,
                       CompiledCondition compiledCondition,
                       RecordTableHandlerCallback recordTableHandlerCallback) throws ConnectionUnavailableException {
        if (isActiveNode) {
            lastEventChunkTimestamp = timestamp;
            recordTableHandlerCallback.delete(deleteConditionParameterMaps, compiledCondition);
            if (log.isDebugEnabled()) {
                log.debug("Last Timestamp for Record Table Delete " + timestamp);
            }
        } else {
            if (eventQueue.size() == queueCapacity) {
                eventQueue.remove();
            }
            eventQueue.add(new RecordTableData(timestamp, EventType.DELETE, recordTableHandlerCallback,
                    compiledCondition, deleteConditionParameterMaps));
        }
    }

    @Override
    public void update(long timestamp, CompiledCondition compiledCondition,
                       List<Map<String, Object>> updateConditionParameterMaps,
                       LinkedHashMap<String, CompiledExpression> updateSetMap,
                       List<Map<String, Object>> updateSetParameterMaps,
                       RecordTableHandlerCallback recordTableHandlerCallback) throws ConnectionUnavailableException {
        if (isActiveNode) {
            lastEventChunkTimestamp = timestamp;
            recordTableHandlerCallback.update(compiledCondition, updateConditionParameterMaps, updateSetMap,
                    updateConditionParameterMaps);
            if (log.isDebugEnabled()) {
                log.debug("Last Timestamp for Record Table Update " + timestamp);
            }

        } else {
            if (eventQueue.size() == queueCapacity) {
                eventQueue.remove();
            }
            eventQueue.add(new RecordTableData(timestamp, EventType.UPDATE,
                    recordTableHandlerCallback, compiledCondition, updateConditionParameterMaps, updateSetMap,
                    updateSetParameterMaps));
        }
    }

    @Override
    public void updateOrAdd(long timestamp, CompiledCondition compiledCondition,
                            List<Map<String, Object>> updateConditionParameterMaps,
                            LinkedHashMap<String, CompiledExpression> updateSetMap,
                            List<Map<String, Object>> updateSetParameterMaps, List<Object[]> addingRecords,
                            RecordTableHandlerCallback recordTableHandlerCallback)
            throws ConnectionUnavailableException {
        if (isActiveNode) {
            lastEventChunkTimestamp = timestamp;
            recordTableHandlerCallback.updateOrAdd(compiledCondition, updateConditionParameterMaps, updateSetMap,
                    updateSetParameterMaps, addingRecords);
            if (log.isDebugEnabled()) {
                log.debug("Last Timestamp for Record Table UpdateAdd " + timestamp);
            }
        } else {
            if (eventQueue.size() == queueCapacity) {
                eventQueue.remove();
            }
            eventQueue.add(new RecordTableData(timestamp, EventType.UPDATE_OR_ADD, recordTableHandlerCallback,
                    compiledCondition, addingRecords, updateConditionParameterMaps, updateSetMap,
                    updateSetParameterMaps));
        }
    }

    @Override
    public Iterator<Object[]> find(long timestamp, Map<String, Object> findConditionParameterMap,
                                   CompiledCondition compiledCondition, RecordTableHandlerCallback recordTableHandlerCallback)
            throws ConnectionUnavailableException {
        if (isActiveNode) {
            lastEventChunkTimestamp = timestamp;
        }
        return recordTableHandlerCallback.find(findConditionParameterMap, compiledCondition);
    }

    @Override
    public boolean contains(long timestamp, Map<String, Object> containsConditionParameterMap,
                            CompiledCondition compiledCondition, RecordTableHandlerCallback recordTableHandlerCallback)
            throws ConnectionUnavailableException {
        if (isActiveNode) {
            lastEventChunkTimestamp = timestamp;
        }
        return recordTableHandlerCallback.contains(containsConditionParameterMap, compiledCondition);
    }

    /**
     * Method that stops the passive node from queuing events and act as active node.
     * All queued events are sent to the record table for appropriate processing
     */
    public void setAsActive() throws ConnectionUnavailableException {

        this.isActiveNode = true;
        if (log.isDebugEnabled()) {
            log.debug("HA Deployment: Changing to active state. Executing buffered record table operations");
        }
        while (eventQueue.peek() != null) {
            RecordTableData recordTableData = eventQueue.remove();
            switch (recordTableData.getEventType()) {
                case ADD:
                    recordTableData.getRecordTableHandlerCallback().add(recordTableData.getRecords());
                    break;
                case DELETE:
                    recordTableData.getRecordTableHandlerCallback().delete(recordTableData.getConditionParameterMaps(),
                            recordTableData.getCompiledCondition());
                    break;
                case UPDATE:
                    recordTableData.getRecordTableHandlerCallback().update(recordTableData.getCompiledCondition(),
                            recordTableData.getConditionParameterMaps(), recordTableData.getSetMap(),
                            recordTableData.getSetParameterMaps());
                    break;
                case UPDATE_OR_ADD:
                    recordTableData.getRecordTableHandlerCallback().updateOrAdd(recordTableData.getCompiledCondition(),
                            recordTableData.getConditionParameterMaps(), recordTableData.getSetMap(),
                            recordTableData.getConditionParameterMaps(), recordTableData.getRecords());
                    break;
                default:
                    break;
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("HA Deployment: Changing to active state. Buffered record table operations complete");
        }
    }

    /**
     * Update the event queue according to the last processed event timestamp of the active node in given record table.
     *
     * @param lastActiveNodeOperationTimestamp timestamp of last processed event of active nodes record table.
     */
    public void trimRecordTableEventQueue(long lastActiveNodeOperationTimestamp) {
        while (eventQueue.peek() != null && eventQueue.peek().getTimestamp() <= lastActiveNodeOperationTimestamp) {
            eventQueue.remove();
        }
    }

    /**
     * Get the timestamp of the last processed events timestamp of the active nodes record table.
     *
     * @return the timestamp of last processed events timestamp of the active nodes record table.
     */
    public long getActiveNodeLastOperationTimestamp() {
        return this.lastEventChunkTimestamp;
    }

    public String getTableId() {
        return tableDefinition.getId();
    }

    public Queue<RecordTableData> getEventQueue() {
        return eventQueue;
    }
}
