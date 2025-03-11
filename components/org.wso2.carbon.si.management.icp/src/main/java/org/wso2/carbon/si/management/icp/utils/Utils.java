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
package org.wso2.carbon.si.management.icp.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.si.management.icp.impl.ArtifactType;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumMap;
import java.util.Map;

public class Utils {

    private static final Logger log = LoggerFactory.getLogger(Utils.class);
    private static final EnumMap<ArtifactType, EnumMap<ICPKey, SiddhiAppApiKey>> FUNCTION_MAP =
            new EnumMap<>(ArtifactType.class);

    static {
        EnumMap<ICPKey, SiddhiAppApiKey> SIDDHI_APP_MAP = new EnumMap<>(ICPKey.class);
        SIDDHI_APP_MAP.put(ICPKey.NAME, SiddhiAppApiKey.APP_NAME);
        SIDDHI_APP_MAP.put(ICPKey.STATUS, SiddhiAppApiKey.STATUS);
        SIDDHI_APP_MAP.put(ICPKey.AGE, SiddhiAppApiKey.AGE);
        SIDDHI_APP_MAP.put(ICPKey.STATS_ENABLED, SiddhiAppApiKey.IS_STAT_ENABLED);
        FUNCTION_MAP.put(ArtifactType.SIDDHI_APPS, SIDDHI_APP_MAP);

        EnumMap<ICPKey, SiddhiAppApiKey> SOURCE_MAP = new EnumMap<>(ICPKey.class);
        SOURCE_MAP.put(ICPKey.NAME, SiddhiAppApiKey.OUTPUT_STREAM_ID);
        SOURCE_MAP.put(ICPKey.TYPE, SiddhiAppApiKey.INPUT_STREAM_ID);
        SOURCE_MAP.put(ICPKey.APP_NAME, SiddhiAppApiKey.APP_NAME);
        SOURCE_MAP.put(ICPKey.STATUS, SiddhiAppApiKey.IS_ACTIVE);
        FUNCTION_MAP.put(ArtifactType.SOURCES, SOURCE_MAP);

        EnumMap<ICPKey, SiddhiAppApiKey> SINK_MAP = new EnumMap<>(ICPKey.class);
        SINK_MAP.put(ICPKey.NAME, SiddhiAppApiKey.INPUT_STREAM_ID);
        SINK_MAP.put(ICPKey.TYPE, SiddhiAppApiKey.OUTPUT_STREAM_ID);
        SINK_MAP.put(ICPKey.APP_NAME, SiddhiAppApiKey.APP_NAME);
        SINK_MAP.put(ICPKey.STATUS, SiddhiAppApiKey.IS_ACTIVE);
        FUNCTION_MAP.put(ArtifactType.SINKS, SINK_MAP);

        EnumMap<ICPKey, SiddhiAppApiKey> QUERY_MAP = new EnumMap<>(ICPKey.class);
        QUERY_MAP.put(ICPKey.NAME, SiddhiAppApiKey.QUERY_NAME);
        QUERY_MAP.put(ICPKey.INPUT_STREAM, SiddhiAppApiKey.INPUT_STREAM_ID);
        QUERY_MAP.put(ICPKey.OUTPUT_STREAM, SiddhiAppApiKey.OUTPUT_STREAM_ID);
        QUERY_MAP.put(ICPKey.APP_NAME, SiddhiAppApiKey.APP_NAME);
        QUERY_MAP.put(ICPKey.QUERY, SiddhiAppApiKey.QUERY);
        QUERY_MAP.put(ICPKey.STATUS, SiddhiAppApiKey.IS_ACTIVE);
        FUNCTION_MAP.put(ArtifactType.QUERIES, QUERY_MAP);

        EnumMap<ICPKey, SiddhiAppApiKey> TABLE_MAP = new EnumMap<>(ICPKey.class);
        TABLE_MAP.put(ICPKey.NAME, SiddhiAppApiKey.TABLE_ID);
        TABLE_MAP.put(ICPKey.APP_NAME, SiddhiAppApiKey.APP_NAME);
        TABLE_MAP.put(ICPKey.STATUS, SiddhiAppApiKey.IS_ACTIVE);
        FUNCTION_MAP.put(ArtifactType.TABLES, TABLE_MAP);

        EnumMap<ICPKey, SiddhiAppApiKey> WINDOW_MAP = new EnumMap<>(ICPKey.class);
        WINDOW_MAP.put(ICPKey.NAME, SiddhiAppApiKey.WINDOW_ID);
        WINDOW_MAP.put(ICPKey.APP_NAME, SiddhiAppApiKey.APP_NAME);
        WINDOW_MAP.put(ICPKey.STATUS, SiddhiAppApiKey.IS_ACTIVE);
        FUNCTION_MAP.put(ArtifactType.WINDOWS, WINDOW_MAP);

        EnumMap<ICPKey, SiddhiAppApiKey> AGGREGATION_MAP = new EnumMap<>(ICPKey.class);
        AGGREGATION_MAP.put(ICPKey.NAME, SiddhiAppApiKey.OUTPUT_STREAM_ID);
        AGGREGATION_MAP.put(ICPKey.INPUT_STREAM, SiddhiAppApiKey.INPUT_STREAM_ID);
        AGGREGATION_MAP.put(ICPKey.APP_NAME, SiddhiAppApiKey.APP_NAME);
        AGGREGATION_MAP.put(ICPKey.STATUS, SiddhiAppApiKey.IS_ACTIVE);
        FUNCTION_MAP.put(ArtifactType.AGGREGATIONS, AGGREGATION_MAP);
    }

