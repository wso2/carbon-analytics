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

package org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.generators.query.input.types;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.windowfilterprojection.QueryWindowConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.windowfilterprojection.WindowFilterProjectionQueryConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.utilities.ConfigBuildingUtilities;
import org.wso2.siddhi.query.api.execution.query.Query;
import org.wso2.siddhi.query.api.execution.query.input.handler.Filter;
import org.wso2.siddhi.query.api.execution.query.input.handler.StreamHandler;
import org.wso2.siddhi.query.api.execution.query.input.handler.Window;
import org.wso2.siddhi.query.api.execution.query.input.stream.BasicSingleInputStream;
import org.wso2.siddhi.query.api.execution.query.input.stream.InputStream;
import org.wso2.siddhi.query.api.execution.query.input.stream.SingleInputStream;
import org.wso2.siddhi.query.api.expression.Expression;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates WindowFilterProjectionQueryConfig with given Siddhi elements
 */
public class WindowFilterProjectionConfigGenerator {
    private String siddhiAppString;

    public WindowFilterProjectionConfigGenerator(String siddhiAppString) {
        this.siddhiAppString = siddhiAppString;
    }

    /**
     * Gets a WindowFilterProjectionQueryConfig object, from the given Siddhi Query object
     * @param queryInputStream      Siddhi Query InputStream object
     * @return                      WindowFilterProjectionQueryConfig object
     */
    public WindowFilterProjectionQueryConfig getWindowFilterProjectionQueryConfig(InputStream queryInputStream) {
        switch (getType(queryInputStream)) {
            case PROJECTION:
                return generateProjectionQueryInput(queryInputStream);
            case FILTER:
                return generateFilterQueryInput(queryInputStream);
            case WINDOW:
                return generateWindowQueryInput(queryInputStream);
            default:
                throw new IllegalArgumentException("Unknown type: " + getType(queryInputStream) +
                        " for generating Window-Filter-Projection Query Config");
        }
    }

    /**
     * Returns the type of WindowFilterProjection Config to generate, from the given Siddhi Query object
     * @param queryInputStream     Siddhi Query InputStream object
     * @return                     Type of WindowFilterProjection Query to generate
     */
    private WindowFilterProjectionQueryType getType(InputStream queryInputStream) {
        List<StreamHandler> streamHandlers = ((SingleInputStream) queryInputStream).getStreamHandlers();
        if (streamHandlers.isEmpty()) {
            return WindowFilterProjectionQueryType.PROJECTION;
        } else {
            for (StreamHandler streamHandler : streamHandlers) {
                if (streamHandler instanceof Window) {
                    return WindowFilterProjectionQueryType.WINDOW;
                }
            }
            return WindowFilterProjectionQueryType.FILTER;
        }
    }

    /**
     * Generates a QueryInputConfig of type Projection, with the given Siddhi Query
     * @param queryInputStream      Siddhi Query InputStream object
     * @return                      WindowFilterProjectionQueryConfig object,
     *                              with the configuration of a Projection query
     */
    private WindowFilterProjectionQueryConfig generateProjectionQueryInput(InputStream queryInputStream) {
        return new WindowFilterProjectionQueryConfig(
                WindowFilterProjectionQueryType.PROJECTION.toString(),
                queryInputStream.getUniqueStreamIds().get(0),
                "",
                null);
    }

    /**
     * Generates a QueryInputConfig of type Filter, with the given Siddhi Query
     * @param queryInputStream      Siddhi Query InputStream object
     * @return                      WindowFilterProjectionQueryConfig object, with the configuration of a Filter query
     */
    private WindowFilterProjectionQueryConfig generateFilterQueryInput(InputStream queryInputStream) {
        String from = queryInputStream.getUniqueStreamIds().get(0);
        // Filter query will have just one StreamHandler, that's the Filter
        Filter filter = (Filter) ((BasicSingleInputStream) queryInputStream).getStreamHandlers().get(0);
        String filterDefinition = ConfigBuildingUtilities.getDefinition(filter, siddhiAppString);
        return new WindowFilterProjectionQueryConfig(
                WindowFilterProjectionQueryType.FILTER.toString(),
                from,
                filterDefinition.substring(1, filterDefinition.length() - 1).trim(),
                null);
    }

    /**
     * Generates a QueryInputConfig of type Window, with the given Siddhi Query
     * @param queryInputStream      Siddhi Query InputStream object
     * @return                      WindowFilterProjectionQueryConfig object, with the configuration of a Window query
     */
    private WindowFilterProjectionQueryConfig generateWindowQueryInput(InputStream queryInputStream) {
        String mainFilter = null;
        String windowFilter = null;
        String function = null;
        List<String> parameters = new ArrayList<>();

        for (StreamHandler streamHandler : ((SingleInputStream) queryInputStream).getStreamHandlers()) {
            if (streamHandler instanceof Filter) {
                String definition;
                // First Filter will be Query's, and the next one will be window's
                if (mainFilter == null) {
                    definition = ConfigBuildingUtilities.getDefinition(streamHandler, siddhiAppString);
                    mainFilter = definition.substring(1, definition.length() - 1).trim();
                } else {
                    definition = ConfigBuildingUtilities.getDefinition(streamHandler, siddhiAppString);
                    windowFilter = definition.substring(1, definition.length() - 1).trim();
                }
            } else if (streamHandler instanceof Window) {
                for (Expression expression : streamHandler.getParameters()) {
                    parameters.add(ConfigBuildingUtilities.getDefinition(expression, siddhiAppString));
                }
                function = ((Window)streamHandler).getName();
            }
        }

        return new WindowFilterProjectionQueryConfig(
                WindowFilterProjectionQueryType.WINDOW.toString(),
                queryInputStream.getUniqueStreamIds().get(0),
                mainFilter,
                new QueryWindowConfig(function, parameters, windowFilter));
    }

    /**
     * Specific Type of the WindowFilterProjection Query
     */
    private enum WindowFilterProjectionQueryType {
        PROJECTION,
        FILTER,
        WINDOW
    }
}
