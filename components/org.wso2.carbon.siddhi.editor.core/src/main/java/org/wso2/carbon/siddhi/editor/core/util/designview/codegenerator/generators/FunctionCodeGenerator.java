package org.wso2.carbon.siddhi.editor.core.util.designview.codegenerator.generators;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.FunctionConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.SiddhiStringBuilderConstants;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.CodeGenerationException;

public class FunctionCodeGenerator {

    /**
     * Generates a function definition string from a FunctionConfig object
     *
     * @param function The FunctionConfig object to be converted
     * @return The converted function definition string
     * @throws CodeGenerationException Error while generating code
     */
    public String generateFunctionCode(FunctionConfig function) throws CodeGenerationException {
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

        return SiddhiStringBuilderConstants.DEFINE_FUNCTION +
                SiddhiStringBuilderConstants.SPACE +
                function.getName() +
                SiddhiStringBuilderConstants.OPEN_SQUARE_BRACKET +
                function.getScriptType() +
                SiddhiStringBuilderConstants.CLOSE_SQUARE_BRACKET +
                SiddhiStringBuilderConstants.SPACE +
                SiddhiStringBuilderConstants.RETURN +
                SiddhiStringBuilderConstants.SPACE +
                function.getReturnType().toLowerCase() +
                SiddhiStringBuilderConstants.SPACE +
                SiddhiStringBuilderConstants.OPEN_CURLY_BRACKET +
                function.getBody().trim() +
                SiddhiStringBuilderConstants.CLOSE_CURLY_BRACKET +
                SiddhiStringBuilderConstants.SEMI_COLON;
    }

}
