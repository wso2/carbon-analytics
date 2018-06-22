package org.wso2.carbon.siddhi.editor.core.util.designview.codegenerator.generators;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.WindowConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.CodeGeneratorConstants;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.SiddhiCodeBuilderConstants;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.CodeGenerationException;

public class WindowCodeGenerator {
    /**
     * Generates a window definition string from a WindowConfig object
     *
     * @param window The WindowConfig object to be converted
     * @return The converted window definition string
     * @throws CodeGenerationException Error while generating code
     */
    public String generateWindow(WindowConfig window) throws CodeGenerationException {
        if (window == null) {
            throw new CodeGenerationException("A given window element is empty");
        } else if (window.getName() == null || window.getName().isEmpty()) {
            throw new CodeGenerationException("The name of a given window element is empty");
        } else if (window.getFunction() == null || window.getFunction().isEmpty()) {
            throw new CodeGenerationException("The function name of the window " + window.getName() + " is empty");
        }

        StringBuilder windowStringBuilder = new StringBuilder();
        windowStringBuilder.append(SubElementCodeGenerator.generateAnnotations(window.getAnnotationList()))
                .append(SiddhiCodeBuilderConstants.DEFINE_WINDOW)
                .append(SiddhiCodeBuilderConstants.SPACE)
                .append(window.getName())
                .append(SiddhiCodeBuilderConstants.SPACE)
                .append(SiddhiCodeBuilderConstants.OPEN_BRACKET)
                .append(SubElementCodeGenerator.generateAttributes(window.getAttributeList()))
                .append(SiddhiCodeBuilderConstants.CLOSE_BRACKET)
                .append(SiddhiCodeBuilderConstants.SPACE)
                .append(window.getFunction())
                .append(SiddhiCodeBuilderConstants.OPEN_BRACKET)
                .append(SubElementCodeGenerator.generateParameterList(window.getParameters()))
                .append(SiddhiCodeBuilderConstants.CLOSE_BRACKET);

        if (window.getOutputEventType() != null && !window.getOutputEventType().isEmpty()) {
            windowStringBuilder.append(SiddhiCodeBuilderConstants.SPACE)
                    .append(SiddhiCodeBuilderConstants.OUTPUT)
                    .append(SiddhiCodeBuilderConstants.SPACE);
            switch (window.getOutputEventType().toUpperCase()) {
                case CodeGeneratorConstants.CURRENT_EVENTS:
                    windowStringBuilder.append(SiddhiCodeBuilderConstants.CURRENT_EVENTS);
                    break;
                case CodeGeneratorConstants.EXPIRED_EVENTS:
                    windowStringBuilder.append(SiddhiCodeBuilderConstants.EXPIRED_EVENTS);
                    break;
                case CodeGeneratorConstants.ALL_EVENTS:
                    windowStringBuilder.append(SiddhiCodeBuilderConstants.ALL_EVENTS);
                    break;
                default:
                    throw new CodeGenerationException("Unidentified window output event type: "
                            + window.getOutputEventType());
            }
        }
        windowStringBuilder.append(SiddhiCodeBuilderConstants.SEMI_COLON);

        return windowStringBuilder.toString();
    }

}
