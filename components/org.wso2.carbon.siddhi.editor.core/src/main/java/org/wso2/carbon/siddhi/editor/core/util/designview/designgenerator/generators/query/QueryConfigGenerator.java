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
import org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.generators.CodeSegmentsPreserver;
import org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.generators.query.input.QueryInputConfigGenerator;
import org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.generators.query.output.QueryOutputConfigGenerator;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.DesignGenerationException;
import org.wso2.carbon.siddhi.editor.core.util.designview.utilities.ConfigBuildingUtilities;
import io.siddhi.query.api.SiddhiApp;
import io.siddhi.query.api.annotation.Annotation;
import io.siddhi.query.api.execution.query.Query;
import io.siddhi.query.api.execution.query.input.stream.InputStream;
import io.siddhi.query.api.execution.query.output.ratelimit.OutputRate;
import io.siddhi.query.api.execution.query.output.stream.OutputStream;
import io.siddhi.query.api.execution.query.selection.OrderByAttribute;
import io.siddhi.query.api.execution.query.selection.Selector;
import io.siddhi.query.api.expression.Expression;
import io.siddhi.query.api.expression.Variable;
import io.siddhi.query.api.expression.constant.Constant;

import java.util.ArrayList;
import java.util.List;

/**
 * Generator to create QueryConfig from Siddhi elements
 */
public class QueryConfigGenerator extends CodeSegmentsPreserver {
    private static final String DEFAULT_QUERY_NAME = "query";
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
    public QueryConfig generateQueryConfig(Query query, int queryCounter)
            throws DesignGenerationException {
        QueryConfig queryConfig = new QueryConfig();

        queryConfig.setQueryInput(generateInput(query.getInputStream()));

        Selector selector = query.getSelector();
        queryConfig.setSelect(generateSelect(selector));
        queryConfig.setGroupBy(generateGroupBy(selector.getGroupByList()));
        queryConfig.setOrderBy(generateOrderBy(selector.getOrderByList()));
        queryConfig.setHaving(generateHaving(selector.getHavingExpression()));
        queryConfig.setLimit(generateLimit(selector.getLimit()));
        queryConfig.setOffset(generateOffset(selector.getOffset()));

        queryConfig.setQueryOutput(generateOutput(query.getOutputStream()));
        queryConfig.setOutputRateLimit(generateOutputRateLimit(query.getOutputRate()));
        queryConfig.setAnnotationListObjects(removeInfoAnnotation(query.getAnnotations()));
        queryConfig.setAnnotationList(generateAnnotationList(query.getAnnotations()));
        queryConfig.setQueryName(generateQueryName(query.getAnnotations(), queryCounter));
        preserveAndBindCodeSegment(query, queryConfig);
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
        AttributesSelectionConfigGenerator attributesSelectionConfigGenerator =
                new AttributesSelectionConfigGenerator(siddhiAppString);
        preserveCodeSegmentsOf(attributesSelectionConfigGenerator);
        return attributesSelectionConfigGenerator.generateAttributesSelectionConfig(selector);
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
            String value = "";
            if(orderByAttribute.getVariable().getStreamId() != null) {
                value = orderByAttribute.getVariable().getStreamId() + ".";
            }
            value += orderByAttribute.getVariable()
                    .getAttributeName();
            orderBy.add(new QueryOrderByConfig(
                    value,
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
     * Generates the long value for the given 'offset' Siddhi Constant
     * @param offset                             Siddhi Constant
     * @return                                  Long value
     * @throws DesignGenerationException        Error while generating value of 'offset'
     */
    private long generateOffset(Constant offset) throws DesignGenerationException {
        if (offset != null) {
            return Long.parseLong(ConfigBuildingUtilities.getDefinition(offset, siddhiAppString));
        }
        return 0;
    }


    /**
     * Generates a string list of Siddhi Annotations, from the given list of Siddhi Annotations.
     * Ignores the 'info' annotation
     * @param queryAnnotations      List of Siddhi Annotations of a query
     * @return                      List of strings, each representing a Siddhi Query Annotation
     */
    private List<String> generateAnnotationList(List<Annotation> queryAnnotations) {
        List<String> annotationList = new ArrayList<>();
        AnnotationConfigGenerator annotationConfigGenerator = new AnnotationConfigGenerator();
        for (Annotation queryAnnotation : queryAnnotations) {
            if (!queryAnnotation.getName().equalsIgnoreCase("info")) {
                annotationList.add(annotationConfigGenerator.generateAnnotationConfig(queryAnnotation));
            }
        }
        preserveCodeSegmentsOf(annotationConfigGenerator);
        return annotationList;
    }

    /**
     * Removes the @info from the query annotations
     * @param queryAnnotations List of Siddhi Annotations of query
     * @return annotationList  List of Siddhi Annotation of query without @info
     */
    private List<Annotation> removeInfoAnnotation(List<Annotation> queryAnnotations) {
        List<Annotation> annotationList = new ArrayList<>();
        for (Annotation queryAnnotation : queryAnnotations) {
            if (!queryAnnotation.getName().equalsIgnoreCase("info")) {
                annotationList.add(queryAnnotation);
            }
        }
        return annotationList;
    }

    /**
     * Extracts the query name from the annotation list, or returns the default query name
     * @param annotations           Query annotation list
     * @param queryNumber           to generate unique query names
     * @return query name           name of the query
     */
    private String generateQueryName(List<Annotation> annotations , int queryNumber) {
        String queryName = "";
        boolean isQueryName = false;
        for (Annotation annotation : annotations) {
            if (annotation.getName().equalsIgnoreCase("info")) {
                preserveCodeSegment(annotation);
                 queryName = annotation.getElement("name");
                 isQueryName = true;
            }
        }
        if (!isQueryName) {
            queryName = DEFAULT_QUERY_NAME + queryNumber;
        }
        return queryName;
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
            return QueryListType.valueOf(queryInputConfig.getType());
        }
        throw new IllegalArgumentException("Type of Query Input is unknown, for adding the Query");
    }
}