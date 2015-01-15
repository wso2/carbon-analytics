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

import org.wso2.carbon.analytics.datasource.core.ChunkedStream.DataChunk;
import org.wso2.carbon.analytics.datasource.core.AnalyticsFileSystem.DataInput;

/**
 * Chucked data input implementation.
 */
public class ChunkedDataInput implements DataInput {
        
    private ChunkedStream stream;
    
    private DataChunk currentChunk;
    
    private long position;
    
    private long length;
    
    public ChunkedDataInput(ChunkedStream stream) {
        this.stream = stream;
        this.length = this.stream.length();
    }

    public ChunkedStream getStream() {
        return stream;
    }
    
    private void checkCurrentChunk() throws IOException {
        if (this.currentChunk == null || !this.currentChunk.containsPosition(this.getPosition())) {
            this.currentChunk = this.getStream().readChunkForPosition(this.getPosition());
        }
    }
    
    private int getIndexInCurrentChunk(long position) {
        return (int) (position - this.currentChunk.getChunkNumber() * this.getStream().getChunkSize());
    }
    
    @Override
    public int read(byte[] buff, int offset, int len)
            throws IOException {
        int remaining, chunkDataIndex;
        if (this.position + len > this.length) {
            len = (int) (this.length - this.position);
            if (len <= 0) {
                len = -1;
            }
        }
        int tmpLen = len;
        while (tmpLen > 0) {
            this.checkCurrentChunk();
            chunkDataIndex = this.getIndexInCurrentChunk(this.position);
            remaining = this.getStream().getChunkSize() - chunkDataIndex;
            if (remaining > tmpLen) {
                remaining = tmpLen;
            } 
            System.arraycopy(this.currentChunk.getData(), chunkDataIndex, buff, offset, remaining);
            this.position += remaining;
            offset += remaining;
            tmpLen -= remaining;            
        }
        return len;
    }
    
    @Override
    public void seek(long pos) {
        this.position = pos;
    }

    @Override
    public long getPosition() {
        return position;
    }

    @Override
    public void close() throws IOException {
        /* nothing to do */
    }

    @Override
    public DataInput makeCopy() {
        ChunkedDataInput in = new ChunkedDataInput(this.stream);
        in.seek(this.position);
        return in;
    }

}
