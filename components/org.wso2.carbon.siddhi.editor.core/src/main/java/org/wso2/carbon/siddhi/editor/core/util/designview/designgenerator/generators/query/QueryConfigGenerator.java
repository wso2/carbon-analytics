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
import org.wso2.siddhi.query.api.SiddhiApp;
import org.wso2.siddhi.query.api.execution.query.Query;

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

    public QueryConfig buildQueryConfig(Query query) {
        // Generate input part
        QueryInputConfigGenerator queryInputConfigGenerator = new QueryInputConfigGenerator();
        QueryInputConfig queryInputConfig =
                queryInputConfigGenerator.generateQueryInput(query, siddhiAppString, siddhiApp);

        // Generate select part
        SelectedAttributesConfigGenerator selectedAttributesConfigGenerator = new SelectedAttributesConfigGenerator();
        AttributesSelectionConfig querySelectConfig =
                selectedAttributesConfigGenerator
                        .generateSelectedAttributesConfig(query.getSelector().getSelectionList());

        // Generate output part
        QueryOutputConfigGenerator queryOutputConfigGenerator =
                new QueryOutputConfigGenerator(query, siddhiAppString, siddhiApp);
        QueryOutputConfig queryOutputConfig = queryOutputConfigGenerator.generateQueryOutput();

        // TODO: 4/8/18 implement
        return null;
    }
}
