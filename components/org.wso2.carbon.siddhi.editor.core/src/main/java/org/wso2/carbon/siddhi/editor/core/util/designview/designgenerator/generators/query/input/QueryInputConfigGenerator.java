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

package org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.generators.query.input;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.QueryInputConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.generators.query.input.types.JoinConfigGenerator;
import org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.generators.query.input.types.PatternConfigGenerator;
import org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.generators.query.input.types.SequenceConfigGenerator;
import org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.generators.query.input.types.WindowFilterProjectionConfigGenerator;
import org.wso2.siddhi.query.api.SiddhiApp;
import org.wso2.siddhi.query.api.execution.query.Query;
import org.wso2.siddhi.query.api.execution.query.input.stream.JoinInputStream;
import org.wso2.siddhi.query.api.execution.query.input.stream.SingleInputStream;
import org.wso2.siddhi.query.api.execution.query.input.stream.StateInputStream;

/**
 * Generator to create the input part of a Query Config
 */
public class QueryInputConfigGenerator {
    /**
     * Generates Config for Query Input, from given Siddhi Query object and the complete Siddhi app string
     * @param query                 Siddhi Query object
     * @param siddhiAppString       Complete Siddhi app string
     * @param siddhiApp             Compiled Siddhi application
     * @return                      QueryInputConfig object
     */
    public QueryInputConfig generateQueryInputConfig(Query query, String siddhiAppString, SiddhiApp siddhiApp) {
        String queryInputType = getQueryInputType(query);
        if (queryInputType.equalsIgnoreCase(QueryInputType.WINDOW_FILTER_PROJECTION.toString())) {
            return new WindowFilterProjectionConfigGenerator(query, siddhiAppString)
                    .getWindowFilterProjectionQueryConfig(query);
        } else if (queryInputType.equalsIgnoreCase(QueryInputType.JOIN.toString())) {
            return new JoinConfigGenerator(query, siddhiAppString, siddhiApp).getJoinQueryConfig();
        } else if (queryInputType.equalsIgnoreCase(QueryInputType.PATTERN.toString())) {
            return new PatternConfigGenerator(query, siddhiAppString).getPatternQueryConfig();
        } else if (queryInputType.equalsIgnoreCase(QueryInputType.SEQUENCE.toString())) {
            return new SequenceConfigGenerator(query, siddhiAppString).getSequenceQueryConfig();
            // TODO: 4/9/18 implement for sequence
        }
        throw new IllegalArgumentException("Unknown type: " + queryInputType);
    }

    /**
     * Gets the type of given Query
     * @param query     Siddhi Query object
     * @return          Type of the query as a String
     */
    private String getQueryInputType(Query query) {
        if (query.getInputStream() instanceof SingleInputStream) {
            return QueryInputType.WINDOW_FILTER_PROJECTION.toString();
        } else if (query.getInputStream() instanceof JoinInputStream) {
            return QueryInputType.JOIN.toString();
        } else if (query.getInputStream() instanceof StateInputStream) {
            return ((StateInputStream)(query.getInputStream())).getStateType().name();
        }
        throw new IllegalArgumentException("Type of query is unknown for generating query input");
    }

    /**
     * Type of the Query's input
     */
    private enum QueryInputType {
        WINDOW_FILTER_PROJECTION,
        JOIN,
        PATTERN,
        SEQUENCE
    }
}
