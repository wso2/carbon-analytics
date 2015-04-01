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
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.fs.AnalyticsFileSystem;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;
import org.wso2.carbon.analytics.datasource.hbase.util.HBaseUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Hadoop Distributed File System (HDFS) {@link org.wso2.carbon.analytics.datasource.core.fs.AnalyticsFileSystem} - implementation.
 */
public class HDFSAnalyticsFileSystem implements AnalyticsFileSystem {

    private FileSystem fileSystem;

    private static final Log log = LogFactory.getLog(HDFSAnalyticsFileSystem.class);

    public HDFSAnalyticsFileSystem(FileSystem fs) throws AnalyticsException {
        this.fileSystem = fs;
    }

    public HDFSAnalyticsFileSystem() {
        this.fileSystem = null;
    }

    @Override
    public void init(Map<String, String> properties) throws AnalyticsException {
        HBaseAnalyticsConfigurationEntry queryConfig = HBaseUtils.lookupConfiguration();
/*        String dsName = properties.get(HBaseAnalyticsDSConstants.DATASOURCE_NAME);
        if (dsName == null) {
            throw new AnalyticsException("The property '" + HBaseAnalyticsDSConstants.DATASOURCE_NAME +
                    "' is required");
        }
        try {
            this.fileSystem = (FileSystem) InitialContext.doLookup(dsName);
        } catch (NamingException e) {
            throw new AnalyticsException("Error in looking up data source: " + e.getMessage(), e);
        }*/

        Configuration conf = new Configuration();
        String hdfsHost = queryConfig.getHdfsHost();
        String hdfsDataDir = queryConfig.getHdfsDataDir();
        conf.set("fs.default.name", hdfsHost);
        conf.set("dfs.data.dir", hdfsDataDir);
        conf.set("fs.hdfs.impl", org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
        conf.set("fs.file.impl", org.apache.hadoop.fs.LocalFileSystem.class.getName());
        try {
            this.fileSystem = FileSystem.get(conf);
        } catch (Exception e) {
            throw new AnalyticsException("Error creating HDFS Configuration: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean exists(String source) throws IOException {
        return this.fileSystem.exists(HBaseUtils.createPath(source));
    }

    @Override
    public List<String> list(String source) throws IOException {
        Path path = HBaseUtils.createPath(source);
        List<String> filePaths = new ArrayList<>();
        /* maintaining normalized version of path string, because we'll be doing String operations on it later */
        source = GenericUtils.normalizePath(source);
        if (!(this.fileSystem.exists(path))) {
            if (log.isDebugEnabled()) {
                log.debug("Path specified (" + source + ") does not exist in the filesystem");
            }
        } else {
            FileStatus[] files = this.fileSystem.listStatus(path);
            if (null == files) {
                throw new IOException("An error occurred while listing files from the specified path: " + path);
            } else {
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
        Path path = HBaseUtils.createPath(source);
        if (!(this.fileSystem.exists(path))) {
            if (log.isDebugEnabled()) {
                log.debug("Path specified (" + source + ") does not exist in the filesystem");
            }
        } else {
            /* Directory will be deleted regardless it being empty or not*/
            this.fileSystem.delete(path, true);
        }
    }

    @Override
    public void mkdir(String source) throws IOException {
        Path path = HBaseUtils.createPath(source);
        if (this.fileSystem.exists(path)) {
            if (log.isDebugEnabled()) {
                log.debug("Path specified (" + source + ") already exists in the filesystem");
            }
        } else {
            this.fileSystem.mkdirs(path);
        }
    }

    @Override
    public DataInput createInput(String source) throws IOException {
        return new HDFSDataInput(HBaseUtils.createPath(source), this.fileSystem);
    }

    @Override
    public OutputStream createOutput(String source) throws IOException {
        Path path = HBaseUtils.createPath(source);
        if (this.fileSystem.exists(path)) {
            if (log.isDebugEnabled()) {
                log.debug("Specified path (" + source + ") already exists in filesystem and has been overwritten.");
            }
            /* Overwriting target path */
            return this.fileSystem.create(path, true);
        } else {
            /* Create new path */
            return this.fileSystem.create(path);
        }
    }

    @Override
    public void sync(String source) throws IOException {
        /* Nothing to do here, since the hadoop filesystem itself is responsible for all sync operations.*/
    }

    @Override
    public long length(String source) throws IOException {
        FileStatus status = this.fileSystem.getFileStatus(HBaseUtils.createPath(source));
        return status.getLen();
    }

    @Override
    public void destroy() throws IOException {
        try {
            this.fileSystem.close();
        } catch (IOException ignore) {
            /* ignore, we'll no longer use the fileSystem instance anyway */
        }
    }

    /**
     * HDFS Implementation of {@link org.wso2.carbon.analytics.datasource.core.fs.AnalyticsFileSystem.DataInput}
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
