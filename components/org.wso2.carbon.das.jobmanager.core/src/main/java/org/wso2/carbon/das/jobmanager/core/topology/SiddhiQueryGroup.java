/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.das.jobmanager.core.topology;

import java.util.List;

/**
 * Data Holder to hold required details of Query Groups in {@link SiddhiTopology}
 */
public class SiddhiQueryGroup {
    private String name;
    private int parallelism;
    private String siddhiApp;
    private List<InputStreamDataHolder> inputStreams;
    private List<OutputStreamDataHolder> outputStream;

    public SiddhiQueryGroup(String name, int parallelism, String siddhiApp,
                            List<InputStreamDataHolder> inputStreams,
                            List<OutputStreamDataHolder> outputStream) {
        this.name = name;
        this.parallelism = parallelism;
        this.siddhiApp = siddhiApp;
        this.inputStreams = inputStreams;
        this.outputStream = outputStream;
    }

    public String getName() {
        return name;
    }

    public int getParallelism() {
        return parallelism;
    }

    public String getSiddhiApp() {
        return siddhiApp;
    }

    public List<InputStreamDataHolder> getInputStreams() {
        return inputStreams;
    }

    public List<OutputStreamDataHolder> getOutputStream() {
        return outputStream;
    }
}
