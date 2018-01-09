/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.stream.processor.core.api;


import javax.ws.rs.core.Response;

/**
 * Auto generated class from Swagger to MSF4J.
 */

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
        date = "2017-05-31T15:43:24.557Z")
public abstract class SiddhiAppsApiService {

    public abstract Response siddhiAppsAppNameDelete(String appName, String username) throws NotFoundException;

    public abstract Response siddhiAppsAppNameGet(String appName, String username) throws NotFoundException;

    public abstract Response siddhiAppsAppNameStatusGet(String appName, String username) throws NotFoundException;

    public abstract Response siddhiAppsAppNameRestorePost(String appName, String revision, String username) throws NotFoundException;

    public abstract Response siddhiAppsAppNameBackupPost(String appName, String username) throws NotFoundException;

    public abstract Response siddhiAppsGet(String isActive, String username) throws NotFoundException;

    public abstract Response siddhiAppsPost(String body, String username) throws NotFoundException;

    public abstract Response siddhiAppsPut(String body, String username) throws NotFoundException;

    public abstract Response siddhiAppsStatisticsGet(String isActive, String username) throws NotFoundException;

    public abstract Response siddhiAppStatsEnable(String appName, boolean statsEnabled, String username) throws
            NotFoundException;

    public abstract Response siddhiAppsStatsEnable(boolean statsEnabled, String username) throws
            NotFoundException;
}
