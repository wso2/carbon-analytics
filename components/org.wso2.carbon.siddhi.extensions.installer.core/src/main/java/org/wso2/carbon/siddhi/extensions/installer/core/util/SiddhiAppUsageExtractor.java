/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.siddhi.extensions.installer.core.util;

import io.siddhi.query.api.SiddhiApp;
import io.siddhi.query.api.annotation.Annotation;
import io.siddhi.query.api.annotation.Element;
import io.siddhi.query.api.definition.StreamDefinition;
import io.siddhi.query.api.definition.TableDefinition;
import io.siddhi.query.compiler.SiddhiCompiler;
import org.wso2.carbon.siddhi.extensions.installer.core.models.SiddhiAppExtensionUsage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Extracts usages of extensions from Siddhi apps.
 */
public class SiddhiAppUsageExtractor {

    private static final String NAMESPACE_KEY = "namespace";
    private static final String SOURCE_NAMESPACE = "source";
    private static final String SINK_NAMESPACE = "sink";
    private static final String STORE_NAMESPACE = "store";

    private SiddhiAppUsageExtractor() {
        // Prevents instantiation.
    }

    /**
     * Extracts usages of extensions from the Siddhi app, that has the given body.
     *
     * @param siddhiAppString Body of a Siddhi app.
     * @return Extension usages that are present in the Siddhi app.
     */
    public static List<SiddhiAppExtensionUsage> extractUsages(String siddhiAppString) {
        SiddhiApp siddhiApp = SiddhiCompiler.parse(siddhiAppString);
        List<SiddhiAppExtensionUsage> usages = new ArrayList<>();
        usages.addAll(extractUsagesFromStreams(siddhiApp));
        usages.addAll(extractUsagesFromTables(siddhiApp));
        return usages;
    }

    private static List<SiddhiAppExtensionUsage> extractUsagesFromStreams(SiddhiApp siddhiApp) {
        List<SiddhiAppExtensionUsage> usages = new ArrayList<>();
        Map<String, StreamDefinition> streamDefinitions = siddhiApp.getStreamDefinitionMap();
        for (Map.Entry<String, StreamDefinition> streamDefinitionEntry : streamDefinitions.entrySet()) {
            List<Annotation> streamAnnotations = streamDefinitionEntry.getValue().getAnnotations();
            if (!streamAnnotations.isEmpty()) {
                usages.addAll(extractInfoFromAnnotations(streamAnnotations, SOURCE_NAMESPACE));
                usages.addAll(extractInfoFromAnnotations(streamAnnotations, SINK_NAMESPACE));
            }
        }
        return usages;
    }

    private static List<SiddhiAppExtensionUsage> extractUsagesFromTables(SiddhiApp siddhiApp) {
        List<SiddhiAppExtensionUsage> usages = new ArrayList<>();
        Map<String, TableDefinition> tableDefinitions = siddhiApp.getTableDefinitionMap();
        for (Map.Entry<String, TableDefinition> tableDefinitionEntry : tableDefinitions.entrySet()) {
            List<Annotation> tableAnnotations = tableDefinitionEntry.getValue().getAnnotations();
            if (!tableAnnotations.isEmpty()) {
                usages.addAll(extractInfoFromAnnotations(tableAnnotations, STORE_NAMESPACE));
            }
        }
        return usages;
    }

    private static List<SiddhiAppExtensionUsage> extractInfoFromAnnotations(List<Annotation> annotations,
                                                                            String namespace) {
        List<Annotation> namespaceAnnotations = annotations.stream()
            .filter(annotation -> annotation.getName().equalsIgnoreCase(namespace)).collect(Collectors.toList());
        List<SiddhiAppExtensionUsage> siddhiAppExtensionUsages = new ArrayList<>();
        for (Annotation namespaceAnnotation : namespaceAnnotations) {
            Map<String, String> annotationProperties = new HashMap<>();
            annotationProperties.put(NAMESPACE_KEY, namespace);
            for (Element element : namespaceAnnotation.getElements()) {
                annotationProperties.put(element.getKey(), element.getValue());
            }
            siddhiAppExtensionUsages.add(new SiddhiAppExtensionUsage(annotationProperties, namespaceAnnotation));
        }
        return siddhiAppExtensionUsages;
    }

}