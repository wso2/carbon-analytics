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

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.attributesselection.AttributesSelectionConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.attributesselection.SelectedAttribute;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.attributesselection.UserDefinedSelectionConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.AttributeSelection;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.SiddhiCodeBuilderConstants;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.CodeGenerationException;
import org.wso2.carbon.siddhi.editor.core.util.designview.utilities.CodeGeneratorUtils;

/**
 * Generate's the code for a select element of a Siddhi query
 */
public class QuerySelectCodeGenerator {

    /**
     * Generate's the Siddhi code representation of a AttributesSelectionConfig object
     *
     * @param attributesSelection The AttributesSelectionConfig object
     * @return The Siddhi code representation of the given AttributesSelectionConfig object
     * @throws CodeGenerationException Error when generating the code
     */
    public static String generateQuerySelect(AttributesSelectionConfig attributesSelection)
            throws CodeGenerationException {
        CodeGeneratorUtils.NullValidator.validateConfigObject(attributesSelection);

        StringBuilder attributesSelectionStringBuilder = new StringBuilder();
        attributesSelectionStringBuilder.append(SiddhiCodeBuilderConstants.SELECT)
                .append(SiddhiCodeBuilderConstants.SPACE);
        switch (attributesSelection.getType().toUpperCase()) {
            case AttributeSelection.TYPE_USER_DEFINED:
                UserDefinedSelectionConfig userDefinedSelection = (UserDefinedSelectionConfig) attributesSelection;
                attributesSelectionStringBuilder.append(generateUserDefinedSelection(userDefinedSelection));
                break;
            case AttributeSelection.TYPE_ALL:
                attributesSelectionStringBuilder.append(SiddhiCodeBuilderConstants.ALL);
                break;
            default:
                throw new CodeGenerationException("Undefined attribute selection type:"
                        + attributesSelection.getType());
        }

        return attributesSelectionStringBuilder.toString();
    }

    /**
     * Generate's the Siddhi code representation of a UserDefinedSelectionConfig object
     *
     * @param userDefinedSelection The UserDefinedSelectionConfig object
     * @return The Siddhi code representation of the given UserDefinedSelectionConfig object
     * @throws CodeGenerationException Error when generating the code
     */
    private static String generateUserDefinedSelection(UserDefinedSelectionConfig userDefinedSelection)
            throws CodeGenerationException {
        if (userDefinedSelection == null || userDefinedSelection.getValue() == null ||
                userDefinedSelection.getValue().isEmpty()) {
            throw new CodeGenerationException("A given user defined selection value is empty");
        }

        StringBuilder userDefinedSelectionStringBuilder = new StringBuilder();

        int attributesLeft = userDefinedSelection.getValue().size();
        for (SelectedAttribute attribute : userDefinedSelection.getValue()) {
            CodeGeneratorUtils.NullValidator.validateConfigObject(attribute);

            userDefinedSelectionStringBuilder.append(attribute.getExpression());
            if (attribute.getAs() != null && !attribute.getAs().isEmpty() &&
                    !attribute.getAs().equals(attribute.getExpression())) {
                userDefinedSelectionStringBuilder.append(SiddhiCodeBuilderConstants.SPACE)
                        .append(SiddhiCodeBuilderConstants.AS)
                        .append(SiddhiCodeBuilderConstants.SPACE)
                        .append(attribute.getAs());
            }
            if (attributesLeft != 1) {
                userDefinedSelectionStringBuilder.append(SiddhiCodeBuilderConstants.COMMA);
            }
            attributesLeft--;
        }

        return userDefinedSelectionStringBuilder.toString();
    }

    private QuerySelectCodeGenerator() {
    }

}
