package org.wso2.carbon.siddhi.editor.core.util.designview.codegenerator.generators;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.TriggerConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.SiddhiCodeBuilderConstants;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.CodeGenerationException;

public class TriggerCodeGenerator {
    /**
     * Generates a trigger definition string from a TriggerConfig object
     *
     * @param trigger The TriggerConfig object to be converted
     * @return The converted trigger definition string
     * @throws CodeGenerationException Error while generating code
     */
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
