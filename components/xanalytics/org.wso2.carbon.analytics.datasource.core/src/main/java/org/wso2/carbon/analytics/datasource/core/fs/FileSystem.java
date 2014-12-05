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

import org.wso2.carbon.analytics.datasource.core.AnalyticsException;

/**
 * Represents a virtual file system data operations.
 */
public interface FileSystem {

    /**
     * Checks if the file already exists.
     * @param path The location of the file
     * @return true if exists, false otherwise
     * @throws AnalyticsException
     */
    boolean exists(String path) throws AnalyticsException;

    /**
     * Lists the files in the directory.
     * @param path The path of the directory
     * @return The list of file name in the directory
     * @throws AnalyticsException
     */
    List<String> list(String path) throws AnalyticsException;

    /**
     * Deletes the file at the given path.
     * @param path The path of the file to be deleted
     * @throws AnalyticsException
     */

    void delete(String path) throws AnalyticsException;
    /**
     * Copies a file from a source to the target.
     * @param sourcePath The source file path
     * @param destPath The destination file path
     * @throws AnalyticsException
     */
    void copy(String sourcePath, String destPath) throws AnalyticsException;
    
    /**
     * Makes a new directory.
     * @param path The path of the directory to be created
     * @throws AnalyticsException
     */
    void mkdir(String path) throws AnalyticsException;
    
    /**
     * Creates an object to do read related operations.
     * @param path The path of the file
     * @return The {@link DataInput} object
     * @throws AnalyticsException
     */
    DataInput createInput(String path) throws AnalyticsException;
    
    /**
     * Creates an object to do write related operations.
     * @param path The path of the file
     * @return The {@link DataOutput} object
     * @throws AnalyticsException
     */
    DataOutput createOutput(String path) throws AnalyticsException;
    
    /**
     * Flushes out any changes cached in the system for the given path.
     * @param path The path where the changes needs to be flushed out
     * @throws AnalyticsException
     */
    void sync(String path) throws AnalyticsException;
    
    /**
     * Returns the length of the given file.
     * @param path The path of the file
     * @return The size of the file in bytes
     * @throws AnalyticsException
     */
    long length(String path) throws AnalyticsException;
    
    /**
     * Represents file data stream reading operations.
     */
    public interface DataInput {
                
        /**
         * Reads data in from the current stream.
         * @param buff The buffer to the data will be read in
         * @param offset The offset of the buffer the data will be written to, 0 indexed
         * @param len The size of the data to be read in bytes
         * @return The actual number of bytes that was read, -1 if we are at EOF
         * @throws AnalyticsException
         */
        int read(byte[] buff, int offset, int len) throws AnalyticsException;
        
        /**
         * Moves the current byte position to the given location.
         * @param pos The position the current position should be moved to
         * @throws AnalyticsException
         */
        void seek(long pos) throws AnalyticsException;
        
        /**
         * Returns the current file pointer.
         * @return The current file pointer position
         * @throws AnalyticsException
         */
        long getPosition() throws AnalyticsException;

        /**
         * Flush and close the current file input stream.
         * @throws AnalyticsException
         */
        void close() throws AnalyticsException;
        
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
         * @throws AnalyticsException
         */
        void write(byte[] data, int offset, int length) throws AnalyticsException;
                
        /**
         * Moves the current byte position to the given location.
         * @param pos The position the current position should be moved to
         * @throws AnalyticsException
         */
        void seek(long pos) throws AnalyticsException;
        
        /**
         * Returns the current file pointer.
         * @return The current file pointer position
         * @throws AnalyticsException
         */
        long getPosition() throws AnalyticsException;
        
        /**
         * Sets the length of the current file.
         * @param length The new file length
         * @throws AnalyticsException
         */
        void setLength(long length) throws AnalyticsException;
        
        /**
         * Flushes the current cached data out to the file.
         * @throws AnalyticsException
         */
        void flush() throws AnalyticsException;
        
        /**
         * Flush and close the current file output stream.
         * @throws AnalyticsException
         */
        void close() throws AnalyticsException;
        
    }
    
    /**
     * {@link DataOutput} close event listener.
     */
    public interface DataOutputClosureListener {
        
        /**
         * Called when the data output's close method is called.
         * @param path The file path the data output represent
         * @param output The {@link DataOutput} object this path represents
         */
        public void dataOutputClosing(String path, DataOutput output);
        
    }

}
