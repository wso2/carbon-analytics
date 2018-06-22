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

package org.wso2.carbon.siddhi.editor.core.util.designview.codegenerator.generators;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.TriggerConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.SiddhiCodeBuilderConstants;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.CodeGenerationException;

/**
 * Generate's the code for a Siddhi trigger element
 */
public class TriggerCodeGenerator {

    public String generateTrigger(TriggerConfig trigger) throws CodeGenerationException {
        if (trigger == null) {
            throw new CodeGenerationException("A given trigger element is empty");
        } else if (trigger.getName() == null || trigger.getName().isEmpty()) {
            throw new CodeGenerationException("The name of a given trigger element is empty");
        } else if (trigger.getAt() == null || trigger.getAt().isEmpty()) {
            throw new CodeGenerationException("The 'at' value of " + trigger.getName() + " is empty");
        }

        return SubElementCodeGenerator.generateAnnotations(trigger.getAnnotationList()) +
                SiddhiCodeBuilderConstants.DEFINE_TRIGGER +
                SiddhiCodeBuilderConstants.SPACE +
                trigger.getName() +
                SiddhiCodeBuilderConstants.SPACE +
                SiddhiCodeBuilderConstants.AT +
                SiddhiCodeBuilderConstants.SPACE +
                trigger.getAt() +
                SiddhiCodeBuilderConstants.SEMI_COLON;
    }

}
