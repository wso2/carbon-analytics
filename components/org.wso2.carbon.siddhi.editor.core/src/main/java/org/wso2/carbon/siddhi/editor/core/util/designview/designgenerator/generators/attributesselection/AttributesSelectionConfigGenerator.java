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
import org.wso2.siddhi.query.api.execution.query.selection.Selector;

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
     * Generates an AttributesSelectionConfig from the given Siddhi Selector
     * @param selector      Siddhi Selector object
     * @return              AttributesSelectionConfig object
     */
    public AttributesSelectionConfig generateAttributesSelectionConfig(Selector selector) {
        String selectString = extractSelectedAttributesString(selector);
        AttributesSelectionConfigFactory attributesSelectionConfigFactory = new AttributesSelectionConfigFactory();
        if (selectString.equals(AttributeSelection.VALUE_ALL)) {
            // All attributes selection (*)
            return attributesSelectionConfigFactory.getAttributesSelectionConfig(AttributeSelection.VALUE_ALL);
        }
        // User defined selection
        return attributesSelectionConfigFactory
                .getAttributesSelectionConfig(generateSelectedAttributeList(selectString));
    }

    /**
     * Extracts only the string of attributes selection, without the 'select' keyword
     * @param selector      Siddhi Selector object
     * @return              Attributes selection string
     */
    private String extractSelectedAttributesString(Selector selector) {
        final String SELECT = "select";
        final String GROUP_BY = "group by";
        final String HAVING = "having";
        String completeSelectString = ConfigBuildingUtilities.getDefinition(selector, siddhiAppString);
        String selectString = completeSelectString.split(GROUP_BY)[0].split(HAVING)[0];
        String selectedCollection = selectString.split(SELECT)[1].replace("\n", "");
        return selectedCollection.trim();
    }

    /**
     * Generates a list of SelectedAttributes from the given string, of selected attributes separated by commas
     * @param selectedAttributesString      Selected attributes separated by commas
     * @return                              List of SelectedAttributes
     */
    private List<SelectedAttribute> generateSelectedAttributeList(String selectedAttributesString) {
        List<SelectedAttribute> selectedAttributes = new ArrayList<>();
        for (String selectedElement : selectedAttributesString.split(",")) {
            String[] nameAndAlias = selectedElement.split("([a|A][s|S])");
            if (nameAndAlias.length == 1) {
                selectedAttributes.add(new SelectedAttribute(nameAndAlias[0].trim(), ""));
            } else {
                selectedAttributes.add(new SelectedAttribute(nameAndAlias[0].trim(), nameAndAlias[1].trim()));
            }
        }
        return selectedAttributes;
    }
}
