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
package org.wso2.carbon.analytics.dataservice.indexing;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.dataservice.AnalyticsIndexException;
import org.wso2.carbon.analytics.datasource.core.fs.FileSystem;
import org.wso2.carbon.analytics.datasource.core.fs.FileSystem.DataInput;

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
    
    public Map<String, IndexType> getIndices(int tenantId, String tableName) throws AnalyticsIndexException {
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
                return this.decodeIndexDetails(new String(out.toByteArray(), DEFAULT_CHARSET));
            } else {
                return new HashMap<String, IndexType>(0);
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
                } catch (IOException e) {
                    log.error("Error in closing data input: " + e.getMessage(), e);
                }
            }            
        }
    }
    
    private String encodeIndexDetails(Map<String, IndexType> columns) {
        boolean firstDone = false;
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, IndexType> entry : columns.entrySet()) {
            if (firstDone) {
                builder.append("\n");
            } else {
                firstDone = true;
            }
            builder.append(entry.getKey() + " " + entry.getValue().name());
        }
        return builder.toString();
    }
    
    private Map<String, IndexType> decodeIndexDetails(String data) throws AnalyticsIndexException {
        String[] lines = data.split("\n");
        Map<String, IndexType> result = new HashMap<String, IndexType>();
        String[] entry;
        for (String line : lines) {
            line = line.trim();
            if (line.length() == 0) {
                continue;
            }
            entry = line.split(" ");
            if (entry.length < 2) {
                throw new AnalyticsIndexException("Invalid index detail entry '" + line + "'");
            }
            result.put(entry[0], IndexType.valueOf(entry[1]));
        }
        return result;
    }
    
    private String generatePath(int tenantId, String tableName) {
        return INDEX_DEFINITION_FS_ROOT + tenantId + "/" + tableName.trim();
    }
        
    public void setIndices(int tenantId, String tableName, Map<String, IndexType> columns) throws AnalyticsIndexException {
        OutputStream out = null;
        try {
            out = this.fileSystem.createOutput(this.generatePath(tenantId, tableName));
            byte[] data = this.encodeIndexDetails(columns).getBytes(DEFAULT_CHARSET);
            out.write(data, 0, data.length);
        } catch (Exception e) {
            throw new AnalyticsIndexException("Error in setting indices: " + e.getMessage(), e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    log.error("Error in closing data output: " + e.getMessage(), e);
                }
            }
        }
    }
    
    public void clearAllIndices(int tenantId, String tableName) throws AnalyticsIndexException {
        try {
            this.fileSystem.delete(this.generatePath(tenantId, tableName));
        } catch (IOException e) {
            throw new AnalyticsIndexException("Error in clearing indices: " + e.getMessage(), e);
        }
    }
            
}
