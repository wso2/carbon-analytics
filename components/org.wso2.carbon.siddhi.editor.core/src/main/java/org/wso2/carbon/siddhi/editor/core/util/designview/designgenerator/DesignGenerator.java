/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.EventFlow;
import org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.builders.EventFlowBuilder;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.DesignGenerationException;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.exception.SiddhiAppCreationException;
import org.wso2.siddhi.query.api.SiddhiApp;
import org.wso2.siddhi.query.compiler.SiddhiCompiler;

/**
 * Generator to create Config objects from Siddhi Code
 */
public class DesignGenerator {
    /**
     * Gets EventFlow configuration for a given Siddhi app code string
     * @param siddhiAppString                   Code representation of the Siddhi app
     * @return                                  Event flow representation of the Siddhi app
     * @throws DesignGenerationException        Error while generating config
     */
    public EventFlow getEventFlow(String siddhiAppString) throws DesignGenerationException {
        SiddhiApp siddhiApp;
        SiddhiAppRuntime siddhiAppRuntime;
        try {
            siddhiApp = SiddhiCompiler.parse(siddhiAppString);
            siddhiAppRuntime = new SiddhiManager().createSiddhiAppRuntime(siddhiApp);
        } catch (Exception e) {
            // Runtime exception occurred. Eg: Missing a library for a particular source
            throw new SiddhiAppCreationException(e.getMessage());
        }

        EventFlowBuilder eventFlowBuilder =
                new EventFlowBuilder(siddhiAppString, siddhiApp, siddhiAppRuntime)
                        .loadAppAnnotations()
                        .loadTriggers()
                        .loadStreams()
                        .loadSources()
                        .loadSinks()
                        .loadTables()
                        .loadWindows()
                        .loadAggregations()
                        .loadExecutionElements()
                        .loadFunctions()
                        .loadEdges();

        return eventFlowBuilder.create();
    }
}
