package org.wso2.carbon.siddhi.editor.core.util.designview.codegenerator.generators;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.sourcesink.SourceSinkConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.sourcesink.mapper.MapperConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.sourcesink.mapper.attribute.MapperListPayloadOrAttribute;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.sourcesink.mapper.attribute.MapperMapPayloadOrAttribute;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.sourcesink.mapper.attribute.MapperPayloadOrAttribute;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.CodeGeneratorConstants;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.SiddhiCodeBuilderConstants;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.CodeGenerationException;

import java.util.Map;

public class SourceSinkCodeGenerator {

    /**
     * Generates a source/sink definition string from a SourceSinkConfig object
     *
     * @param sourceSink The SourceSinkConfig object to be converted
     * @return The converted source/sink definition string
     * @throws CodeGenerationException Error while generating code
     */
    public String generateSourceSink(SourceSinkConfig sourceSink) throws CodeGenerationException {
        if (sourceSink == null) {
            throw new CodeGenerationException("A given source/sink element is empty");
        } else if (sourceSink.getAnnotationType() == null || sourceSink.getAnnotationType().isEmpty()) {
            throw new CodeGenerationException("The annotation type for a given source/sink element is empty");
        } else if (sourceSink.getType() == null || sourceSink.getType().isEmpty()) {
            throw new CodeGenerationException("The type attribute for a given source/sink element is empty");
        }

        StringBuilder sourceSinkStringBuilder = new StringBuilder();
        if (sourceSink.getAnnotationType().equalsIgnoreCase(CodeGeneratorConstants.SOURCE)) {
            sourceSinkStringBuilder.append(SiddhiCodeBuilderConstants.SOURCE_ANNOTATION);
        } else if (sourceSink.getAnnotationType().equalsIgnoreCase(CodeGeneratorConstants.SINK)) {
            sourceSinkStringBuilder.append(SiddhiCodeBuilderConstants.SINK_ANNOTATION);
        } else {
            throw new CodeGenerationException("Unidentified source/sink type: " + sourceSink.getType());
        }

        sourceSinkStringBuilder.append(sourceSink.getType())
                .append(SiddhiCodeBuilderConstants.SINGLE_QUOTE);

        if (sourceSink.getOptions() != null && !sourceSink.getOptions().isEmpty()) {
            sourceSinkStringBuilder.append(SiddhiCodeBuilderConstants.COMMA)
                    .append(SubElementCodeGenerator.generateParameterList(sourceSink.getOptions()));
        }

        if (sourceSink.getMap() != null) {
            sourceSinkStringBuilder.append(SiddhiCodeBuilderConstants.COMMA)
                    .append(generateMapper(sourceSink.getMap()));
        }

        sourceSinkStringBuilder.append(SiddhiCodeBuilderConstants.CLOSE_BRACKET);

        return sourceSinkStringBuilder.toString();
    }

    /**
     * Generates a Siddhi string representation of a map annotation from a MapperConfig object
     *
     * @param mapper The MapperConfig object to be converted
     * @return The string representation of the MapperConfig object
     * @throws CodeGenerationException Error while generating code
     */
    private String generateMapper(MapperConfig mapper) throws CodeGenerationException {
        if (mapper.getType() == null || mapper.getType().isEmpty()) {
            throw new CodeGenerationException("The map type of a given source/sink map element is empty");
        }

        StringBuilder mapperStringBuilder = new StringBuilder();
        mapperStringBuilder.append(SiddhiCodeBuilderConstants.MAP_ANNOTATION)
                .append(mapper.getType())
                .append(SiddhiCodeBuilderConstants.SINGLE_QUOTE);

        if (mapper.getOptions() != null && !mapper.getOptions().isEmpty()) {
            mapperStringBuilder.append(SiddhiCodeBuilderConstants.COMMA)
                    .append(SubElementCodeGenerator.generateParameterList(mapper.getOptions()));
        }

        if (mapper.getPayloadOrAttribute() != null) {
            mapperStringBuilder.append(SiddhiCodeBuilderConstants.COMMA);
            if (mapper.getPayloadOrAttribute().getAnnotationType()
                    .equalsIgnoreCase(CodeGeneratorConstants.ATTRIBUTE)) {
                mapperStringBuilder.append(SiddhiCodeBuilderConstants.ATTRIBUTES_ANNOTATION);
            } else if (mapper.getPayloadOrAttribute().getAnnotationType()
                    .equalsIgnoreCase(CodeGeneratorConstants.PAYLOAD)) {
                mapperStringBuilder.append(SiddhiCodeBuilderConstants.PAYLOAD_ANNOTATION);
            }
            mapperStringBuilder.append(generateMapperPayloadOrAttribute(mapper.getPayloadOrAttribute()))
                    .append(SiddhiCodeBuilderConstants.CLOSE_BRACKET);
        }
        mapperStringBuilder.append(SiddhiCodeBuilderConstants.CLOSE_BRACKET);

        return mapperStringBuilder.toString();
    }

