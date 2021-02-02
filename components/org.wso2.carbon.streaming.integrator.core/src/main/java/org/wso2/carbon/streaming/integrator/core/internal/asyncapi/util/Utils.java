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

package org.wso2.carbon.streaming.integrator.core.internal.asyncapi.util;

import io.siddhi.core.exception.SiddhiAppRuntimeException;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.VFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Utils {
    private static final Logger log = LoggerFactory.getLogger(Utils.class);

    /**
     * Zip the files in sourceFileUri in zipFile
     *
     * @param zipFile output ZIP file location
     */
    public static void zip(String sourceFileUri, String zipFile, List<String> fileList) throws IOException {
        byte[] buffer = new byte[1024];
        FileInputStream in = null;
        ZipOutputStream zos = null;
        FileOutputStream fos = null;
        try {
            if (!zipFile.endsWith(Constants.ZIP_FILE_EXTENSION)) {
                zipFile = zipFile.concat("." + Constants.ZIP_FILE_EXTENSION);
            }
            fos = new FileOutputStream(zipFile);
            zos = new ZipOutputStream(fos);
            if (log.isDebugEnabled()) {
                log.debug("Output to Zip : " + zipFile + " started for folder/ file: " + sourceFileUri);
            }
            for (String file : fileList) {
                if (file.compareTo(".zip")!=0) {
                    if (log.isDebugEnabled()) {
                        log.debug("File Adding : " + file + " to " + zipFile + ".");
                    }
                    if (file.indexOf(File.separator) == 0) {
                        file = file.substring(1);
                    }
                    ZipEntry ze = new ZipEntry(file);
                    zos.putNextEntry(ze);
                    in = new FileInputStream(sourceFileUri + File.separator + file);
                    int len;
                    while ((len = in.read(buffer)) > 0) {
                        zos.write(buffer, 0, len);
                    }
                    in.close();
                }
            }
            zos.closeEntry();
            if (log.isDebugEnabled()) {
                log.debug("Output to Zip : " + zipFile + " is complete for folder/ file: " + sourceFileUri);
            }
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    log.error("IO exception occurred when closing zip input stream for file path: " + sourceFileUri);
                }
            }
            if (zos != null) {
                try {
                    zos.close();
                } catch (IOException e) {
                    log.error("IO exception occurred when closing zip output stream for file path: " + sourceFileUri);
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    log.error("IO exception occurred when closing file output stream for file path: " + sourceFileUri);
                }
            }
        }
    }

    /**
     * Traverse a directory and get all files,
     * and add the file into fileList
     *
     * @param node file or directory
     */
    public static void generateFileList(String sourceFileUri, File node, List<String> fileList) {
        //add file only
        if (node.isFile() && !node.getName().contains(".DS_Store")) {
            fileList.add(generateZipEntry(node.getAbsoluteFile().toString(), sourceFileUri));
        }
        if (node.isDirectory()) {
            String[] subNote = node.list();
            if (subNote != null) {
                for (String filename : subNote) {
                    generateFileList(sourceFileUri, new File(node, filename), fileList);
                }
            }
        }
    }

    /**
     * Format the file path for zip
     *
     * @param file file path
     * @return Formatted file path
     */
    private static String generateZipEntry(String file, String sourceFileUri) {
        return file.substring(sourceFileUri.length());
    }

    /**
     * Get the file object for the given path
     *
     * @param filePathUri uri of the file path
     */
    public static FileObject getFileObject(String filePathUri) {
        FileSystemOptions opts = new FileSystemOptions();
        FileSystemManager fsManager;
        try {
            fsManager = VFS.getManager();
            return fsManager.resolveFile(filePathUri, opts);
        } catch (FileSystemException e) {
            throw new SiddhiAppRuntimeException("Exception occurred when getting VFS manager", e);
        }
    }
}
