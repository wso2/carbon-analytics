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

import org.apache.log4j.Logger;
import org.wso2.carbon.das.jobmanager.core.model.ResourceNode;
import org.wso2.carbon.das.jobmanager.core.util.HTTPClientUtil;

import java.io.IOException;

import okhttp3.Response;

public class SiddhiAppDeployer {
    private static final Logger LOG = Logger.getLogger(SiddhiAppDeployer.class);
    private static final String SERVICE_ENDPOINT = "http://%s:%s/siddhi-apps%s";

    public static boolean deploy(ResourceNode node, String siddhiApp) {
        Response response = null;
        try {
            response = HTTPClientUtil.doPostRequest(String.format(SERVICE_ENDPOINT,
                    node.getHttpInterface().getHost(), node.getHttpInterface().getPort(), ""),
                    siddhiApp
            );
            return response.code() == 201;
        } catch (IOException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Error occurred while deploying Siddhi app to " + node, e);
            }
            return false;
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

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
}
