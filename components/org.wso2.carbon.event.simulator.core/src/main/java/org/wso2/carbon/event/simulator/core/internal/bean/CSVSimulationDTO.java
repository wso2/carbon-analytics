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
package org.wso2.carbon.event.simulator.core.internal.bean;


import java.util.List;

/**
 * CSVSimulationDTO returns the configuration for CSV simulation
 */
public class CSVSimulationDTO extends StreamConfigurationDTO {

    private String fileName;
    private String delimiter;
    private List<Integer> indices;
    /**
     * Flag to indicate whether the CSV records are ordered by timestamp or not
     */
    private boolean isOrdered = true;

    public CSVSimulationDTO() {
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public boolean getIsOrdered() {
        return isOrdered;
    }

    public void setIsOrdered(boolean ordered) {
        isOrdered = ordered;
    }

    public List<Integer> getIndices() {
        return indices;
    }

    public void setIndices(List<Integer> indices) {
        this.indices = indices;
    }

    @Override
    public String toString() {
        return getStreamConfiguration() +
                "\n fileName : " + fileName +
                "\n delimiter : " + delimiter +
                "\n isOrdered : " + isOrdered +
                "\n indices : " + indices + "\n";

    }
}
