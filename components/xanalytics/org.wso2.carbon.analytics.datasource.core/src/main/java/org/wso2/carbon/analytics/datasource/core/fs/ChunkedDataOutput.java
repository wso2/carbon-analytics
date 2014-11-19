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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.wso2.carbon.analytics.datasource.core.AnalyticsDataSourceException;
import org.wso2.carbon.analytics.datasource.core.ChunkedStream;
import org.wso2.carbon.analytics.datasource.core.ChunkedStream.DataChunk;
import org.wso2.carbon.analytics.datasource.core.fs.FileSystem.DataOutput;

/**
 * Chucked data output implementation.
 */
public class ChunkedDataOutput implements DataOutput {

    private static final int DEFAULT_DATA_CHUNK_FLUSH_THRESHOLD = 100;
    
    private ChunkedStream stream;
        
    private long position;
    
    private Map<Long, DataChunk> dataChunks = new HashMap<Long, ChunkedStream.DataChunk>();
    
    private long length;
    
    private int flushChunkThreshold;
    
    public ChunkedDataOutput(ChunkedStream stream, int flushChunkThreshold) throws AnalyticsDataSourceException {
        this.stream = stream;
        this.length = this.stream.length();
        this.flushChunkThreshold = flushChunkThreshold;
    }
    
    public ChunkedDataOutput(ChunkedStream stream) throws AnalyticsDataSourceException {
        this(stream, DEFAULT_DATA_CHUNK_FLUSH_THRESHOLD);
    }

    public ChunkedStream getStream() {
        return stream;
    }
    
    private DataChunk loadChunk(long chunkNumber) {
        DataChunk result = this.dataChunks.get(chunkNumber);
        if (result == null) {
            result = this.getStream().createEmptyChunk(chunkNumber);
            this.dataChunks.put(chunkNumber, result);
        }
        return result;
    }
    
    private DataChunk loadChunkForPosition(long position) {
        return this.loadChunk(this.getStream().getChunkNumber(position));
    }
    
    @Override
    public void write(byte[] data, int offset, int len) throws AnalyticsDataSourceException {
        DataChunk chunk;
        int chunkPosition, remaining;
        while (len > 0) {
            chunk = this.loadChunkForPosition(this.position);
            chunkPosition = (int) (this.position % this.getStream().getChunkSize());
            remaining = this.getStream().getChunkSize() - chunkPosition;
            if (remaining > len) {                
                remaining = len;
            } 
            System.arraycopy(data, offset, chunk.getData(), chunkPosition, remaining);
            if (!chunk.isWhole()) {
                /* if the chunk is not whole, that means, the original source data is not filled in yet,
                 * so we have to mark the data areas we fill in this chunk, so we can later read in from
                 * the original source and fill in the blanks */
                chunk.markModified(chunkPosition, remaining);
            }
            this.position += remaining;
            offset += remaining;
            len -= remaining;
            if (this.dataChunks.size() >= this.flushChunkThreshold) {
                this.flush();
            }
        }
    }

    @Override
    public void seek(long pos) throws AnalyticsDataSourceException {
        long chunkNumber = this.getStream().getChunkNumber(pos);
        this.dataChunks.put(chunkNumber, this.getStream().readChunk(chunkNumber));
    }

    @Override
    public long getPosition() throws AnalyticsDataSourceException {
        return position;
    }

    @Override
    public void setLength(long length) throws AnalyticsDataSourceException {
        this.getStream().setLength(length);
    }

    @Override
    public void flush() throws AnalyticsDataSourceException {
        if (this.position > this.length) {
            this.setLength(this.position);
        }
        List<DataChunk> dataChunks = new ArrayList<ChunkedStream.DataChunk>(this.dataChunks.values());
        for (DataChunk dataChunk : dataChunks) {
            if (!dataChunk.isWhole()) {
                this.makeWhole(dataChunk);
            }
        }
        this.getStream().writeChunks(dataChunks);
    }
    
    private void makeWhole(DataChunk dataChunk) throws AnalyticsDataSourceException {
        DataChunk originalChunk = this.getStream().readChunk(dataChunk.getChunkNumber());
        List<int[]> missingSections = dataChunk.calculateMissingDataSections();
        for (int[] section : missingSections) {
            System.arraycopy(originalChunk.getData(), section[0], dataChunk.getData(), section[0], 
                    section[1] - section[0]);
        }
    }

    @Override
    public void close() throws AnalyticsDataSourceException {
        this.flush();
    }

}
