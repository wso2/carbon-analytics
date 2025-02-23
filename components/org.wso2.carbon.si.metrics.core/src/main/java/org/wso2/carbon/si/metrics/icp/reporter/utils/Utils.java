/*
 *  Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.wso2.carbon.si.metrics.icp.reporter.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.si.metrics.icp.reporter.impl.ArtifactType;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Utils {

    private static final Logger log = LoggerFactory.getLogger(Utils.class);
    private static final Map<ArtifactType, Map<String, String>> FUNCTION_MAP = new HashMap<>();

    static {
        Map<String, String> SIDDHI_APP_MAP = new HashMap<>();
        SIDDHI_APP_MAP.put(Constants.NAME, Constants.APP_NAME);
        SIDDHI_APP_MAP.put(Constants.STATUS, Constants.STATUS);
        SIDDHI_APP_MAP.put(Constants.AGE, Constants.AGE);
        SIDDHI_APP_MAP.put(Constants.STATS_ENABLED, Constants.IS_STAT_ENABLED);
        FUNCTION_MAP.put(ArtifactType.SIDDHI_APPS, SIDDHI_APP_MAP);

        Map<String, String> SOURCE_MAP = new HashMap<>();
        SOURCE_MAP.put(Constants.NAME, Constants.OUTPUT_STREAM_ID);
        SOURCE_MAP.put(Constants.TYPE, Constants.INPUT_STREAM_ID);
        SOURCE_MAP.put(Constants.APP_NAME, Constants.APP_NAME);
        SOURCE_MAP.put(Constants.STATUS, Constants.IS_ACTIVE);
        FUNCTION_MAP.put(ArtifactType.SOURCES, SOURCE_MAP);

        Map<String, String> SINK_MAP = new HashMap<>();
        SINK_MAP.put(Constants.NAME, Constants.INPUT_STREAM_ID);
        SINK_MAP.put(Constants.TYPE, Constants.OUTPUT_STREAM_ID);
        SINK_MAP.put(Constants.APP_NAME, Constants.APP_NAME);
        SINK_MAP.put(Constants.STATUS, Constants.IS_ACTIVE);
        FUNCTION_MAP.put(ArtifactType.SINKS, SINK_MAP);

        Map<String, String> QUERY_MAP = new HashMap<>();
        QUERY_MAP.put(Constants.NAME, Constants.QUERY_NAME);
        QUERY_MAP.put(Constants.INPUT_STREAM, Constants.INPUT_STREAM_ID);
        QUERY_MAP.put(Constants.OUTPUT_STREAM, Constants.OUTPUT_STREAM_ID);
        QUERY_MAP.put(Constants.APP_NAME, Constants.APP_NAME);
        QUERY_MAP.put(Constants.QUERY, Constants.QUERY);
        QUERY_MAP.put(Constants.STATUS, Constants.IS_ACTIVE);
        FUNCTION_MAP.put(ArtifactType.QUERIES, QUERY_MAP);

        Map<String, String> TABLE_MAP = new HashMap<>();
        TABLE_MAP.put(Constants.NAME, Constants.TABLE_ID);
        TABLE_MAP.put(Constants.APP_NAME, Constants.APP_NAME);
        TABLE_MAP.put(Constants.STATUS, Constants.IS_ACTIVE);
        FUNCTION_MAP.put(ArtifactType.TABLES, TABLE_MAP);

        Map<String, String> WINDOW_MAP = new HashMap<>();
        WINDOW_MAP.put(Constants.NAME, Constants.WINDOW_ID);
        WINDOW_MAP.put(Constants.APP_NAME, Constants.APP_NAME);
        WINDOW_MAP.put(Constants.STATUS, Constants.IS_ACTIVE);
        FUNCTION_MAP.put(ArtifactType.WINDOWS, WINDOW_MAP);

        Map<String, String> AGGREGATION_MAP = new HashMap<>();
        AGGREGATION_MAP.put(Constants.NAME, Constants.OUTPUT_STREAM_ID);
        AGGREGATION_MAP.put(Constants.INPUT_STREAM, Constants.INPUT_STREAM_ID);
        AGGREGATION_MAP.put(Constants.APP_NAME, Constants.APP_NAME);
        AGGREGATION_MAP.put(Constants.STATUS, Constants.IS_ACTIVE);
        FUNCTION_MAP.put(ArtifactType.AGGREGATIONS, AGGREGATION_MAP);
    }

    public static JsonObject transformArtifacts(JsonArray artifacts, ArtifactType artifactType) {
        Map<String, String> mapping = FUNCTION_MAP.get(artifactType);
        JsonArray transformedArtifacts = new JsonArray(artifacts.size());

        for (JsonElement element : artifacts) {
            JsonObject artifact = element.getAsJsonObject();
            JsonObject details = transformArtifactDetails(artifact, mapping, artifactType);

            JsonObject transformedArtifact = new JsonObject();
            transformedArtifact.addProperty(Constants.NAME, artifact.get(mapping.get(Constants.NAME)).getAsString());
            transformedArtifact.add(Constants.DETAILS, details);
            transformedArtifacts.add(transformedArtifact);
        }
        JsonObject response = new JsonObject();
        response.add(Constants.LIST, transformedArtifacts);
        return response;
    }

    private static JsonObject transformArtifactDetails(JsonObject artifact, Map<String, String> mapping,
                                                       ArtifactType artifactType) {
        JsonObject details = new JsonObject();

        for (Map.Entry<String, String> entry : mapping.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (key.equals(Constants.STATUS) && artifactType != ArtifactType.SIDDHI_APPS) {
                details.addProperty(Constants.STATUS, getStatusString(artifact.get(Constants.IS_ACTIVE)));
            } else {
                details.addProperty(key, artifact.get(value).getAsString());
            }
        }

        extractAnnotationDetails(artifact, details);
        return details;
    }

    private static void extractAnnotationDetails(JsonObject jsonObject, JsonObject details) {
        if (!jsonObject.has(Constants.ANNOTATION_ELEMENTS) || !jsonObject.get(Constants.ANNOTATION_ELEMENTS).isJsonObject()) {
            return;
        }
        JsonObject annotationElements = jsonObject.getAsJsonObject(Constants.ANNOTATION_ELEMENTS);
        annotationElements.entrySet().forEach(entry ->
                details.addProperty(convertToCamelCase(entry.getKey()), entry.getValue().getAsString())
        );
    }

    public static JsonObject getLogFileList() {
        String carbonHome = System.getProperty(Constants.ENV_CARBON_HOME);
        if (carbonHome == null) {
            return null;
        }
        Path logDirPath = getLogDirectoryPath(carbonHome);

        JsonArray logFiles = new JsonArray();
        try (DirectoryStream<Path> paths = Files.newDirectoryStream(logDirPath, "*.log")) {
            for (Path path : paths) {
                JsonObject logfileObject = new JsonObject();
                logfileObject.addProperty("FileName", path.getFileName().toString());
                logfileObject.addProperty("Size", getFileSize(path.toFile()));
                logFiles.add(logfileObject);
            }
        } catch (IOException e) {
            log.error("Could not find the log directory : {}", logDirPath, e);
        }
        JsonObject response = new JsonObject();
        response.add(Constants.LIST, logFiles);
        return response;
    }

    public static Path getLogDirectoryPath(String carbonHome) {
        return Paths.get(carbonHome, Constants.WSO2, Constants.SERVER, Constants.LOGS);
    }

    private static String getFileSize(File file) {
        long bytes = file.length();
        int unit = 1024;
        if (bytes < unit) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    private static String convertToCamelCase(String value) {
        String[] parts = value.split("\\.");
        for (int i = 1; i < parts.length; i++) {
            String part = parts[i];
            parts[i] = Character.toUpperCase(part.charAt(0)) + part.substring(1);
        }
        return String.join("", parts);
    }

    private static String getStatusString(JsonElement element) {
        return Boolean.parseBoolean(element.getAsString()) ? Constants.ENABLED : Constants.DISABLED;
    }
}
