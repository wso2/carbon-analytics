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
package org.wso2.carbon.status.dashboard.core.eventFlow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Holder to hold required details of Query Groups in
 */
public class SiddhiQueryDataHolder {
    private String name;
    private Map<String, InputStreamDataHolder> inputStreams;
    private Map<String, OutputStreamDataHolder> outputStreams;
    private List<String> queryList;

    public SiddhiQueryDataHolder() {
        this.queryList = new ArrayList<>();
        inputStreams = new HashMap<>();
        outputStreams = new HashMap<>();

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public void addQuery(String query) {
        queryList.add(query);
    }

    public void addInputStreamHolder(String key, InputStreamDataHolder inputStreamDataHolder) {
        inputStreams.put(key, inputStreamDataHolder);
    }

    public void addOutputStreamHolder(String key, OutputStreamDataHolder OutputStreamDataHolder) {
        outputStreams.put(key, OutputStreamDataHolder);
    }

    public Map<String, InputStreamDataHolder> getInputStreams() {
        return inputStreams;
    }

    public Map<String, OutputStreamDataHolder> getOutputStreams() {
        return outputStreams;
    }

}
