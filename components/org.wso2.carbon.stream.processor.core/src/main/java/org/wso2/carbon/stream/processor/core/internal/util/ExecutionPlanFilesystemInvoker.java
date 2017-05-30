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
import org.wso2.carbon.kernel.utils.Utils;
import org.wso2.carbon.stream.processor.core.internal.exception.ExecutionPlanConfigurationException;


import java.io.*;

public class ExecutionPlanFilesystemInvoker {
    private static final Log log = LogFactory.getLog(ExecutionPlanFilesystemInvoker.class);

    public static boolean save(String executionPlan, String executionPlanName)
            throws ExecutionPlanConfigurationException {

        ExecutionPlanFilesystemInvoker.validatePath(executionPlanName);
        String filePath = Utils.getCarbonHome().toString() + File.separator + EventProcessorConstants.
                SIDDHIQL_DEPLOYMENT_DIRECTORY + File.separator + EventProcessorConstants.SIDDHIQL_FILES_DIRECTORY +
                File.separator + executionPlanName + EventProcessorConstants.SIDDHIQL_FILE_EXTENSION;
        try {
            OutputStreamWriter writer = null;
            try {
                File file = new File(filePath);
                writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
                writer.write(executionPlan);
                log.info("Execution plan: " + executionPlanName + " saved in the filesystem");
            } finally {
                if (writer != null) {
                    writer.flush();
                    writer.close();
                }
            }
            return true;
        } catch (IOException e) {
            log.error("Error while saving " + executionPlanName, e);
            throw new ExecutionPlanConfigurationException("Error while saving ", e);
        }
    }

    public static boolean delete(String fileName)
            throws ExecutionPlanConfigurationException {
        try {
            ExecutionPlanFilesystemInvoker.validatePath(fileName);
            String filePath = getFilePathFromFilename(fileName);
            File file = new File(filePath);
            if (file.exists()) {
                boolean fileDeleted = file.delete();
                if (!fileDeleted) {
                    log.error("Could not delete " + fileName);
                    return false;
                } else {
                    log.info(fileName + " is deleted from the file system");
                    return true;
                }
            }
        } catch (Exception e) {
            throw new ExecutionPlanConfigurationException("Error while deleting the execution plan file ", e);
        }
        return false;
    }

    public static String readExecutionPlanConfigFile(String fileName)
            throws ExecutionPlanConfigurationException {
        BufferedReader bufferedReader = null;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            ExecutionPlanFilesystemInvoker.validatePath(fileName);
            String filePath = getFilePathFromFilename(fileName);
            bufferedReader = new BufferedReader(new FileReader(filePath));
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
        } catch (FileNotFoundException e) {
            throw new ExecutionPlanConfigurationException("Execution plan file not found, " + fileName + "," +
                    e.getMessage(), e);
        } catch (IOException e) {
            throw new ExecutionPlanConfigurationException("Cannot read the execution plan file, " + fileName + "," +
                    e.getMessage(), e);
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e) {
                log.error("Error occurred when reading the file, " + fileName + "," + e.getMessage(), e);
            }
        }
        return stringBuilder.toString().trim();
    }

    private static void validatePath(String fileName) throws ExecutionPlanConfigurationException {
        if (fileName.contains("../") || fileName.contains("..\\")) {
            throw new ExecutionPlanConfigurationException("File name contains restricted path elements. " + fileName);
        }
    }

    private static String getFilePathFromFilename(String fileName) {
        return Utils.getCarbonHome() + File.separator + EventProcessorConstants.
                SIDDHIQL_DEPLOYMENT_DIRECTORY + File.separator + EventProcessorConstants.SIDDHIQL_FILES_DIRECTORY +
                File.separator + fileName;
    }
}
