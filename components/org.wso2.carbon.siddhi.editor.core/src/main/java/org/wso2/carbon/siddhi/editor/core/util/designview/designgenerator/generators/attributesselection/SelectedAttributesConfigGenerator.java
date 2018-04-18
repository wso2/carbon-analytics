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

package org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.generators.attributesselection;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.attributesselection.AttributesSelectionConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.attributesselection.SelectedAttribute;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.AttributeSelection;
import org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.factories.AttributesSelectionConfigFactory;
import org.wso2.siddhi.query.api.execution.query.selection.OutputAttribute;
import org.wso2.siddhi.query.api.expression.AttributeFunction;
import org.wso2.siddhi.query.api.expression.Variable;

import java.util.ArrayList;
import java.util.List;

/**
 * Generator for creating Config for selection of Attributes
 */
public class SelectedAttributesConfigGenerator {
    /**
     * Generates config object with the given list of selected attributes
     * @param outputAttributes      List of selected attributes
     * @return                      Config object with selected attributes
     */
    public AttributesSelectionConfig generateSelectedAttributesConfig(List<OutputAttribute> outputAttributes) {
        List<SelectedAttribute> selectedAttributes = new ArrayList<>();
        AttributesSelectionConfigFactory attributesSelectionConfigFactory = new AttributesSelectionConfigFactory();
        if (!outputAttributes.isEmpty()) {
            for (OutputAttribute outputAttribute : outputAttributes) {
                selectedAttributes.add(generateSelectedAttributeConfig(outputAttribute));
            }
            return attributesSelectionConfigFactory.getAttributesSelectionConfig(selectedAttributes);
        }
        // Empty outputAttributes after a successful compilation of Siddhi app means "select *"
        return attributesSelectionConfigFactory.getAttributesSelectionConfig(AttributeSelection.VALUE_ALL);
    }

    /**
     * Generates SelectedAttribute object, for the given OutputAttribute,
     * which is a selection of a Siddhi Aggregation
     * @param outputAttribute   A selected attribute
     * @return                  SelectedAttribute object representation for the given selected attribute
     */
    private SelectedAttribute generateSelectedAttributeConfig(OutputAttribute outputAttribute) {
        if (outputAttribute.getExpression() instanceof AttributeFunction) {
            // Attribute is a Function
            AttributeFunction attributeFunction = (AttributeFunction) (outputAttribute.getExpression());
            if (attributeFunction.getParameters()[0] instanceof Variable) {
                Variable attribute = (Variable)attributeFunction.getParameters()[0];
                String attributeName = attribute.getAttributeName();
                if (attribute.getStreamId() != null) {
                    attributeName = attribute.getStreamId() + "." + attributeName;
                }

                return new SelectedAttribute(
                        String.format("%s(%s)", attributeFunction.getName(), attributeName),
                        outputAttribute.getRename());
            } else {
                throw new IllegalArgumentException("Parameter of the AttributeFunction is of unknown class");
            }
        } else if (outputAttribute.getExpression() instanceof Variable) {
            // Attribute is a Variable
            Variable expression = (Variable)(outputAttribute.getExpression());

            String attributeName = expression.getAttributeName();
            String attributeAlias = "";
            if (expression.getStreamId() != null) {
                attributeName = expression.getStreamId() + "." + attributeName;
            }
            if (outputAttribute.getRename() != null) {
                attributeAlias = outputAttribute.getRename();
            }

            return new SelectedAttribute(attributeName, attributeAlias);
        } else {
            throw new IllegalArgumentException("Expression of the OutputAttribute is of unknown class");
        }
    }
}
