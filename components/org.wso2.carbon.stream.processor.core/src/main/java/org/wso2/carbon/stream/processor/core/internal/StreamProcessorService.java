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
import org.wso2.carbon.stream.processor.core.internal.exception.SiddhiAppConfigurationException;
import org.wso2.carbon.stream.processor.core.internal.exception.SiddhiAppDeploymentException;
import org.wso2.carbon.stream.processor.core.internal.util.SiddhiAppFilesystemInvoker;
import org.wso2.carbon.stream.processor.core.internal.util.SiddhiAppProcessorConstants;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.query.api.ExecutionPlan;
import org.wso2.siddhi.query.api.annotation.Element;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;
import org.wso2.siddhi.query.api.util.AnnotationHelper;
import org.wso2.siddhi.query.compiler.SiddhiCompiler;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Class which manage Siddhi Apps.
 */
public class StreamProcessorService {

    private Map<String, ExecutionPlanRuntime> siddhiAppRuntimeMap = new ConcurrentHashMap<>();
    private Map<String, Map<String, InputHandler>> siddhiAppSpecificInputHandlerMap = new ConcurrentHashMap<>();
    private Map<String, SiddhiAppConfiguration> siddhiAppConfigurationMap = new ConcurrentHashMap<>();
    private Map<String, String> siddhiAppFileMap = new ConcurrentHashMap<>();
    private static final Logger log = LoggerFactory.getLogger(StreamProcessorService.class);

    public void deploySiddhiApp(String siddhiApp, String siddhiAppFileName) {
        ExecutionPlan parsedExecutionPlan = SiddhiCompiler.parse(siddhiApp);
        Element nameAnnotation = AnnotationHelper.getAnnotationElement(SiddhiAppProcessorConstants.ANNOTATION_NAME_NAME,
                null, parsedExecutionPlan.getAnnotations());

        if (nameAnnotation == null || nameAnnotation.getValue().isEmpty()) {
            throw new ExecutionPlanValidationException("Siddhi App name must be provided as @Plan:name('name').");
        }
        String siddhiAppName = nameAnnotation.getValue();

        if (!siddhiAppRuntimeMap.containsKey(siddhiAppName)) {
            SiddhiManager siddhiManager = StreamProcessorDataHolder.getSiddhiManager();
            SiddhiAppConfiguration siddhiAppConfiguration = new SiddhiAppConfiguration();
            siddhiAppConfiguration.setName(siddhiAppName);
            siddhiAppConfiguration.setSiddhiApp(siddhiApp);
            siddhiAppConfigurationMap.put(siddhiAppName, siddhiAppConfiguration);
            ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(siddhiApp);

            if (executionPlanRuntime != null) {
                Set<String> streamNames = executionPlanRuntime.getStreamDefinitionMap().keySet();
                Map<String, InputHandler> inputHandlerMap =
                        new ConcurrentHashMap<String, InputHandler>(streamNames.size());
                for (String streamName : streamNames) {
                    inputHandlerMap.put(streamName, executionPlanRuntime.getInputHandler(streamName));
                }

                siddhiAppSpecificInputHandlerMap.put(siddhiAppName, inputHandlerMap);
                siddhiAppRuntimeMap.put(siddhiAppName, executionPlanRuntime);
                if (siddhiAppFileName != null) {
                    siddhiAppFileMap.put(siddhiAppFileName, siddhiAppName);
                } else {
                    siddhiAppFileMap.put(siddhiAppName, siddhiAppName);
                }

                executionPlanRuntime.start();
                log.info("Siddhi App " + siddhiAppName + " deployed successfully.");
            }
        }

    }

    public void undeployExecutionPlan(String siddhiAppFileName) {

        if (siddhiAppFileMap.containsKey(siddhiAppFileName)) {
            String siddhiAppName = siddhiAppFileMap.get(siddhiAppFileName);
            siddhiAppFileMap.remove(siddhiAppFileName);

            if (siddhiAppRuntimeMap.containsKey(siddhiAppName)) {
                siddhiAppRuntimeMap.get(siddhiAppName).shutdown();
                siddhiAppRuntimeMap.remove(siddhiAppName);
            }

            if (siddhiAppConfigurationMap.containsKey(siddhiAppName)) {
                siddhiAppConfigurationMap.remove(siddhiAppName);
            }

            if (siddhiAppSpecificInputHandlerMap.containsKey(siddhiAppName)) {
                siddhiAppSpecificInputHandlerMap.remove(siddhiAppName);
            }

            log.info("Siddhi App " + siddhiAppName + " undeployed successfully.");
        }
    }

    public boolean delete(String siddhiAppFileName) throws SiddhiAppConfigurationException,
            SiddhiAppDeploymentException {

        if (siddhiAppFileMap.containsValue(siddhiAppFileName)) {
            for (Map.Entry<String, String> entry : siddhiAppFileMap.entrySet()) {
                if (entry.getValue().equalsIgnoreCase(siddhiAppFileName)) {
                    SiddhiAppFilesystemInvoker.delete(entry.getKey());
                }
            }
            return true;
        }
        return false;
    }

    public boolean save(String siddhiApp, boolean isUpdate) throws SiddhiAppConfigurationException,
            SiddhiAppDeploymentException {

        String siddhiAppName = "";
        try {
            ExecutionPlan parsedExecutionPlan = SiddhiCompiler.parse(siddhiApp);
            Element nameAnnotation = AnnotationHelper.
                    getAnnotationElement(SiddhiAppProcessorConstants.ANNOTATION_NAME_NAME,
                            null, parsedExecutionPlan.getAnnotations());

            if (nameAnnotation == null || nameAnnotation.getValue().isEmpty()) {
                throw new SiddhiAppConfigurationException("Siddhi App name must " +
                        "be provided as @Plan:name('name').");
            }

            siddhiAppName = nameAnnotation.getValue();
            if (isUpdate || !siddhiAppRuntimeMap.containsKey(siddhiAppName)) {
                SiddhiManager siddhiManager = StreamProcessorDataHolder.getSiddhiManager();
                ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(siddhiApp);
                if (executionPlanRuntime != null) {
                    SiddhiAppFilesystemInvoker.save(siddhiApp, siddhiAppName);
                    return true;
                }
            }
        } catch (SiddhiAppDeploymentException e) {
            log.error("Exception occurred when saving Siddhi App : " + siddhiAppName, e);
            throw e;
        } catch (Exception e) {
            log.error("Exeception occurred when validating Siddhi App " + siddhiAppName, e);
            throw new SiddhiAppConfigurationException(e);
        }
        return false;
    }


    public Map<String, ExecutionPlanRuntime> getSiddhiAppRuntimeMap() {
        return siddhiAppRuntimeMap;
    }

    public Map<String, Map<String, InputHandler>> getSiddhiAppSpecificInputHandlerMap() {
        return siddhiAppSpecificInputHandlerMap;
    }

    public Map<String, SiddhiAppConfiguration> getSiddhiAppConfigurationMap() {
        return siddhiAppConfigurationMap;
    }
}
