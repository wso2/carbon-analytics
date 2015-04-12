/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.event.input.adapter.file.internal.util;


public final class FileEventAdapterConstants {

    private FileEventAdapterConstants() {
    }

    public static final String EVENT_ADAPTER_TYPE_FILE = "file";
    public static final String EVENT_ADAPTER_CONF_FILEPATH = "filepath";
    public static final String EVENT_ADAPTER_CONF_FILEPATH_HINT = "filepathHint";
    public static final int MIN_THREAD = 8;
    public static final int MAX_THREAD = 100;
    public static final long DEFAULT_KEEP_ALIVE_TIME = 20;
    public static final int JOB_QUEUE_SIZE = 1000;
    public static final int AXIS_TIME_INTERVAL_IN_MILLISECONDS = 10000;
    public static final String MIN_THREAD_NAME = "minThread";
    public static final String MAX_THREAD_NAME = "maxThread";
    public static final String DEFAULT_KEEP_ALIVE_TIME_NAME = "defaultKeepAliveTime";
    public static final String JOB_QUEUE_SIZE_NAME = "jobQueueSize";

}
