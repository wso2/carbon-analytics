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
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.output.QueryOutputConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.generators.attributesselection.AttributesSelectionConfigGenerator;
import org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.generators.query.input.QueryInputConfigGenerator;
import org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.generators.query.output.QueryOutputConfigGenerator;
import org.wso2.carbon.siddhi.editor.core.util.designview.utilities.ConfigBuildingUtilities;
import org.wso2.siddhi.query.api.SiddhiApp;
import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.execution.query.Query;
import org.wso2.siddhi.query.api.execution.query.selection.OrderByAttribute;
import org.wso2.siddhi.query.api.expression.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Generates QueryConfig object out of given Siddhi elements
 */
public class QueryConfigGenerator {
    /**
     * Generates a QueryConfig object with the given Siddhi Query object
     * @param query                 Siddhi Query object
     * @param siddhiAppString       Complete Siddhi app string
     * @param siddhiApp             Compiled Siddhi app
     * @return                      QueryConfig object
     */
    public QueryConfig generateQueryConfig(Query query, String siddhiAppString, SiddhiApp siddhiApp) {
        // Generate Query Input
        QueryInputConfigGenerator queryInputConfigGenerator = new QueryInputConfigGenerator();
        QueryInputConfig queryInputConfig =
                queryInputConfigGenerator.generateQueryInputConfig(query.getInputStream(), siddhiAppString, siddhiApp);

        // Generate Query Select
        AttributesSelectionConfigGenerator attributesSelectionConfigGenerator = new AttributesSelectionConfigGenerator();
        AttributesSelectionConfig querySelectConfig =
                attributesSelectionConfigGenerator
                        .generateAttributesSelectionConfig(query.getSelector().getSelectionList());

        // Generate Query Output
        QueryOutputConfigGenerator queryOutputConfigGenerator = new QueryOutputConfigGenerator();
        QueryOutputConfig queryOutputConfig =
                queryOutputConfigGenerator.generateQueryOutputConfig(query.getOutputStream(), siddhiAppString);

        // Get the Id of the Query
        String queryId = null;
        for (Annotation annotation : query.getAnnotations()) {
            if (annotation.getName().equalsIgnoreCase(SiddhiQueryAnnotation.INFO.toString())) {
                queryId = annotation.getElement(SiddhiQueryAnnotation.NAME.toString());
                break;
            }
        }
        if (queryId == null) {
            // Set UUID if no Id has been given for the Query
            queryId = UUID.randomUUID().toString();
        }

        // Get Group By List
        List<String> groupBy = new ArrayList<>();
        for (Variable variable : query.getSelector().getGroupByList()) {
            groupBy.add(variable.getAttributeName());
        }

        // Get Having clause
        String having = "";
        if (query.getSelector().getHavingExpression() != null) {
            having = ConfigBuildingUtilities.getDefinition(query.getSelector().getHavingExpression(), siddhiAppString);
        }

        // Get OutputRateLimit
        String outputRateLimit = "";
        if (query.getOutputRate() != null) {
            outputRateLimit = ConfigBuildingUtilities.getDefinition(query.getOutputRate(), siddhiAppString);
        }

        // Get orderBy
        List<QueryOrderByConfig> orderBy = new ArrayList<>();
        for (OrderByAttribute orderByAttribute : query.getSelector().getOrderByList()) {
            orderBy.add(new QueryOrderByConfig(
                    orderByAttribute.getVariable().getAttributeName(),
                    orderByAttribute.getOrder().name()));
        }


//        return new QueryConfig(
//                queryId,
//                queryInputConfig,
//                querySelectConfig,
//                groupBy,
//                having,
//                outputRateLimit,
//                queryOutputConfig,
//                null); // TODO: 4/19/18 annotationList

        return new QueryConfig(
                queryId,
                queryInputConfig,
                querySelectConfig,
                groupBy,
                orderBy,
                Long.valueOf(ConfigBuildingUtilities.getDefinition(
                        query.getSelector().getLimit(),
                        siddhiAppString)),
                having,
                outputRateLimit,
                queryOutputConfig,
                null);
    }

    /**
     * Annotation name of a Siddhi Query, needed for getting details of the Query
     */
    private enum SiddhiQueryAnnotation {
        INFO,
        NAME,
        DESCRIPTION
    }
}
