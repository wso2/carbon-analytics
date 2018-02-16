/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.analytics.permissions.PermissionProvider;
import org.wso2.carbon.analytics.permissions.bean.Permission;
import org.wso2.carbon.sp.jobmanager.core.api.NotFoundException;
import org.wso2.carbon.sp.jobmanager.core.api.WorkersApiService;
import org.wso2.carbon.sp.jobmanager.core.impl.utils.Constants;
import org.wso2.carbon.sp.jobmanager.core.internal.ManagerDataHolder;
import org.wso2.carbon.sp.jobmanager.core.internal.ServiceDataHolder;
import org.wso2.carbon.sp.jobmanager.core.internal.services.DatasourceServiceComponent;
import org.wso2.carbon.sp.jobmanager.core.model.ResourceNode;
import org.wso2.carbon.sp.jobmanager.core.model.ResourcePool;
import org.wso2.msf4j.Request;

import java.util.Map;
import javax.ws.rs.core.Response;

/**
 * Distributed Siddhi Service Implementataion Class
 */

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
                            date = "2018-02-03T14:53:27.713Z")
@Component(service = WorkersApiService.class, immediate = true)

public class WorkersApiServiceImpl extends WorkersApiService {
    private static final Log logger = LogFactory.getLog(WorkersApiServiceImpl.class);
    private static final String MANAGE_SIDDHI_APP_PERMISSION_STRING = "siddhiApp.manage";
    private static final String VIEW_SIDDHI_APP_PERMISSION_STRING = "siddhiApp.view";

    private static String getUserName(org.wso2.msf4j.Request request) {
        Object username = request.getProperty("username");
        return username != null ? username.toString() : null;
    }

    public Response getResourceCluster() {
        ResourcePool resourcePool = ServiceDataHolder.getResourcePool();
        Map<String, ResourceNode> resourceNodeList = resourcePool.getResourceNodeMap();
        if (resourceNodeList != null) {
            return Response.ok().entity(resourceNodeList.size()).build();
        } else {
            return Response.status(Response.Status.NO_CONTENT).entity("0").build();
        }
    }

    /**
     * This method is to retrieve the resource node details in the cluster
     *
     * @param request
     * @return the count of the worker nodes in the resource cluster.
     * @throws NotFoundException
     */

    @Override
    public Response getWorkers(Request request) throws NotFoundException {
        if (getUserName(request) != null && !(getPermissionProvider().hasPermission(getUserName(request), new
                Permission(Constants.PERMISSION_APP_NAME, VIEW_SIDDHI_APP_PERMISSION_STRING)) || getPermissionProvider()
                .hasPermission(getUserName(request), new Permission(Constants.PERMISSION_APP_NAME,
                                                                    MANAGE_SIDDHI_APP_PERMISSION_STRING)))) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Insufficient permissions to get the "
                                                                                + "execution plan").build();
        } else {
            return getResourceCluster();
        }
    }

    private PermissionProvider getPermissionProvider() {
        return ManagerDataHolder.getInstance().getPermissionProvider();
    }

    @Reference(
            name = "org.wso2.carbon.sp.jobmanager.core.internal.services.DatasourceServiceComponent",
            service = DatasourceServiceComponent.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterServiceDatasource"
    )
    public void regiterServiceDatasource(DatasourceServiceComponent datasourceServiceComponent) {
        if (logger.isDebugEnabled()) {
            logger.debug("@Reference(bind) DatasourceServiceComponent");
        }
    }

    public void unregisterServiceDatasource(DatasourceServiceComponent datasourceServiceComponent) {
        if (logger.isDebugEnabled()) {
            logger.debug("@Reference(unbind) DatasourceServiceComponent");
        }
    }

}
