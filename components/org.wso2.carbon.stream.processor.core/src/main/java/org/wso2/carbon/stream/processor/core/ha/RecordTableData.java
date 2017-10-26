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

import org.wso2.siddhi.core.table.record.RecordTableHandlerCallback;
import org.wso2.siddhi.core.util.collection.operator.CompiledCondition;
import org.wso2.siddhi.core.util.collection.operator.CompiledExpression;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Object that holds information required for add, delete, update and updateOrAdd record table events.
 */
public class RecordTableData {

    private RecordTableHandlerCallback recordTableHandlerCallback;
    private CompiledCondition compiledCondition;
    private List<Object[]> records;
    private List<Map<String, Object>> conditionParameterMaps;
    private LinkedHashMap<String, CompiledExpression> setMap;
    private List<Map<String, Object>> setParameterMaps;
    private long timestamp;
    private EventType eventType;

    /**
     * Constructor for Add operation
     */
    RecordTableData(long timestamp, EventType eventType, RecordTableHandlerCallback recordTableHandlerCallback,
                    List<Object[]> records) {
        this.timestamp = timestamp;
        this.eventType = eventType;
        this.recordTableHandlerCallback = recordTableHandlerCallback;
        this.records = records;
    }

    /**
     * Constructor for Delete operation
     */
    RecordTableData(long timestamp, EventType eventType, RecordTableHandlerCallback recordTableHandlerCallback,
                    CompiledCondition compiledCondition, List<Map<String, Object>> conditionParameterMaps) {
        this.timestamp = timestamp;
        this.eventType = eventType;
        this.recordTableHandlerCallback = recordTableHandlerCallback;
        this.compiledCondition = compiledCondition;
        this.conditionParameterMaps = conditionParameterMaps;
    }

    /**
     * Constructor for Update operation
     */
    RecordTableData(long timestamp, EventType eventType, RecordTableHandlerCallback recordTableHandlerCallback,
                    CompiledCondition compiledCondition, List<Map<String, Object>> conditionParameterMaps,
                    LinkedHashMap<String, CompiledExpression> setMap, List<Map<String, Object>> setParameterMaps) {
        this.timestamp = timestamp;
        this.eventType = eventType;
        this.recordTableHandlerCallback = recordTableHandlerCallback;
        this.compiledCondition = compiledCondition;
        this.conditionParameterMaps = conditionParameterMaps;
        this.setMap = setMap;
        this.setParameterMaps = setParameterMaps;
    }

    /**
     * Constructor for UpdateOrAdd operation
     */
    RecordTableData(long timestamp, EventType eventType, RecordTableHandlerCallback recordTableHandlerCallback,
                    CompiledCondition compiledCondition, List<Object[]> records,
                    List<Map<String, Object>> conditionParameterMaps, LinkedHashMap<String, CompiledExpression> setMap,
                    List<Map<String, Object>> setParameterMaps) {
        this.timestamp = timestamp;
        this.eventType = eventType;
        this.recordTableHandlerCallback = recordTableHandlerCallback;
        this.compiledCondition = compiledCondition;
        this.records = records;
        this.conditionParameterMaps = conditionParameterMaps;
        this.setMap = setMap;
        this.setParameterMaps = setParameterMaps;
    }

    RecordTableHandlerCallback getRecordTableHandlerCallback() {
        return recordTableHandlerCallback;
    }

    CompiledCondition getCompiledCondition() {
        return compiledCondition;
    }

    List<Object[]> getRecords() {
        return records;
    }

    List<Map<String, Object>> getConditionParameterMaps() {
        return conditionParameterMaps;
    }

    LinkedHashMap<String, CompiledExpression> getSetMap() {
        return setMap;
    }

    List<Map<String, Object>> getSetParameterMaps() {
        return setParameterMaps;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public EventType getEventType() {
        return eventType;
    }

    public enum EventType {
        ADD,
        DELETE,
        UPDATE,
        UPDATE_OR_ADD
    }
}