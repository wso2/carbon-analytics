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

package io.siddhi.distribution.editor.core.core.util.designview.codegenerator.generators;

import io.siddhi.distribution.editor.core.core.util.designview.beans.configs.siddhielements.TriggerConfig;
import io.siddhi.distribution.editor.core.core.util.designview.constants.SiddhiCodeBuilderConstants;
import io.siddhi.distribution.editor.core.core.util.designview.exceptions.CodeGenerationException;
import io.siddhi.distribution.editor.core.core.util.designview.utilities.CodeGeneratorUtils;

/**
 * Generates the code for a Siddhi trigger element
 */
public class TriggerCodeGenerator {

    /**
     * Generates the Siddhi code representation of a TriggerConfig object
     *
     * @param trigger The TriggerConfig object
     * @param isGeneratingToolTip If it is generating a tooltip or not
     * @return The Siddhi code representation of the given TriggerConfig object
     * @throws CodeGenerationException Error when generating the code
     */
    public String generateTrigger(TriggerConfig trigger, boolean isGeneratingToolTip) throws CodeGenerationException {
        CodeGeneratorUtils.NullValidator.validateConfigObject(trigger);
        StringBuilder triggerStringBuilder = new StringBuilder();

        //to append quotes to start and cronExpression
        if (trigger.getCriteriaType().equalsIgnoreCase(SiddhiCodeBuilderConstants.AT )) {
            String triggerCriteria = trigger.getCriteria();
            triggerCriteria = SiddhiCodeBuilderConstants.SINGLE_QUOTE  + triggerCriteria + SiddhiCodeBuilderConstants.SINGLE_QUOTE;
            trigger.setTriggerCriteria(triggerCriteria);
        }
        if (!isGeneratingToolTip) {
            triggerStringBuilder.append(SubElementCodeGenerator.generateComment(trigger.getPreviousCommentSegment()));
        }

        triggerStringBuilder.append(SubElementCodeGenerator.generateAnnotations(trigger.getAnnotationList()))
                .append(SiddhiCodeBuilderConstants.NEW_LINE)
                .append(SiddhiCodeBuilderConstants.DEFINE_TRIGGER)
                .append(SiddhiCodeBuilderConstants.SPACE)
                .append(trigger.getName())
                .append(SiddhiCodeBuilderConstants.SPACE)
                .append(SiddhiCodeBuilderConstants.AT)
                .append(SiddhiCodeBuilderConstants.SPACE)
                .append(trigger.getCriteria())
                .append(SiddhiCodeBuilderConstants.SEMI_COLON);

        return  triggerStringBuilder.toString();
    }

}
