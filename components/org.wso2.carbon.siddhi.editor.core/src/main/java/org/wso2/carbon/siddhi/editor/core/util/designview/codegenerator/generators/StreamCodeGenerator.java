package org.wso2.carbon.siddhi.editor.core.util.designview.codegenerator.generators;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.StreamConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.SiddhiCodeBuilderConstants;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.CodeGenerationException;

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
