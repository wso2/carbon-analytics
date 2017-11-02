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

package org.wso2.carbon.das.jobmanager.core;

import com.google.gson.Gson;
import org.apache.log4j.Logger;
import org.wso2.carbon.das.jobmanager.core.appCreator.SiddhiQuery;
import org.wso2.carbon.das.jobmanager.core.model.ResourceNode;
import org.wso2.carbon.das.jobmanager.core.util.HTTPClientUtil;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import okhttp3.Response;

public class SiddhiAppDeployer {
    private static final Logger LOG = Logger.getLogger(SiddhiAppDeployer.class);
    private static final String SERVICE_ENDPOINT = "http://%s:%s/siddhi-apps%s";

    /**
     * Deploy Siddhi app and return it's name
     *
     * @param node        node to be deployed.
     * @param siddhiQuery SiddhiQuery holder
     * @return Siddhi app name.
     */
    public static String deploy(ResourceNode node, SiddhiQuery siddhiQuery) {
        Response response = null;
        try {
            response = HTTPClientUtil.doPostRequest(String.format(SERVICE_ENDPOINT,
                    node.getHttpInterface().getHost(), node.getHttpInterface().getPort(), ""),
                    siddhiQuery.getApp());
            if (response.code() == 201) {
                String locationHeader = response.header("Location", null);
                return (locationHeader != null && !locationHeader.isEmpty())
                        ? locationHeader.substring(locationHeader.lastIndexOf('/') + 1) : null;
            } else if (response.code() == 409) {
                response.close();
                response = HTTPClientUtil.doPutRequest(String.format(SERVICE_ENDPOINT,
                        node.getHttpInterface().getHost(), node.getHttpInterface().getPort(), ""),
                        siddhiQuery.getApp());
                if (response.code() == 200) {
                    return siddhiQuery.getAppName();
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } catch (IOException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Error occurred while deploying Siddhi app to " + node, e);
            }
            return null;
        } finally {
            if (response != null) {
                response.close();
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
        Response response = null;
        try {
            response = HTTPClientUtil.doDeleteRequest(String.format(SERVICE_ENDPOINT,
                    node.getHttpInterface().getHost(), node.getHttpInterface().getPort(), "/" + siddhiAppName)
            );
            return response.code() == 200;
        } catch (IOException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Error occurred while up-deploying Siddhi app from " + node, e);
            }
            return false;
        } finally {
            if (response != null) {
                response.close();
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
        Response response = null;
        String[] apps = new String[0];
        try {
            response = HTTPClientUtil.doGetRequest(String.format(SERVICE_ENDPOINT,
                    node.getHttpInterface().getHost(), node.getHttpInterface().getPort(), "")
            );
            if (response.code() == 200) {
                apps = new Gson().fromJson(response.body().string(), String[].class);
            }
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Error occurred while retrieving deployed Siddhi apps from " + node, e);
            }
        } finally {
            if (response != null) {
                response.close();
            }
        }
        return Arrays.asList(apps);
    }
}
