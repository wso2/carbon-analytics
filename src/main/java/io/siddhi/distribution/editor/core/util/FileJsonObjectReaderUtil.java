/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;

/**
 * Util class to et json object for files/directories for js-tree.
 */
public class FileJsonObjectReaderUtil {

    private static final Logger logger = LoggerFactory.getLogger(FileJsonObjectReaderUtil.class);

    public static  JsonArray listDirectoryInPath(String path, List<String> directories) throws IOException {
        Path ioPath = Paths.get(path);
        JsonArray dirs = new JsonArray();
        Iterator<Path> iterator = Files.list(ioPath).iterator();
        while (iterator.hasNext()) {
            Path next = iterator.next();
            if (Files.isDirectory(next) && !Files.isHidden(next)) {
                JsonObject jsnObj = getJsonObjForFile(next, true);
                if (directories.contains(jsnObj.get("text").toString())) {
                    dirs.add(jsnObj);
                }
            }
        }
        return dirs;
    }

    public static JsonObject getJsonObjForFile(Path root, boolean checkChildren) {
        JsonObject rootObj = new JsonObject();
        Path path = root.getFileName();
        rootObj.addProperty("text", path != null ?
                path.toString() : root.toString());
        rootObj.addProperty("id", Paths.get(Constants.CARBON_HOME).relativize(root).toString());
        if (Files.isDirectory(root) && checkChildren) {
            rootObj.addProperty("type", "folder");
            try {
                if (Files.list(root).count() > 0) {
                    rootObj.addProperty("children", Boolean.TRUE);
                } else {
                    rootObj.addProperty("children", Boolean.FALSE);
                }
            } catch (IOException e) {
                logger.debug("Error while fetching children of " + LogEncoder.removeCRLFCharacters(root.toString()), e);
                rootObj.addProperty("error", e.toString());
            }
        } else if (Files.isRegularFile(root) && checkChildren) {
            rootObj.addProperty("type", "file");
            rootObj.addProperty("children", Boolean.FALSE);
        }
        return rootObj;
    }

    public static JsonArray listFilesInPath(Path path, String fileExtension) throws IOException {
        JsonArray dirs = new JsonArray();
        Iterator<Path> iterator = Files.list(path).iterator();
        while (iterator.hasNext()) {
            Path next = iterator.next();
            if ((Files.isDirectory(next) || Files.isRegularFile(next)) && !Files.isHidden(next)) {
                JsonObject jsnObj = getJsonObjForFile(next, true);
                if (Files.isRegularFile(next)) {
                    Path aPath = next.getFileName();
                    if (aPath != null && aPath.toString().endsWith(fileExtension)) {
                        dirs.add(jsnObj);
                    }
                } else {
                    dirs.add(jsnObj);
                }

            }
        }
        return dirs;
    }

    public static JsonObject getJsonRootObject(JsonArray filteredDirectoryFiles) {
        JsonObject rootObj = new JsonObject();
        rootObj.add("id", new JsonPrimitive("root"));
        rootObj.add("text", new JsonPrimitive(""));
        rootObj.addProperty("type", "folder");
        rootObj.add("children", filteredDirectoryFiles);
        return rootObj;
    }

}
