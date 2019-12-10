/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.siddhi.distribution.editor.core.util;

import io.siddhi.annotation.Example;
import io.siddhi.annotation.Extension;
import io.siddhi.annotation.Parameter;
import io.siddhi.annotation.ParameterOverload;
import io.siddhi.annotation.ReturnAttribute;
import io.siddhi.annotation.util.DataType;
import io.siddhi.core.SiddhiAppRuntime;
import io.siddhi.distribution.editor.core.commons.metadata.AttributeMetaData;
import io.siddhi.distribution.editor.core.commons.metadata.ExampleMetaData;
import io.siddhi.distribution.editor.core.commons.metadata.MetaData;
import io.siddhi.distribution.editor.core.commons.metadata.ParameterMetaData;
import io.siddhi.distribution.editor.core.commons.metadata.ProcessorMetaData;
import io.siddhi.distribution.editor.core.commons.metadata.SyntaxMetaData;
import io.siddhi.distribution.editor.core.internal.EditorDataHolder;
import io.siddhi.distribution.editor.core.util.designview.constants.SiddhiCodeBuilderConstants;
import io.siddhi.query.api.definition.AbstractDefinition;
import io.siddhi.query.api.definition.AggregationDefinition;
import io.siddhi.query.api.definition.StreamDefinition;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for getting the meta data for the in built and extension processors in Siddhi.
 */
public class SourceEditorUtils {

    static final Logger LOGGER = Logger.getLogger(SourceEditorUtils.class);

    private SourceEditorUtils() {

    }

    /**
     * Validate the siddhi app string using the Siddhi Manager.
     * Will return a valid siddhiAppRuntime
     *
     * @param siddhiApp Siddhi app string
     * @return Valid siddhi app runtime
     */
    //this method need to be added if we need exceptions which are thrown from siddhiAppRuntime.start()
    public static SiddhiAppRuntime validateSiddhiApp(String siddhiApp) {

        SiddhiAppRuntime siddhiAppRuntime = null;
        try {
            siddhiAppRuntime = EditorDataHolder.getSiddhiManager().createSiddhiAppRuntime(siddhiApp);
            siddhiAppRuntime.start();
        } finally {
            if (siddhiAppRuntime != null) {
                siddhiAppRuntime.shutdown();
            }
        }
        return siddhiAppRuntime;
    }

    /**
     * Get the definition of the inner streams in the partitions.
     * Inner streams will be separated based on the partition
     *
     * @param siddhiAppRuntime                  Siddhi app runtime created after validating
     * @param partitionsWithMissingInnerStreams Required inner stream names separated based on partition it belongs to
     * @return The inner stream definitions separated base on the partition it belongs to
     */
    public static List<List<AbstractDefinition>> getInnerStreamDefinitions(SiddhiAppRuntime siddhiAppRuntime,
                                                                           List<List<String>>
                                                                                   partitionsWithMissingInnerStreams) {

        List<List<AbstractDefinition>> innerStreamDefinitions = new ArrayList<>();

        // Transforming the element ID to partition inner streams map to element ID no to partition inner streams map
        Map<Integer, Map<String, AbstractDefinition>> innerStreamsMap = new ConcurrentHashMap<>();
        siddhiAppRuntime.getPartitionedInnerStreamDefinitionMap().entrySet().parallelStream().forEach(
                entry -> innerStreamsMap.put(
                        Integer.valueOf(entry.getKey().split("-")[1]),
                        entry.getValue()
                )
        );

        // Creating an ordered list of partition inner streams based on partition element ID
        // This is important since the client sends the missing inner streams 2D list
        // with partitions in the order they are in the siddhi app
        List<Map<String, AbstractDefinition>> rankedPartitionsWithInnerStreams = new ArrayList<>();
        List<Integer> rankedPartitionElementIds = new ArrayList<>();
        for (Map.Entry<Integer, Map<String, AbstractDefinition>> entry :
                innerStreamsMap.entrySet()) {
            int i = 0;
            for (; i < rankedPartitionsWithInnerStreams.size(); i++) {
                if (entry.getKey() < rankedPartitionElementIds.get(i)) {
                    break;
                }
            }
            rankedPartitionsWithInnerStreams.add(i, entry.getValue());
            rankedPartitionElementIds.add(i, entry.getKey());
        }

        // Extracting the requested stream definitions from based on the order
        // in rankedPartitionsWithInnerStreams and partitionsWithMissingInnerStreams
        // The inner stream definitions 2D list fetched from the Siddhi Manager
        // and the missing inner streams 2D list are now in the same order
        // Therefore the outer loops in both lists can be looped together
        for (int i = 0; i < partitionsWithMissingInnerStreams.size(); i++) {
            List<String> partitionWithMissingInnerStreams = partitionsWithMissingInnerStreams.get(i);
            Map<String, AbstractDefinition> partitionWithInnerStreams = rankedPartitionsWithInnerStreams.get(i);
            List<AbstractDefinition> innerStreamDefinition = new ArrayList<>();

            for (String missingInnerStream : partitionWithMissingInnerStreams) {
                AbstractDefinition streamDefinition = partitionWithInnerStreams.get(missingInnerStream);
                if (streamDefinition != null) {
                    innerStreamDefinition.add(streamDefinition);
                }
            }
            innerStreamDefinitions.add(innerStreamDefinition);
        }

        return innerStreamDefinitions;
    }

