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

import org.wso2.carbon.das.jobmanager.core.util.DistributedConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Holder to hold required details of Query Groups in {@link SiddhiTopology}
 */
public class SiddhiQueryGroup {
    private String name;
    private int parallelism;
    private String siddhiApp;
    private Map<String, InputStreamDataHolder> inputStreams;
    private Map<String, OutputStreamDataHolder> outputStream;
    private List<String> queryList;

    public SiddhiQueryGroup(String name, int parallelism, Map<String, InputStreamDataHolder> inputStreams,
                            Map<String, OutputStreamDataHolder> outputStream) {
        this.name = name;
        this.parallelism = parallelism;
        this.inputStreams = inputStreams;
        this.outputStream = outputStream;
        this.queryList = new ArrayList<>();
    }

    public SiddhiQueryGroup() {
        this.queryList = new ArrayList<>();
        siddhiApp = " ";
        inputStreams = new HashMap<>();
        outputStream = new HashMap<>();

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getParallelism() {
        return parallelism;
    }

    public void setParallelism(int parallelism) {
        this.parallelism = parallelism;
    }

    public String getSiddhiApp() {
        //combination of InputStream definitions , OutputStream and queries
        StringBuilder stringBuilder = new StringBuilder("@App:name('${" + DistributedConstants.APP_NAME + "}') \n");

        for (InputStreamDataHolder inputStreamDataHolder : inputStreams.values()) {

            siddhiApp = inputStreamDataHolder.getStreamDefinition();
            if (siddhiApp != null) {
                stringBuilder.append(siddhiApp).append(";\n");
            }
        }

        for (OutputStreamDataHolder outputStreamDataHolder : outputStream.values()) {
            siddhiApp = outputStreamDataHolder.getStreamDefinition();
            if (siddhiApp != null) {
                stringBuilder.append(siddhiApp).append(";\n");
            }
        }

        for (String aQueryList : queryList) {
            stringBuilder.append(aQueryList).append(";\n");
        }

        siddhiApp = stringBuilder.toString();
        return stringBuilder.toString();
    }

    public void addQuery(String query) {
        queryList.add(query);
    }

    public void addInputStreamHolder(String key, InputStreamDataHolder inputStreamDataHolder) {
        inputStreams.put(key, inputStreamDataHolder);
    }

    public void addOutputStreamHolder(String key, OutputStreamDataHolder OutputStreamDataHolder) {
        outputStream.put(key, OutputStreamDataHolder);
    }

    public Map<String, InputStreamDataHolder> getInputStreams() {
        return inputStreams;
    }

    public Map<String, OutputStreamDataHolder> getOutputStream() {
        return outputStream;
    }

}
