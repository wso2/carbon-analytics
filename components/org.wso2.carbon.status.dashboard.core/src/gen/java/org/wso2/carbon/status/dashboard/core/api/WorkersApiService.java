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
package org.wso2.carbon.status.dashboard.core.api;

import org.wso2.carbon.status.dashboard.core.exception.DashboardException;
import org.wso2.carbon.status.dashboard.core.model.StatsEnable;
import org.wso2.carbon.status.dashboard.core.model.Worker;

import javax.ws.rs.core.Response;
import java.sql.SQLException;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen", date = "2017-09-11T07:55:11.886Z")
public abstract class WorkersApiService {
    public abstract Response addWorker(Worker worker,String username) throws NotFoundException;

    public abstract Response deleteWorker(String id,String username) throws NotFoundException, SQLException;

    public abstract Response enableSiddhiAppStats(String workerId, String appName, StatsEnable statEnable,
                                                  String username) throws NotFoundException;

    public abstract Response getHADetails(String id) throws NotFoundException;

    public abstract Response getAllSiddhiApps(String id ,String period, String type,Integer pageName) throws
            NotFoundException;

    public abstract Response getAllWorkers() throws NotFoundException, SQLException;

    public abstract Response getAppHistory(String id,String appName,String period,String type) throws NotFoundException;

    public abstract Response getComponentHistory(String id,String appName,String componentType,String componentId,String
            period,String type) throws NotFoundException;

    public abstract Response getSiddhiAppDetails(String id,String appName) throws NotFoundException;

    public abstract Response getWorkerConfig(String id) throws NotFoundException, SQLException;

    public abstract Response getWorkerGeneralDetails(String id) throws NotFoundException;

    public abstract Response getWorkerHistory(String id,String period,String type,Boolean more) throws
            NotFoundException;

    public abstract Response testConnection(String id) throws NotFoundException;

    public abstract Response getDashboardConfig() throws NotFoundException;

    public abstract Response getSiddhiAppComponents(String id,String appName) throws NotFoundException;

    public abstract Response getRolesByUsername(String username,String permisstionString);
}
