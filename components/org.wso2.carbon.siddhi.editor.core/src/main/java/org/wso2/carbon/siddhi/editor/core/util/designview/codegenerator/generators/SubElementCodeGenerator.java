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

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.CommentCodeSegment;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.AttributeConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.StoreConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.streamhandler.FilterConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.streamhandler.FunctionWindowConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.streamhandler.StreamHandlerConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.CodeGeneratorConstants;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.SiddhiCodeBuilderConstants;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.CodeGenerationException;
import org.wso2.carbon.siddhi.editor.core.util.designview.utilities.CodeGeneratorUtils;

import java.util.List;
import java.util.Map;

/**
 * Generate's the code for a sub-element of a Siddhi element
 */
public class SubElementCodeGenerator {

    /**
     * Generate's the Siddhi code representation of a CommentCodeSegment object
     *
     * @param comment The CommentCodeSegment object
     * @return The Siddhi code representation of the given CommentCodeSegment object
     * @throws CodeGenerationException Error when generating the code
     */
    public static String generateComment(CommentCodeSegment comment) throws CodeGenerationException {
        if (comment == null) {
            return SiddhiCodeBuilderConstants.EMPTY_STRING;
        } else if (comment.getContent() == null || comment.getContent().isEmpty()) {
            throw new CodeGenerationException("The content of a given comment object is empty");
        }
        return comment.getContent();
    }

    /**
     * Generate's the Siddhi code representation of a AttributeConfig list
     *
     * @param attributes The AttributeConfig list
     * @return The Siddhi code representation of the given AttributeConfig list
     * @throws CodeGenerationException Error when generating the code
     */
    static String generateAttributes(List<AttributeConfig> attributes) throws CodeGenerationException {
        if (attributes == null || attributes.isEmpty()) {
            throw new CodeGenerationException("A given attribute list is empty");
        }

        StringBuilder stringBuilder = new StringBuilder();
        int attributesLeft = attributes.size();
        for (AttributeConfig attribute : attributes) {
            CodeGeneratorUtils.NullValidator.validateConfigObject(attribute);

            stringBuilder.append(attribute.getName())
                    .append(SiddhiCodeBuilderConstants.SPACE)
                    .append(attribute.getType().toLowerCase());
            if (attributesLeft != 1) {
                stringBuilder.append(SiddhiCodeBuilderConstants.COMMA);
            }
            attributesLeft--;
        }
        return stringBuilder.toString();
    }

    /**
     * Generate's the Siddhi code representation of a annotations list
     *
     * @param annotations The annotations list
     * @return The Siddhi code representation of the given annotations list
     */
    public static String generateAnnotations(List<String> annotations) {
        if (annotations == null || annotations.isEmpty()) {
            return SiddhiCodeBuilderConstants.EMPTY_STRING;
        }

        StringBuilder annotationsStringBuilder = new StringBuilder();
        for (String annotation : annotations) {
            annotationsStringBuilder.append(annotation);
        }

        return annotationsStringBuilder.toString();
    }

    /**
     * Generate's the Siddhi code representation of a StoreConfig object
     *
     * @param store The StoreConfig object
     * @return The Siddhi code representation of the given StoreConfig object
     * @throws CodeGenerationException Error when generating the code
     */
    static String generateStore(StoreConfig store) throws CodeGenerationException {
        if (store == null) {
            return SiddhiCodeBuilderConstants.EMPTY_STRING;
        }
        CodeGeneratorUtils.NullValidator.validateConfigObject(store);

        StringBuilder storeStringBuilder = new StringBuilder();

        storeStringBuilder.append(SiddhiCodeBuilderConstants.STORE_ANNOTATION)
                .append(store.getType())
                .append(SiddhiCodeBuilderConstants.SINGLE_QUOTE)
                .append(SiddhiCodeBuilderConstants.COMMA);
        Map<String, String> options = store.getOptions();
        int optionsLeft = options.size();
        for (Map.Entry<String, String> entry : options.entrySet()) {
            storeStringBuilder.append(entry.getKey())
                    .append(SiddhiCodeBuilderConstants.EQUAL)
                    .append(SiddhiCodeBuilderConstants.SINGLE_QUOTE)
                    .append(entry.getValue())
                    .append(SiddhiCodeBuilderConstants.SINGLE_QUOTE);
            if (optionsLeft != 1) {
                storeStringBuilder.append(SiddhiCodeBuilderConstants.COMMA);
            }
            optionsLeft--;
        }
        storeStringBuilder.append(SiddhiCodeBuilderConstants.CLOSE_BRACKET);

        return storeStringBuilder.toString();
    }

    /**
     * Generate's the Siddhi code representation of a parameters list
     *
     * @param parameters The parameters list
     * @return The Siddhi code representation of the given parameters list
     */
    public static String generateParameterList(List<String> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return SiddhiCodeBuilderConstants.EMPTY_STRING;
        }

