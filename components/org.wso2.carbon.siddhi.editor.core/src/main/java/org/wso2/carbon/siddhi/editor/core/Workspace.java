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
package org.wso2.carbon.siddhi.editor.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Interface for the workspace related functionality.
 */
public interface Workspace {

    /**
     * List the roots of the system.
     * @return array of directories
     * @throws IOException
     */
    JsonArray listRoots() throws IOException;

    /**
     * List the files of a given directory path of the system.
     * @return array of files
     * @throws IOException
     */
    JsonArray listDirectoryFiles(String path) throws IOException;

    /**
     * List of directories in the path.
     * @param path provided path
     * @return array of directories
     * @throws IOException
     */
    JsonArray listDirectoriesInPath(String path) throws IOException;

    /**
     * List of files in the path.
     * @param path provided path
     * @return array of files
     * @throws IOException
     */
    JsonArray listFilesInPath(Path path) throws IOException;

    /**
     * Whether file exist.
     * @param path provided path
     * @return json object
     * @throws IOException
     */
    JsonObject exists(Path path) throws IOException;

    /**
     * create file or directory.
     * @param path provided path
     * @param type type of artifact eg:folder
     * @throws IOException
     */
    void create(String path, String type) throws IOException;

    /**
     * delete file or directory.
     * @param path provided path
     * @param type type of artifact eg:folder
     * @throws IOException
     */
    void delete(String path, String type) throws IOException;

    /**
     * Read content.
     * @param path provided path
     * @return content read
     * @throws IOException
     */
    JsonObject read(Path path) throws IOException;

    /**
     *
     * @param logger
     * @param timestamp
     * @param level
     * @param URL
     * @param message
     * @param layout
     * @throws IOException
     */
    void log(String logger, String timestamp, String level, String URL, String message, String layout)
            throws IOException;
}
