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

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.FunctionConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.SiddhiCodeBuilderConstants;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.CodeGenerationException;
import org.wso2.carbon.siddhi.editor.core.util.designview.utilities.CodeGeneratorUtils;

/**
 * Generates the code for a Siddhi function element
 */
public class FunctionCodeGenerator {

    /**
     * Generates the Siddhi code representation of a FunctionConfig object
     *
     * @param function The FunctionConfig object
     * @return The Siddhi code representation of the given FunctionConfig object
     * @throws CodeGenerationException Error when generating the code
     */
    public String generateFunction(FunctionConfig function, boolean isToolTip) throws CodeGenerationException {
        CodeGeneratorUtils.NullValidator.validateConfigObject(function);
        StringBuilder functionStringBuilder = new StringBuilder();
        if (!isToolTip) {
            functionStringBuilder.append(SubElementCodeGenerator.generateComment(function.getPreviousCommentSegment()));
        }
        functionStringBuilder.append(SiddhiCodeBuilderConstants.DEFINE_FUNCTION)
                .append(SiddhiCodeBuilderConstants.SPACE)
                .append(function.getName())
                .append(SiddhiCodeBuilderConstants.OPEN_SQUARE_BRACKET)
                .append(function.getScriptType())
                .append(SiddhiCodeBuilderConstants.CLOSE_SQUARE_BRACKET )
                .append(SiddhiCodeBuilderConstants.SPACE)
                .append(SiddhiCodeBuilderConstants.RETURN)
                .append(SiddhiCodeBuilderConstants.SPACE )
                .append(function.getReturnType().toLowerCase())
                .append(SiddhiCodeBuilderConstants.SPACE)
                .append(SiddhiCodeBuilderConstants.OPEN_CURLY_BRACKET)
                .append(SiddhiCodeBuilderConstants.NEW_LINE)
                .append(function.getBody().trim())
                .append(SiddhiCodeBuilderConstants.NEW_LINE)
                .append(SiddhiCodeBuilderConstants.CLOSE_CURLY_BRACKET)
                .append(SiddhiCodeBuilderConstants.SEMI_COLON);

        return functionStringBuilder.toString();
    }
}
