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

import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.wso2.carbon.event.simulator.core.exception.FileAlreadyExistsException;
import org.wso2.carbon.event.simulator.core.exception.FileLimitExceededException;
import org.wso2.carbon.event.simulator.core.exception.FileOperationsException;
import org.wso2.carbon.event.simulator.core.exception.InvalidFileException;
import org.wso2.carbon.event.simulator.core.internal.util.EventSimulatorConstants;
import org.wso2.carbon.event.simulator.core.internal.util.ValidatedInputStream;
import org.wso2.carbon.event.simulator.core.service.EventSimulatorDataHolder;
import org.wso2.carbon.event.simulator.core.util.LogEncoder;
import org.wso2.msf4j.formparam.FileInfo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
     * @param fileInfo    fileInfo of file being uploaded
     * @param inputStream inputStream of file content
     * @param destination destination where the file should be copied to i.e. destination
     * @throws FileAlreadyExistsException if the file exists in 'destination' directory
     * @throws FileOperationsException    if an IOException occurs while copying uploaded stream to
     *                                    'destination' directory
     * @throws InvalidFileException       if the file being uploaded is not a csv file
     */
    public void uploadFile(FileInfo fileInfo, InputStream inputStream, String destination)
            throws FileAlreadyExistsException, FileOperationsException, InvalidFileException {
        String fileName = fileInfo.getFileName();
        // Validate file extension
        if (FilenameUtils.isExtension(fileName, EventSimulatorConstants.CSV_FILE_EXTENSION)) {
            if (!fileStore.checkExists(fileName)) {
                //use ValidatedInputStream to check whether the file size is less than the maximum size allowed ie 8MB
                try (ValidatedInputStream validatedStream = new ValidatedInputStream(inputStream,
                        EventSimulatorDataHolder.getInstance().getMaximumFileSize())) {
                    Files.copy(validatedStream, Paths.get(destination, "temp.temp"));
                    FileUtils.moveFile(
                            FileUtils.getFile(Paths.get(destination, "temp.temp").toString()),
                            FileUtils.getFile(Paths.get(destination, fileName).toString()));
                    if (log.isDebugEnabled()) {
                        log.debug("Successfully uploaded CSV file '" + fileName + "'.");
                    }
                } catch (java.nio.file.FileAlreadyExistsException | FileExistsException e) {
                    /*
                     * since the deployer takes about 15 seconds to update the fileStore, 2 consecutive requests
                     * upload the same csv file will result in java.nio.file.FileAlreadyExistsException
                     */
                    log.error("File '" + fileName + "' already exists.");
                    throw new FileAlreadyExistsException("File '" + fileName + "' already exists.");
                } catch (FileLimitExceededException e) {
//                    since the validated input streams checks for the file size while streaming, part of the file may
//                    be copied prior to raising an error for file exceeding the maximum size limit.
                    deleteFile("temp.temp", destination);
                    log.error("File '" + fileName + "' exceeds the maximum file size of " +
                            (EventSimulatorDataHolder.getInstance().getMaximumFileSize() / 1024 / 1024) + " MB.", e);
                    throw new FileLimitExceededException("File '" + fileName + "' exceeds the maximum file size of " +
                            (EventSimulatorDataHolder.getInstance().getMaximumFileSize() / 1024 / 1024) + " MB.", e);
                } catch (IOException e) {
                    log.error("Error occurred while copying the file '" + fileName + "'. ", e);
                    throw new FileOperationsException("Error occurred while copying the file '" + fileName + "'. ", e);
                }
            } else {
                log.error("File '" + fileName + "' already exists.");
                throw new FileAlreadyExistsException("File '" + fileName + "' already exists.");
            }
        } else {
            log.error("File '" + fileName + "' is not a CSV file.");
            throw new InvalidFileException("File '" + fileName + "' is not a CSV file.");
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
                /*
                 * Update deletes the csv file and uploads the new file.
                 * since the deployer takes about 15 seconds to update the fileStore, a request to update a csv file
                 * where existing file name and new file names is the same may fail if the filename isn't deleted
                 * from the fileStore.
                 * */
                fileStore.deleteFile(fileName);
            }
            return Files.deleteIfExists(Paths.get(destination, fileName));
        } catch (IOException e) {
            log.error("Error occurred while deleting the file '" + LogEncoder.getEncodedString(fileName) + "'", e);
            throw new FileOperationsException("Error occurred while deleting the file '" + fileName + "'", e);
        }
    }

    /**
     * validateFileSource() is used to validate that the CSV file exists
     *
     * @param fileName    name of file being validated
     */
    public boolean validateFileExists(String fileName) {
        return fileStore.checkExists(fileName);
    }

    /**
     * retrieveFileNameList() is used to retrieve the names of CSV files already uploaded
     *
     * @param extension         type of files retrieved
     * @param directoryLocation the directory where the files reside
     * @throws FileOperationsException if an IO error occurs while reading file names
     */
    public List<String> retrieveFileNameList(String extension, Path directoryLocation) throws FileOperationsException {
        try {
            List<File> filesInFolder = Files.walk(directoryLocation)
                    .filter(Files::isRegularFile)
                    .filter(file -> FilenameUtils.isExtension(file.toString(), extension))
                    .map(Path::toFile)
                    .collect(Collectors.toList());
            if (log.isDebugEnabled()) {
                log.debug("Retrieved files with extension " + extension + ".");
            }
            List<String> fileNameList = new ArrayList<>();
            for (File file : filesInFolder) {
                fileNameList.add(file.getName());
            }
            return fileNameList;
        } catch (IOException e) {
            log.error("Error occurred when retrieving '" + extension + "' file names list");
            throw new FileOperationsException("Error occurred when retrieving '" + extension + "' file names list");
        }
    }
}
