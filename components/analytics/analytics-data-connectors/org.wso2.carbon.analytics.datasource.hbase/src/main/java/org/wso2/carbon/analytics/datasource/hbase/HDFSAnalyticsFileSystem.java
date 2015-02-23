/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.analytics.datasource.hbase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.wso2.carbon.analytics.datasource.core.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.AnalyticsFileSystem;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Hadoop Distributed File System (HDFS) {@link org.wso2.carbon.analytics.datasource.core.AnalyticsFileSystem} - implementation.
 */
public class HDFSAnalyticsFileSystem implements AnalyticsFileSystem {

    private FileSystem fileSystem;

    private static final Log log = LogFactory.getLog(HDFSAnalyticsFileSystem.class);

    public HDFSAnalyticsFileSystem(FileSystem fs) throws AnalyticsException {
        this.fileSystem = fs;
    }

    @Override
    public void init(Map<String, String> properties) throws AnalyticsException {
        String dsName = properties.get(HBaseAnalyticsDSConstants.DATASOURCE_NAME);
        if (dsName == null) {
            throw new AnalyticsException("The property '" + HBaseAnalyticsDSConstants.DATASOURCE_NAME +
                    "' is required");
        }
        try {
            this.fileSystem = (FileSystem) InitialContext.doLookup(dsName);
        } catch (NamingException e) {
            throw new AnalyticsException("Error in looking up data source: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean exists(String source) throws IOException {
        source = GenericUtils.normalizePath(source);
        Path path = new Path(source);
        return this.fileSystem.exists(path);
    }

    @Override
    public List<String> list(String source) throws IOException {
        source = GenericUtils.normalizePath(source);
        Path path = new Path(source);
        List<String> filePaths = new ArrayList<>();
        if (!(this.fileSystem.exists(path))) {
            log.debug("Path specified (" + source + ") does not exist in the filesystem");
        } else {
            FileStatus[] files = this.fileSystem.listStatus(path);
            if (null == files) {
                throw new IOException("An error occurred while listing files from the specified path.");
            } else {
                /* Expensive in-memory array iteration*/
                for (FileStatus file : files) {
                    if (null != file) {
                        filePaths.add(file.getPath().toString().substring(file.getPath().toString().lastIndexOf(source) +
                                source.length() + 1));
                    }
                }
            }
        }
        return filePaths;
    }

    @Override
    public void delete(String source) throws IOException {
        source = GenericUtils.normalizePath(source);
        Path path = new Path(source);
        if (!(this.fileSystem.exists(path))) {
            log.debug("Path specified (" + source + ") does not exist in the filesystem");
        } else {
            /* Directory will be deleted regardless it being empty or not*/
            this.fileSystem.delete(new Path(source), true);
        }
    }

    @Override
    public void mkdir(String source) throws IOException {
        source = GenericUtils.normalizePath(source);
        Path path = new Path(source);
        if (this.fileSystem.exists(path)) {
            log.debug("Path specified (" + source + ") already exists in the filesystem");
        } else {
            this.fileSystem.mkdirs(path);
        }
    }

    @Override
    public DataInput createInput(String source) throws IOException {
        source = GenericUtils.normalizePath(source);
        Path path = new Path(source);
        return new HDFSDataInput(path, this.fileSystem);
    }

    @Override
    public OutputStream createOutput(String source) throws IOException {
        source = GenericUtils.normalizePath(source);
        Path path = new Path(source);
        if (this.fileSystem.exists(path)) {
            log.debug("Specified path (" + source + ") already exists in filesystem and has been overwritten.");
            /* Overwriting target path */
            return this.fileSystem.create(path, true);
        } else {
            /* Create new path */
            return this.fileSystem.create(path);
        }
    }

    @Override
    public void sync(String path) throws IOException {
        /* Nothing to do here, since the hadoop filesystem itself is responsible for all sync operations.*/
    }

    @Override
    public long length(String source) throws IOException {
        source = GenericUtils.normalizePath(source);
        Path path = new Path(source);
        FileStatus status = this.fileSystem.getFileStatus(path);
        return status.getLen();
    }

    public void close() throws IOException {
        this.fileSystem.close();
    }

    /**
     * HDFS Implementation of {@link org.wso2.carbon.analytics.datasource.core.AnalyticsFileSystem.DataInput}
     */
    public class HDFSDataInput implements DataInput {

        private FileSystem fileSystem;
        private Path path;

        private FSDataInputStream stream;

        public HDFSDataInput(Path path, FileSystem fs) throws IOException {
            this.fileSystem = fs;
            this.path = path;
            this.stream = fs.open(path);
        }

        @Override
        public int read(byte[] buff, int offset, int len) throws IOException {
            return this.stream.read(buff, offset, len);
        }

        @Override
        public void seek(long pos) throws IOException {
            this.stream.seek(pos);
        }

        @Override
        public long getPosition() {
            try {
                return this.stream.getPos();
            } catch (IOException e) {
                /* Not worrying about IOException at compile time */
                throw new HDFSRuntimeException("Error getting the current file pointer from stream: ", e);
            }
        }

        @Override
        public void close() throws IOException {
            this.stream.close();
        }

        @Override
        public DataInput makeCopy() throws IOException {
            return new HDFSDataInput(this.path, this.fileSystem);
        }
    }

    public class HDFSRuntimeException extends RuntimeException {
        HDFSRuntimeException(String s, Throwable t) {
            super(s, t);
        }
    }

}
