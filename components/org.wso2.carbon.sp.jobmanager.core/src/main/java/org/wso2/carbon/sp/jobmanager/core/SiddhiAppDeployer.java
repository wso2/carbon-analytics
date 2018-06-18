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

package org.wso2.carbon.sp.jobmanager.core;

import com.google.gson.Gson;
import org.apache.log4j.Logger;
import org.wso2.carbon.sp.jobmanager.core.api.ResourceServiceFactory;
import org.wso2.carbon.sp.jobmanager.core.appcreator.SiddhiQuery;
import org.wso2.carbon.sp.jobmanager.core.impl.utils.Constants;
import org.wso2.carbon.sp.jobmanager.core.model.ResourceNode;
import org.wso2.carbon.sp.jobmanager.core.util.HTTPSClientUtil;

import java.util.Arrays;
import java.util.List;

/**
 * This class gives the details of the deployed siddhi application's details.
 */
public class SiddhiAppDeployer {
    private static final Logger LOG = Logger.getLogger(SiddhiAppDeployer.class);

    /**
     * Deploy Siddhi app and return it's name
     *
     * @param node        node to be deployed.
     * @param siddhiQuery SiddhiQuery holder
     * @return Siddhi app name.
     */
    public static String deploy(ResourceNode node, SiddhiQuery siddhiQuery) {
        feign.Response resourceResponse = null;
        try {
            resourceResponse = ResourceServiceFactory.getResourceHttpsClient(Constants.PROTOCOL +
                            HTTPSClientUtil.generateURLHostPort(node.getHttpsInterface().getHost(),
                                    String.valueOf(node.getHttpsInterface().getPort())),
                    node.getHttpsInterface().getUsername(), node.getHttpsInterface().getPassword())
                    .postSiddhiApp(siddhiQuery.getApp());
            if (resourceResponse != null) {
                if (resourceResponse.status() == 201) {
                    String locationHeader = resourceResponse.headers().getOrDefault("Location", null).toString();
                    return (locationHeader != null && !locationHeader.isEmpty())
                            ? locationHeader.substring(locationHeader.lastIndexOf('/') + 1) : null;
                } else if (resourceResponse.status() == 409) {
                    resourceResponse.close();
                    resourceResponse = ResourceServiceFactory.getResourceHttpsClient(Constants.PROTOCOL +
                                    HTTPSClientUtil.generateURLHostPort(node.getHttpsInterface().getHost(),
                                            String.valueOf(node.getHttpsInterface().getPort())),
                            node.getHttpsInterface().getUsername(), node.getHttpsInterface().getPassword())
                            .putSiddhiApp(siddhiQuery.getApp());
                    if (resourceResponse.status() == 200) {
                        return siddhiQuery.getAppName();
                    } else {
                        return null;
                    }
                }
            }
            return null;
        } catch (feign.FeignException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Error occurred while deploying Siddhi app to " + node, e);
            }
            return null;
        } finally {
            if (resourceResponse != null) {
                resourceResponse.close();
            }
        }
    }

    /**
     * Un-deploy Siddhi app and return whether it successfully un-deployed or not.
     *
     * @param node          node to be deployed.
     * @param siddhiAppName name of the Siddhi app.
     * @return a boolean stating whether the app get un-deployed or not.
     */
    public static boolean unDeploy(ResourceNode node, String siddhiAppName) {
        feign.Response resourceResponse = null;
        try {
            resourceResponse = ResourceServiceFactory.getResourceHttpsClient(Constants.PROTOCOL +
                            HTTPSClientUtil.generateURLHostPort(node.getHttpsInterface().getHost(),
                                    String.valueOf(node.getHttpsInterface().getPort())),
                    node.getHttpsInterface().getUsername(), node.getHttpsInterface().getPassword())
                    .deleteSiddhiApp(siddhiAppName);
            return resourceResponse.status() == 200;
        } catch (feign.FeignException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Error occurred while up-deploying Siddhi app from " + node, e);
            }
            return false;
        } finally {
            if (resourceResponse != null) {
                resourceResponse.close();
            }
        }
    }

    /**
     * Get list of deployed Siddhi app names in a given resource node.
     *
     * @param node resource node.
     * @return list of deployed Siddhi app names.
     */
    public static List<String> getDeployedApps(ResourceNode node) {
        feign.Response resourceResponse = null;
        String[] apps = new String[0];
        try {
            resourceResponse = ResourceServiceFactory.getResourceHttpsClient(Constants.PROTOCOL +
                            HTTPSClientUtil.generateURLHostPort(node.getHttpsInterface().getHost(),
                                    String.valueOf(node.getHttpsInterface().getPort())),
                    node.getHttpsInterface().getUsername(), node.getHttpsInterface().getPassword())
                    .getSiddhiApps();
            if (resourceResponse.status() == 200) {
                apps = new Gson().fromJson(resourceResponse.body().toString(), String[].class);
            }
        } catch (feign.FeignException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Error occurred while retrieving deployed Siddhi apps from " + node, e);
            }
        } finally {
            if (resourceResponse != null) {
                resourceResponse.close();
            }
        }
        return Arrays.asList(apps);
    }
}
