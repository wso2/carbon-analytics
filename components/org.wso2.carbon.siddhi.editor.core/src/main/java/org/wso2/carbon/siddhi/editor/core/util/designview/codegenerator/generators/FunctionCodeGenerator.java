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

/**
 * Generate's the code for a Siddhi function element
 */
public class FunctionCodeGenerator {

    public String generateFunction(FunctionConfig function) throws CodeGenerationException {
        if (function == null) {
            throw new CodeGenerationException("A given function element is empty");
        } else if (function.getName() == null || function.getName().isEmpty()) {
            throw new CodeGenerationException("The name of a given function element is empty");
        } else if (function.getScriptType() == null || function.getScriptType().isEmpty()) {
            throw new CodeGenerationException("The 'script type' of " + function.getName() + " is empty");
        } else if (function.getReturnType() == null || function.getReturnType().isEmpty()) {
            throw new CodeGenerationException("The return type of " + function.getName() + " is empty");
        } else if (function.getBody() == null || function.getBody().isEmpty()) {
            throw new CodeGenerationException("The 'body' value of " + function.getName() + " is empty");
        }

        return SiddhiCodeBuilderConstants.DEFINE_FUNCTION +
                SiddhiCodeBuilderConstants.SPACE +
                function.getName() +
                SiddhiCodeBuilderConstants.OPEN_SQUARE_BRACKET +
                function.getScriptType() +
                SiddhiCodeBuilderConstants.CLOSE_SQUARE_BRACKET +
                SiddhiCodeBuilderConstants.SPACE +
                SiddhiCodeBuilderConstants.RETURN +
                SiddhiCodeBuilderConstants.SPACE +
                function.getReturnType().toLowerCase() +
                SiddhiCodeBuilderConstants.SPACE +
                SiddhiCodeBuilderConstants.OPEN_CURLY_BRACKET +
                function.getBody().trim() +
                SiddhiCodeBuilderConstants.CLOSE_CURLY_BRACKET +
                SiddhiCodeBuilderConstants.SEMI_COLON;
    }

}