        StringBuilder parametersStringBuilder = new StringBuilder();
        int parametersLeft = parameters.size();
        for (String parameter : parameters) {
            parametersStringBuilder.append(parameter);
            if (parametersLeft != 1) {
                parametersStringBuilder.append(SiddhiCodeBuilderConstants.COMMA);
            }
            parametersLeft--;
        }

        return parametersStringBuilder.toString();
    }

    /**
     * Generate's the Siddhi code representation of a StreamHandlerConfig list
     *
     * @param streamHandlerList The StreamHandlerConfig list
     * @return The Siddhi code representation of the given StreamHandlerConfig list
     * @throws CodeGenerationException Error when generating the code
     */
    public static String generateStreamHandlerList(List<StreamHandlerConfig> streamHandlerList)
            throws CodeGenerationException {
        if (streamHandlerList == null || streamHandlerList.isEmpty()) {
            return SiddhiCodeBuilderConstants.EMPTY_STRING;
        }

        StringBuilder streamHandlerListStringBuilder = new StringBuilder();
        for (StreamHandlerConfig streamHandler : streamHandlerList) {
            streamHandlerListStringBuilder.append(generateStreamHandler(streamHandler));
        }

        return streamHandlerListStringBuilder.toString();
    }

    /**
     * Generate's the Siddhi code representation of a StreamHandlerConfig object
     *
     * @param streamHandler The StreamHandlerConfig object
     * @return The Siddhi code representation of the given StreamHandlerConfig object
     * @throws CodeGenerationException Error when generating the code
     */
    private static String generateStreamHandler(StreamHandlerConfig streamHandler) throws CodeGenerationException {
        CodeGeneratorUtils.NullValidator.validateConfigObject(streamHandler);

        StringBuilder streamHandlerStringBuilder = new StringBuilder();

        switch (streamHandler.getType().toUpperCase()) {
            case CodeGeneratorConstants.FILTER:
                FilterConfig filter = (FilterConfig) streamHandler;
                streamHandlerStringBuilder.append(SiddhiCodeBuilderConstants.OPEN_SQUARE_BRACKET)
                        .append(filter.getValue())
                        .append(SiddhiCodeBuilderConstants.CLOSE_SQUARE_BRACKET);
                break;
            case CodeGeneratorConstants.FUNCTION:
                FunctionWindowConfig function = (FunctionWindowConfig) streamHandler;
                streamHandlerStringBuilder.append(SiddhiCodeBuilderConstants.HASH)
                        .append(function.getValue().getFunction())
                        .append(SiddhiCodeBuilderConstants.OPEN_BRACKET)
                        .append(generateParameterList(function.getValue().getParameters()))
                        .append(SiddhiCodeBuilderConstants.CLOSE_BRACKET);
                break;
            case CodeGeneratorConstants.WINDOW:
                FunctionWindowConfig window = (FunctionWindowConfig) streamHandler;
                streamHandlerStringBuilder.append(SiddhiCodeBuilderConstants.HASH)
                        .append(SiddhiCodeBuilderConstants.WINDOW)
                        .append(SiddhiCodeBuilderConstants.FULL_STOP)
                        .append(window.getValue().getFunction())
                        .append(SiddhiCodeBuilderConstants.OPEN_BRACKET)
                        .append(generateParameterList(window.getValue().getParameters()))
                        .append(SiddhiCodeBuilderConstants.CLOSE_BRACKET);
                break;
            default:
                throw new CodeGenerationException("Unidentified stream handler type: " + streamHandler.getType());
        }

        return streamHandlerStringBuilder.toString();
    }

    /**
     * Generate's the Siddhi code representation of a output event type
     *
     * @param eventType The output event type
     * @return The Siddhi code representation of the given output event type
     * @throws CodeGenerationException Error when generating the code
     */
    public static String generateForEventType(String eventType) throws CodeGenerationException {
        if (eventType == null || eventType.isEmpty()) {
            return SiddhiCodeBuilderConstants.EMPTY_STRING;
        }

        StringBuilder forEventTypeStringBuilder = new StringBuilder();
        forEventTypeStringBuilder.append(SiddhiCodeBuilderConstants.SPACE)
                .append(SiddhiCodeBuilderConstants.FOR)
                .append(SiddhiCodeBuilderConstants.SPACE);

        switch (eventType.toUpperCase()) {
            case CodeGeneratorConstants.CURRENT_EVENTS:
                forEventTypeStringBuilder.append(SiddhiCodeBuilderConstants.CURRENT_EVENTS);
                break;
            case CodeGeneratorConstants.EXPIRED_EVENTS:
                forEventTypeStringBuilder.append(SiddhiCodeBuilderConstants.EXPIRED_EVENTS);
                break;
            case CodeGeneratorConstants.ALL_EVENTS:
                forEventTypeStringBuilder.append(SiddhiCodeBuilderConstants.ALL_EVENTS);
                break;
            default:
                throw new CodeGenerationException("Unidentified 'for' event type: " + eventType);
        }

        return forEventTypeStringBuilder.toString();
    }

    private SubElementCodeGenerator() {
    }

}
