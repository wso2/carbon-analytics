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
package org.wso2.carbon.event.simulator.core.internal.generator.csv.util;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.event.simulator.core.exception.SimulatorInitializationException;
import org.wso2.carbon.event.simulator.core.internal.util.EventSimulatorConstants;
import org.wso2.carbon.event.simulator.core.service.EventSimulatorDataHolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * FileStore keeps a record of all CSV files that have been uploaded to the system
 *
 * @see FileUploader
 */
public class FileStore {
    private static final Logger log = LoggerFactory.getLogger(FileStore.class);
    private static final FileStore fileStore = new FileStore();
    /**
     * Concurrent list that holds names of uploaded CSV files
     */
    private final List<String> fileNameList = Collections.synchronizedList(new ArrayList<>());

    private FileStore() {}

    /**
     * Method to return Singleton Object of FileStore
     *
     * @return fileStore
     */
    public static FileStore getFileStore() {
        return fileStore;
    }

    /**
     * Method to add file data into in memory
     *
     * @param filename name of file uploaded
     */
    public void addFile(String filename) {
        fileNameList.add(filename);
    }

    /**
     * Method to check whether the File Name  already exists in directory
     *
     * @param fileName File name of the file
     * @return true if exist false if not exist
     */
    public boolean checkExists(String fileName) {
        return fileNameList.contains(fileName);
    }

    /**
     * deleteFile() is used to delete a file name from csv file store
     *
     * @param fileName name of file being deleted
     * */
    public void deleteFile(String fileName) {
        fileNameList.remove(fileName);
    }

}
