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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

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
     * @throws AnalyticsDataSourceException
     */
    public abstract DataChunk readChunk(long n) throws AnalyticsDataSourceException;
    
    public DataChunk readChunkForPosition(long position) throws AnalyticsDataSourceException {
        return this.readChunk(this.getChunkNumber(position));
    }
    
    public DataChunk createEmptyChunk(long n) {
        return new DataChunk(n, new byte[this.getChunkSize()], false);
    }
    
    /**
     * Writes the given data chunks target stream.
     * @param data The chunk data
     * @throws AnalyticsDataSourceException
     */
    public abstract void writeChunks(List<DataChunk> chunks) throws AnalyticsDataSourceException;
    
    /**
     * Returns the length of the current stream.
     * @return The stream length
     * @throws AnalyticsDataSourceException
     */
    public abstract long length() throws AnalyticsDataSourceException;
    
    /**
     * Sets the length of the current stream.
     * @param length The stream length
     * @throws AnalyticsDataSourceException
     */
    public abstract void setLength(long length) throws AnalyticsDataSourceException;
    
    /**
     * Represents a data chunk.
     */
    public class DataChunk {
        
        private static final int MODIFICATION_BEGIN = 1;
        
        private static final int MODIFICATION_END = 2;
        
        private long chunkNumber;
        
        private byte[] data;
        
        private boolean whole;
        
        private SortedMap<Integer, List<Integer>> modificationEvents = new TreeMap<Integer, List<Integer>>();
        
        public DataChunk(long chunkNumber, byte[] data) {
            this(chunkNumber, data, true);
        }
        
        public DataChunk(long chunkNumber, byte[] data, boolean whole) {
            this.chunkNumber = chunkNumber;
            this.data = data;
            this.whole = whole;
        }

        public long getChunkNumber() {
            return chunkNumber;
        }

        public byte[] getData() {
            return data;
        }

        public boolean isWhole() {
            return whole;
        }
        
        public void setWhole(boolean whole) {
            this.whole = whole;
        }
        
        public boolean containsPosition(long position) {
            return ChunkedStream.this.getChunkNumber(position) == this.getChunkNumber();
        }
        
        public void markModified(int position, int length) {
            List<Integer> events = this.modificationEvents.get(position);
            if (events == null) {
                events = new ArrayList<Integer>();
                this.modificationEvents.put(position, events);
            }
            events.add(MODIFICATION_BEGIN);
            events = this.modificationEvents.get(position + length);
            if (events == null) {
                events = new ArrayList<Integer>();
                this.modificationEvents.put(position + length, events);
            }
            events.add(MODIFICATION_END);
            if (this.calculateMissingDataSections().size() == 0) {
                this.setWhole(true);
            }
        }
        
        public List<int[]> calculateMissingDataSections() {
            List<int[]> result = new ArrayList<int[]>();
            int beginCount = 0;
            int lastEmptyLocation = 0;
            for (Map.Entry<Integer, List<Integer>> entry : this.modificationEvents.entrySet()) {
                for (int mode : entry.getValue()) {
                    if (mode == MODIFICATION_BEGIN) {
                        if (beginCount == 0) {
                            if (entry.getKey() > lastEmptyLocation) {
                                result.add(new int[] { lastEmptyLocation, entry.getKey() });
                            }
                        }
                        beginCount++;
                    } else if (mode == MODIFICATION_END) {
                        beginCount--;
                        if (beginCount == 0) {
                            lastEmptyLocation = entry.getKey();
                        }
                    }
                }
            }
            if (lastEmptyLocation < this.getData().length) {
                result.add(new int[] { lastEmptyLocation, this.getData().length });
            }
            return result;
        }
        
    }
    
}
