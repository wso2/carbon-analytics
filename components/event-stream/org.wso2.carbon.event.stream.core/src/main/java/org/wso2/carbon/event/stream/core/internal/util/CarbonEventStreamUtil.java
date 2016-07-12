/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.event.stream.core.internal.util;

import org.wso2.carbon.event.stream.core.exception.EventStreamConfigurationException;
import org.wso2.carbon.utils.CarbonUtils;

import java.nio.file.Path;
import java.nio.file.Paths;

public class CarbonEventStreamUtil {
    /**
     * Validate the given file path is in the parent directory itself.
     *
     * @param parentDirectory
     * @param filePath
     */
    public static void validatePath(String parentDirectory, String filePath) throws EventStreamConfigurationException {
        Path parentPath = Paths.get(parentDirectory);
        Path subPath = Paths.get(filePath).normalize();
        if (!subPath.startsWith(parentPath)) {
            // If not valid, test for tmp/carbonapps directory
            parentPath = Paths.get(CarbonUtils.getTmpDir(), "carbonapps");
            if (!subPath.startsWith(parentPath)) {
                throw new EventStreamConfigurationException("File path is invalid: " + filePath);
            }
        }
    }
}
