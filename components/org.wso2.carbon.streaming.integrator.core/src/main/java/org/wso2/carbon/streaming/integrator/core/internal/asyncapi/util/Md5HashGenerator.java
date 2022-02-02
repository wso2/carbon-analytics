/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.streaming.integrator.core.internal.asyncapi.util;

import org.apache.commons.io.comparator.NameFileComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;


/**
 * This class generates MD5 hash value for the given files of the zip
 * using MD5 as the hashing algorithm.
 */
public class Md5HashGenerator {
    private static final Logger log = LoggerFactory.getLogger(Md5HashGenerator.class);
    /**
     * This generates the md5 hash value using the given algorithm
     *
     * @param path files available directory location
     * @return String
     */
    public static String generateHash(String path) throws IOException, NoSuchAlgorithmException {
        File dir = new File(path);
        return validateInputParams(dir.listFiles());
    }

    /**
     * This traversal through the directories and calculate md5
     *
     * @param files list of files available directory location
     * @return HashMap<String, String>
     */
    private static String validateInputParams(File[] files) throws IOException, NoSuchAlgorithmException {
        if (files != null) {
            // Order files according to the alphabetical order of file names when concatenating hashes.
            Arrays.sort(files, NameFileComparator.NAME_COMPARATOR);
            return calculateHash(files);
        }
        return null;
    }


    /**
     * This generates the hash value for files using the MD5 algorithm
     *
     * @param files, the List of File objects included in zip
     * @return String
     * @throws NoSuchAlgorithmException if the given algorithm is invalid or not found in {@link MessageDigest}
     */
    private static String calculateHash(File[] files) throws NoSuchAlgorithmException, IOException {
        MessageDigest md5Digest = MessageDigest.getInstance("MD5");
        return getFileChecksum(md5Digest, files[0]) + getFileChecksum(md5Digest, files[1]);
    }

    /**
     * Method returns the hashed value for the file using MD5 hashing as default
     *
     * @param file the updated/created time of the resource in UNIX time
     * @return String
     * @throws IOException if the given algorithm is invalid or not found in {@link MessageDigest}
     */
    private static String getFileChecksum(MessageDigest digest, File file) throws IOException {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            byte[] byteArray = new byte[1024];
            int bytesCount;
            while ((bytesCount = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            }
            fis.close();
            byte[] bytes = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte aByte : bytes) {
                sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        }  finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    log.error("Exception occurred when closing file input stream for: " + file.getName() + " when generating md5.", e);
                }
            }
        }
    }

}
