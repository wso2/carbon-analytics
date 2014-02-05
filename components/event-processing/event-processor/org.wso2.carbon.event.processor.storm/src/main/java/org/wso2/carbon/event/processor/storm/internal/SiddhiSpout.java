/**
 * Copyright (c) 2005 - 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.wso2.carbon.event.processor.storm.internal;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.processor.core.ExecutionPlanConfiguration;
import org.wso2.carbon.event.processor.storm.internal.ds.StormProcessorValueHolder;
import org.wso2.carbon.event.processor.core.internal.stream.EventConsumer;
import org.wso2.carbon.event.processor.storm.internal.util.StormProcessorConstants;
import org.wso2.carbon.event.statistics.EventStatisticsMonitor;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class SiddhiSpout extends BaseRichSpout implements EventConsumer {
    private static Log log = LogFactory.getLog(SiddhiSpout.class);
    private final int tenantId;
    private final boolean traceEnabled;
    private final boolean statisticsEnabled;
    private Logger trace = Logger.getLogger(StormProcessorConstants.EVENT_TRACE_LOGGER);
    private Object owner;
    private EventStatisticsMonitor statisticsMonitor;
    private String tracerPrefix = "";
    private SpoutOutputCollector _collector;
    private BlockingQueue<Object[]> inputQueue;
    private StreamDefinition siddhiStreamDefinition;
    private String streamId;
    private boolean useDefaultAsStreamName = true;

    public SiddhiSpout(String streamId, ExecutionPlanConfiguration executionPlanConfiguration) {
        this.streamId = streamId;
        this.owner = executionPlanConfiguration;
        this.tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        this.traceEnabled = executionPlanConfiguration.isTracingEnabled();
        this.statisticsEnabled = executionPlanConfiguration.isStatisticsEnabled();
        this.inputQueue = new LinkedBlockingDeque<Object[]>();
        if (statisticsEnabled) {
            statisticsMonitor = StormProcessorValueHolder.getEventStatisticsService().getEventStatisticMonitor(tenantId, StormProcessorConstants.EVENT_PROCESSOR, executionPlanConfiguration.getName(), streamId + " (" + siddhiStreamDefinition.getStreamId() + ")");
        }
        if (traceEnabled) {
            this.tracerPrefix = "TenantId=" + tenantId + " : " + StormProcessorConstants.EVENT_PROCESSOR + " : " + executionPlanConfiguration.getName() + "," + streamId + " (" + siddhiStreamDefinition.getStreamId() + "), before processing " + System.getProperty("line.separator");
        }
    }

    @Override
    public void consumeEvents(Object[][] events) {

        if (traceEnabled) {
            trace.info(tracerPrefix + Arrays.deepToString(events));
        }

        for (Object[] eventData : events) {
            if (statisticsEnabled) {
                statisticsMonitor.incrementRequest();
            }
            inputQueue.offer(eventData);
        }

    }

    @Override
    public void consumeEvents(Event[] events) {
        for (Event event : events) {
            if (traceEnabled) {
                trace.info(tracerPrefix + Arrays.deepToString(events));
            }
            if (statisticsEnabled) {
                statisticsMonitor.incrementRequest();
            }
            inputQueue.offer(event.getData());
        }
    }

    @Override
    public void consumeEvent(Object[] eventData) {
        if (traceEnabled) {
            trace.info(tracerPrefix + Arrays.deepToString(eventData));
        }
        if (statisticsEnabled) {
            statisticsMonitor.incrementRequest();
        }
        inputQueue.offer(eventData);
    }

    @Override
    public void consumeEvent(Event event) {
        if (traceEnabled) {
            trace.info(tracerPrefix + event);
        }
        if (statisticsEnabled) {
            statisticsMonitor.incrementRequest();
        }
        inputQueue.offer(event.getData());
    }

    @Override
    public Object getOwner() {
        return owner;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        List<Attribute> attributeList = siddhiStreamDefinition.getAttributeList();
        List<String> attributeNames = new ArrayList<String>(attributeList.size());
        for (Attribute attribute : attributeList) {
            attributeNames.add(attribute.getName());
        }
        if (useDefaultAsStreamName) {
            declarer.declare(new Fields(attributeNames));
        } else {
            declarer.declareStream(streamId, new Fields(attributeNames));
        }
    }

    @Override
    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
        this._collector = collector;
    }

    @Override
    public void nextTuple() {
        try {
            if (useDefaultAsStreamName) {
                _collector.emit(Arrays.asList(inputQueue.take()));
            } else {
                _collector.emit(streamId, Arrays.asList(inputQueue.take()));
            }
        } catch (InterruptedException e) {
            log.error("Thread interrupted while waiting for events on stream " + streamId);
        }
    }

}
