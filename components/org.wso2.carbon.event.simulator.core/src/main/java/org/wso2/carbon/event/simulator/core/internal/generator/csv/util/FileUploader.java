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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.wso2.carbon.event.simulator.core.exception.FileAlreadyExistsException;
import org.wso2.carbon.event.simulator.core.exception.FileOperationsException;
import org.wso2.carbon.event.simulator.core.internal.util.ValidatedInputStream;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * This class used to upload and delete the CSV file which is uploaded by user.
 */

public class FileUploader {
    private static final Logger log = Logger.getLogger(FileUploader.class);
    private static final FileUploader fileUploader = new FileUploader(FileStore.getFileStore());
    /**
     * FileStore object which holds details of uploaded file
     */
    private FileStore fileStore;

    private FileUploader(FileStore fileStore) {
        this.fileStore = fileStore;
    }

    /**
     * getFileUploaderInstance() returns Singleton FileUploader object
     *
     * @return fileUploader
     */
    public static FileUploader getFileUploaderInstance() {
        return fileUploader;
    }

    /**
     * Method to upload a CSV file.
     *
     * @param source      location of the file
     * @param destination destination where the file should be copied to i.e. deployment/simulator/csvFiles
     * @throws FileAlreadyExistsException if the file exists in 'deployment/simulator/csvFiles' directory
     * @throws FileOperationsException    if an IOException occurs while copying uploaded stream to
     *                                    'deployment/simulator//csvFiles' directory
     */
    public void uploadFile(String source, String destination) throws FileAlreadyExistsException,
            FileOperationsException {
        String fileName = FilenameUtils.getName(source);
        // Validate file extension
        if (FilenameUtils.getExtension(fileName).equals("csv")) {
            if (!fileStore.checkExists(fileName)) {
//                use ValidatedInputStream to check whether the file size is less than the maximum size allowed ie 8MB
                try (ValidatedInputStream inputStream = new ValidatedInputStream(FileUtils.openInputStream(new
                        File(source)), 8388608)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Initialize a File reader for CSV file '" + fileName + "'.");
                    }
                    Files.copy(inputStream, Paths.get(destination, fileName));
                    if (log.isDebugEnabled()) {
                        log.debug("Successfully uploaded CSV file '" + fileName + "' to directory '" + destination
                                + "'");
                    }
                    fileStore.addFile(fileName);
                } catch (IOException e) {
                    log.error("Error occurred while copying the file '" + fileName + "' to directory '" +
                            destination + "'. ", e);
                    throw new FileOperationsException("Error occurred while copying the file '" + fileName + "' to " +
                            "directory '" + destination + "'. ", e);
                }
            } else {
                log.error("File '" + fileName + "' already exists in directory '" + destination + "'");
                throw new FileAlreadyExistsException("File '" + fileName + "' already exists in directory '" +
                        destination + "'");
            }
        } else {
            log.error("File '" + fileName + " has an invalid content type. Please upload a valid CSV file .");
            throw new FileOperationsException("File '" + fileName + " has an invalid content type."
                    + " Please upload a valid CSV file .");
        }

    }

    /**
     * Method to delete an uploaded file.
     *
     * @param fileName name of CSV file to be deleted
     * @throws FileOperationsException if an IOException occurs while deleting file
     */
    public boolean deleteFile(String fileName, String destination) throws FileOperationsException {
        try {
            if (fileStore.checkExists(fileName)) {
                fileStore.removeFile(fileName, destination);
                if (log.isDebugEnabled()) {
                    log.debug("Deleted file '" + fileName + "' from directory '" + destination + "'");
                }
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            log.error("Error occurred while deleting the file '" + fileName + "' from directory '" +
                    destination + "'", e);
            throw new FileOperationsException("Error occurred while deleting the file '" + fileName + "' from " +
                    "directory '" + destination + "'", e);
        }
    }

}
