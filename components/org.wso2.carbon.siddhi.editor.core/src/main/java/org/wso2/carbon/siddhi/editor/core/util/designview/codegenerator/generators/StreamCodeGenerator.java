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

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.StreamConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.SiddhiCodeBuilderConstants;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.CodeGenerationException;

/**
 * Generate's the code for a Siddhi stream element
 */
public class StreamCodeGenerator {

    public String generateStream(StreamConfig stream) throws CodeGenerationException {
        if (stream == null) {
            throw new CodeGenerationException("A given stream element is empty");
        } else if (stream.getName() == null || stream.getName().isEmpty()) {
            throw new CodeGenerationException("The name of a given stream element is empty");
        }

        return SubElementCodeGenerator.generateAnnotations(stream.getAnnotationList()) +
                SiddhiCodeBuilderConstants.DEFINE_STREAM +
                SiddhiCodeBuilderConstants.SPACE +
                stream.getName() +
                SiddhiCodeBuilderConstants.SPACE +
                SiddhiCodeBuilderConstants.OPEN_BRACKET +
                SubElementCodeGenerator.generateAttributes(stream.getAttributeList()) +
                SiddhiCodeBuilderConstants.CLOSE_BRACKET +
                SiddhiCodeBuilderConstants.SEMI_COLON;
    }

}
