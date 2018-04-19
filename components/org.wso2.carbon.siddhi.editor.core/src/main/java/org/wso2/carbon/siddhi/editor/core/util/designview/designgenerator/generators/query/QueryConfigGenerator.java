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
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.QueryInputConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.output.QueryOutputConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.generators.attributesselection.SelectedAttributesConfigGenerator;
import org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.generators.query.input.QueryInputConfigGenerator;
import org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.generators.query.output.QueryOutputConfigGenerator;
import org.wso2.carbon.siddhi.editor.core.util.designview.utilities.ConfigBuildingUtilities;
import org.wso2.siddhi.query.api.SiddhiApp;
import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.execution.query.Query;
import org.wso2.siddhi.query.api.expression.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Generator to create a Siddhi Query config
 */
public class QueryConfigGenerator {
    private String siddhiAppString;
    private SiddhiApp siddhiApp;

    public QueryConfigGenerator(String siddhiAppString, SiddhiApp siddhiApp) {
        this.siddhiAppString = siddhiAppString;
        this.siddhiApp = siddhiApp;
    }

    public QueryConfig generateQueryConfig(Query query) {
        // Generate Query Input
        QueryInputConfigGenerator queryInputConfigGenerator = new QueryInputConfigGenerator();
        QueryInputConfig queryInputConfig =
                queryInputConfigGenerator.generateQueryInputConfig(query.getInputStream(), siddhiAppString, siddhiApp);

        // Generate Query Select
        SelectedAttributesConfigGenerator selectedAttributesConfigGenerator = new SelectedAttributesConfigGenerator();
        AttributesSelectionConfig querySelectConfig =
                selectedAttributesConfigGenerator
                        .generateSelectedAttributesConfig(query.getSelector().getSelectionList());

        // Generate Query Output
        QueryOutputConfigGenerator queryOutputConfigGenerator = new QueryOutputConfigGenerator();
        QueryOutputConfig queryOutputConfig =
                queryOutputConfigGenerator.generateQueryOutputConfig(query.getOutputStream(), siddhiAppString);

        // Set the Name & Id of the Query
        String queryId = null;
        for (Annotation annotation : query.getAnnotations()) {
            if (annotation.getName().equalsIgnoreCase(SiddhiQueryAnnotation.INFO.toString())) {
                queryId = annotation.getElement(SiddhiQueryAnnotation.NAME.toString());
                break;
            }
        }

        // Set UUID if no name has been given for the query
        if (queryId == null) {
            queryId = UUID.randomUUID().toString();
        }

        // Group By
        List<String> groupBy = new ArrayList<>();
        for (Variable variable : query.getSelector().getGroupByList()) {
            groupBy.add(variable.getAttributeName());
        }

        // Having
        String having = "";
        if (query.getSelector().getHavingExpression() != null) {
            having = ConfigBuildingUtilities.getDefinition(query.getSelector().getHavingExpression(), siddhiAppString);
        }

        return new QueryConfig(
                queryId,
                queryInputConfig,
                querySelectConfig,
                groupBy,
                having,
                null,
                queryOutputConfig,
                null);
    }

    private enum SiddhiQueryAnnotation {
        INFO,
        NAME,
        DESCRIPTION
    }
}
