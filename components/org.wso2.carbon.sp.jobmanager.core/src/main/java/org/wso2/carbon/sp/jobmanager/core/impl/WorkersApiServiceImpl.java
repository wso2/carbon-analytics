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
package org.wso2.carbon.sp.jobmanager.core.impl;

import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.sp.jobmanager.core.api.NotFoundException;
import org.wso2.carbon.sp.jobmanager.core.api.WorkersApiService;
import org.wso2.carbon.sp.jobmanager.core.internal.ServiceDataHolder;
import org.wso2.carbon.sp.jobmanager.core.model.ResourceNode;
import org.wso2.carbon.sp.jobmanager.core.model.ResourcePool;

import java.util.Map;
import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
                            date = "2018-02-03T14:53:27.713Z")
@Component(service = WorkersApiService.class, immediate = true)
public class WorkersApiServiceImpl extends WorkersApiService {
    @Override
    public Response getWorkers() throws NotFoundException {
        ResourcePool resourcePool = ServiceDataHolder.getResourcePool();
        Map<String, ResourceNode> resourceNodeList = resourcePool.getResourceNodeMap();
        if (resourceNodeList != null) {
            return Response.ok().entity(resourceNodeList.size()).build();
        } else {
            return Response.status(Response.Status.NO_CONTENT).entity("0").build();
        }
    }
}