    /**
     * Get the definitions of the streams that are requested.
     * used for fetching the definitions of streams that queries output into without defining them first
     *
     * @param siddhiAppRuntime Siddhi app runtime created after validating
     * @param missingStreams   Required stream names
     * @return The stream definitions
     */
    public static List<AbstractDefinition> getStreamDefinitions(SiddhiAppRuntime siddhiAppRuntime,
                                                                List<String> missingStreams) {

        List<AbstractDefinition> streamDefinitions = new ArrayList<>();
        Map<String, StreamDefinition> streamDefinitionMap = siddhiAppRuntime.getStreamDefinitionMap();
        for (String stream : missingStreams) {
            AbstractDefinition streamDefinition = streamDefinitionMap.get(stream);
            if (streamDefinition != null) {
                streamDefinitions.add(streamDefinition);
            }
        }
        return streamDefinitions;
    }

    /**
     * Get the definitions of the aggregations that are requested.
     * used for fetching the definitions of aggregations
     *
     * @param siddhiAppRuntime              Siddhi app runtime created after validating
     * @param missingAggregationDefinitions Required stream names
     * @return The stream definitions
     */
    public static List<AbstractDefinition> getAggregationDefinitions(SiddhiAppRuntime siddhiAppRuntime,
                                                                     List<String> missingAggregationDefinitions) {

        List<AbstractDefinition> aggregationDefinitions = new ArrayList<>();
        Map<String, AggregationDefinition> aggregationDefinitionMap = siddhiAppRuntime.getAggregationDefinitionMap();
        for (String aggregation : missingAggregationDefinitions) {
            AbstractDefinition aggregationDefinition = aggregationDefinitionMap.get(aggregation);
            if (aggregationDefinition != null) {
                aggregationDefinitions.add(aggregationDefinition);
            }
        }
        return aggregationDefinitions;
    }

    /**
     * Returns the in built processor meta data.
     * Scans for all classes in all jars in the classpath
     *
     * @return In-built processor meta data
     */
    public static MetaData getInBuiltProcessorMetaData() {

        Map<String, Set<Class<?>>> processorClassMap = getClassesInClassPathFromPackages();
        return generateInBuiltMetaData(processorClassMap);
    }

    /**
     * Returns the extension processor meta data.
     * Gets the meta data from the siddhi manager
     *
     * @return Extension processor meta data
     */
    public static Map<String, MetaData> getExtensionProcessorMetaData() {

        Map<String, Class> extensionsMap = EditorDataHolder.getSiddhiManager().getExtensions();
        return generateExtensionsMetaData(extensionsMap);
    }

