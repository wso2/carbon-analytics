package org.wso2.carbon.bam.toolbox.deployer.util;

import org.wso2.carbon.databridge.commons.IndexDefinitionConstants;

/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class StreamDefnDTO {
    private String fileName;
    private String username;
    private String password;
    private String indexes;

    public StreamDefnDTO(String fileName) {
        this.fileName = fileName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFileName() {
        return fileName;
    }

    public void setIndexes(String secondaryIndexes, String customIndexes,
                           String fixedSearchProperties, boolean incrementalIndex,
                           String arbitraryIndexes) {
        indexes = "";
        if (arbitraryIndexes != null)
            indexes += "[" + IndexDefinitionConstants.ARBITRARY_INDEX + ":=" +
                    arbitraryIndexes + "]" + ",\n";
        if (customIndexes != null)
            indexes += "[" + IndexDefinitionConstants.CUSTOM_INDEX + ":=" +
                    customIndexes + "]" + ",\n";
        if (fixedSearchProperties != null)
            indexes += "[" + IndexDefinitionConstants.FIXED_INDEX + ":=" +
                    fixedSearchProperties + "]" + ",\n";
        if (incrementalIndex)
            indexes += "[" + IndexDefinitionConstants.INCREMENTAL_INDEX + ":=" +
                    incrementalIndex + "]" + ",\n";
        if (secondaryIndexes != null)
            indexes += "[" + IndexDefinitionConstants.SECONDARY_INDEX + ":=" +
                    secondaryIndexes + "]" + ",\n";
        if (!indexes.isEmpty()) {
            indexes = indexes.substring(0, indexes.length() - 2);
        }
    }

    public String getIndexes() {
        return indexes;
    }

}
