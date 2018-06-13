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

package org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.generators.query;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.attributesselection.AttributesSelectionConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.QueryConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.QueryOrderByConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.QueryInputConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.join.JoinConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.patternsequence.PatternSequenceConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.windowfilterprojection.WindowFilterProjectionConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.output.QueryOutputConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.query.QueryListType;
import org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.generators.AttributesSelectionConfigGenerator;
import org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.generators.AnnotationConfigGenerator;
import org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.generators.query.input.QueryInputConfigGenerator;
import org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.generators.query.output.QueryOutputConfigGenerator;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.DesignGenerationException;
import org.wso2.carbon.siddhi.editor.core.util.designview.utilities.ConfigBuildingUtilities;
import org.wso2.siddhi.query.api.SiddhiApp;
import org.wso2.siddhi.query.api.execution.query.Query;
import org.wso2.siddhi.query.api.execution.query.input.stream.InputStream;
import org.wso2.siddhi.query.api.execution.query.output.ratelimit.OutputRate;
import org.wso2.siddhi.query.api.execution.query.output.stream.OutputStream;
import org.wso2.siddhi.query.api.execution.query.selection.OrderByAttribute;
import org.wso2.siddhi.query.api.execution.query.selection.Selector;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.Variable;
import org.wso2.siddhi.query.api.expression.constant.Constant;

import java.util.ArrayList;
import java.util.List;

/**
 * Generator to create QueryConfig from Siddhi elements
 */
public class QueryConfigGenerator {
    private String siddhiAppString;
    private SiddhiApp siddhiApp;

    public QueryConfigGenerator(String siddhiAppString, SiddhiApp siddhiApp) {
        this.siddhiAppString = siddhiAppString;
        this.siddhiApp = siddhiApp;
    }

    /**
     * Generates a QueryConfig object with the given Siddhi Query object
     * @param query                 Siddhi Query object
     * @return                      QueryConfig object
     */
    public QueryConfig generateQueryConfig(Query query)
            throws DesignGenerationException {
        QueryConfig queryConfig = new QueryConfig();

        queryConfig.setQueryInput(generateInput(query.getInputStream()));

        Selector selector = query.getSelector();
        queryConfig.setSelect(generateSelect(selector));
        queryConfig.setGroupBy(generateGroupBy(selector.getGroupByList()));
        queryConfig.setOrderBy(generateOrderBy(selector.getOrderByList()));
        queryConfig.setHaving(generateHaving(selector.getHavingExpression()));
        queryConfig.setLimit(generateLimit(selector.getLimit()));

        queryConfig.setQueryOutput(generateOutput(query.getOutputStream()));
        queryConfig.setOutputRateLimit(generateOutputRateLimit(query.getOutputRate()));
        queryConfig.setAnnotationList(
                new AnnotationConfigGenerator().generateAnnotationConfigList(query.getAnnotations()));

        return queryConfig;
    }

    /**
     * Generates QueryInputConfig object from the given Siddhi InputStream
     * @param inputStream                       Siddhi InputStream
     * @return                                  QueryInputConfig
     * @throws DesignGenerationException        Error while generating QueryInputConfig
     */
    private QueryInputConfig generateInput(InputStream inputStream) throws DesignGenerationException {
        return new QueryInputConfigGenerator(siddhiAppString, siddhiApp).generateQueryInputConfig(inputStream);
    }

    /**
     * Generates AttributesSelectionConfig from the given Siddhi Selector
     * @param selector      Siddhi Selector
     * @return              AttributesSelectionConfig
     */
    private AttributesSelectionConfig generateSelect(Selector selector) {
        return new AttributesSelectionConfigGenerator(siddhiAppString).generateAttributesSelectionConfig(selector);
    }

