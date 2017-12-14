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
package org.wso2.carbon.siddhi.editor.core.internal.local;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.siddhi.editor.core.Workspace;
import org.wso2.carbon.siddhi.editor.core.util.Constants;
import org.wso2.carbon.siddhi.editor.core.util.LogEncoder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Iterator;

/**
 * Workspace implementation for local file system.
 */
public class LocalFSWorkspace implements Workspace {

    private static final Logger logger = LoggerFactory.getLogger(LocalFSWorkspace.class);
    private static final String FILE_EXTENSION = ".siddhi";
    private static final String FOLDER_TYPE = "folder";
    private static final String CONTENT = "content";

    @Override
    public JsonArray listRoots() throws IOException {
        final Iterable<Path> rootDirs = FileSystems.getDefault().getRootDirectories();
        JsonArray rootArray = new JsonArray();
        for (Path root : rootDirs) {
            JsonObject rootObj = getJsonObjForFile(root, false);
            try {
                if (Files.isDirectory(root) && Files.list(root).count() > 0) {
                    JsonArray children = new JsonArray();
                    Iterator<Path> rootItr = Files.list(root).iterator();
                    while (rootItr.hasNext()) {
                        Path next = rootItr.next();
                        if (Files.isDirectory(next) && !Files.isHidden(next)) {
                            JsonObject childObj = getJsonObjForFile(next, true);
                            children.add(childObj);
                        }
                    }
                    rootObj.add("children", children);
                }
            } catch (IOException e) {
                logger.debug("Error while traversing children of " + e.toString(), e);
                rootObj.addProperty("error", e.toString());
            }
            if (Files.isDirectory(root)) {
                rootArray.add(rootObj);
            }
        }
        return rootArray;
    }

    @Override
    public JsonArray listDirectoryFiles(String path) throws IOException {
        String location = "";
        if (path.equals("")) {
            location = (Paths.get(Constants.RUNTIME_PATH,
                                  Constants.DIRECTORY_DEPLOYMENT)).toString();
        }
        return listWorkspaceDirectoryInPath(location);
    }

    public JsonArray listWorkspaceDirectoryInPath(String path) throws IOException {
        Path ioPath = Paths.get(path);
        JsonArray dirs = new JsonArray();
        Iterator<Path> iterator = Files.list(ioPath).iterator();
        while (iterator.hasNext()) {
            Path next = iterator.next();
            if (Files.isDirectory(next) && !Files.isHidden(next)) {
                JsonObject jsnObj = getJsonObjForFile(next, true);
                if (jsnObj.get("text").toString().equals("\"" + Constants.DIRECTORY_WORKSPACE + "\"")) {
                    dirs.add(jsnObj);
                }
            }
        }
        return dirs;
    }

    @Override
    public JsonArray listDirectoriesInPath(String path) throws IOException {
        Path ioPath = Paths.get(path);
        JsonArray dirs = new JsonArray();
        Iterator<Path> iterator = Files.list(ioPath).iterator();
        while (iterator.hasNext()) {
            Path next = iterator.next();
            if (Files.isDirectory(next) && !Files.isHidden(next)) {
                JsonObject jsnObj = getJsonObjForFile(next, true);
                dirs.add(jsnObj);
            }
        }
        return dirs;
    }

    @Override
    public JsonObject read(Path path) throws IOException {
        byte[] fileContent = Files.readAllBytes(path);
        JsonObject content = new JsonObject();
        content.addProperty(CONTENT, new String(fileContent, Charset.defaultCharset()));
        return content;
    }

    @Override
    public void delete(String path, String type) throws IOException {
        Path ioPath = Paths.get(path);
        if (FOLDER_TYPE.equals(type)) {
            Files.walk(ioPath, FileVisitOption.FOLLOW_LINKS)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } else {
            Files.delete(ioPath);
        }
    }

    @Override
    public void create(String path, String type) throws IOException {
        Path ioPath = Paths.get(path);
        if (FOLDER_TYPE.equals(type)) {
            Files.createDirectories(ioPath);
        } else {
            Files.createFile(ioPath);
        }
    }

    @Override
    public void log(String loggerID, String timestamp, String level,
                    String URL, String message, String layout) throws IOException {
        Logger frontEndLog = LoggerFactory.getLogger(loggerID);
        String logMessage = "client-timestamp: " + timestamp + ", page: " + URL + ", message: " + message;
        switch (level) {
            case "TRACE":
                frontEndLog.trace(logMessage);
                break;
            case "DEBUG":
                frontEndLog.debug(logMessage);
                break;
            case "INFO":
                frontEndLog.info(logMessage);
                break;
            case "WARN":
                frontEndLog.warn(logMessage);
                break;
            case "ERROR":
                frontEndLog.error(logMessage);
                break;
            case "FATAL":
                frontEndLog.error(logMessage);
                break;
            default:
                frontEndLog.debug(logMessage);
        }

    }

    private JsonObject getJsonObjForFile(Path root, boolean checkChildren) {
        JsonObject rootObj = new JsonObject();
        Path path = root.getFileName();
        rootObj.addProperty("text", path != null ?
                                    path.toString() : root.toString());
        rootObj.addProperty("id", root.toAbsolutePath().toString());
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

    @Override
    public JsonArray listFilesInPath(Path path) throws IOException {
        JsonArray dirs = new JsonArray();
        Iterator<Path> iterator = Files.list(path).iterator();
        while (iterator.hasNext()) {
            Path next = iterator.next();
            if ((Files.isDirectory(next) || Files.isRegularFile(next)) && !Files.isHidden(next)) {
                JsonObject jsnObj = getJsonObjForFile(next, true);
                if (Files.isRegularFile(next)) {
                    Path aPath = next.getFileName();
                    if (aPath != null && aPath.toString().endsWith(FILE_EXTENSION)) {
                        dirs.add(jsnObj);
                    }
                } else {
                    dirs.add(jsnObj);
                }

            }
        }
        return dirs;
    }

    @Override
    public JsonObject exists(Path path) throws IOException {
        JsonObject result = new JsonObject();
        boolean exists = Files.exists(path);
        result.addProperty("file", path.toString());
        result.addProperty("exists", exists);
        return result;
    }
}
