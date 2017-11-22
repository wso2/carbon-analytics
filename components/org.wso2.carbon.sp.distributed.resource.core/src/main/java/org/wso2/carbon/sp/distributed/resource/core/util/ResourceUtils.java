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

package org.wso2.carbon.sp.distributed.resource.core.util;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class ResourceUtils {
    private static final Logger LOG = LoggerFactory.getLogger(ResourceUtils.class);

    /**
     * Clean the Siddhi apps deployment directory.
     */
    public static void cleanSiddhiAppsDirectory() {
        String directoryPath = org.wso2.carbon.utils.Utils.getRuntimePath() + File.separator +
                ResourceConstants.SIDDHI_APP_DEPLOYMENT_DIRECTORY + File.separator +
                ResourceConstants.SIDDHI_APP_FILES_DIRECTORY;
        try {
            FileUtils.cleanDirectory(new File(directoryPath));
            LOG.info("Successfully cleaned the Siddhi apps directory.");
        } catch (IOException e) {
            LOG.error("Error occurred while cleaning the Siddhi apps directory.", e);
        }
    }
}
