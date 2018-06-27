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

package org.wso2.carbon.siddhi.editor.core.util.designview.codegenerator.generators.query.subelements;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.output.QueryOutputConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.output.types.DeleteOutputConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.output.types.InsertOutputConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.output.types.UpdateInsertIntoOutputConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.output.types.setattribute.SetAttributeConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.codegenerator.generators.SubElementCodeGenerator;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.CodeGeneratorConstants;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.SiddhiCodeBuilderConstants;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.CodeGenerationException;
import org.wso2.carbon.siddhi.editor.core.util.designview.utilities.CodeGeneratorUtils;

/**
 * Generate's the code for a output element of a Siddhi query
 */
public class QueryOutputCodeGenerator {

    /**
     * Generate's the Siddhi code representation of a QueryOutputConfig object
     *
     * @param queryOutput The QueryOutputConfig object
     * @return The Siddhi code representation of the given QueryOutputConfig object
     * @throws CodeGenerationException Error when generating the code
     */
    public static String generateQueryOutput(QueryOutputConfig queryOutput) throws CodeGenerationException {
        CodeGeneratorUtils.NullValidator.validateConfigObject(queryOutput);

        StringBuilder queryOutputStringBuilder = new StringBuilder();
        switch (queryOutput.getType().toUpperCase()) {
            case CodeGeneratorConstants.INSERT:
                InsertOutputConfig insertOutputConfig = (InsertOutputConfig) queryOutput.getOutput();
                queryOutputStringBuilder.append(generateInsertOutput(insertOutputConfig, queryOutput.getTarget()));
                break;
            case CodeGeneratorConstants.DELETE:
                DeleteOutputConfig deleteOutputConfig = (DeleteOutputConfig) queryOutput.getOutput();
                queryOutputStringBuilder.append(generateDeleteOutput(deleteOutputConfig, queryOutput.getTarget()));
                break;
            case CodeGeneratorConstants.UPDATE:
            case CodeGeneratorConstants.UPDATE_OR_INSERT_INTO:
                UpdateInsertIntoOutputConfig updateInsertIntoOutput =
                        (UpdateInsertIntoOutputConfig) queryOutput.getOutput();
                queryOutputStringBuilder.append(generateUpdateOutput(queryOutput.getType(),
                        updateInsertIntoOutput, queryOutput.getTarget()));
                break;
            default:
                throw new CodeGenerationException("Unidentified query output type: " + queryOutput.getType());
        }

        return queryOutputStringBuilder.toString();
    }

    /**
     * Generate's the Siddhi code representation of a InsertOutputConfig object
     *
     * @param insertOutput The InsertOutputConfig object
     * @param target       The name of the output stream
     * @return The Siddhi code representation of the given InsertOutputConfig object
     * @throws CodeGenerationException Error when generating the code
     */
    private static String generateInsertOutput(InsertOutputConfig insertOutput, String target)
            throws CodeGenerationException {
        CodeGeneratorUtils.NullValidator.validateConfigObject(insertOutput);
        if (target == null || target.isEmpty()) {
            throw new CodeGenerationException("The 'target' value of a given query output element is empty");
        }

        StringBuilder insertOutputStringBuilder = new StringBuilder();

        insertOutputStringBuilder.append(SiddhiCodeBuilderConstants.INSERT)
                .append(SiddhiCodeBuilderConstants.SPACE);

        if (insertOutput.getEventType() != null && !insertOutput.getEventType().isEmpty()) {
            switch (insertOutput.getEventType().toUpperCase()) {
                case CodeGeneratorConstants.CURRENT_EVENTS:
                    insertOutputStringBuilder.append(SiddhiCodeBuilderConstants.CURRENT_EVENTS)
                            .append(SiddhiCodeBuilderConstants.SPACE);
                    break;
                case CodeGeneratorConstants.EXPIRED_EVENTS:
                    insertOutputStringBuilder.append(SiddhiCodeBuilderConstants.EXPIRED_EVENTS)
                            .append(SiddhiCodeBuilderConstants.SPACE);
                    break;
                case CodeGeneratorConstants.ALL_EVENTS:
                    insertOutputStringBuilder.append(SiddhiCodeBuilderConstants.ALL_EVENTS)
                            .append(SiddhiCodeBuilderConstants.SPACE);
                    break;
                default:
                    throw new CodeGenerationException("Unidentified insert query output event type: "
                            + insertOutput.getEventType());
            }
        }

        insertOutputStringBuilder.append(SiddhiCodeBuilderConstants.INTO)
                .append(SiddhiCodeBuilderConstants.SPACE)
                .append(target)
                .append(SiddhiCodeBuilderConstants.SEMI_COLON);

        return insertOutputStringBuilder.toString();
    }

