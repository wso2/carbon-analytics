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
import org.wso2.carbon.siddhi.editor.core.util.designview.utilities.ConfigBuildingUtilities;
import org.wso2.siddhi.query.api.SiddhiApp;
import org.wso2.siddhi.query.api.execution.query.selection.OutputAttribute;
import org.wso2.siddhi.query.api.expression.AttributeFunction;
import org.wso2.siddhi.query.api.expression.Variable;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates AttributesSelectionConfig with given Siddhi elements
 */
public class AttributesSelectionConfigGenerator {
    private String siddhiAppString;

    public AttributesSelectionConfigGenerator(String siddhiAppString) {
        this.siddhiAppString = siddhiAppString;
    }

    /**
     * Generates config object with the given list of selected attributes
     * @param outputAttributes      List of selected attributes
     * @return                      Config object with selected attributes
     */
    public AttributesSelectionConfig generateAttributesSelectionConfig(List<OutputAttribute> outputAttributes) {
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
     * Generates SelectedAttribute object, for the given OutputAttribute, which is a selection of a Siddhi Attribute
     * @param outputAttribute       A selected attribute
     * @return                      SelectedAttribute object
     */
    private SelectedAttribute generateSelectedAttributeConfig(OutputAttribute outputAttribute) {
        String[] splitString = ConfigBuildingUtilities.getDefinition(outputAttribute, siddhiAppString).split(" as ");
        if (splitString.length == 1) {
            return new SelectedAttribute(splitString[0].trim(), "");
        }
        return new SelectedAttribute(splitString[0].trim(), splitString[1].trim());
    }
}
