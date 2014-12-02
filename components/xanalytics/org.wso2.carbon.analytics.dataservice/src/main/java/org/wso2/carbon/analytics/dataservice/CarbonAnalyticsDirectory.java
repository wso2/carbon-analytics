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

import java.io.IOException;
import java.util.Collection;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.Lock;
import org.apache.lucene.store.LockFactory;
import org.apache.lucene.store.LockObtainFailedException;
import org.wso2.carbon.analytics.datasource.core.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.AnalyticsLockException;
import org.wso2.carbon.analytics.datasource.core.fs.FileSystem;
import org.wso2.carbon.analytics.datasource.core.fs.FileSystem.DataInput;
import org.wso2.carbon.analytics.datasource.core.fs.FileSystem.DataOutput;
import org.wso2.carbon.analytics.datasource.core.lock.LockProvider;

/**
 * This represents a Lucene {@link Directory} implementation using Carbon Analytics {@link FileSystem}.
 */
public class CarbonAnalyticsDirectory extends Directory {

    private FileSystem fileSystem;
        
    private String path;
    
    public CarbonAnalyticsDirectory(FileSystem fileSystem, LockProvider lockProvider, 
            String path) throws AnalyticsException {
        this.fileSystem = fileSystem;
        this.path = this.normalisePath(path);
        try {
            this.setLockFactory(new AnalyticsIndexLockFactoryAdaptor(lockProvider));
            this.getLockFactory().setLockPrefix(this.getPath());
        } catch (IOException e) {
            throw new AnalyticsException("Error in creating Carbon analytics directory: " + e.getMessage(), e);
        }
    }
    
    private String normalisePath(String path) {
        if (path.endsWith("/")) {
            path += "/";
        }
        return path;
    }
    
    public FileSystem getFileSystem() {
        return fileSystem;
    }
    
    public String getPath() {
        return path;
    }

    @Override
    public void close() throws IOException {
        /* nothing to do */
    }

    private String generatePath(String name) {
        return this.getPath() + name;
    }
    
    @Override
    public IndexOutput createOutput(String name, IOContext ctx) throws IOException {
        DataOutput output;
        try {
            String path = this.generatePath(name);
            output = this.fileSystem.createOutput(path);
            return new AnalyticsIndexOutputAdaptor(output, path);
        } catch (AnalyticsException e) {
            throw new IOException("Error in creating index output for file '" + name + "': " + e.getMessage(), e);
        }        
    }

    @Override
    public void deleteFile(String name) throws IOException {
        try {
            this.fileSystem.delete(this.generatePath(name));
        } catch (AnalyticsException e) {
            throw new IOException("Error in deleting file '" + this.generatePath(name) + "': " + e.getMessage(), e);
        }
    }

    @Override
    public boolean fileExists(String name) throws IOException {
        return this.fileExists(this.generatePath(name));
    }

    @Override
    public long fileLength(String name) throws IOException {
        return this.fileLength(this.generatePath(name));
    }

    @Override
    public String[] listAll() throws IOException {
        try {
            return this.fileSystem.list(this.generatePath(this.getPath())).toArray(new String[0]);
        } catch (AnalyticsException e) {
            throw new IOException("Error in listing files in the directory '" + 
                    this.getPath() + "': " + e.getMessage(), e);
        }
    }

    @Override
    public IndexInput openInput(String name, IOContext ctx) throws IOException {
        String path = this.generatePath(name);
        DataInput input;
        try {
            input = this.fileSystem.createInput(path);
            return new AnalyticsIndexInputAdaptor(path, input);
        } catch (AnalyticsException e) {
            throw new IOException("Error in creating index input for file '" + name + "': " + e.getMessage(), e);
        }
        
    }

    @Override
    public void sync(Collection<String> names) throws IOException {
        /* nothing to do */
    }
    
    /**
     * Lucene {@link IndexOutput} adaptor implementation using Carbon analytics {@link DataOutput}.
     */
    private class AnalyticsIndexOutputAdaptor extends IndexOutput {

        private DataOutput dataOutput;
        
        private String path;
        
        public AnalyticsIndexOutputAdaptor(DataOutput dataOutput, String path) {
            this.dataOutput = dataOutput;
            this.path = path;
        }
        
        @Override
        public void close() throws IOException {
            try {
                this.dataOutput.close();
            } catch (AnalyticsException e) {
                throw new IOException("Error in closing data output: " + e.getMessage(), e);
            }
        }

        @Override
        public void flush() throws IOException {
            try {
                this.dataOutput.flush();
            } catch (AnalyticsException e) {
                throw new IOException("Error in flushing data output: " + e.getMessage(), e);
            }
        }

        @Override
        public long getFilePointer() {
            try {
                return this.dataOutput.getPosition();
            } catch (AnalyticsException e) {
                throw new RuntimeException("Error in retrieving file position: " + e.getMessage(), e);
            }
        }

        @Override
        public long length() throws IOException {
            try {
                return fileSystem.length(this.path);
            } catch (AnalyticsException e) {
                throw new IOException("Error in retrieving file length: " + e.getMessage(), e);
            }
        }

        @Override
        public void seek(long pos) throws IOException {
            try {
                this.dataOutput.seek(pos);
            } catch (AnalyticsException e) {
                throw new IOException("Error in index output file seek: " + e.getMessage(), e);
            }
        }

