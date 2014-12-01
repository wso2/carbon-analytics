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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.datasource.core.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.fs.FileSystem;
import org.wso2.carbon.analytics.datasource.core.fs.FileSystem.DataInput;
import org.wso2.carbon.analytics.datasource.core.fs.FileSystem.DataOutput;

/**
 * This class represents a repository for storing index definitions.
 */
public class AnalyticsIndexDefinitionRepository {

    private static final Log log = LogFactory.getLog(AnalyticsIndexDefinitionRepository.class);
    
    private static final String INDEX_DEFINITION_FS_ROOT = "/_meta/index/";
    
    private static final String DEFAULT_CHARSET = "UTF8";
    
    private FileSystem fileSystem;
    
    public AnalyticsIndexDefinitionRepository(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }
    
    public Set<String> getIndices(int tenantId, String tableName) throws AnalyticsIndexException {
        DataInput in = null;
        ByteArrayOutputStream out = null;
        try {
            String path = this.generatePath(tenantId, tableName);
            if (this.fileSystem.exists(path)) {
                this.fileSystem.createInput(path);
                in = this.fileSystem.createInput(path);
                out = new ByteArrayOutputStream();
                byte[] buff = new byte[1024];
                int i;
                while ((i = in.read(buff, 0, buff.length)) > 0) {
                    out.write(buff, 0, i);
                }
                return this.decode(new String(out.toByteArray(), DEFAULT_CHARSET));
            } else {
                return new HashSet<String>(0);
            }
        } catch (Exception e) {
            throw new AnalyticsIndexException("Error in retrieving index definitions: " + e.getMessage(), e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    log.error("Error in closing data output buffer: " + e.getMessage(), e);
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (AnalyticsException e) {
                    log.error("Error in closing data input: " + e.getMessage(), e);
                }
            }            
        }
    }
    
    private String encode(Set<String> columns) {
        boolean firstDone = false;
        StringBuilder builder = new StringBuilder();
        for (Iterator<String> itr = columns.iterator(); itr.hasNext();) {
            if (firstDone) {
                builder.append("\n");
            } else {
                firstDone = true;
            }
            builder.append(itr.next());
        }
        return builder.toString();
    }
    
    private Set<String> decode(String data) {
        return new HashSet<String>(Arrays.asList(data.split("\n")));
    }
    
    private String generatePath(int tenantId, String tableName) {
        return INDEX_DEFINITION_FS_ROOT + tenantId + "/" + tableName.trim();
    }
        
    public void setIndices(int tenantId, String tableName, Set<String> columns) throws AnalyticsIndexException {
        DataOutput out = null;
        try {
            out = this.fileSystem.createOutput(this.generatePath(tenantId, tableName));
            byte[] data = this.encode(columns).getBytes(DEFAULT_CHARSET);
            out.write(data, 0, data.length);
        } catch (Exception e) {
            throw new AnalyticsIndexException("Error in setting indices: " + e.getMessage(), e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (AnalyticsException e) {
                    log.error("Error in closing data output: " + e.getMessage(), e);
                }
            }
        }
    }
    
    public void clearAllIndices(int tenantId, String tableName) throws AnalyticsIndexException {
        try {
            this.fileSystem.delete(this.generatePath(tenantId, tableName));
        } catch (AnalyticsException e) {
            throw new AnalyticsIndexException("Error in clearing indices: " + e.getMessage(), e);
        }
    }
            
}
