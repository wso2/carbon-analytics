package org.wso2.carbon.siddhi.editor.core.util.designview.codegenerator.generators;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.TriggerConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.SiddhiStringBuilderConstants;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.CodeGenerationException;
import org.wso2.carbon.siddhi.editor.core.util.designview.utilities.CodeGeneratorHelper;

public class TriggerCodeGenerator {

    /**
     * Generates a trigger definition string from a TriggerConfig object
     *
     * @param trigger The TriggerConfig object to be converted
     * @return The converted trigger definition string
     * @throws CodeGenerationException Error while generating code
     */
    private String generateTriggerCode(TriggerConfig trigger) throws CodeGenerationException {
        if (trigger == null) {
            throw new CodeGenerationException("A given trigger element is empty");
        } else if (trigger.getName() == null || trigger.getName().isEmpty()) {
            throw new CodeGenerationException("The name of a given trigger element is empty");
        } else if (trigger.getAt() == null || trigger.getAt().isEmpty()) {
            throw new CodeGenerationException("The 'at' value of " + trigger.getName() + " is empty");
        }

        return CodeGeneratorHelper.getAnnotations(trigger.getAnnotationList()) +
                SiddhiStringBuilderConstants.DEFINE_TRIGGER +
                SiddhiStringBuilderConstants.SPACE +
                trigger.getName() +
                SiddhiStringBuilderConstants.SPACE +
                SiddhiStringBuilderConstants.AT +
                SiddhiStringBuilderConstants.SPACE +
                trigger.getAt() +
                SiddhiStringBuilderConstants.SEMI_COLON;
    }

}
