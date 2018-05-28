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

package org.wso2.carbon.siddhi.editor.core.util.designview;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.EventFlow;
import org.wso2.carbon.siddhi.editor.core.util.designview.codegenerator.CodeGenerator;
import org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.DesignGenerator;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.DesignGenerationException;
import org.wso2.carbon.siddhi.editor.core.util.designview.singletons.CodeGeneratorSingleton;
import org.wso2.carbon.siddhi.editor.core.util.designview.singletons.DesignGeneratorSingleton;

/**
 * Converts a Siddhi app between Code and Design
 */
public class SiddhiAppConverter {

    /**
     * Generates Siddhi app's Visual representation to be shown in the design view, from the given code string
     * @param siddhiAppCode                     Code representation of the Siddhi app
     * @return                                  Event flow representation of the Siddhi app
     * @throws DesignGenerationException        Error while converting to design view
     */
    public EventFlow generateDesign(String siddhiAppCode) throws DesignGenerationException {
        DesignGenerator designGenerator = DesignGeneratorSingleton.getInstance();
        return designGenerator.getEventFlow(siddhiAppCode);
    }

    /**
     * Generates a Siddhi app's code to be displayed in the source view of a given EventFlow instance
     *
     * @param eventFlow Event flow representation of the Siddhi app
     * @return Code representation of the Siddhi app
     */
    public String generateCode(EventFlow eventFlow) {
        CodeGenerator codeGenerator = CodeGeneratorSingleton.getInstance();
        return codeGenerator.generateSiddhiAppCode(eventFlow);
    }

}