    /**
     * Generates a Siddhi string representation of a MapperPayloadOrAttribute object
     *
     * @param payloadOrAttribute The MapperPayloadOrAttribute object to be converted
     * @return A Siddhi string representation of the given MapperPayloadOrAttribute object
     * @throws CodeGenerationException Error while generating code
     */
    private String generateMapperPayloadOrAttribute(MapperPayloadOrAttribute payloadOrAttribute)
            throws CodeGenerationException {
        if (payloadOrAttribute.getType() == null || payloadOrAttribute.getType().isEmpty()) {
            throw new CodeGenerationException("The 'type' value of a given source/sink map attribute element is empty");
        }

        StringBuilder mapperAttributeStringBuilder = new StringBuilder();
        switch (payloadOrAttribute.getType().toUpperCase()) {
            case CodeGeneratorConstants.MAP:
                MapperMapPayloadOrAttribute mapperMapAttribute = (MapperMapPayloadOrAttribute) payloadOrAttribute;
                if (mapperMapAttribute.getValue() == null || mapperMapAttribute.getValue().isEmpty()) {
                    throw new CodeGenerationException("The key-value pair values of" +
                            " a given source/sink map attribute element is empty");
                }
                int mapEntriesLeft = mapperMapAttribute.getValue().size();
                for (Map.Entry<String, String> entry : mapperMapAttribute.getValue().entrySet()) {
                    mapperAttributeStringBuilder.append(entry.getKey())
                            .append(SiddhiCodeBuilderConstants.EQUAL)
                            .append(SiddhiCodeBuilderConstants.SINGLE_QUOTE)
                            .append(entry.getValue())
                            .append(SiddhiCodeBuilderConstants.SINGLE_QUOTE);
                    if (mapEntriesLeft != 1) {
                        mapperAttributeStringBuilder.append(SiddhiCodeBuilderConstants.COMMA);
                    }
                    mapEntriesLeft--;
                }
                break;
            case CodeGeneratorConstants.LIST:
                MapperListPayloadOrAttribute mapperListAttribute = (MapperListPayloadOrAttribute) payloadOrAttribute;
                if (mapperListAttribute.getValue() == null || mapperListAttribute.getValue().isEmpty()) {
                    throw new CodeGenerationException("The list values of a given sink/source" +
                            " map attribute element is empty");
                }
                int valuesLeft = mapperListAttribute.getValue().size();
                for (String value : mapperListAttribute.getValue()) {
                    mapperAttributeStringBuilder.append(SiddhiCodeBuilderConstants.DOUBLE_QUOTE)
                            .append(value)
                            .append(SiddhiCodeBuilderConstants.DOUBLE_QUOTE);
                    if (valuesLeft != 1) {
                        mapperAttributeStringBuilder.append(SiddhiCodeBuilderConstants.COMMA);
                    }
                    valuesLeft--;
                }
                break;
            default:
                throw new CodeGenerationException("Unidentified mapper attribute type: "
                        + payloadOrAttribute.getType());
        }

        return mapperAttributeStringBuilder.toString();
    }

}
