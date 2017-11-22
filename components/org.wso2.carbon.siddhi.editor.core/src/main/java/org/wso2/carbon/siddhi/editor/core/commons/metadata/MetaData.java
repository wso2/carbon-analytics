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
package org.wso2.carbon.siddhi.editor.core.commons.metadata;

import java.util.LinkedList;
import java.util.List;

/**
 * For storing meta data for a extension namespace or in-built processors
 * Used in JSON responses
 */
public class MetaData {
    private List<ProcessorMetaData> functions;
    private List<ProcessorMetaData> streamProcessors;
    private List<ProcessorMetaData> windowProcessors;
    private List<ProcessorMetaData> sources;
    private List<ProcessorMetaData> sinks;
    private List<ProcessorMetaData> sourceMaps;
    private List<ProcessorMetaData> sinkMaps;
    private List<ProcessorMetaData> stores;

    public MetaData() {
        functions = new LinkedList<>();
        streamProcessors = new LinkedList<>();
        windowProcessors = new LinkedList<>();
        sources = new LinkedList<>();
        sinks = new LinkedList<>();
        sourceMaps = new LinkedList<>();
        sinkMaps = new LinkedList<>();
        stores = new LinkedList<>();
    }

    public List<ProcessorMetaData> getFunctions() {
        return functions;
    }

    public void setFunctions(List<ProcessorMetaData> functions) {
        this.functions = functions;
    }

    public List<ProcessorMetaData> getStreamProcessors() {
        return streamProcessors;
    }

    public void setStreamProcessors(List<ProcessorMetaData> streamProcessors) {
        this.streamProcessors = streamProcessors;
    }

    public List<ProcessorMetaData> getWindowProcessors() {
        return windowProcessors;
    }

    public void setWindowProcessors(List<ProcessorMetaData> windowProcessors) {
        this.windowProcessors = windowProcessors;
    }

    public List<ProcessorMetaData> getSources() {
        return sources;
    }

    public void setSources(List<ProcessorMetaData> sources) {
        this.sources = sources;
    }

    public List<ProcessorMetaData> getSinks() {
        return sinks;
    }

    public void setSinks(List<ProcessorMetaData> sinks) {
        this.sinks = sinks;
    }

    public List<ProcessorMetaData> getSourceMaps() {
        return sourceMaps;
    }

    public void setSourceMaps(List<ProcessorMetaData> sourceMaps) {
        this.sourceMaps = sourceMaps;
    }

    public List<ProcessorMetaData> getSinkMaps() {
        return sinkMaps;
    }

    public void setSinkMaps(List<ProcessorMetaData> sinkMaps) {
        this.sinkMaps = sinkMaps;
    }

    public List<ProcessorMetaData> getStores() {
        return stores;
    }

    public void setStores(List<ProcessorMetaData> stores) {
        this.stores = stores;
    }
}