    /**
     * Generate's the Siddhi code representation of a DeleteOutputConfig object
     *
     * @param deleteOutput The DeleteOutputConfig object
     * @param target       The name of the output stream
     * @return The Siddhi code representation of the given DeleteOutputConfig object
     * @throws CodeGenerationException Error when generating the code
     */
    private static String generateDeleteOutput(DeleteOutputConfig deleteOutput, String target)
            throws CodeGenerationException {
        CodeGeneratorUtils.NullValidator.validateConfigObject(deleteOutput);
        if (target == null || target.isEmpty()) {
            throw new CodeGenerationException("The 'target' value of a given delete query output is empty");
        }

        return SiddhiCodeBuilderConstants.DELETE +
                SiddhiCodeBuilderConstants.SPACE +
                target +
                SubElementCodeGenerator.generateForEventType(deleteOutput.getEventType()) +
                SiddhiCodeBuilderConstants.SPACE +
                SiddhiCodeBuilderConstants.ON +
                SiddhiCodeBuilderConstants.SPACE +
                deleteOutput.getOn() +
                SiddhiCodeBuilderConstants.SEMI_COLON;
    }

    /**
     * Generate's the Siddhi code representation of a UpdateInsertIntoOutputConfig object
     *
     * @param type                   The type of output (i.e Either 'update' or 'update-or-insert-into')
     * @param updateInsertIntoOutput The UpdateInsertIntoOutputConfig object
     * @param target                 The name of the output stream
     * @return The Siddhi code representation of the given UpdateInsertIntoOutputConfig object
     * @throws CodeGenerationException Error when generating the code
     */
    private static String generateUpdateOutput(String type, UpdateInsertIntoOutputConfig updateInsertIntoOutput,
                                               String target) throws CodeGenerationException {
        CodeGeneratorUtils.NullValidator.validateConfigObject(updateInsertIntoOutput);
        if (target == null || target.isEmpty()) {
            throw new CodeGenerationException("The 'target' value of a given update/insert" +
                    " query output element is empty");
        }

        StringBuilder updateInsertIntoOutputStringBuilder = new StringBuilder();
        if (type.equalsIgnoreCase(CodeGeneratorConstants.UPDATE)) {
            updateInsertIntoOutputStringBuilder.append(SiddhiCodeBuilderConstants.UPDATE);
        } else if (type.equalsIgnoreCase(CodeGeneratorConstants.UPDATE_OR_INSERT_INTO)) {
            updateInsertIntoOutputStringBuilder.append(SiddhiCodeBuilderConstants.UPDATE_OR_INSERT_INTO);
        }

        updateInsertIntoOutputStringBuilder.append(SiddhiCodeBuilderConstants.SPACE)
                .append(target)
                .append(SubElementCodeGenerator.generateForEventType(updateInsertIntoOutput.getEventType()))
                .append(SiddhiCodeBuilderConstants.SPACE);

        if (updateInsertIntoOutput.getSet() != null && !updateInsertIntoOutput.getSet().isEmpty()) {
            updateInsertIntoOutputStringBuilder.append(SiddhiCodeBuilderConstants.SET)
                    .append(SiddhiCodeBuilderConstants.SPACE);
            int setAttributesLeft = updateInsertIntoOutput.getSet().size();
            for (SetAttributeConfig setAttribute : updateInsertIntoOutput.getSet()) {
                updateInsertIntoOutputStringBuilder.append(generateSetAttribute(setAttribute));

                if (setAttributesLeft != 1) {
                    updateInsertIntoOutputStringBuilder.append(SiddhiCodeBuilderConstants.COMMA);
                }
                setAttributesLeft--;
            }
            updateInsertIntoOutputStringBuilder.append(SiddhiCodeBuilderConstants.SPACE);
        }

        updateInsertIntoOutputStringBuilder.append(SiddhiCodeBuilderConstants.ON)
                .append(SiddhiCodeBuilderConstants.SPACE)
                .append(updateInsertIntoOutput.getOn())
                .append(SiddhiCodeBuilderConstants.SEMI_COLON);

        return updateInsertIntoOutputStringBuilder.toString();
    }

    /**
     * Generate's the Siddhi code representation of a SetAttributeConfig object
     *
     * @param setAttribute The SetAttributeConfig object
     * @return The Siddhi code representation of the given SetAttributeConfig object
     * @throws CodeGenerationException Error when generating the code
     */
    private static String generateSetAttribute(SetAttributeConfig setAttribute) throws CodeGenerationException {
        CodeGeneratorUtils.NullValidator.validateConfigObject(setAttribute);

        return setAttribute.getAttribute() +
                SiddhiCodeBuilderConstants.SPACE +
                SiddhiCodeBuilderConstants.EQUAL +
                SiddhiCodeBuilderConstants.SPACE +
                setAttribute.getValue();
    }

    private QueryOutputCodeGenerator() {
    }

}
