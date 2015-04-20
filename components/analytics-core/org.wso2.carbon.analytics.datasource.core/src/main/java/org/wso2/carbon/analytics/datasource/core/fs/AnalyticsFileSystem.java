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

import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;


/**
 * Represents a virtual file system data operations, the path values can either terminate with "/" or not,
 * the implementations must normalize this and store it.
 */
public interface AnalyticsFileSystem {

    /**
     * This method initializes the AnalyticsFileSystem implementation, and is called once before any other method.
     * @param properties The properties associated with this analytics file system
     * @throws AnalyticsException
     */
    void init(Map<String, String> properties) throws AnalyticsException;
    
    /**
     * Checks if the file already exists.
     * @param path The location of the file
     * @return true if exists, false otherwise
     * @throws IOException
     */
    boolean exists(String path) throws IOException;

    /**
     * Lists the files in the directory.
     * @param path The path of the directory
     * @return The list of file names in the directory
     * @throws IOException
     */
    List<String> list(String path) throws IOException;

    /**
     * Deletes the file at the given path.
     * @param path The path of the file to be deleted
     * @throws IOException
     */
    void delete(String path) throws IOException;
    
    /**
     * Makes a new directory.
     * @param path The path of the directory to be created
     * @throws IOException
     */
    void mkdir(String path) throws IOException;
    
    /**
     * Creates an object to do read related operations.
     * @param path The path of the file
     * @return The {@link DataInput} object
     * @throws IOException
     */
    DataInput createInput(String path) throws IOException;
    
    /**
     * Creates an {@link OutputStream} to do write related operations.
     * @param path The path of the file
     * @return The {@link OutputStream} object
     * @throws IOException
     */
    OutputStream createOutput(String path) throws IOException;
    
    /**
     * Flushes out any changes cached in the system for the given path.
     * @param path The path where the changes needs to be flushed out
     * @throws IOException
     */
    void sync(String path) throws IOException;
    
    /**
     * Returns the length of the given file.
     * @param path The path of the file
     * @return The size of the file in bytes
     * @throws IOException
     */
    long length(String path) throws IOException;

    /**
     *  Destroys the AnalyticsFileSystem implementation and closes
     *  all connection transients initiated by the AnalyticsFileSystem
     *  implementation
     *  @throws IOException
     */
    void destroy() throws IOException;
    
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
         * @throws IOException
         */
        int read(byte[] buff, int offset, int len) throws IOException;
        
        /**
         * Moves the current byte position to the given location.
         * @param pos The position the current position should be moved to
         * @throws IOException
         */
        void seek(long pos) throws IOException;
        
        /**
         * Returns the current file pointer.
         * @return The current file pointer position
         */
        long getPosition();

        /**
         * Flush and close the current file input stream.
         * @throws IOException
         */
        void close() throws IOException;
        
        /**
         * Creates a copy of the current data input object, with its copy having an
         * independent data pointer.
         * @return The cloned {@link DataInput} object
         * @throws IOException
         */
        DataInput makeCopy() throws IOException;
        
    }
    
}
