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

import org.wso2.carbon.status.dashboard.core.model.Node;
import org.wso2.carbon.status.dashboard.core.model.StatsEnable;

import java.io.IOException;
import java.sql.SQLException;
import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
        date = "2017-09-11T07:55:11.886Z")
public abstract class MonitoringApiService {
    public abstract Response addWorker(Node worker, String username) throws NotFoundException;

    public abstract Response addManager(Node manager, String username) throws NotFoundException;

    public abstract Response deleteWorker(String id, String username) throws NotFoundException, SQLException;

    public abstract Response deleteManager(String id, String username) throws NotFoundException, SQLException;

    public abstract Response enableSiddhiAppStats(String workerId, String appName, StatsEnable statEnable,
                                                  String username) throws NotFoundException;

    public abstract Response getHADetails(String id, String username) throws NotFoundException;

    public abstract Response getAllSiddhiApps(String id, String period, String type, Integer pageName,
                                              String username) throws NotFoundException;

    public abstract Response getAllWorkers(String username) throws NotFoundException, SQLException;

    public abstract Response getAppHistory(String id, String appName, String period, String type,
                                           String username) throws NotFoundException;

    public abstract Response getComponentHistory(String id, String appName, String componentType, String componentId,
                                                 String period, String type, String username) throws NotFoundException;

    public abstract Response getSiddhiAppDetails(String id, String appName, String username) throws NotFoundException;

    public abstract Response getWorkerConfig(String id, String username) throws NotFoundException, SQLException;

    public abstract Response populateWorkerGeneralDetails(String id, String username) throws NotFoundException;

    public abstract Response getWorkerHistory(String id, String period, String type, Boolean more, String username)
            throws NotFoundException;

    public abstract Response testConnection(String id, String username) throws NotFoundException;

    public abstract Response getDashboardConfig(String username) throws NotFoundException;

    public abstract Response getSiddhiAppComponents(String id, String appName,
                                                    String username) throws NotFoundException;

    public abstract Response getRolesByUsername(String username, String permisstionString);

    public abstract Response getRuntimeEnv(String id, String username) throws NotFoundException;

    public abstract Response getManagerHADetails(String id, String username) throws NotFoundException;

    public abstract Response getSiddhiApps(String id, String username) throws NotFoundException, IOException;

    public abstract Response getManagers(String username) throws NotFoundException, SQLException;

    public abstract Response getManagerSiddhiAppTextView(String id, String appName,
                                                         String username) throws NotFoundException;

    public abstract Response getChildAppsDetails(String id, String appName,
                                                 String username) throws NotFoundException, IOException;

    public abstract Response getChildAppsTransportDetails(String id, String appName,
                                                          String username) throws NotFoundException, IOException;

    public abstract Response getClusterResourceNodeDetails(String id, String username)
            throws NotFoundException, IOException;

    public abstract Response getSingleDeploymentSiddhiApps(String username) throws NotFoundException, SQLException;

    public abstract Response getHASiddhiApps(String username) throws NotFoundException, SQLException;

    public abstract Response getAllManagersSiddhiApps(String username) throws NotFoundException, SQLException;
}
