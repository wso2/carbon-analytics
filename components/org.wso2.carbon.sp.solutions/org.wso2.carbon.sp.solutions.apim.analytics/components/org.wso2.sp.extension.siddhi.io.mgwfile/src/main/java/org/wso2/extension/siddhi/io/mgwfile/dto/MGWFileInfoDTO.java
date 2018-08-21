/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.extension.siddhi.io.mgwfile.dto;

/**
 * This class represents an uploaded file
 */
public class MGWFileInfoDTO {
    private String fileName;
    private long timeStamp;

    /**
     * Constructor
     *
     * @param fileName  name of the file
     * @param timeStamp Timestamp of the file creation
     */
    public MGWFileInfoDTO(String fileName, long timeStamp) {
        this.fileName = fileName;
        this.timeStamp = timeStamp;
    }

    /**
     * Get the file name set by a constructor
     * @return String name of the file
     */
    public String getFileName() {
        return fileName;
    }

    /**
     *
     * Get the time stamp set by a constructor
     * @return long Timestamp of the file creation
     */
    public long getTimeStamp() {
        return timeStamp;
    }


    @Override
    public String toString() {
        return "[ FileName : " + fileName + ", TimeStamp : " + timeStamp + "]";
    }
}
