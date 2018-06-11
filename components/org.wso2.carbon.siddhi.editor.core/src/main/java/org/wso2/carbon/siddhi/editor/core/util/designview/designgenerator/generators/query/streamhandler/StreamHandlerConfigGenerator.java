/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.generators.query.streamhandler;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.streamhandler.FilterConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.streamhandler.FunctionWindowConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.streamhandler.FunctionWindowValue;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.streamhandler.StreamHandlerConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.query.StreamHandlerType;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.DesignGenerationException;
import org.wso2.carbon.siddhi.editor.core.util.designview.utilities.ConfigBuildingUtilities;
import org.wso2.siddhi.query.api.execution.query.input.handler.Filter;
import org.wso2.siddhi.query.api.execution.query.input.handler.StreamFunction;
import org.wso2.siddhi.query.api.execution.query.input.handler.StreamHandler;
import org.wso2.siddhi.query.api.execution.query.input.handler.Window;
import org.wso2.siddhi.query.api.expression.Expression;

import java.util.ArrayList;
import java.util.List;

/**
 * Generator to create StreamHandlerConfig from Siddhi elements
 */
public class StreamHandlerConfigGenerator {
    private String siddhiAppString;

    public StreamHandlerConfigGenerator(String siddhiAppString) {
        this.siddhiAppString = siddhiAppString;
    }

    /**
     * Generates StreamHandlerConfig from the given Siddhi StreamHandler
     * @param streamHandler                     Siddhi StreamHandler object
     * @return                                  StreamHandlerConfig object
     * @throws DesignGenerationException        Error while generating config
     */
    public StreamHandlerConfig generateStreamHandlerConfig(StreamHandler streamHandler)
            throws DesignGenerationException {
        if (streamHandler instanceof Filter) {
            return generateFilterConfig((Filter) streamHandler);
        } else if (streamHandler instanceof StreamFunction) {
            return generateFunction((StreamFunction) streamHandler);
        } else if (streamHandler instanceof Window) {
            return generateWindow((Window) streamHandler);
        }
        throw new DesignGenerationException("Unknown type of StreamHandler for generating config");
    }

    /**
     * Generates a list of StreamHandlerConfigs, from the given list of Siddhi StreamHandlers
     * @param streamHandlers                    List of Siddhi StreamHandler objects
     * @return                                  List of StreamHandlerConfig objects
     * @throws DesignGenerationException        Error while generating a StreamHandlerConfig
     */
    public List<StreamHandlerConfig> generateStreamHandlerConfigList(List<StreamHandler> streamHandlers)
            throws DesignGenerationException {
        List<StreamHandlerConfig> streamHandlerConfigList = new ArrayList<>();
        for (StreamHandler streamHandler : streamHandlers) {
            streamHandlerConfigList.add(generateStreamHandlerConfig(streamHandler));
        }
        return streamHandlerConfigList;
    }

    /**
     * Generates FilterConfig from the given Siddhi Filter
     * @param filter                            Siddhi Filter object
     * @return                                  FilterConfig object
     * @throws DesignGenerationException        Error while generating filter
     */
    private FilterConfig generateFilterConfig(Filter filter) throws DesignGenerationException {
        String filterDefinition = ConfigBuildingUtilities.getDefinition(filter, siddhiAppString);
        return new FilterConfig(filterDefinition.substring(1, filterDefinition.length() - 1).trim());
    }

    /**
     * Generates Function config from the given Siddhi StreamFunction
     * @param streamFunction                    Siddhi StreamFunction object
     * @return                                  FunctionWindowConfig object
     * @throws DesignGenerationException        Error while generating function
     */
    private FunctionWindowConfig generateFunction(StreamFunction streamFunction) throws DesignGenerationException {
        StringBuilder function = new StringBuilder();
        if (streamFunction.getNamespace() != null && !streamFunction.getNamespace().isEmpty()) {
            function.append(streamFunction.getNamespace());
            function.append(":");
        }
        function.append(streamFunction.getName());

        return new FunctionWindowConfig(
                StreamHandlerType.FUNCTION.toString(),
                new FunctionWindowValue(
                        function.toString(),
                        generateParameters(streamFunction.getParameters())));
    }

    /**
     * Generates Window config from the given Siddhi Window
     * @param window                            Siddhi Window object
     * @return                                  FunctionWindowConfig object
     * @throws DesignGenerationException        Error while generating window
     */
    private FunctionWindowConfig generateWindow(Window window) throws DesignGenerationException {
        StringBuilder function = new StringBuilder();
        if (window.getNamespace() != null && !window.getNamespace().isEmpty()) {
            function.append(window.getNamespace());
            function.append(":");
        }
        function.append(window.getName());

        return new FunctionWindowConfig(
                StreamHandlerType.WINDOW.toString(),
                new FunctionWindowValue(
                        function.toString(),
                        generateParameters(window.getParameters())));
    }

    /**
     * Generates a string list of parameters, from the given list of Siddhi Expressions
     * @param parameters                        Siddhi Expressions
     * @return                                  String list of parameters
     * @throws DesignGenerationException        Error while generating parameters
     */
    private List<String> generateParameters(Expression[] parameters) throws DesignGenerationException {
        List<String> parameterStrings = new ArrayList<>();
        for (Expression parameter : parameters) {
            parameterStrings.add(ConfigBuildingUtilities.getDefinition(parameter, siddhiAppString));
        }
        return parameterStrings;
    }
}