    public static JsonObject transformArtifacts(JsonArray artifacts, ArtifactType artifactType) {
        EnumMap<ICPKey, SiddhiAppApiKey> mapping = FUNCTION_MAP.get(artifactType);
        JsonArray transformedArtifacts = new JsonArray(artifacts.size());

        for (JsonElement element : artifacts) {
            JsonObject artifact = element.getAsJsonObject();
            JsonObject details = transformArtifactDetails(artifact, mapping, artifactType);

            JsonObject transformedArtifact = new JsonObject();
            transformedArtifact.addProperty(Constants.NAME,
                    artifact.get(mapping.get(ICPKey.NAME).getValue()).getAsString());
            transformedArtifact.add(Constants.DETAILS, details);
            transformedArtifacts.add(transformedArtifact);
        }
        JsonObject response = new JsonObject();
        response.add(Constants.LIST, transformedArtifacts);
        return response;
    }

    private static JsonObject transformArtifactDetails(JsonObject artifact, EnumMap<ICPKey, SiddhiAppApiKey> mapping,
                                                       ArtifactType artifactType) {
        JsonObject details = new JsonObject();

        for (Map.Entry<ICPKey, SiddhiAppApiKey> entry : mapping.entrySet()) {
            String key = entry.getKey().getValue();
            String value = entry.getValue().getValue();

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
        if (!jsonObject.has(Constants.ANNOTATION_ELEMENTS) ||
                !jsonObject.get(Constants.ANNOTATION_ELEMENTS).isJsonObject()) {
            return;
        }
        JsonObject annotationElements = jsonObject.getAsJsonObject(Constants.ANNOTATION_ELEMENTS);
        annotationElements.entrySet().forEach(entry ->
                details.addProperty(capitalizeAndRemoveDots(entry.getKey()), entry.getValue().getAsString())
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
                Path fileName = path.getFileName();
                if (fileName == null) {
                    continue;
                }
                JsonObject logfileObject = new JsonObject();
                logfileObject.addProperty("FileName", fileName.toString());
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
        return FileUtils.byteCountToDisplaySize(FileUtils.sizeOf(file));
    }

    private static String capitalizeAndRemoveDots(String value) {
        return WordUtils.capitalizeFully(value, '.').replaceAll("\\.", " ");
    }

    private static String getStatusString(JsonElement element) {
        return Boolean.parseBoolean(element.getAsString()) ? Constants.ENABLED : Constants.DISABLED;
    }
}
