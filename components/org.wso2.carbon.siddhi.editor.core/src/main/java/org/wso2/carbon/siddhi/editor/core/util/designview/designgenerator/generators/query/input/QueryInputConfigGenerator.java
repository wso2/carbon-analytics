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
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.query.QueryInputType;
import org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.generators.query.input.types.*;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.DesignGenerationException;
import io.siddhi.query.api.SiddhiApp;
import io.siddhi.query.api.execution.query.input.stream.InputStream;
import io.siddhi.query.api.execution.query.input.stream.JoinInputStream;
import io.siddhi.query.api.execution.query.input.stream.SingleInputStream;
import io.siddhi.query.api.execution.query.input.stream.StateInputStream;

/**
 * Generator to create QueryInputConfig from Siddhi elements
 */
public class QueryInputConfigGenerator {
    private String siddhiAppString;
    private SiddhiApp siddhiApp;

    public QueryInputConfigGenerator(String siddhiAppString, SiddhiApp siddhiApp) {
        this.siddhiAppString = siddhiAppString;
        this.siddhiApp = siddhiApp;
    }

    /**
     * Generates Config for Query Input, from given Siddhi Query object and the complete Siddhi app string
     * @param queryInputStream                  Siddhi Query InputStream object
     * @return                                  QueryInputConfig object
     * @throws DesignGenerationException        Error while generating config
     */
    public QueryInputConfig generateQueryInputConfig(InputStream queryInputStream) throws DesignGenerationException {
        QueryInputType queryInputType = getQueryInputType(queryInputStream);
        if (queryInputType == QueryInputType.WINDOW_FILTER_PROJECTION) {
            return new WindowFilterProjectionConfigGenerator(siddhiAppString)
                    .generateWindowFilterProjectionConfig(queryInputStream);
        } else if (queryInputType == QueryInputType.JOIN) {
            return new JoinConfigGenerator().getJoinQueryConfig(queryInputStream, siddhiApp, siddhiAppString);
        } else if (queryInputType == QueryInputType.PATTERN || queryInputType == QueryInputType.SEQUENCE) {
            return new PatternSequenceConfigGenerator(siddhiAppString, queryInputType)
                    .generatePatternSequenceConfig(queryInputStream);
        }
        throw new DesignGenerationException("Unable to generate QueryInputConfig for type: " + queryInputType);
    }

    /**
     * Gets the type of the Query's Input, with the given Siddhi InputStream object
     * @param queryInputStream      Siddhi InputStream object, which contains data about the Query's input part
     * @return                      QueryInputType
     */
    private QueryInputType getQueryInputType(InputStream queryInputStream) {
        if (queryInputStream instanceof SingleInputStream) {
            return QueryInputType.WINDOW_FILTER_PROJECTION;
        } else if (queryInputStream instanceof JoinInputStream) {
            return QueryInputType.JOIN;
        } else if (queryInputStream instanceof StateInputStream) {
            // PATTERN or SEQUENCE
            return QueryInputType.valueOf(((StateInputStream) queryInputStream).getStateType().name());
        }
        throw new IllegalArgumentException("Type of query is unknown for generating query input");
    }
}
