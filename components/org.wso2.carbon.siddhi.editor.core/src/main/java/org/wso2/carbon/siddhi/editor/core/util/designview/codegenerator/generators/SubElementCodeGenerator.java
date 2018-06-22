package org.wso2.carbon.siddhi.editor.core.util.designview.codegenerator.generators;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.AttributeConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.StoreConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.streamhandler.FilterConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.streamhandler.FunctionWindowConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.streamhandler.StreamHandlerConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.CodeGeneratorConstants;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.SiddhiCodeBuilderConstants;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.CodeGenerationException;

import java.util.List;
import java.util.Map;

public class SubElementCodeGenerator {

    /**
     * Generates a string representation of a list of attributes for a definition
     * Attributes Example - (name string, age int, ...)
     *
     * @param attributes The list of AttributeConfig objects to be converted
     * @return The converted attributes as a string
     * @throws CodeGenerationException Error while generating code
     */
    public static String generateAttributes(List<AttributeConfig> attributes) throws CodeGenerationException {
        if (attributes == null || attributes.isEmpty()) {
            throw new CodeGenerationException("A given attribute list is empty");
        }

        StringBuilder stringBuilder = new StringBuilder();
        int attributesLeft = attributes.size();
        for (AttributeConfig attribute : attributes) {
            if (attribute == null) {
                throw new CodeGenerationException("A given attribute element is empty");
            } else if (attribute.getName() == null || attribute.getName().isEmpty()) {
                throw new CodeGenerationException("The 'name' of a given attribute element is empty");
            } else if (attribute.getType() == null || attribute.getType().isEmpty()) {
                throw new CodeGenerationException("The 'type' value of a given attribute element is empty");
            }
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
     * Generates a string representation of the annotations of a Siddhi element
     *
     * @param annotations The list of AnnotationConfig objects to be converted
     * @return The string representation of all the annotations
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
     * Generates a string representation of a Siddhi store annotation
     *
     * @param store The StoreConfig instance to be converted
     * @return The string representation of a store annotation
     * @throws CodeGenerationException Error while generating code
     */
    public static String generateStore(StoreConfig store) throws CodeGenerationException {
        if (store == null) {
            return SiddhiCodeBuilderConstants.EMPTY_STRING;
        } else if (store.getType() == null || store.getType().isEmpty()) {
            throw new CodeGenerationException("The 'type' value of a given store element is empty");
        } else if (store.getOptions() == null || store.getOptions().isEmpty()) {
            throw new CodeGenerationException("The options map of a given store element is empty");
        }

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
     * Generate a string of parameters for a specific window/function
     * Example of parameter: (10 min, 4, 'regex', ...)
     *
     * @param parameters The list of parameters to be converted
     * @return The string of the parameters separated by a comma(,)
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
     * Generates a Siddhi string representation of a list of StreamHandlerConfig objects
     *
     * @param streamHandlerList The list of StreamHandlerConfig objects to be converted
     * @return The result string representation of the list of StreamHandlerConfig objects
     * @throws CodeGenerationException Error while generating code
     */
    public static String generateStreamHandlerList(List<StreamHandlerConfig> streamHandlerList)
            throws CodeGenerationException {
        if (streamHandlerList == null || streamHandlerList.isEmpty()) {
            return SiddhiCodeBuilderConstants.EMPTY_STRING;
        }

        StringBuilder streamhandlerListStringBuilder = new StringBuilder();

        for (StreamHandlerConfig streamHandler : streamHandlerList) {
            streamhandlerListStringBuilder.append(generateStreamHandler(streamHandler));
        }

        return streamhandlerListStringBuilder.toString();
    }

    /**
     * Generates a Siddhi string representation of a given StreamHandlerConfig object
     *
     * @param streamHandler The StreamHandlerConfig object to be converted
     * @return The string representation of the given StreamHandlerConfig object
     * @throws CodeGenerationException Error while generating code
     */
    public static String generateStreamHandler(StreamHandlerConfig streamHandler) throws CodeGenerationException {
        if (streamHandler == null) {
            throw new CodeGenerationException("A given stream handler element is empty");
        } else if (streamHandler.getType() == null || streamHandler.getType().isEmpty()) {
            throw new CodeGenerationException("The 'type' value of a given stream handler element is empty");
        }

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
     * Generates a Siddhi string representation for a 'for <eventType>' output,
     * where the <eventType> can be 'CURRENT_EVENTS', 'EXPIRED_EVENTS' or 'ALL_EVENTS'
     *
     * @param eventType The event type that is used
     * @return The string representation for the event type in Siddhi
     * @throws CodeGenerationException Error while generating code
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
