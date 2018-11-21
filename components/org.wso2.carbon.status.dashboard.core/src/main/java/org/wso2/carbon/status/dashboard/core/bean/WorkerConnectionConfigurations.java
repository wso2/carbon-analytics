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

import org.wso2.carbon.config.annotation.Element;

/**
 * Bean class contains the configuration details of the fign client.
 */
public class WorkerConnectionConfigurations {
    @Element(description = "Connection timeout of a feign client")
    private Integer clientConnectionTimeOut;
    
    @Element(description = "Connection timeout of a feign client")
    private Integer clientReadTimeOut;
    
    public WorkerConnectionConfigurations() {
    }
    
    public WorkerConnectionConfigurations(Integer clientConnectionTimeOut, Integer clientReadTimeOut) {
        this.clientConnectionTimeOut = clientConnectionTimeOut;
        this.clientReadTimeOut = clientReadTimeOut;
    }
    
    public Integer getConnectionTimeOut() {
        return clientConnectionTimeOut;
    }
    
    public void setConnectionTimeOut(Integer connectionTimeOut) {
        this.clientConnectionTimeOut = connectionTimeOut;
    }
    
    public Integer getReadTimeOut() {
        return clientReadTimeOut;
    }
    
    public void setReadTimeOut(Integer readTimeOut) {
        this.clientReadTimeOut = readTimeOut;
    }
}
