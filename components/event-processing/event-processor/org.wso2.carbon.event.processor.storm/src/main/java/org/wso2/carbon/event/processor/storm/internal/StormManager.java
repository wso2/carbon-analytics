/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.event.processor.storm.internal;

import backtype.storm.Config;
import org.wso2.carbon.event.processor.core.ExecutionPlanConfiguration;
import org.wso2.carbon.event.processor.storm.internal.listener.StormOutputStreamListener;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.config.SiddhiConfiguration;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.ArrayList;
import java.util.List;

public class StormManager {
    private Config config;
    private List<SiddhiBolt> stormBoltList = new ArrayList<SiddhiBolt>();
    private List<SiddhiSpout> stormSpoutList = new ArrayList<SiddhiSpout>();

    private SiddhiManager dummySiddhiManager;

    public void submitExecutionPlan(String executionPlan) {
        dummySiddhiManager.addExecutionPlan(executionPlan);
        for(StreamDefinition streamDefinition: dummySiddhiManager.getStreamDefinitions()) {
            stormBoltList.add(new SiddhiBolt());
        }
    }

    public InputHandler addSpoutFor(StreamDefinition streamDefinition, ExecutionPlanConfiguration executionPlanConfiguration) {
        stormSpoutList.add(new SiddhiSpout(streamDefinition.getStreamId(), executionPlanConfiguration));
        return null;
    }

    public void addBoltFor(StormOutputStreamListener stormOutputStreamListener) {

    }

    public StormManager(Config config, SiddhiConfiguration siddhiConfiguration) {
        this.config = config;
        dummySiddhiManager = new SiddhiManager(siddhiConfiguration);
    }
}
