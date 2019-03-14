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

package io.siddhi.distribution.editor.core.util.designview.beans.configs.siddhielements.sourcesink;

import io.siddhi.distribution.editor.core.util.designview.beans.configs.siddhielements.SiddhiElementConfig;
import io.siddhi.distribution.editor.core.util.designview.beans.configs.siddhielements.sourcesink.mapper.MapperConfig;

import java.util.List;

/**
 * Represents Siddhi Source/Sink.
 */
public class SourceSinkConfig extends SiddhiElementConfig {
    private String annotationType;
    private String connectedElementName;
    private String type;
    private List<String> options;
    private MapperConfig map;
    private boolean isCorrelationIdExist;
    private String correlationId;

    public SourceSinkConfig(String annotationType, String connectedElementName, String type,
                            List<String> options, MapperConfig map, boolean isCorrelationIdExist,
                            String correlationId) {
        this.annotationType = annotationType;
        this.connectedElementName = connectedElementName;
        this.type = type;
        this.options = options;
        this.map = map;
        this.isCorrelationIdExist = isCorrelationIdExist;
        this.correlationId = correlationId;
    }

    public String getAnnotationType() {
        return annotationType;
    }

    public String getConnectedElementName() {
        return connectedElementName;
    }

    public String getType() {
        return type;
    }

    public List<String> getOptions() {
        return options;
    }

    public MapperConfig getMap() {
        return map;
    }

    public boolean isCorrelationIdExist() {
        return isCorrelationIdExist;
    }

    public String getCorrelationId() {
        return correlationId;
    }
}