    /**
     * Generates QueryOutputConfig from the given Siddhi OutputStream
     * @param outputStream                      Siddhi OutputStream
     * @return                                  QueryOutputConfig
     * @throws DesignGenerationException        Error while generating QueryOutputConfig
     */
    private QueryOutputConfig generateOutput(OutputStream outputStream) throws DesignGenerationException {
        return new QueryOutputConfigGenerator(siddhiAppString).generateQueryOutputConfig(outputStream);
    }

    /**
     * Generates 'groupBy' string list from the given Siddhi Variables list
     * @param groupByList                       List of Siddhi Variables
     * @return                                  List of strings, representing the groupBy list
     * @throws DesignGenerationException        Error while generating groupBy list
     */
    private List<String> generateGroupBy(List<Variable> groupByList) throws DesignGenerationException {
        List<String> groupBy = new ArrayList<>();
        for (Variable variable : groupByList) {
            groupBy.add(ConfigBuildingUtilities.getDefinition(variable, siddhiAppString));
        }
        return groupBy;
    }

    /**
     * Generates QueryOrderByConfig list from the given Siddhi OrderByAttribute list
     * @param orderByAttributeList      Siddhi OrderByAttribute list
     * @return                          QueryOrderByConfig list
     */
    private List<QueryOrderByConfig> generateOrderBy(List<OrderByAttribute> orderByAttributeList) {
        List<QueryOrderByConfig> orderBy = new ArrayList<>();
        for (OrderByAttribute orderByAttribute : orderByAttributeList) {
            orderBy.add(new QueryOrderByConfig(
                    orderByAttribute.getVariable().getAttributeName(),
                    orderByAttribute.getOrder().name()));
        }
        return orderBy;
    }

    /**
     * Generates 'having' expression as a string, from the given Siddhi Expression
     * @param havingExpression                  Siddhi Expression
     * @return                                  'having' expression string
     * @throws DesignGenerationException        Error while generating 'having' expression string
     */
    private String generateHaving(Expression havingExpression) throws DesignGenerationException {
        if (havingExpression != null) {
            return ConfigBuildingUtilities.getDefinition(havingExpression, siddhiAppString);
        }
        return "";
    }

    /**
     * Generates string for the given Siddhi OutputRate
     * @param outputRate                        Siddhi OutputRate
     * @return                                  'outputRateLimit' string
     * @throws DesignGenerationException        Error while generating 'outputRateLimit' string
     */
    private String generateOutputRateLimit(OutputRate outputRate) throws DesignGenerationException {
        final String OUTPUT = "output";
        if (outputRate != null) {
            return ConfigBuildingUtilities.getDefinition(outputRate, siddhiAppString).split(OUTPUT)[1].trim();
        }
        return "";
    }

    /**
     * Generates the long value for the given 'limit' Siddhi Constant
     * @param limit                             Siddhi Constant
     * @return                                  Long value
     * @throws DesignGenerationException        Error while generating value of 'limit'
     */
    private long generateLimit(Constant limit) throws DesignGenerationException {
        if (limit != null) {
            return Long.parseLong(ConfigBuildingUtilities.getDefinition(limit, siddhiAppString));
        }
        return 0;
    }

    /**
     * Gets the category of the query, for adding and maintaining in relevant lists
     * @param queryConfig       QueryConfig object
     * @return                  QueryListType
     */
    public static QueryListType getQueryListType(QueryConfig queryConfig) {
        QueryInputConfig queryInputConfig = queryConfig.getQueryInput();
        if (queryInputConfig instanceof WindowFilterProjectionConfig) {
            return QueryListType.WINDOW_FILTER_PROJECTION;
        } else if (queryInputConfig instanceof JoinConfig) {
            return QueryListType.JOIN;
        } else if (queryInputConfig instanceof PatternSequenceConfig) {
            if (QueryListType.valueOf(queryInputConfig.getType()) == QueryListType.SEQUENCE) {
                // TODO add Sequences
                throw new IllegalArgumentException("Sequence Queries are not supported");
            }
            return QueryListType.valueOf(queryInputConfig.getType());
        }
        throw new IllegalArgumentException("Type of Query Input is unknown, for adding the Query");
    }
}
