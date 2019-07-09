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

package org.wso2.carbon.streaming.integrator.core.ha;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.table.record.RecordTableHandler;
import org.wso2.siddhi.core.table.record.RecordTableHandlerCallback;
import org.wso2.siddhi.core.util.collection.operator.CompiledCondition;
import org.wso2.siddhi.core.util.collection.operator.CompiledExpression;
import org.wso2.siddhi.core.util.collection.operator.CompiledSelection;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.TableDefinition;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.wso2.carbon.streaming.integrator.core.ha.RecordTableData.EventType;

/**
 * Implementation of {@link RecordTableHandler} used for two node minimum HA
 */
public class HACoordinationRecordTableHandler extends RecordTableHandler {

    private boolean isActiveNode;
    private long lastEventChunkTimestamp;
    private TableDefinition tableDefinition;
    private static final Logger log = Logger.getLogger(HACoordinationRecordTableHandler.class);


    public HACoordinationRecordTableHandler() {
    }

    @Override
    public void init(String elementId, TableDefinition tableDefinition) {
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
                    updateSetParameterMaps);
            if (log.isDebugEnabled()) {
                log.debug("Last Timestamp for Record Table Update " + timestamp);
            }

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
        }
    }

    @Override
    public Iterator<Object[]> find(long timestamp, Map<String, Object> findConditionParameterMap,
                                   CompiledCondition compiledCondition,
                                   RecordTableHandlerCallback recordTableHandlerCallback)
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

    @Override
    public Iterator<Object[]> query(long timestamp, Map<String, Object> propertiesMap,
                                    CompiledCondition compiledCondition,
                                    CompiledSelection compiledSelection,
                                    RecordTableHandlerCallback recordTableHandlerCallback)
            throws ConnectionUnavailableException {
        return query(timestamp, propertiesMap, compiledCondition, compiledSelection,
                null, recordTableHandlerCallback);
    }

    @Override
    public Iterator<Object[]> query(long timestamp, Map<String, Object> propertiesMap,
                                    CompiledCondition compiledCondition,
                                    CompiledSelection compiledSelection, Attribute[] outputAttributes,
                                    RecordTableHandlerCallback recordTableHandlerCallback)
            throws ConnectionUnavailableException {
        if (isActiveNode) {
            lastEventChunkTimestamp = timestamp;
        }
        return recordTableHandlerCallback.query(propertiesMap, compiledCondition, compiledSelection, outputAttributes);
    }

    /**
     * Method that stops the passive node from queuing events and act as active node.
     * All queued events are sent to the record table for appropriate processing
     */
    public void setAsActive() throws ConnectionUnavailableException {
        this.isActiveNode = true;
        if (log.isDebugEnabled()) {
            log.debug("HA Deployment: Changing to active state.");
        }
    }

    /**
     * Method that changes the state when node becomes passive
     */
    public void setAsPassive() {
        this.isActiveNode = false;
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
}