        @Override
        public void writeByte(byte data) throws IOException {
            try {
                byte[] buff = new byte[] { data };
                this.dataOutput.write(buff, 0, buff.length);
            } catch (AnalyticsException e) {
                throw new IOException("Error in writing data: " + e.getMessage(), e);
            }
        }

        @Override
        public void writeBytes(byte[] data, int offset, int length) throws IOException {
            try {
                this.dataOutput.write(data, 0, data.length);
            } catch (AnalyticsException e) {
                throw new IOException("Error in writing data: " + e.getMessage(), e);
            }
        }
        
    }
    
    /**
     * Lucene {@link IndexInput} adaptor implementation using Carbon analytics {@link DataInput}.
     */
    private class AnalyticsIndexInputAdaptor extends IndexInput {
        
        private DataInput dataInput;
        
        private String path;
        
        protected AnalyticsIndexInputAdaptor(String path, DataInput dataInput) {
            super(path);
            this.path = path;
            this.dataInput = dataInput;            
        }

        @Override
        public void close() throws IOException {
            try {
                this.dataInput.close();
            } catch (AnalyticsException e) {
                throw new IOException("Error in closing index input: " + e.getMessage(), e);
            }
        }

        @Override
        public long getFilePointer() {
            try {
                return this.dataInput.getPosition();
            } catch (AnalyticsException e) {
                throw new RuntimeException("Error in retrieving file location: " + e.getMessage(), e);
            }
        }

        @Override
        public long length() {
            try {
                return fileSystem.length(this.path);
            } catch (AnalyticsException e) {
                throw new RuntimeException("Error in retrieving file length: " + e.getMessage(), e);
            }
        }

        @Override
        public void seek(long pos) throws IOException {
            try {
                this.dataInput.seek(pos);
            } catch (AnalyticsException e) {
                throw new IOException("Error in index input file seek: " + e.getMessage(), e);
            }
        }

        @Override
        public byte readByte() throws IOException {
            try {
                byte[] buff = new byte[1];
                this.dataInput.read(buff, 0, buff.length);
                return buff[0];
            } catch (AnalyticsException e) {
                throw new RuntimeException("Error in reading data: " + e.getMessage(), e);
            }
        }

        @Override
        public void readBytes(byte[] data, int offset, int length) throws IOException {
            try {
                this.dataInput.read(data, offset, length);
            } catch (AnalyticsException e) {
                throw new RuntimeException("Error in reading data: " + e.getMessage(), e);
            }
        }
        
    }
    
    /**
     * Lucene {@link LockFactory} adaptor implementation using Carbon analytics {@link LockProvider}.
     */
    private class AnalyticsIndexLockFactoryAdaptor extends LockFactory {

        private LockProvider lockProvider;
        
        public AnalyticsIndexLockFactoryAdaptor(LockProvider lockProvider) {
            this.lockProvider = lockProvider;
        }
        
        private String generateLockName(String lockName) {
            return this.getLockPrefix() + lockName;
        }
        
        @Override
        public void clearLock(String lockName) throws IOException {
            try {
                this.lockProvider.clearLock(this.generateLockName(lockName));
            } catch (AnalyticsLockException e) {
                throw new IOException("Error in clearing lock '" + lockName + "': " + e.getMessage(), e);
            }
        }

        @Override
        public Lock makeLock(String lockName) {
            try {
                return new AnalyticsIndexLockAdaptor(this.lockProvider.getLock(this.generateLockName(lockName)));
            } catch (AnalyticsLockException e) {
                throw new RuntimeException("Error in creating lock '" + lockName + "': " + e.getMessage(), e);
            }
        }
        
    }
    
    /**
     * Lucene {@link Lock} adaptor implementation using Carbon analytics {@link org.wso2.carbon.analytics.datasource.core.lock.Lock}.
     */
    private class AnalyticsIndexLockAdaptor extends Lock {

        private org.wso2.carbon.analytics.datasource.core.lock.Lock lock;
        
        public AnalyticsIndexLockAdaptor(org.wso2.carbon.analytics.datasource.core.lock.Lock lock) {
            this.lock = lock;
        }
        
        @Override
        public boolean isLocked() throws IOException {
            try {
                return this.lock.isLocked();
            } catch (AnalyticsLockException e) {
                throw new IOException("Error in creating lock#isLocked: " + e.getMessage(), e);
            }
        }

        @Override
        public boolean obtain() throws IOException {
            try {
                this.lock.acquire();
                return true;
            } catch (AnalyticsLockException e) {
                throw new IOException("Error in creating lock#isLocked: " + e.getMessage(), e);
            }
        }
        
        @Override
        public boolean obtain(long lockWaitTimeout) throws IOException, LockObtainFailedException {
            try {
                if (!this.lock.acquire(lockWaitTimeout)) {
                    throw new LockObtainFailedException("Timed out in obtaining a lock for '" + 
                            lockWaitTimeout + "' ms.");
                }
                return true;
            } catch (AnalyticsLockException e) {
                throw new IOException("Error in creating lock#isLocked: " + e.getMessage(), e);
            }
        }

        @Override
        public void release() throws IOException {
//            try {
//                this.lock.release();
//            } catch (AnalyticsLockException e) {
//                throw new IOException("Error in releasing lock: " + e.getMessage(), e);
//            }
        }
        
    }

}
