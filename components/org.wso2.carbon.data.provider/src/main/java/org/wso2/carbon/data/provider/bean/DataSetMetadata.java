/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.data.provider.bean;

/**
 * model class for the metadata of the data.
 */
public class DataSetMetadata {
    private final String[] names;
    private final Types[] types;

    /**
     * enum for define the supportive data column types.
     */
    public enum Types {
        LINEAR, ORDINAL, TIME, OBJECT
    }

    public DataSetMetadata(int columnCount) {
        names = new String[columnCount];
        types = new Types[columnCount];
    }

    public String[] getNames() {
        return names;
    }

    public Types[] getTypes() {
        return types;
    }

    public void put(int columnCount, String name, Types type) {
        this.names[columnCount] = name;
        this.types[columnCount] = type;
    }

    public int getColumnCount() {
        if (names != null) {
            return names.length;
        } else {
            return 0;
        }
    }
}
