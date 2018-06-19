package org.wso2.carbon.siddhi.editor.core.util.designview.codegenerator.generators;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.sourcesink.SourceSinkConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.CodeGeneratorConstants;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.SiddhiStringBuilderConstants;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.CodeGenerationException;
import org.wso2.carbon.siddhi.editor.core.util.designview.utilities.CodeGeneratorHelper;

public class SourceSinkCodeGenerator {

    /**
     * Generates a source/sink definition string from a SourceSinkConfig object
     *
     * @param sourceSink The SourceSinkConfig object to be converted
     * @return The converted source/sink definition string
     * @throws CodeGenerationException Error while generating code
     */
    private String generateSourceSinkString(SourceSinkConfig sourceSink) throws CodeGenerationException {
        if (sourceSink == null) {
            throw new CodeGenerationException("A given source/sink element is empty");
        } else if (sourceSink.getAnnotationType() == null || sourceSink.getAnnotationType().isEmpty()) {
            throw new CodeGenerationException("The annotation type for a given source/sink element is empty");
        } else if (sourceSink.getType() == null || sourceSink.getType().isEmpty()) {
            throw new CodeGenerationException("The type attribute for a given source/sink element is empty");
        }

        StringBuilder sourceSinkStringBuilder = new StringBuilder();
        if (sourceSink.getAnnotationType().equalsIgnoreCase(CodeGeneratorConstants.SOURCE)) {
            sourceSinkStringBuilder.append(SiddhiStringBuilderConstants.SOURCE_ANNOTATION);
        } else if (sourceSink.getAnnotationType().equalsIgnoreCase(CodeGeneratorConstants.SINK)) {
            sourceSinkStringBuilder.append(SiddhiStringBuilderConstants.SINK_ANNOTATION);
        } else {
            throw new CodeGenerationException("Unidentified source/sink type: " + sourceSink.getType());
        }

        sourceSinkStringBuilder.append(sourceSink.getType())
                .append(SiddhiStringBuilderConstants.SINGLE_QUOTE);

        if (sourceSink.getOptions() != null && !sourceSink.getOptions().isEmpty()) {
            sourceSinkStringBuilder.append(SiddhiStringBuilderConstants.COMMA)
                    .append(CodeGeneratorHelper.getParameterList(sourceSink.getOptions()));
        }

        if (sourceSink.getMap() != null) {
            sourceSinkStringBuilder.append(SiddhiStringBuilderConstants.COMMA)
                    .append(CodeGeneratorHelper.getMapper(sourceSink.getMap()));
        }

        sourceSinkStringBuilder.append(SiddhiStringBuilderConstants.CLOSE_BRACKET);

        return sourceSinkStringBuilder.toString();
    }

}
