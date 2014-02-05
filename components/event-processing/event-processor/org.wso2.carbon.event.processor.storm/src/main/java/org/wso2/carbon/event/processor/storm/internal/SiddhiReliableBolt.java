package org.wso2.carbon.event.processor.storm.internal;

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import org.apache.log4j.Logger;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.config.SiddhiConfiguration;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.output.StreamCallback;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SiddhiReliableBolt extends BaseBasicBolt {
    private static transient Logger log = Logger.getLogger(SiddhiReliableBolt.class);
    private transient SiddhiManager siddhiManager;
    private String[] exportedStreams;
    private BasicOutputCollector collector;
    private String executionPlan;
    private boolean useDefaultAsStreamName = true;
    private String componentID;

    public SiddhiReliableBolt() {
        init();
    }

    public SiddhiReliableBolt(String executionPlan, InputHandler inputHandler, String[] exportedStreams) {
        this.executionPlan = executionPlan;
        this.exportedStreams = exportedStreams;
        init();
    }

    public void init() {
        SiddhiConfiguration configuration = new SiddhiConfiguration();
        SiddhiManager siddhiManager = new SiddhiManager(configuration);

        if(this.executionPlan != null) {
            siddhiManager.addExecutionPlan(executionPlan);
        }
        this.siddhiManager = siddhiManager;
        for (final String streamId : exportedStreams) {
            siddhiManager.addCallback(streamId, new StreamCallback() {
                @Override
                public void receive(Event[] events) {
                    for (Event event : events) {
                        List<Object> asList = Arrays.asList(event.getData());
                        log.debug(componentID + ">Siddhi: Emit Event " + event);
                        if (useDefaultAsStreamName) {
                            getCollector().emit(asList);
                        } else {
                            getCollector().emit(streamId, asList);
                        }

                    }
                }
            });
        }

    }

    @Override
    public void prepare(Map stormConf, TopologyContext context) {
        super.prepare(stormConf, context);
        this.componentID = context.getThisComponentId();
    }

    @Override
    public void execute(Tuple tuple, BasicOutputCollector collector) {
        try {
            String streamID = tuple.getSourceStreamId();

            this.collector = collector;
            if (siddhiManager == null) {
                //Bolt get saved and reloaded, this handle that condition
                init();
            }

            InputHandler inputHandler = siddhiManager.getInputHandler(streamID);
            if (inputHandler == null) {
                //If we cannot find a stream with the given name, we will try using the component ID.
                //Since most Storm programs are written only using component IDs and not streams, this
                //code added to handle this case
                inputHandler = siddhiManager.getInputHandler(tuple.getSourceComponent());
            }
            if (inputHandler != null) {
                log.debug(componentID + ">Siddhi: Received Event " + tuple);
                inputHandler.send(tuple.getValues().toArray());
            } else {
                throw new RuntimeException("Input handler for stream " + streamID + " not found");
            }

        } catch (InterruptedException e) {
            log.error(e);
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        if (siddhiManager == null) {
            init();
        }

        boolean useDefault = (exportedStreams.length == 1) && useDefaultAsStreamName;

        for (String streamId : exportedStreams) {
            StreamDefinition streamDefinition = siddhiManager.getStreamDefinition(streamId);
            if (streamDefinition == null) {
                throw new RuntimeException("Cannot find stream " + streamId + " " + this);
            }
            List<Attribute> attributeList = streamDefinition.getAttributeList();
            List<String> list = new ArrayList<String>();
            for (Attribute attribute : attributeList) {
                list.add(attribute.getName());
            }
            Fields feilds = new Fields(list);
            if (useDefault) {
                //this is so that most common Storm topologies would work with
                //Siddhi bolt.  See setUseDefaultAsStreamName(..)
                log.info(componentID + ">Siddhi: declaring stream as default");
                declarer.declare(feilds);
            } else {
                declarer.declareStream(streamId, feilds);
            }

        }
    }

    public BasicOutputCollector getCollector() {
        return collector;
    }

    public boolean isUseDefaultAsStreamName() {
        return useDefaultAsStreamName;
    }

    /**
     * When this is true and there is only one stream to expose, Siddhi bolt will export the resulting stream as default.
     * This set to true by default.
     *
     * @param useDefaultAsStreamName
     */
    public void setUseDefaultAsStreamName(boolean useDefaultAsStreamName) {
        this.useDefaultAsStreamName = useDefaultAsStreamName;
    }


}
