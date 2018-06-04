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

package org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.generators.query.output;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.output.*;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.output.types.UpdateInsertIntoOutputConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.output.types.setattribute.SetAttributeConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.output.types.DeleteOutputConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.output.types.InsertOutputConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.query.QueryOutputType;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.DesignGenerationException;
import org.wso2.carbon.siddhi.editor.core.util.designview.utilities.ConfigBuildingUtilities;
import org.wso2.siddhi.query.api.execution.query.output.stream.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Generator to create QueryOutputConfig from Siddhi elements
 */
public class QueryOutputConfigGenerator {
    private String siddhiAppString;

    public QueryOutputConfigGenerator(String siddhiAppString) {
        this.siddhiAppString = siddhiAppString;
    }

    /**
     * Generates Config for a Siddhi Query's output part
     * @param queryOutputStream                 Siddhi QueryOutputStream
     * @return                                  QueryOutputConfig
     * @throws DesignGenerationException        Error while generating Query Output
     */
    public QueryOutputConfig generateQueryOutputConfig(OutputStream queryOutputStream)
            throws DesignGenerationException {
        if (queryOutputStream instanceof InsertIntoStream) {
            return generateInsertOutputConfig((InsertIntoStream) queryOutputStream);
        } else if (queryOutputStream instanceof DeleteStream) {
            return generateDeleteOutputConfig((DeleteStream) queryOutputStream);
        } else if (queryOutputStream instanceof UpdateStream) {
            return generateUpdateOutputConfig((UpdateStream) queryOutputStream);
        } else if (queryOutputStream instanceof UpdateOrInsertStream) {
            return generateUpdateOrInsertIntoOutputConfig((UpdateOrInsertStream) queryOutputStream);
        }
        throw new DesignGenerationException("Unknown type of Query Output Stream for generating Query Output Config");
    }

    /**
     * Generates a QueryOutputConfig of type 'insert into'
     * @param insertIntoStream      Siddhi InsertIntoStream
     * @return                      QueryOutputConfig
     */
    private QueryOutputConfig generateInsertOutputConfig(InsertIntoStream insertIntoStream) {
        return new QueryOutputConfig(
                QueryOutputType.INSERT.toString(),
                new InsertOutputConfig(insertIntoStream.getOutputEventType().name()),
                insertIntoStream.getId());
    }

    /**
     * Generates a QueryOutputConfig of type 'delete'
     * @param deleteStream                      Siddhi DeleteStream
     * @return                                  QueryOutputConfig
     * @throws DesignGenerationException        Error while generating QueryOutputConfig
     */
    private QueryOutputConfig generateDeleteOutputConfig(DeleteStream deleteStream) throws DesignGenerationException {
        return new QueryOutputConfig(
                QueryOutputType.DELETE.toString(),
                new DeleteOutputConfig(
                        deleteStream.getOutputEventType().name(),
                        ConfigBuildingUtilities.getDefinition(deleteStream.getOnDeleteExpression(), siddhiAppString)),
                deleteStream.getId());
    }

    /**
     * Generates a QueryOutputConfig of type 'update'
     * @param updateStream                      Siddhi UpdateStream
     * @return                                  QueryOutputConfig
     * @throws DesignGenerationException        Error while generating QueryOutputConfig
     */
    private QueryOutputConfig generateUpdateOutputConfig(UpdateStream updateStream) throws DesignGenerationException {
        return new QueryOutputConfig(
                QueryOutputType.UPDATE.toString(),
                new UpdateInsertIntoOutputConfig(
                        updateStream.getOutputEventType().name(),
                        generateSetAttributeConfigsList(updateStream.getUpdateSet().getSetAttributeList()),
                        ConfigBuildingUtilities.getDefinition(updateStream.getOnUpdateExpression(), siddhiAppString)),
                updateStream.getId());
    }

    /**
     * Generates a QueryOutputConfig of type 'update or insert into'
     * @param updateOrInsertStream              Siddhi UpdateOrInsertStream
     * @return                                  QueryOutputConfig
     * @throws DesignGenerationException        Error while generating QueryOutputConfig
     */
    private QueryOutputConfig generateUpdateOrInsertIntoOutputConfig(UpdateOrInsertStream updateOrInsertStream)
            throws DesignGenerationException {
        return new QueryOutputConfig(
                QueryOutputType.UPDATE_OR_INSERT_INTO.toString(),
                new UpdateInsertIntoOutputConfig(
                        updateOrInsertStream.getOutputEventType().name(),
                        generateSetAttributeConfigsList(updateOrInsertStream.getUpdateSet().getSetAttributeList()),
                        ConfigBuildingUtilities.getDefinition(
                                updateOrInsertStream.getOnUpdateExpression(), siddhiAppString)),
                updateOrInsertStream.getId());
    }

    /**
     * Generates config of attributes, set in 'update' or 'update or insert into' queries
     * @param setAttributes                     List of Siddhi SetAttributes
     * @return                                  List of SetAttributeConfigs
     * @throws DesignGenerationException        Error while generating config
     */
    private List<SetAttributeConfig> generateSetAttributeConfigsList(List<UpdateSet.SetAttribute> setAttributes)
            throws DesignGenerationException {
        List<SetAttributeConfig> setAttributeConfigs = new ArrayList<>();
        for (UpdateSet.SetAttribute setAttribute : setAttributes) {
            // Attribute name and value
            String attributeName = setAttribute.getTableVariable().getAttributeName();
            if (setAttribute.getTableVariable().getStreamId() != null) {
                attributeName = setAttribute.getTableVariable().getStreamId() + "." + attributeName;
            }
            String attributeValue =
                    ConfigBuildingUtilities.getDefinition(setAttribute.getAssignmentExpression(), siddhiAppString);

            setAttributeConfigs.add(new SetAttributeConfig(attributeName, attributeValue));
        }
        return setAttributeConfigs;
    }
}
