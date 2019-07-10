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

/**
 * Generates the code for a sub-element of a Siddhi element
 */
public class SubElementCodeGenerator {

    /**
     * Generates the Siddhi code representation of a CommentCodeSegment object
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
     * Generates the Siddhi code representation of the query's name
     *
     * @param queryName The Siddhi query's name
     * @return The Siddhi code representation of a Siddhi query name annotation
     */
    public static String generateQueryName(String queryName) {
        StringBuilder queryNameStringBuilder = new StringBuilder();
        if (queryName != null && !queryName.isEmpty()) {
            queryNameStringBuilder.append(SiddhiCodeBuilderConstants.QUERY_NAME_ANNOTATION)
                    .append(queryName)
                    .append(SiddhiCodeBuilderConstants.SINGLE_QUOTE)
                    .append(SiddhiCodeBuilderConstants.CLOSE_BRACKET);
        }
        else {
            queryNameStringBuilder.append(SiddhiCodeBuilderConstants.DEFAULT_QUERY_NAME_ANNOTATION);
        }

        return queryNameStringBuilder.toString();
    }

    /**
     * Generates the Siddhi code representation of a AttributeConfig list
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
     * Generates the Siddhi code representation of a annotations list
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
     * Generates the Siddhi code representation of a StoreConfig object
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
                .append(SiddhiCodeBuilderConstants.SINGLE_QUOTE);
        List<String> options = store.getOptions();
        storeStringBuilder.append(SiddhiCodeBuilderConstants.COMMA);
        storeStringBuilder.append(generateParameterList(options));
        storeStringBuilder.append(SiddhiCodeBuilderConstants.CLOSE_BRACKET);

        return storeStringBuilder.toString();
    }

    /**
     * Generates the Siddhi code representation of a parameters list
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
     * Generates the Siddhi code representation of a StreamHandlerConfig list
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
     * Generates the Siddhi code representation of a StreamHandlerConfig object
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
     * Generates the Siddhi code representation of a output event type
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

    /**
     *Converts an io.siddhi.query.api.annotation.Element in string format to another string with escape characters
     * which is the siddhi app code representation of the Element.
     * @param elementStr The Element in String format. E.g. title = "The wonder of "foo""
     * @return The code representation of the Element. E.g. title = """The wonder of "foo""""
     */
    private static String toStringWithEscapeChars(String elementStr) {
        String key = null, value;
        if (elementStr == null || elementStr.isEmpty()) {
            throw new IllegalArgumentException("Input string is either null or empty.");
        }
        String[] keyValuePair = elementStr.split(SiddhiCodeBuilderConstants.ELEMENT_KEY_VALUE_SEPARATOR);
        if (keyValuePair.length == 1) {
            value = keyValuePair[0].trim().substring(1, keyValuePair[0].length() - 1);
        } else if (keyValuePair.length == 2) {
            key = keyValuePair[0];
            value = keyValuePair[1].trim().substring(1, keyValuePair[1].length() - 1);
        } else {
            throw new IllegalArgumentException("Could not convert to Element object. String format is invalid: "
                    + elementStr);
        }

        StringBuilder valueWithQuotes = new StringBuilder();
        if (value != null && (value.contains("\"") || value.contains("\n"))) {
            valueWithQuotes.append(SiddhiCodeBuilderConstants.ESCAPE_SEQUENCE).append(value)
                    .append(SiddhiCodeBuilderConstants.ESCAPE_SEQUENCE);
        } else {
            valueWithQuotes.append(SiddhiCodeBuilderConstants.DOUBLE_QUOTE).append(value)
                    .append(SiddhiCodeBuilderConstants.DOUBLE_QUOTE);
        }
        if (key != null) {
            return key + SiddhiCodeBuilderConstants.ELEMENT_KEY_VALUE_SEPARATOR + valueWithQuotes.toString();
        } else {
            return valueWithQuotes.toString();
        }
    }
}
