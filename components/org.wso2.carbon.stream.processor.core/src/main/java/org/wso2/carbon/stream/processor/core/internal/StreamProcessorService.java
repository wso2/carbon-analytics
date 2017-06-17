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

package org.wso2.carbon.stream.processor.core.internal;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.stream.processor.core.internal.exception.SiddhiAppAlreadyExistException;
import org.wso2.carbon.stream.processor.core.internal.exception.SiddhiAppConfigurationException;
import org.wso2.carbon.stream.processor.core.internal.exception.SiddhiAppDeploymentException;
import org.wso2.carbon.stream.processor.core.internal.util.SiddhiAppFilesystemInvoker;
import org.wso2.carbon.stream.processor.core.internal.util.SiddhiAppProcessorConstants;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.query.api.ExecutionPlan;
import org.wso2.siddhi.query.api.annotation.Element;
import org.wso2.siddhi.query.api.util.AnnotationHelper;
import org.wso2.siddhi.query.compiler.SiddhiCompiler;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Class which manage Siddhi Apps.
 */
public class StreamProcessorService {

    private Map<String, SiddhiAppConfiguration> deployedSiddhiAppMap = new ConcurrentHashMap<>();
    private Map<String, SiddhiAppFile> siddhiAppFileMap = new ConcurrentHashMap<>();
    private static final Logger log = LoggerFactory.getLogger(StreamProcessorService.class);

    public void deploySiddhiApp(String siddhiApp, String siddhiAppFileName) throws SiddhiAppConfigurationException,
            SiddhiAppAlreadyExistException {

        String siddhiAppName = null;
        SiddhiAppFile siddhiAppFile = new SiddhiAppFile(siddhiApp);
        SiddhiAppConfiguration siddhiAppConfiguration = new SiddhiAppConfiguration();
        siddhiAppName = getSiddhiAppName(siddhiApp);

        if(deployedSiddhiAppMap.containsKey(siddhiAppName)){
            throw new SiddhiAppAlreadyExistException("There is a Siddhi App with name " + siddhiAppName +
                    " is already exist");
        }

        siddhiAppFile.setSiddhiAppName(siddhiAppName);
        SiddhiManager siddhiManager = StreamProcessorDataHolder.getSiddhiManager();
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(siddhiApp);

        if (executionPlanRuntime != null) {
            Set<String> streamNames = executionPlanRuntime.getStreamDefinitionMap().keySet();
            Map<String, InputHandler> inputHandlerMap =
                    new ConcurrentHashMap<String, InputHandler>(streamNames.size());
            for (String streamName : streamNames) {
                inputHandlerMap.put(streamName, executionPlanRuntime.getInputHandler(streamName));
            }

            executionPlanRuntime.start();
            log.info("Siddhi App " + siddhiAppName + " deployed successfully.");

            siddhiAppFile.setActive(true);
            siddhiAppConfiguration = new SiddhiAppConfiguration(inputHandlerMap, executionPlanRuntime);
            siddhiAppFileMap.put(siddhiAppFileName, siddhiAppFile);
            deployedSiddhiAppMap.put(siddhiAppName, siddhiAppConfiguration);
        }
    }

    public void undeployExecutionPlan(String siddhiAppFileName) {

        if (siddhiAppFileMap.containsKey(siddhiAppFileName)) {
            SiddhiAppFile siddhiAppFile = siddhiAppFileMap.get(siddhiAppFileName);
            if (siddhiAppFile != null) {
                String siddhiAppName = siddhiAppFile.getSiddhiAppName();
                if (siddhiAppName != null) {
                    if (deployedSiddhiAppMap.containsKey(siddhiAppName)) {
                        ExecutionPlanRuntime executionPlanRuntime = deployedSiddhiAppMap.
                                get(siddhiAppName).getExecutionPlanRuntime();
                        executionPlanRuntime.shutdown();
                        deployedSiddhiAppMap.remove(siddhiAppName);
                    }
                }
            }
            siddhiAppFileMap.remove(siddhiAppFileName);
            log.info("Siddhi App File " + siddhiAppFileName + " undeployed successfully.");
        }
    }

    public boolean delete(String siddhiAppFileName) throws SiddhiAppConfigurationException,
            SiddhiAppDeploymentException {

        if (!siddhiAppFileName.endsWith(SiddhiAppProcessorConstants.SIDDHIQL_FILE_EXTENSION)) {
            siddhiAppFileName += SiddhiAppProcessorConstants.SIDDHIQL_FILE_EXTENSION;
        }

        if (siddhiAppFileMap.containsKey(siddhiAppFileName)) {
            SiddhiAppFilesystemInvoker.delete(siddhiAppFileName);
            return true;
        }
        return false;
    }

    public String validateAndSave(String siddhiApp, boolean isUpdate) throws SiddhiAppConfigurationException,
            SiddhiAppDeploymentException {

        String siddhiAppName = "";
        try {
            siddhiAppName = getSiddhiAppName(siddhiApp);
            if (isUpdate || !deployedSiddhiAppMap.containsKey(siddhiAppName)) {
                SiddhiManager siddhiManager = StreamProcessorDataHolder.getSiddhiManager();
                ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(siddhiApp);
                if (executionPlanRuntime != null) {
                    SiddhiAppFilesystemInvoker.save(siddhiApp, siddhiAppName);
                    return siddhiAppName;
                }
            }
        } catch (SiddhiAppDeploymentException e) {
            log.error("Exception occurred when saving Siddhi App : " + siddhiAppName, e);
            throw e;
        } catch (Exception e) {
            log.error("Exception occurred when validating Siddhi App " + siddhiAppName, e);
            throw new SiddhiAppConfigurationException(e);
        }
        return null;
    }

    public String getSiddhiAppName(String siddhiApp) throws SiddhiAppConfigurationException {
        try {
            ExecutionPlan parsedExecutionPlan = SiddhiCompiler.parse(siddhiApp);
            Element nameAnnotation = AnnotationHelper.
                    getAnnotationElement(SiddhiAppProcessorConstants.ANNOTATION_NAME_NAME,
                            null, parsedExecutionPlan.getAnnotations());

            if (nameAnnotation == null || nameAnnotation.getValue().isEmpty()) {
                throw new SiddhiAppConfigurationException("Siddhi App name must " +
                        "be provided as @Plan:name('name').");
            }

            return nameAnnotation.getValue();

        } catch (Exception e) {
            throw new SiddhiAppConfigurationException("Exception occurred when retrieving Siddhi App Name ", e);
        }
    }

    public boolean isExists(String siddhiApp) throws SiddhiAppConfigurationException {
        return deployedSiddhiAppMap.containsKey(getSiddhiAppName(siddhiApp));
    }

    public void addSiddhiAppFile(String siddhiAppFileName, SiddhiAppFile siddhiAppFile){
        siddhiAppFileMap.put(siddhiAppFileName, siddhiAppFile);
    }

    public Map<String, SiddhiAppConfiguration> getDeployedSiddhiAppMap() {
        return deployedSiddhiAppMap;
    }

    public Map<String, SiddhiAppFile> getSiddhiAppFileMap() {
        return siddhiAppFileMap;
    }
}
