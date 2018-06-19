package org.wso2.carbon.siddhi.editor.core.util.designview.codegenerator.generators;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.StreamConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.SiddhiStringBuilderConstants;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.CodeGenerationException;
import org.wso2.carbon.siddhi.editor.core.util.designview.utilities.CodeGeneratorHelper;

public class StreamCodeGenerator {

    /**
     * Generates a stream definition string from a StreamConfig object
     *
     * @param stream The StreamConfig object to be converted
     * @return The converted stream definition string
     * @throws CodeGenerationException Error while generating code
     */
    public String generateStreamCode(StreamConfig stream) throws CodeGenerationException {
        if (stream == null) {
            throw new CodeGenerationException("A given stream element is empty");
        } else if (stream.getName() == null || stream.getName().isEmpty()) {
            throw new CodeGenerationException("The name of a given stream element is empty");
        }

        return CodeGeneratorHelper.getAnnotations(stream.getAnnotationList()) +
                SiddhiStringBuilderConstants.DEFINE_STREAM +
                SiddhiStringBuilderConstants.SPACE +
                stream.getName() +
                SiddhiStringBuilderConstants.SPACE +
                SiddhiStringBuilderConstants.OPEN_BRACKET +
                CodeGeneratorHelper.getAttributes(stream.getAttributeList()) +
                SiddhiStringBuilderConstants.CLOSE_BRACKET +
                SiddhiStringBuilderConstants.SEMI_COLON;
    }
}
