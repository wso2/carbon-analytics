/*
 * Copyright (c)  2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.siddhi.distribution.editor.core.internal;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.deployment.engine.exception.CarbonDeploymentException;

/**
 * WorkspaceDeployer is responsible for all editor workspace related deployment tasks.
 */
public class WorkspaceDeployer {

    private static final Logger log = LoggerFactory.getLogger(WorkspaceDeployer.class);

    static void deployConfigFile(String siddhiAppFileName, String siddhiApp) throws Exception {
        EditorDataHolder.getDebugProcessorService().deploy(FilenameUtils.getBaseName(siddhiAppFileName), siddhiApp);
        log.info("Siddhi App " + siddhiAppFileName + " successfully deployed.");
    }

    static void unDeployConfigFile(String key) throws Exception {
        try {
            String siddhiAppName = FilenameUtils.getBaseName((String) key);
            if (EditorDataHolder.getSiddhiAppMap().containsKey(siddhiAppName)) {
                EditorDataHolder.getDebugProcessorService().undeploy(siddhiAppName);
            }
        } catch (Exception e) {
            throw new CarbonDeploymentException(e.getMessage(), e);
        }
    }
}
