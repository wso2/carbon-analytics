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
import org.wso2.carbon.event.simulator.core.exception.FileNotFoundException;
import org.wso2.carbon.event.simulator.core.exception.FileOperationsException;
import org.wso2.carbon.event.simulator.core.exception.InvalidFileException;
import org.wso2.carbon.event.simulator.core.internal.util.EventSimulatorConstants;
import org.wso2.carbon.event.simulator.core.internal.util.ValidatedInputStream;
import org.wso2.carbon.event.simulator.core.service.EventSimulatorDataHolder;

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
     * @param destination destination where the file should be copied to i.e. destination
     * @throws FileAlreadyExistsException if the file exists in 'destination' directory
     * @throws FileOperationsException    if an IOException occurs while copying uploaded stream to
     *                                    'destination' directory
     * @throws InvalidFileException       if the file being uploaded is not a csv file
     * @throws FileNotFoundException if the file does not exists
     */
    public void uploadFile(String source, String destination) throws FileAlreadyExistsException,
            FileOperationsException, InvalidFileException, FileNotFoundException {
        String fileName = FilenameUtils.getName(source);
        // Validate file extension
        validateFileSource(source);
        if (!fileStore.checkExists(fileName)) {
//                use ValidatedInputStream to check whether the file size is less than the maximum size allowed ie 8MB
            try (ValidatedInputStream inputStream = new ValidatedInputStream(FileUtils.openInputStream(new
                    File(source)), EventSimulatorDataHolder.getInstance().getMaximumFileSize())) {
                Files.copy(inputStream, Paths.get(destination, fileName));
                if (log.isDebugEnabled()) {
                    log.debug("Successfully uploaded CSV file '" + fileName + "' to directory '" + destination
                            + "'");
                }
            } catch (java.nio.file.FileAlreadyExistsException e) {
                /**
                 * since the deployer takes about 15 seconds to update the fileStore, 2 consecutive requests
                 * upload the same csv file will result in java.nio.file.FileAlreadyExistsException
                 */
                log.error("File '" + fileName + "' already exists.");
                throw new FileAlreadyExistsException("File '" + fileName + "' already exists.");
            } catch (IOException e) {
                log.error("Error occurred while copying the file '" + fileName + "'. ", e);
                throw new FileOperationsException("Error occurred while copying the file '" + fileName + "'. ", e);
            }
        } else {
            log.error("File '" + fileName + "' already exists.");
            throw new FileAlreadyExistsException("File '" + fileName + "' already exists.");
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
                /**
                 * since the deployer takes about 15 seconds to update the fileStore, a request to update a csv file may
                 * fail if the filename isn't deleted from the filestore
                 * */
                fileStore.deleteFile(fileName);
                return Files.deleteIfExists(Paths.get(destination, fileName));
            } else {
                return false;
            }
        } catch (IOException e) {
            log.error("Error occurred while deleting the file '" + fileName + "'", e);
            throw new FileOperationsException("Error occurred while deleting the file '" + fileName + "'", e);
        }
    }

    /**
     * validateFileSource() is used to validate that the file exists and its a csv file.
     *
     * @param source file path of source
     * @throws InvalidFileException if the file doesn't exists or doesn't have '.csv' extension
     * @throws FileNotFoundException if the file does not exists
     */
    public void validateFileSource(String source) throws InvalidFileException, FileNotFoundException {
        File csvFile = new File(source);
        String fileName = csvFile.getName();
        if (FilenameUtils.isExtension(fileName, EventSimulatorConstants.CSV_FILE_EXTENSION)) {
            if (!csvFile.exists()) {
                log.error("File '" + fileName + "' does not exist.");
                throw new FileNotFoundException("File '" + fileName + "' does not exist.");
            }
        } else {
            log.error("File '" + fileName + " has an invalid content type. File type supported is '." +
                    EventSimulatorConstants.CSV_FILE_EXTENSION + "'.");
            throw new InvalidFileException("File '" + fileName + " has an invalid content type. File type " +
                    "supported is '." + EventSimulatorConstants.CSV_FILE_EXTENSION + "'.");
        }

    }

}
