/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.streaming.integrator.common;

/**
 * Describes a listener which listens to events related to Siddhi app deployment.
 */
public interface SiddhiAppDeploymentListener {
    /**
     * Gets notified before a Siddhi app's deployment.
     * This differs from {@link #onDeploy(String, String)} because, in cases where a Siddhi app has
     * missing extensions, this method will be called, but the latter one will not.
     *
     * @param siddhiAppName Name of the Siddhi app which is going to be deployed.
     * @param siddhiAppBody Body of the Siddhi app which is going to be deployed.
     */
    void beforeDeploy(String siddhiAppName, String siddhiAppBody);

    /**
     * Gets notified about a Siddhi app's deployment.
     *
     * @param siddhiAppName Name of the deployed Siddhi app.
     * @param siddhiAppBody Body of the deployed Siddhi app.
     */
    void onDeploy(String siddhiAppName, String siddhiAppBody);

    /**
     * Gets notified about a Siddhi app's deletion.
     *
     * @param siddhiAppName Name of the deleted Siddhi app.
     */
    void onDelete(String siddhiAppName);
}
