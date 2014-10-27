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
package org.wso2.carbon.analytics.datasource.core;

import java.util.List;

/**
 * Represents a virtual file system data operations.
 */
public interface FileSystem {

    boolean exists(String path) throws AnalyticsDataSourceException;

    List<String> list(String path) throws AnalyticsDataSourceException;

    void delete(String path) throws AnalyticsDataSourceException;

    void copy(String sourcePath, String destPath) throws AnalyticsDataSourceException;
    
    void mkdir(String path) throws AnalyticsDataSourceException;
    
    DataInput createInput(String path) throws AnalyticsDataSourceException;
    
    DataOutput createOutput(String path) throws AnalyticsDataSourceException;
    
    void sync(String path) throws AnalyticsDataSourceException;
    
    long length(String path) throws AnalyticsDataSourceException;
    
    public interface DataInput {
                
        int read(byte[] buff, int offset, int len) throws AnalyticsDataSourceException;
        
        void seek(long pos) throws AnalyticsDataSourceException;
        
        long getPosition() throws AnalyticsDataSourceException;

        void close() throws AnalyticsDataSourceException;
        
    }
    
    public interface DataOutput {
        
        void write(byte[] data, int offset, int length) throws AnalyticsDataSourceException;
                
        void seek(long pos) throws AnalyticsDataSourceException;
        
        long getPosition() throws AnalyticsDataSourceException;
        
        void setLength(long length) throws AnalyticsDataSourceException;
        
        void flush() throws AnalyticsDataSourceException;
        
        void close() throws AnalyticsDataSourceException;
        
    }

}
