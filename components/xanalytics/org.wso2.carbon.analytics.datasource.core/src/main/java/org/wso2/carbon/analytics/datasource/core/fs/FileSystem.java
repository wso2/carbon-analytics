/*
 *  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.analytics.datasource.core.fs;

import java.util.List;

import org.wso2.carbon.analytics.datasource.core.AnalyticsDataSourceException;

/**
 * Represents a virtual file system data operations.
 */
public interface FileSystem {

    /**
     * Checks if the file already exists.
     * @param path The location of the file
     * @return true if exists, false otherwise
     * @throws AnalyticsDataSourceException
     */
    boolean exists(String path) throws AnalyticsDataSourceException;

    /**
     * Lists the files in the directory.
     * @param path The path of the directory
     * @return The list of file name in the directory
     * @throws AnalyticsDataSourceException
     */
    List<String> list(String path) throws AnalyticsDataSourceException;

    /**
     * Deletes the file at the given path.
     * @param path The path of the file to be deleted
     * @throws AnalyticsDataSourceException
     */
    void delete(String path) throws AnalyticsDataSourceException;

    /**
     * Copies a file from a source to the target.
     * @param sourcePath The source file path
     * @param destPath The destination file path
     * @throws AnalyticsDataSourceException
     */
    void copy(String sourcePath, String destPath) throws AnalyticsDataSourceException;
    
    /**
     * Makes a new directory.
     * @param path The path of the directory to be created
     * @throws AnalyticsDataSourceException
     */
    void mkdir(String path) throws AnalyticsDataSourceException;
    
    /**
     * Creates an object to do read related operations.
     * @param path The path of the file
     * @return The {@link DataInput} object
     * @throws AnalyticsDataSourceException
     */
    DataInput createInput(String path) throws AnalyticsDataSourceException;
    
    /**
     * Creates an object to do write related operations.
     * @param path The path of the file
     * @return The {@link DataOutput} object
     * @throws AnalyticsDataSourceException
     */
    DataOutput createOutput(String path) throws AnalyticsDataSourceException;
    
    /**
     * Flushes out any changes cached in the system for the given path.
     * @param path The path where the changes needs to be flushed out
     * @throws AnalyticsDataSourceException
     */
    void sync(String path) throws AnalyticsDataSourceException;
    
    /**
     * Returns the length of the given file.
     * @param path The path of the file
     * @return The size of the file in bytes
     * @throws AnalyticsDataSourceException
     */
    long length(String path) throws AnalyticsDataSourceException;
    
    /**
     * Represents file data stream reading operations.
     */
    public interface DataInput {
                
        /**
         * Reads data in from the current stream.
         * @param buff The buffer to the data will be read in
         * @param offset The offset of the buffer the data will be written to, 0 indexed
         * @param len The size of the data to be read in bytes
         * @return The actual number of bytes that was read
         * @throws AnalyticsDataSourceException
         */
        int read(byte[] buff, int offset, int len) throws AnalyticsDataSourceException;
        
        /**
         * Moves the current byte position to the given location.
         * @param pos The position the current position should be moved to
         * @throws AnalyticsDataSourceException
         */
        void seek(long pos) throws AnalyticsDataSourceException;
        
        /**
         * Returns the current file pointer.
         * @return The current file pointer position
         * @throws AnalyticsDataSourceException
         */
        long getPosition() throws AnalyticsDataSourceException;

        /**
         * Flush and close the current file input stream.
         * @throws AnalyticsDataSourceException
         */
        void close() throws AnalyticsDataSourceException;
        
    }
    
    /**
     * Represents file data stream writing operations.
     */
    public interface DataOutput {
        
        /**
         * Writes the given data to the stream.
         * @param data The data to be written out
         * @param offset The offset from the buffer to read the data from, 0 indexed
         * @param length The length of the data to be written out
         * @throws AnalyticsDataSourceException
         */
        void write(byte[] data, int offset, int length) throws AnalyticsDataSourceException;
                
        /**
         * Moves the current byte position to the given location.
         * @param pos The position the current position should be moved to
         * @throws AnalyticsDataSourceException
         */
        void seek(long pos) throws AnalyticsDataSourceException;
        
        /**
         * Returns the current file pointer.
         * @return The current file pointer position
         * @throws AnalyticsDataSourceException
         */
        long getPosition() throws AnalyticsDataSourceException;
        
        /**
         * Sets the length of the current file.
         * @param length The new file length
         * @throws AnalyticsDataSourceException
         */
        void setLength(long length) throws AnalyticsDataSourceException;
        
        /**
         * Flushes the current cached data out to the file.
         * @throws AnalyticsDataSourceException
         */
        void flush() throws AnalyticsDataSourceException;
        
        /**
         * Flush and close the current file output stream.
         * @throws AnalyticsDataSourceException
         */
        void close() throws AnalyticsDataSourceException;
        
    }

}