    /**
     * Returns processor types to Classes map with classes in the packages in processor type to package name map.
     *
     * @return Processor types to Classes map
     */
    private static Map<String, Set<Class<?>>> getClassesInClassPathFromPackages() {

        String[] classPathNames = System.getProperty("java.class.path").split(File.pathSeparator);
        Map<String, Set<Class<?>>> classSetMap = new HashMap<>();
        // Looping the jars
        for (String classPathName : classPathNames) {
            if (classPathName.endsWith(".jar")) {
                JarInputStream stream = null;
                try {
                    stream = new JarInputStream(new FileInputStream(classPathName));
                    JarEntry jarEntry = stream.getNextJarEntry();
                    // Looping the classes in jar to get classes in the specified package
                    while (jarEntry != null) {
                        /*
                         * Path separator for linux and windows machines needs to be replaces separately
                         * The path separator in the jar entries depends on the machine where the jar was built
                         */
                        String jarEntryName = jarEntry.getName().replace("/", ".");
                        jarEntryName = jarEntryName.replace("\\", ".");

                        try {
                            // Looping the set of packages
                            for (Map.Entry<String, String> entry : Constants.PACKAGE_NAME_MAP.entrySet()) {
                                if (jarEntryName.endsWith(".class") && jarEntryName.startsWith(entry.getValue())) {
                                    Set<Class<?>> classSet = classSetMap.get(entry.getKey());
                                    if (classSet == null) {
                                        classSet = new HashSet<>();
                                        classSetMap.put(entry.getKey(), classSet);
                                    }
                                    classSet.add(Class.forName(jarEntryName.substring(0, jarEntryName.length() - 6)));
                                }
                            }
                        } catch (ClassNotFoundException e) {
                            LOGGER.debug("Failed to load class " +
                                    jarEntryName.substring(0, jarEntryName.length() - 6), e);
                        }
                        jarEntry = stream.getNextJarEntry();
                    }
                } catch (IOException e) {
                    LOGGER.debug("Failed to open the jar input stream for " + classPathName, e);
                } finally {
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException e) {
                            LOGGER.debug("Failed to close the jar input stream for " + classPathName, e);
                        }
                    }
                }
            }
        }
        return classSetMap;
    }

    /**
     * Generate a MetaData object using the class map provided for inbuilt processors.
     *
     * @param classMap processor types to class map
     */
    private static MetaData generateInBuiltMetaData(Map<String, Set<Class<?>>> classMap) {

        MetaData metaData = new MetaData();

        // Generating the function meta data list containing function executors and attribute aggregators
        List<ProcessorMetaData> functionMetaData = new ArrayList<>();
        populateInBuiltProcessorMetaDataList(functionMetaData, classMap, Constants.FUNCTION_EXECUTOR);
        populateInBuiltProcessorMetaDataList(functionMetaData, classMap, Constants.ATTRIBUTE_AGGREGATOR);
        metaData.setFunctions(functionMetaData);

        // Generating the stream processor meta data list containing stream processor and stream function
        List<ProcessorMetaData> streamProcessorMetaData = new ArrayList<>();
        populateInBuiltProcessorMetaDataList(streamProcessorMetaData, classMap, Constants.STREAM_FUNCTION_PROCESSOR);
        populateInBuiltProcessorMetaDataList(streamProcessorMetaData, classMap, Constants.STREAM_PROCESSOR);
        metaData.setStreamProcessors(streamProcessorMetaData);

        // Generating the window processor meta data list
        List<ProcessorMetaData> windowProcessorMetaData = new ArrayList<>();
        populateInBuiltProcessorMetaDataList(windowProcessorMetaData, classMap, Constants.WINDOW_PROCESSOR);
        metaData.setWindowProcessors(windowProcessorMetaData);

        return metaData;
    }

    /**
     * populate the targetProcessorMetaDataList with the annotated data in the classes in
     * the class map for the specified processor type.
     *
     * @param targetProcessorMetaDataList List of processor meta data objects to populate
     * @param classMap                    processor types to set of class map from which
     *                                    the metadata should be extracted
     * @param processorType               The type of the processor of which meta data needs to be extracted
     */
    private static void populateInBuiltProcessorMetaDataList(List<ProcessorMetaData> targetProcessorMetaDataList,
                                                             Map<String, Set<Class<?>>> classMap,
                                                             String processorType) {

        Set<Class<?>> classSet = classMap.get(processorType);
        if (classSet != null) {
            for (Class<?> processorClass : classSet) {
                ProcessorMetaData processorMetaData = generateProcessorMetaData(processorClass, processorType);
                if (processorMetaData != null) {
                    targetProcessorMetaDataList.add(processorMetaData);
                }
            }
        }
    }

    /**
     * Generate a MetaData object map using the class map provided for extension processors.
     * The return map's key is the namespace and the meta data object contains the different types of processors
     *
     * @param extensionsMap Map from which the meta data needs to be extracted
     */
    private static Map<String, MetaData> generateExtensionsMetaData(Map<String, Class> extensionsMap) {

        Map<String, MetaData> metaDataMap = new HashMap<>();
        for (Map.Entry<String, Class> entry : extensionsMap.entrySet()) {
            String namespace = "";
            String processorName;
            if (entry.getKey().contains(":")) {
                namespace = entry.getKey().split(":")[0];
                processorName = entry.getKey().split(":")[1];
            } else {
                processorName = entry.getKey();
            }

            MetaData metaData = metaDataMap.computeIfAbsent(namespace, k -> new MetaData());

            Class<?> extensionClass = entry.getValue();
            String processorType = null;
            List<ProcessorMetaData> processorMetaDataList = null;
            if (Constants.SUPER_CLASS_MAP.get(Constants.FUNCTION_EXECUTOR)
                    .isAssignableFrom(extensionClass)) {
                processorType = Constants.FUNCTION_EXECUTOR;
                processorMetaDataList = metaData.getFunctions();
            } else if (Constants.SUPER_CLASS_MAP.get(Constants.ATTRIBUTE_AGGREGATOR)
                    .isAssignableFrom(extensionClass)) {
                processorType = Constants.ATTRIBUTE_AGGREGATOR;
                processorMetaDataList = metaData.getFunctions();
            } else if (Constants.SUPER_CLASS_MAP.get(Constants.INCREMENTAL_AGGREGATOR)
                    .isAssignableFrom(extensionClass)) {
                processorType = Constants.INCREMENTAL_AGGREGATOR;
                processorMetaDataList = metaData.getFunctions();
            } else if (Constants.SUPER_CLASS_MAP.get(Constants.STREAM_FUNCTION_PROCESSOR)
                    .isAssignableFrom(extensionClass)) {
                processorType = Constants.STREAM_FUNCTION_PROCESSOR;
                processorMetaDataList = metaData.getStreamProcessors();
            } else if (Constants.SUPER_CLASS_MAP.get(Constants.STREAM_PROCESSOR)
                    .isAssignableFrom(extensionClass)) {
                processorType = Constants.STREAM_PROCESSOR;
                processorMetaDataList = metaData.getStreamProcessors();
            } else if (Constants.SUPER_CLASS_MAP.get(Constants.WINDOW_PROCESSOR)
                    .isAssignableFrom(extensionClass)) {
                processorType = Constants.WINDOW_PROCESSOR;
                processorMetaDataList = metaData.getWindowProcessors();
            } else if (Constants.SUPER_CLASS_MAP.get(Constants.SOURCE).isAssignableFrom(extensionClass)) {
                processorType = Constants.SOURCE;
                processorMetaDataList = metaData.getSources();
            } else if (Constants.SUPER_CLASS_MAP.get(Constants.SINK).isAssignableFrom(extensionClass)) {
                processorType = Constants.SINK;
                processorMetaDataList = metaData.getSinks();
            } else if (Constants.SUPER_CLASS_MAP.get(Constants.SOURCEMAP).isAssignableFrom(extensionClass)) {
                processorType = Constants.SOURCEMAP;
                processorMetaDataList = metaData.getSourceMaps();
            } else if (Constants.SUPER_CLASS_MAP.get(Constants.SINKMAP).isAssignableFrom(extensionClass)) {
                processorType = Constants.SINKMAP;
                processorMetaDataList = metaData.getSinkMaps();
            } else if (Constants.SUPER_CLASS_MAP.get(Constants.STORE).isAssignableFrom(extensionClass)) {
                processorType = Constants.STORE;
                processorMetaDataList = metaData.getStores();
            }

            if (processorMetaDataList != null) {
                ProcessorMetaData processorMetaData =
                        generateProcessorMetaData(extensionClass, processorType, processorName);

                if (processorMetaData != null) {
                    processorMetaDataList.add(processorMetaData);
                }
            }
            /*
            else {
                LOGGER.warn("Discarded extension " + extensionClass.getCanonicalName() +
                        " belonging to an unknown type ");
            }
            */
        }
        return metaDataMap;
    }

    /**
     * Generate processor meta data from the annotated data in the class.
     * This generates a processor name using the class name
     *
     * @param processorClass Class from which meta data should be extracted from
     * @param processorType  The processor type of the class
     * @return processor meta data
     */
    private static ProcessorMetaData generateProcessorMetaData(Class<?> processorClass,
                                                               String processorType) {

        String processorName = processorClass.getName();
        // Getting the class name
        processorName = processorName.substring(processorName.lastIndexOf('.') + 1);
        // Removing the super class postfix
        processorName = processorName.replace(processorType, "");

        // Check if the processor class is a subclass of the super class and not the superclass itself
        // This check is important because the inbuilt processor scan retrieves the super classes as well
        if (!Constants.SUPER_CLASS_MAP.get(processorType).equals(processorClass)) {
            processorName = processorName.substring(0, 1).toLowerCase(Locale.getDefault()) + processorName.substring(1);
            return generateProcessorMetaData(processorClass, processorType, processorName);
        } else {
            return null;
        }
    }

    /**
     * generate the parameter data type syntax for extension syntax generation.
     *
     * @param parameterName        parameter name from parameter
     * @param parameterDataTypeMap Parameter datatype map
     * @return Parameter Data Type
     */
    private static StringBuilder parameterDataTypeGeneration(String parameterName,
                                                             Map<String, DataType[]> parameterDataTypeMap) {
        StringBuilder parameterDataType = new StringBuilder(" <");
        DataType[] parameterType = parameterDataTypeMap.get(parameterName);
        for (int i = 0; i < parameterType.length; i++) {
            DataType dataType = parameterType[i];
            if (i != parameterType.length - 1) {
                parameterDataType.append(dataType).append("|");
            } else {
                parameterDataType.append(dataType);
            }
        }
        parameterDataType.append("> ");
        return parameterDataType;
    }

    /**
     * generate the syntax  for functions,windows,stream processor and aggregate function.
     *
     * @param extension            Extension data from extension anotation
     * @param parameterDataTypeMap Parameter datatype map
     * @return syntaxList
     */
    private static List<SyntaxMetaData> windowFunctionSyntaxGeneration(Extension extension,
                                                                       Map<String, DataType[]> parameterDataTypeMap) {
        List<SyntaxMetaData> syntaxList = new ArrayList<>();
        StringBuilder syntax = new StringBuilder();
        StringBuilder clipBoardSyntax = new StringBuilder();
        //Add return syntax to the extension if it is a functions and aggregate functions
        for (ReturnAttribute returnAttribute : extension.returnAttributes()) {
            syntax.append("<");
            for (int j = 0; j < (returnAttribute.type()).length; j++) {
                if (j != returnAttribute.type().length - 1) {
                    syntax.append(returnAttribute.type()[j]).append("|");
                } else {
                    syntax.append(returnAttribute.type()[j]);
                }
            }
            syntax.append("> ");
        }

        // Add name syntax to the extension
        if (!extension.namespace().isEmpty()) {
            syntax.append(extension.namespace()).append(":");
            clipBoardSyntax.append(extension.namespace()).append(":");
        }
        syntax.append(extension.name());
        clipBoardSyntax.append(extension.name());

        //Add parameter and its data type syntax to extension based on the parameter overloads
        if (extension.parameterOverloads().length > 0) {
            StringBuilder parameterSyntax;
            StringBuilder parameterClipboardSyntax;
            for (ParameterOverload parameterOverload : extension.parameterOverloads()) {
                parameterSyntax = new StringBuilder();
                parameterClipboardSyntax = new StringBuilder();

                if (parameterOverload.parameterNames().length == 0) {
                    parameterSyntax.append("()");
                    parameterClipboardSyntax.append("()");
                } else {
                    parameterSyntax.append("(");
                    parameterClipboardSyntax.append("( ");
                    for (int j = 0; j < parameterOverload.parameterNames().length; j++) {
                        String parameterName = parameterOverload.parameterNames()[j];
                        if (parameterName.equalsIgnoreCase(SiddhiCodeBuilderConstants.THREE_DOTS)) {
                            String paramName = parameterOverload.parameterNames()[j - 1];
                            parameterSyntax.append(parameterDataTypeGeneration(paramName, parameterDataTypeMap))
                                            .append(SiddhiCodeBuilderConstants.THREE_DOTS);
                            parameterClipboardSyntax.append(paramName).append(SiddhiCodeBuilderConstants.THREE_DOTS);
                        } else {
                            parameterSyntax.append(
                                    parameterDataTypeGeneration(parameterName, parameterDataTypeMap).toString());
                            if (j != parameterOverload.parameterNames().length - 1) {
                                parameterSyntax.append(parameterName).append(",");
                                parameterClipboardSyntax.append(parameterName).append(",");
                            } else {
                                parameterSyntax.append(parameterName);
                                parameterClipboardSyntax.append(parameterName);
                            }
                        }
                    }
                    parameterClipboardSyntax.append(" )");
                    parameterSyntax.append(" )");
                }
                SyntaxMetaData syntaxMetaData = new SyntaxMetaData(
                        syntax.toString() + parameterSyntax.toString(),
                        clipBoardSyntax.toString() + parameterClipboardSyntax.toString());
                syntaxList.add(syntaxMetaData);
            }
        } else {
            syntax.append("(");
            clipBoardSyntax.append("( ");
            for (int i = 0; i < extension.parameters().length; i++) {
                Parameter parameter = extension.parameters()[i];
                syntax.append(parameterDataTypeGeneration(parameter.name(), parameterDataTypeMap));
                if (i != extension.parameters().length - 1) {
                    syntax.append(parameter.name()).append(",");
                    clipBoardSyntax.append(parameter.name()).append(",");
                } else {
                    syntax.append(parameter.name());
                    clipBoardSyntax.append(parameter.name());
                }
            }
            syntax.append(" )");
            clipBoardSyntax.append(" )");

            SyntaxMetaData syntaxMetaData = new SyntaxMetaData(syntax.toString(), clipBoardSyntax.toString());
            syntaxList.add(syntaxMetaData);
        }
        return syntaxList;
    }

    /**
     * generate the syntax  for source and sink extension.
     *
     * @param extension            Extension data from extension anotation
     * @param parameterDataTypeMap Parameter datatype map
     * @return syntaxList
     */
    private static List<SyntaxMetaData> sourceSinkSyntaxGeneration(Extension extension,
                                                                   Map<String, DataType[]> parameterDataTypeMap
                                                                  ) {
        StringBuilder syntax = new StringBuilder();
        StringBuilder clipBoardSyntax = new StringBuilder();
        if (!extension.namespace().isEmpty()) {
            clipBoardSyntax.append("@").append(extension.namespace());
            syntax.append("@").append(extension.namespace());
        }
        clipBoardSyntax.append("(type=\"").append(extension.name()).append("\", ");
        syntax.append("(type=\"").append(extension.name()).append("\", ");

        for (Parameter parameter : extension.parameters()) {
            clipBoardSyntax.append(parameter.name()).append(" =\"\", ");
            syntax.append(" ").append(parameter.name()).append("=")
                    .append("\"").append(parameterDataTypeGeneration(parameter.name(), parameterDataTypeMap))
                    .append("\",");
        }
        SyntaxMetaData syntaxMetaData = new SyntaxMetaData(syntax.append(" @map (...))").toString(),
                clipBoardSyntax.append(" @map (...))").toString());
        return Collections.singletonList(syntaxMetaData);
    }

    /**
     * generate the syntax  for source mapper and sink mapper extension.
     *
     * @param extension            Extension data from extension anotation
     * @param parameterDataTypeMap Parameter datatype map
     * @return syntaxList
     */
    private static List<SyntaxMetaData> sourceSinkMapSyntaxGeneration(Extension extension,
                                                                      Map<String, DataType[]> parameterDataTypeMap) {
        StringBuilder syntax = new StringBuilder();
        StringBuilder clipBoardSyntax = new StringBuilder();
        if (!extension.namespace().isEmpty()) {
            if (extension.namespace().equalsIgnoreCase("sinkMapper")) {
                syntax.append("@sink");
            } else {
                syntax.append("@source");
            }
        }
        clipBoardSyntax.append("@map(type= \"").append(extension.name()).append("\"");
        syntax.append("(").append(SiddhiCodeBuilderConstants.THREE_DOTS)
                .append("@map(type= \"").append(extension.name()).append("\"");

        for (Parameter parameter : extension.parameters()) {
            clipBoardSyntax.append(", ").append(parameter.name()).append(" =\"\"");
            syntax.append(", ").append(parameter.name()).append("=")
                    .append("\"").append(parameterDataTypeGeneration(parameter.name(), parameterDataTypeMap))
                    .append("\"");
        }

        SyntaxMetaData syntaxMetaData = new SyntaxMetaData(syntax.append("))").toString(),
                clipBoardSyntax.append(")").toString());
        return Collections.singletonList(syntaxMetaData);
    }

    /**
     * generate the syntax  for store  extension.
     *
     * @param extension            Extension data from extension anotation
     * @param parameterDataTypeMap Parameter datatype map
     * @return syntaxList
     */
    private static List<SyntaxMetaData> storeSyntaxGeneration(Extension extension,
                                                              Map<String, DataType[]> parameterDataTypeMap) {
        StringBuilder syntax = new StringBuilder();
        StringBuilder clipBoardSyntax = new StringBuilder();
        if (!extension.namespace().isEmpty()) {
            clipBoardSyntax.append("@").append(extension.namespace());
            syntax.append("@").append(extension.namespace());
        }
        clipBoardSyntax.append("(type=\"").append(extension.name()).append("\",");
        syntax.append("(type=\"").append(extension.name()).append("\",");

        for (int i = 0; i < extension.parameters().length; i++) {
            Parameter parameter = extension.parameters()[i];
            if (i != extension.parameters().length - 1) {
                clipBoardSyntax.append(" ").append(parameter.name()).append(" =\"\",");
                syntax.append(" ").append(parameter.name()).append("=")
                        .append("\"").append(parameterDataTypeGeneration(parameter.name(), parameterDataTypeMap))
                        .append("\",");
            } else {
                clipBoardSyntax.append(" ").append(parameter.name()).append(" =\"\"");
                syntax.append(" ").append(parameter.name()).append("=")
                        .append("\"").append(parameterDataTypeGeneration(parameter.name(), parameterDataTypeMap))
                        .append("\"");
            }
        }
        syntax.append(")\n")
                .append("@PrimaryKey(\"PRIMARY_KEY\")\n")
                .append("@Index(\"INDEX\")");
        clipBoardSyntax.append(")\n")
                .append("@PrimaryKey(\"PRIMARY_KEY\")\n")
                .append("@Index(\"INDEX\")");

        SyntaxMetaData syntaxMetaData = new SyntaxMetaData(syntax.toString(), clipBoardSyntax.toString());
        return Collections.singletonList(syntaxMetaData);
    }

    /**
     * Generate processor meta data from the annotated data in the class.
     *
     * @param processorClass Class from which meta data should be extracted from
     * @param processorType  The processor type of the class
     * @param processorName  The name of the processor
     * @return processor meta data
     */
    private static ProcessorMetaData generateProcessorMetaData(Class<?> processorClass, String processorType,
                                                               String processorName) {

        ProcessorMetaData processorMetaData = null;

        Extension extensionAnnotation = processorClass.getAnnotation(Extension.class);

        if (extensionAnnotation != null) {
            processorMetaData = new ProcessorMetaData();
            processorMetaData.setName(processorName);
            processorMetaData.setType(processorType);
            // Adding Description annotation data
            processorMetaData.setDescription(extensionAnnotation.description());

            // Adding Namespace annotation data
            processorMetaData.setNamespace(extensionAnnotation.namespace());

            //  Adding Parameter annotation data
            //define parameter map to generate parameter syntax
            Map<String, DataType[]> parameterDataTypeMap = new HashMap<>();
            if (extensionAnnotation.parameters().length > 0) {
                List<ParameterMetaData> parameterMetaDataList = new ArrayList<>();
                for (Parameter parameter : extensionAnnotation.parameters()) {
                    ParameterMetaData parameterMetaData = new ParameterMetaData();
                    parameterDataTypeMap.put(parameter.name(), parameter.type());
                    parameterMetaData.setName(parameter.name());
                    parameterMetaData.setType(Arrays.asList(parameter.type()));
                    parameterMetaData.setIsDynamic(parameter.dynamic());
                    parameterMetaData.setOptional(parameter.optional());
                    parameterMetaData.setDescription(parameter.description());
                    parameterMetaData.setDefaultValue(parameter.defaultValue());
                    parameterMetaDataList.add(parameterMetaData);
                }
                processorMetaData.setParameters(parameterMetaDataList);
            }

            // Generate syntax annotation data for function, attribute aggregation,windows,stream
            // function and stream
            if (Constants.FUNCTION_EXECUTOR.equalsIgnoreCase(processorType) ||
                    Constants.ATTRIBUTE_AGGREGATOR.equalsIgnoreCase(processorType) ||
                    Constants.WINDOW_PROCESSOR.equalsIgnoreCase(processorType) ||
                    Constants.STREAM_FUNCTION_PROCESSOR.equalsIgnoreCase(processorType) ||
                    Constants.STREAM_PROCESSOR.equalsIgnoreCase(processorType)) {
                processorMetaData.setSyntax(
                        windowFunctionSyntaxGeneration(extensionAnnotation, parameterDataTypeMap));
            }

            //Generate syntax annotation data for source and sink
            if (Constants.SOURCE.equals(processorType) || Constants.SINK.equals(processorType)) {
                processorMetaData.setSyntax(
                        sourceSinkSyntaxGeneration(extensionAnnotation, parameterDataTypeMap));
            }

            //Generate Syntax annotation data for source mapper and sink mapper
            if (Constants.SOURCEMAP.equals(processorType) || Constants.SINKMAP.equals(processorType)) {
                processorMetaData.setSyntax(
                        sourceSinkMapSyntaxGeneration(extensionAnnotation, parameterDataTypeMap));
            }

            //Generate Syntax annotation data for store
            if (Constants.STORE.equals(processorType)) {
                processorMetaData.setSyntax(
                        storeSyntaxGeneration(extensionAnnotation, parameterDataTypeMap));
            }

            //Adding Example annotation data
            List<ExampleMetaData> examplesList = new ArrayList<>();
            for (Example exampleAnnotation : extensionAnnotation.examples()) {
                ExampleMetaData exampleMetaData =
                        new ExampleMetaData(exampleAnnotation.syntax(), exampleAnnotation.description());
                examplesList.add(exampleMetaData);
            }
            processorMetaData.setExamples(examplesList);

            // Adding ReturnEvent annotation data
            // Adding return event additional attributes
            if (Constants.WINDOW_PROCESSOR.equals(processorType) ||
                    Constants.STREAM_PROCESSOR.equals(processorType) ||
                    Constants.STREAM_FUNCTION_PROCESSOR.equals(processorType) ||
                    Constants.FUNCTION_EXECUTOR.equals(processorType)) {
                List<AttributeMetaData> attributeMetaDataList = new ArrayList<>();
                if (extensionAnnotation.returnAttributes().length > 0) {
                    for (ReturnAttribute additionalAttribute : extensionAnnotation.returnAttributes()) {
                        AttributeMetaData attributeMetaData = new AttributeMetaData();
                        attributeMetaData.setName(additionalAttribute.name());
                        attributeMetaData.setType(Arrays.asList(additionalAttribute.type()));
                        attributeMetaData.setDescription(additionalAttribute.description());
                        attributeMetaDataList.add(attributeMetaData);
                    }
                }
                processorMetaData.setReturnAttributes(attributeMetaDataList);
            }
            //Adding parameter overloads data
            if (extensionAnnotation.parameterOverloads().length > 0) {
                List<String[]> parameterOverloads = new ArrayList<>();
                for (ParameterOverload parameterOverload : extensionAnnotation.parameterOverloads()) {
                    parameterOverloads.add(parameterOverload.parameterNames());
                }
                processorMetaData.setParameterOverloads(parameterOverloads);
            }
        }
        return processorMetaData;
    }

    public static String populateSiddhiAppWithVars(Map<String, String> envMap, String siddhiApp) {

        if (siddhiApp.contains("$")) {
            String envPattern = "\\$\\{(\\w+)\\}";
            Pattern expr = Pattern.compile(envPattern);
            Matcher matcher = expr.matcher(siddhiApp);
            while (matcher.find()) {
                for (int i = 1; i <= matcher.groupCount(); i++) {
                    String envValue = envMap.get(matcher.group(i));
                    if (envValue != null) {
                        envValue = envValue.replace("\\", "\\\\");
                        Pattern subexpr = Pattern.compile("\\$\\{" + matcher.group(i) + "\\}");
                        siddhiApp = subexpr.matcher(siddhiApp).replaceAll(envValue);
                    }
                }
            }
        }
        return siddhiApp;
    }
}
