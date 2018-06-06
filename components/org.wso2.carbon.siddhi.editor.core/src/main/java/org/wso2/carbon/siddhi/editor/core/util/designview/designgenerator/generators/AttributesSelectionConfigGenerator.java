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

package org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.generators;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.attributesselection.AllSelectionConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.attributesselection.AttributesSelectionConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.attributesselection.SelectedAttribute;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.attributesselection.UserDefinedSelectionConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.DesignGenerationException;
import org.wso2.carbon.siddhi.editor.core.util.designview.utilities.ConfigBuildingUtilities;
import org.wso2.siddhi.query.api.execution.query.selection.OutputAttribute;
import org.wso2.siddhi.query.api.execution.query.selection.Selector;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates to create AttributesSelectionConfig with given Siddhi elements
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
        List<SelectedAttribute> selectedAttributes = new ArrayList<>();
        for (OutputAttribute outputAttribute : selector.getSelectionList()) {
            try {
                selectedAttributes.add(generateSelectedAttribute(outputAttribute));
            } catch (DesignGenerationException e) {
                // Selector object has been successfully compiled by the Siddhi Compiler, but no query indexes
                // The OutputAttribute object was a result of 'select *'
                return new AllSelectionConfig();
            }
        }
        return new UserDefinedSelectionConfig(selectedAttributes);
    }

    /**
     * Generates a SelectedAttribute object from the given Siddhi OutputAttribute
     * @param outputAttribute                   Siddhi OutputAttribute object
     * @return                                  SelectedAttribute object
     * @throws DesignGenerationException        Error while getting the definition of the OutputAttribute
     */
    private SelectedAttribute generateSelectedAttribute(OutputAttribute outputAttribute)
            throws DesignGenerationException {
        return new SelectedAttribute(
                ConfigBuildingUtilities.getDefinition(outputAttribute.getExpression(), siddhiAppString),
                outputAttribute.getRename());

    }
}
