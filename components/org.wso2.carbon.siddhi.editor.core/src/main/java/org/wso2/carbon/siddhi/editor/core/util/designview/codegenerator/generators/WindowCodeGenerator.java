package org.wso2.carbon.siddhi.editor.core.util.designview.codegenerator.generators;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.WindowConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.CodeGeneratorConstants;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.SiddhiStringBuilderConstants;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.CodeGenerationException;
import org.wso2.carbon.siddhi.editor.core.util.designview.utilities.CodeGeneratorHelper;

public class WindowCodeGenerator {

    /**
     * Generates a window definition string from a WindowConfig object
     *
     * @param window The WindowConfig object to be converted
     * @return The converted window definition string
     * @throws CodeGenerationException Error while generating code
     */
    public String generateWindowCode(WindowConfig window) throws CodeGenerationException {
        if (window == null) {
            throw new CodeGenerationException("A given window element is empty");
        } else if (window.getName() == null || window.getName().isEmpty()) {
            throw new CodeGenerationException("The name of a given window element is empty");
        } else if (window.getFunction() == null || window.getFunction().isEmpty()) {
            throw new CodeGenerationException("The function name of the window " + window.getName() + " is empty");
        }

        StringBuilder windowStringBuilder = new StringBuilder();
        windowStringBuilder.append(CodeGeneratorHelper.getAnnotations(window.getAnnotationList()))
                .append(SiddhiStringBuilderConstants.DEFINE_WINDOW)
                .append(SiddhiStringBuilderConstants.SPACE)
                .append(window.getName())
                .append(SiddhiStringBuilderConstants.SPACE)
                .append(SiddhiStringBuilderConstants.OPEN_BRACKET)
                .append(CodeGeneratorHelper.getAttributes(window.getAttributeList()))
                .append(SiddhiStringBuilderConstants.CLOSE_BRACKET)
                .append(SiddhiStringBuilderConstants.SPACE)
                .append(window.getFunction())
                .append(SiddhiStringBuilderConstants.OPEN_BRACKET)
                .append(CodeGeneratorHelper.getParameterList(window.getParameters()))
                .append(SiddhiStringBuilderConstants.CLOSE_BRACKET);

        if (window.getOutputEventType() != null && !window.getOutputEventType().isEmpty()) {
            windowStringBuilder.append(SiddhiStringBuilderConstants.SPACE)
                    .append(SiddhiStringBuilderConstants.OUTPUT)
                    .append(SiddhiStringBuilderConstants.SPACE);
            switch (window.getOutputEventType().toUpperCase()) {
                case CodeGeneratorConstants.CURRENT_EVENTS:
                    windowStringBuilder.append(SiddhiStringBuilderConstants.CURRENT_EVENTS);
                    break;
                case CodeGeneratorConstants.EXPIRED_EVENTS:
                    windowStringBuilder.append(SiddhiStringBuilderConstants.EXPIRED_EVENTS);
                    break;
                case CodeGeneratorConstants.ALL_EVENTS:
                    windowStringBuilder.append(SiddhiStringBuilderConstants.ALL_EVENTS);
                    break;
                default:
                    throw new CodeGenerationException("Unidentified window output event type: "
                            + window.getOutputEventType());
            }
        }
        windowStringBuilder.append(SiddhiStringBuilderConstants.SEMI_COLON);

        return windowStringBuilder.toString();
    }

}
