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

import java.io.IOException;
import java.util.List;

/**
 * Base class for chucked data stream implementations.
 */
public abstract class ChunkedStream {

    private int chunkSize;
    
    public ChunkedStream(int chunkSize) {
        this.chunkSize = chunkSize;
    }
    
    public int getChunkSize() {
        return chunkSize;
    }
    
    /**
     * Returns chunk number given the byte position of the stream.
     * @param position The 0 based byte position
     * @return The 0 based chunk index
     */
    public long getChunkNumber(long position) {
        return position / this.getChunkSize();
    }
    
    /**
     * Reads in the nth chunk in the input, the chunk should contain the fully initialized 
     * buffer size, as mentioned in the chunk size of the stream. 
     * @param n The 0 based index of the chunk
     * @return The the data chunk that was read
     * @throws IOException
     */
    public abstract DataChunk readChunk(long n) throws IOException;
    
    public DataChunk readChunkForPosition(long position) throws IOException {
        return this.readChunk(this.getChunkNumber(position));
    }
    
    public DataChunk createEmptyChunk(long n) {
        return new DataChunk(n, new byte[this.getChunkSize()]);
    }
    
    /**
     * Writes the given data chunks target stream.
     * @param data The chunk data
     * @throws IOException
     */
    public abstract void writeChunks(List<DataChunk> chunks) throws IOException;
    
    /**
     * Returns the length of the current stream.
     * @return The stream length
     */
    public abstract long length();
    
    /**
     * Sets the length of the current stream.
     * @param length The stream length
     * @throws IOException
     */
    public abstract void setLength(long length) throws IOException;
    
    /**
     * Represents a data chunk.
     */
    public class DataChunk {
        
        private long chunkNumber;
        
        private byte[] data;
        
        public DataChunk(long chunkNumber, byte[] data) {
            this.chunkNumber = chunkNumber;
            this.data = data;
        }
        
        public long getChunkNumber() {
            return chunkNumber;
        }

        public byte[] getData() {
            return data;
        }

        public boolean containsPosition(long position) {
            return ChunkedStream.this.getChunkNumber(position) == this.getChunkNumber();
        }
        
    }
    
}
