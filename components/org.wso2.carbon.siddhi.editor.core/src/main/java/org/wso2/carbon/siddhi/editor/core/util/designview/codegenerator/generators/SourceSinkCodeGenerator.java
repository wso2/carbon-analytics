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

package org.wso2.carbon.siddhi.editor.core.util.designview.codegenerator.generators;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.sourcesink.SourceSinkConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.sourcesink.mapper.MapperConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.sourcesink.mapper.attribute.MapperListPayloadOrAttribute;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.sourcesink.mapper.attribute.MapperMapPayloadOrAttribute;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.sourcesink.mapper.attribute.MapperPayloadOrAttribute;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.CodeGeneratorConstants;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.SiddhiCodeBuilderConstants;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.CodeGenerationException;
import org.wso2.carbon.siddhi.editor.core.util.designview.utilities.CodeGeneratorUtils;

import java.util.Map;

/**
 * Generate's the code for a Siddhi source/sink element
 */
public class SourceSinkCodeGenerator {

    /**
     * Generate's the Siddhi code representation of a SourceSinkConfig object
     *
     * @param sourceSink The SourceSinkConfig object
     * @return The Siddhi code representation of the given SourceSinkConfig object
     * @throws CodeGenerationException Error when generating the code
     */
    public String generateSourceSink(SourceSinkConfig sourceSink) throws CodeGenerationException {
        CodeGeneratorUtils.NullValidator.validateConfigObject(sourceSink);

        StringBuilder sourceSinkStringBuilder = new StringBuilder();
        sourceSinkStringBuilder.append(
                SubElementCodeGenerator.generateComment(sourceSink.getPreviousCommentSegment()));
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
     * Generate's the Siddhi code representation of a MapperConfig object
     *
     * @param mapper The MapperConfig object
     * @return The Siddhi code representation of the given MapperConfig object
     * @throws CodeGenerationException Error when generating the code
     */
    private String generateMapper(MapperConfig mapper) throws CodeGenerationException {
        CodeGeneratorUtils.NullValidator.validateConfigObject(mapper);

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
     * Generate's the Siddhi code representation of a MapperPayloadOrAttribute object
     *
     * @param payloadOrAttribute The MapperPayloadOrAttribute object
     * @return The Siddhi code representation of the given MapperConfig object
     * @throws CodeGenerationException Error when generating the code
     */
    private String generateMapperPayloadOrAttribute(MapperPayloadOrAttribute payloadOrAttribute)
            throws CodeGenerationException {
        CodeGeneratorUtils.NullValidator.validateConfigObject(payloadOrAttribute);

        StringBuilder mapperAttributeStringBuilder = new StringBuilder();
        switch (payloadOrAttribute.getType().toUpperCase()) {
            case CodeGeneratorConstants.MAP:
                mapperAttributeStringBuilder.append(
                        generateMapPayloadOrAttribute((MapperMapPayloadOrAttribute) payloadOrAttribute));
                break;
            case CodeGeneratorConstants.LIST:
                mapperAttributeStringBuilder.append(
                        generateListPayloadOrAttribute((MapperListPayloadOrAttribute) payloadOrAttribute));
                break;
            default:
                throw new CodeGenerationException("Unidentified mapper attribute type: "
                        + payloadOrAttribute.getType());
        }

        return mapperAttributeStringBuilder.toString();
    }

    /**
     * Generate's the Siddhi code representation of a MapperListPayloadOrAttribute object
     *
     * @param mapperListAttribute The MapperListPayloadOrAttribute object
     * @return The Siddhi code representation of the given MapperListPayloadOrAttribute object
     * @throws CodeGenerationException Error when generating the code
     */
    private String generateListPayloadOrAttribute(MapperListPayloadOrAttribute mapperListAttribute)
            throws CodeGenerationException {
        CodeGeneratorUtils.NullValidator.validateConfigObject(mapperListAttribute);

        StringBuilder mapperListAttributeStringBuilder = new StringBuilder();
        int valuesLeft = mapperListAttribute.getValue().size();
        for (String value : mapperListAttribute.getValue()) {
            mapperListAttributeStringBuilder.append(SiddhiCodeBuilderConstants.DOUBLE_QUOTE)
                    .append(value)
                    .append(SiddhiCodeBuilderConstants.DOUBLE_QUOTE);
            if (valuesLeft != 1) {
                mapperListAttributeStringBuilder.append(SiddhiCodeBuilderConstants.COMMA);
            }
            valuesLeft--;
        }

        return mapperListAttributeStringBuilder.toString();
    }

    /**
     * Generate's the Siddhi code representation of a MapperMapPayloadOrAttribute object
     *
     * @param mapperMapAttribute The MapperMapPayloadOrAttribute object
     * @return The Siddhi code representation of the given MapperMapPayloadOrAttribute object
     * @throws CodeGenerationException Error when generating the code
     */
    private String generateMapPayloadOrAttribute(MapperMapPayloadOrAttribute mapperMapAttribute)
            throws CodeGenerationException {
        CodeGeneratorUtils.NullValidator.validateConfigObject(mapperMapAttribute);

        StringBuilder mapperMapAttributeStringBuilder = new StringBuilder();
        int mapEntriesLeft = mapperMapAttribute.getValue().size();
        for (Map.Entry<String, String> entry : mapperMapAttribute.getValue().entrySet()) {
            mapperMapAttributeStringBuilder.append(entry.getKey())
                    .append(SiddhiCodeBuilderConstants.EQUAL)
                    .append(SiddhiCodeBuilderConstants.SINGLE_QUOTE)
                    .append(entry.getValue())
                    .append(SiddhiCodeBuilderConstants.SINGLE_QUOTE);
            if (mapEntriesLeft != 1) {
                mapperMapAttributeStringBuilder.append(SiddhiCodeBuilderConstants.COMMA);
            }
            mapEntriesLeft--;
        }

        return mapperMapAttributeStringBuilder.toString();
    }

}
