/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.status.dashboard.core.bean;

import java.util.List;

/**
 * This Hold the worker specific line chart data.
 */
public class MetricsLineCharts {
    private List<List<Object>> data;
    private String [] dataLabels;
    private String [] dataUnit;


    public MetricsLineCharts() {
    }

    public List<List<Object>> getData() {
        return data;
    }

    public void setData(List<List<Object>> data) {
        this.data = data;
    }

    public String[] getDataLabels() {
        return dataLabels;
    }

    public void setDataLabels(String[] dataLabels) {
        this.dataLabels = dataLabels;
    }

    public String[] getDataUnit() {
        return dataUnit;
    }

    public void setDataUnit(String[] dataUnit) {
        this.dataUnit = dataUnit;
    }
}
