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

import org.wso2.carbon.status.dashboard.core.exception.StatusDashboardValidationException;

import java.util.List;

/**
 * WorkerConfigurationDetails bean class.
 *
 */
public class NodeConfigurationDetails {

    private String workerId;
    private String host;
    private int port;


    public NodeConfigurationDetails(String workerId, String host, int port) {
        this.workerId = workerId;
        this.host = host;
        this.port = port;
    }

    public NodeConfigurationDetails() {
    }

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Object[] toArray() {
        return new Object[]{workerId, host, port};
    }

    public void setArrayList(List values) throws StatusDashboardValidationException {
        Object[] objects = new Object[]{workerId, host, port};
        if (values.size() != objects.length) {
            throw new StatusDashboardValidationException("Invalid length of object which is recieved data has:" +
                    values.size() + " while bean has " + objects.length);
        }
        for (int i = 0; i < objects.length; i++) {
            switch (i) {
                case 0:
                    workerId = (String) values.get(i);
                    break;
                case 1:
                    host = (String) values.get(i);
                    break;
                case 2:
                    port = (Integer) values.get(i);
                    break;
                default:
                    throw new StatusDashboardValidationException("Invalid object:" + values.get(i));
            }
        }


    }

    public static String getColumnLabeles() {
        return "WORKERID,HOST,PORT";
    }

    public static String getManagerColumnLabeles() {
        return "MANAGERID,HOST,PORT";
    }
}
