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

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.wso2.carbon.analytics.datasource.core.fs.ChunkedStream.DataChunk;

/**
 * Chucked data output implementation.
 */
public class ChunkedDataOutput extends OutputStream {

    private static final int DEFAULT_DATA_CHUNK_FLUSH_THRESHOLD = 200;
    
    private ChunkedStream stream;

    private long position;
    
    private List<DataChunk> dataChunks = new ArrayList<ChunkedStream.DataChunk>();
        
    private int flushChunkThreshold;
    
    private boolean flushed;
    
    public ChunkedDataOutput(ChunkedStream stream, int flushChunkThreshold) throws IOException {
        this.stream = stream;
        this.flushChunkThreshold = flushChunkThreshold;
    }
    
    public ChunkedDataOutput(ChunkedStream stream) throws IOException {
        this(stream, DEFAULT_DATA_CHUNK_FLUSH_THRESHOLD);
    }

    public ChunkedStream getStream() {
        return stream;
    }
    
    @Override
    public void write(int b) throws IOException {
        byte[] data = new byte[] { (byte) b };
        this.write(data, 0, 1);
    }
    
    @Override
    public void write(byte[] data, int offset, int len) throws IOException {
        DataChunk chunk;
        int chunkPosition, remaining;
        while (len > 0) {
            this.flushed = false;
            chunkPosition = (int) (this.position % this.getStream().getChunkSize());
            if (chunkPosition == 0) {
                this.dataChunks.add(this.getStream().createEmptyChunk(this.position / this.getStream().getChunkSize()));
            }
            chunk = this.dataChunks.get(this.dataChunks.size() - 1);            
            remaining = this.getStream().getChunkSize() - chunkPosition;
            if (remaining > len) {                
                remaining = len;
            }
            System.arraycopy(data, offset, chunk.getData(), chunkPosition, remaining);
            this.position += remaining;
            offset += remaining;
            len -= remaining;
            if (this.dataChunks.size() >= this.flushChunkThreshold) {
                this.flush();
            }
        }
    }

    @Override
    public void flush() throws IOException {
        if (this.flushed) {
            return;
        }
        int chunkPosition = (int) (this.position % this.getStream().getChunkSize());
        DataChunk activeChunk = null;
        if (chunkPosition > 0) {
            activeChunk = this.dataChunks.get(this.dataChunks.size() - 1);
        }
        this.getStream().writeChunks(this.dataChunks);
        this.getStream().setLength(this.position);        
        this.dataChunks.clear();
        if (activeChunk != null) {
            this.dataChunks.add(activeChunk);
        }
        this.flushed = true;
    }

    @Override
    public void close() throws IOException {
        this.flush();
    }

}
