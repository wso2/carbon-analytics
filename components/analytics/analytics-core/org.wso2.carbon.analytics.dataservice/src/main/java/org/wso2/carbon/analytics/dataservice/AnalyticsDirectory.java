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
package org.wso2.carbon.analytics.dataservice;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.Lock;
import org.apache.lucene.store.LockFactory;
import org.apache.lucene.store.OutputStreamIndexOutput;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.fs.AnalyticsFileSystem;
import org.wso2.carbon.analytics.datasource.core.fs.AnalyticsFileSystem.DataInput;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This represents a Lucene {@link Directory} implementation using Carbon Analytics {@link AnalyticsFileSystem}.
 */
public class AnalyticsDirectory extends Directory {

    private static final int OUTPUT_STREAM_BUFFER_SIZE = 1024 * 10;

    private AnalyticsFileSystem analyticsFileSystem;
        
    private String path;
    
    private LockFactory lockFactory;
    
    public AnalyticsDirectory(AnalyticsFileSystem analyticsFileSystem, LockFactory lockFactory, 
            String path) throws AnalyticsException {
        this.analyticsFileSystem = analyticsFileSystem;
        this.path = GenericUtils.normalizePath(path);
        this.lockFactory = lockFactory;
        this.getLockFactory().setLockPrefix(this.getPath());
    }
    
    public AnalyticsFileSystem getFileSystem() {
        return analyticsFileSystem;
    }
    
    public String getPath() {
        return path;
    }

    @Override
    public void close() throws IOException {
        /* nothing to do */
    }

    private String generateFilePath(String name) {
        return this.getPath() + "/" + name;
    }
    
    @Override
    public IndexOutput createOutput(String name, IOContext ctx) throws IOException {
        String path = this.generateFilePath(name);
        OutputStream out = this.analyticsFileSystem.createOutput(path);
        return new OutputStreamIndexOutput(out, OUTPUT_STREAM_BUFFER_SIZE);
    }

    @Override
    public void deleteFile(String name) throws IOException {
        this.analyticsFileSystem.delete(this.generateFilePath(name));
    }

    @Override
    public boolean fileExists(String name) throws IOException {
        return this.analyticsFileSystem.exists(this.generateFilePath(name));        
    }

    @Override
    public long fileLength(String name) throws IOException {
        return this.analyticsFileSystem.length(this.generateFilePath(name));        
    }

    @Override
    public String[] listAll() throws IOException {
        return this.analyticsFileSystem.list(this.getPath()).toArray(new String[0]);
    }

    @Override
    public IndexInput openInput(String name, IOContext ctx) throws IOException {
        String path = this.generateFilePath(name);
        DataInput input = this.analyticsFileSystem.createInput(path);
        return new AnalyticsIndexInputAdaptor(path, input);
    }

    @Override
    public void sync(Collection<String> names) throws IOException {
        for (String name : names) {
            this.getFileSystem().sync(this.generateFilePath(name));            
        }
    }
    
    /**
     * Lucene {@link IndexInput} adaptor implementation using Carbon analytics {@link DataInput}.
     */
    private class AnalyticsIndexInputAdaptor extends IndexInput {
        
        private DataInput dataInput;
        
        private String path;
        
        private long length;
        
        private long offset;
        
        private List<IndexInput> clonedInputs = new ArrayList<IndexInput>();
        
        protected AnalyticsIndexInputAdaptor(String path, DataInput dataInput) throws IOException {
            this(path, dataInput, 0, analyticsFileSystem.length(path), path);            
        }
        
        protected AnalyticsIndexInputAdaptor(String path, DataInput dataInput, long offset, 
                long length, String sliceDescription) throws IOException {
            super(sliceDescription);
            this.path = path;
            this.dataInput = dataInput;
            this.offset = offset;
            this.length = length;
            this.seek(0);
        }

        @Override
        public void close() throws IOException {
            for (IndexInput in : this.clonedInputs) {
                in.close();
            }
            this.dataInput.close();
        }

        @Override
        public long getFilePointer() {
            return this.dataInput.getPosition() - this.offset;
        }

        @Override
        public long length() {
            return this.length;
        }

        @Override
        public void seek(long pos) throws IOException {
            this.dataInput.seek(pos + this.offset);            
        }

        @Override
        public byte readByte() throws IOException {
            byte[] buff = new byte[1];
            this.dataInput.read(buff, 0, buff.length);
            return buff[0];            
        }

        @Override
        public void readBytes(byte[] data, int offset, int length) throws IOException {
            this.dataInput.read(data, offset, length);
        }

        @Override
        public IndexInput slice(String sliceDescription, long offset, long length) throws IOException {
            return new AnalyticsIndexInputAdaptor(this.path, this.dataInput.makeCopy(), 
                    this.offset + offset, length, sliceDescription);
        }
        
        @Override
        public IndexInput clone() {
            IndexInput in;
            try {
                in = new AnalyticsIndexInputAdaptor(this.path, this.dataInput.makeCopy(), 
                        this.offset, this.length, this.toString());
                this.clonedInputs.add(in);
                return in;
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }            
        }
        
    }
    
    @Override
    public void clearLock(String name) throws IOException {
        this.getLockFactory().clearLock(name);
    }

    @Override
    public LockFactory getLockFactory() {
        return lockFactory;
    }

    @Override
    public Lock makeLock(String name) {
        return this.getLockFactory().makeLock(name);
    }

    @Override
    public void setLockFactory(LockFactory lockFactory) throws IOException {
        this.lockFactory = lockFactory;
    }

}
