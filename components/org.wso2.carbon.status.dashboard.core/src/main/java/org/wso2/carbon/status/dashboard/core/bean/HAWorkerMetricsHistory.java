/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.wso2.carbon.streaming.integrator.core.internal.util.SiddhiAppProcessorConstants;
import io.siddhi.core.util.SiddhiConstants;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Model for siddhi app overview data.
 */
public class HAWorkerMetricsHistory {
    private MetricsLineCharts sendingThroughput;
    private MetricsLineCharts receivingThroughput;
    private String receivingThroughputRecent;
    private String sendingThroughputRecent;
    private String workerId;

    public HAWorkerMetricsHistory(String workerId) {
        this.workerId = workerId;
        sendingThroughput = new MetricsLineCharts();
        receivingThroughput = new MetricsLineCharts();
    }

    public void setSendingThroughputRecent(List<List<Object>> throughput) {
        if ((throughput != null) && (!throughput.isEmpty())) {
            this.sendingThroughputRecent = NumberFormat.getIntegerInstance()
                    .format((throughput.get(throughput.size() - 1)).get(1));
        } else {
            sendingThroughputRecent = "0";
        }
    }

    public void setReceivingThroughputRecent(List<List<Object>> throughput) {
        if ((throughput != null) && (!throughput.isEmpty())) {
            this.receivingThroughputRecent = NumberFormat.getIntegerInstance()
                    .format((throughput.get(throughput.size() - 1)).get(1));
        } else {
            receivingThroughputRecent = "0";
        }
    }

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }

    public MetricsLineCharts getSendingThroughputThroughput() {
        return sendingThroughput;
    }

    public MetricsLineCharts getReceivingThroughput() {
        return receivingThroughput;
    }

    public void setThroughput(List<List<Object>> throughputData) {
        List<List<Object>> sendingThroughput = new ArrayList<>();
        List<List<Object>> receivingThroughput = new ArrayList<>();
        for (int i = 0; i < throughputData.size(); i++) {
            if (throughputData.get(i).get(2).toString().startsWith(SiddhiAppProcessorConstants.HA_METRICS_PREFIX +
                    SiddhiConstants.METRIC_DELIMITER + SiddhiAppProcessorConstants.HA_METRICS_RECEIVING_THROUGHPUT)) {
                throughputData.get(i).remove(2);
                receivingThroughput.add(throughputData.get(i));
            } else {
                throughputData.get(i).remove(2);
                sendingThroughput.add(throughputData.get(i));
            }
        }
        this.sendingThroughput.setData(sendingThroughput);
        this.receivingThroughput.setData(receivingThroughput);
        setSendingThroughputRecent(sendingThroughput);
        setReceivingThroughputRecent(receivingThroughput);
    }

    public String getReceivingThroughputRecent() {
        return receivingThroughputRecent;
    }

    public String getSendingThroughputRecent() {
        return sendingThroughputRecent;
    }
}
