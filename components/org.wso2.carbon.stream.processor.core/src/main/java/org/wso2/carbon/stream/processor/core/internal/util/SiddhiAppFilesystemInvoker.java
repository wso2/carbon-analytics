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
package org.wso2.carbon.stream.processor.core.internal.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.kernel.configprovider.CarbonConfigurationException;
import org.wso2.carbon.kernel.configprovider.ConfigProvider;
import org.wso2.carbon.kernel.utils.Utils;
import org.wso2.carbon.stream.processor.core.internal.StreamProcessorDataHolder;
import org.wso2.carbon.stream.processor.core.internal.exception.SiddhiAppConfigurationException;
import org.wso2.carbon.stream.processor.core.internal.exception.SiddhiAppDeploymentException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.LinkedHashMap;

public class SiddhiAppFilesystemInvoker {
    private static final Log log = LogFactory.getLog(SiddhiAppFilesystemInvoker.class);
    private static String directoryPath;

    public static boolean save(String siddhiApp, String siddhiAppName)
            throws SiddhiAppConfigurationException, SiddhiAppDeploymentException {

        SiddhiAppFilesystemInvoker.validatePath(siddhiAppName);
        String filePath = getFilePathFromFilename(siddhiAppName);
        try {
            OutputStreamWriter writer = null;
            try {
                File file = new File(filePath);
                writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
                writer.write(siddhiApp);
                log.info("Siddhi App: " + siddhiAppName + " saved in the filesystem");
            } finally {
                if (writer != null) {
                    writer.flush();
                    writer.close();
                }
            }
            return true;
        } catch (IOException e) {
            throw new SiddhiAppDeploymentException("Error while saving the Siddhi App : " + siddhiAppName, e);
        }
    }

    public static boolean delete(String siddhiAppName)
            throws SiddhiAppConfigurationException, SiddhiAppDeploymentException {
        try {
            SiddhiAppFilesystemInvoker.validatePath(siddhiAppName);
            String filePath = getFilePathFromFilename(siddhiAppName);
            File file = new File(filePath);
            if (file.exists()) {
                boolean fileDeleted = file.delete();
                if (!fileDeleted) {
                    log.error("Could not delete " + siddhiAppName);
                    return false;
                } else {
                    log.info(siddhiAppName + " is deleted from the file system");
                    return true;
                }
            }
        } catch (Exception e) {
            throw new SiddhiAppDeploymentException("Error while deleting the Siddhi App : " + siddhiAppName, e);
        }
        return false;
    }

    private static void validatePath(String fileName) throws SiddhiAppConfigurationException {
        if (fileName.contains("../") || fileName.contains("..\\")) {
            throw new SiddhiAppConfigurationException("File name contains restricted path elements. : " + fileName);
        }
    }

    private static String getFilePathFromFilename(String fileName) throws SiddhiAppDeploymentException {
        ConfigProvider configProvider = StreamProcessorDataHolder.getInstance().getConfigProvider();
        if (directoryPath == null && configProvider != null) {
            try {
                LinkedHashMap wso2ArtifactDeploymentMap = (LinkedHashMap) configProvider.
                        getConfigurationMap(SiddhiAppProcessorConstants.WSO2_ARTIFACT_DEPLOYMENT_NS);
                if (wso2ArtifactDeploymentMap != null) {
                    Object directoryPathObject = wso2ArtifactDeploymentMap.get(SiddhiAppProcessorConstants.
                            WSO2_ARTIFACT_DEPLOYMENT_REPOSITORY_LOCATION);
                    if (directoryPathObject != null) {
                        directoryPath = directoryPathObject.toString();
                    }
                }

            } catch (CarbonConfigurationException e) {
                throw new SiddhiAppDeploymentException("Exception occurred when deriving the WSO2 deployment " +
                        "directory folder path", e);
            }
        }

        if (directoryPath == null) {
            directoryPath = Utils.getCarbonHome() + File.separator + SiddhiAppProcessorConstants.
                    SIDDHI_APP_DEPLOYMENT_DIRECTORY + File.separator +
                    SiddhiAppProcessorConstants.SIDDHI_APP_FILES_DIRECTORY;
        }

        return directoryPath + File.separator + fileName + SiddhiAppProcessorConstants.SIDDHI_APP_FILE_EXTENSION;
    }
}
