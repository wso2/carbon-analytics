/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.event.input.adapter.filetail.internal.util;


public final class FileTailEventAdapterConstants {

    private FileTailEventAdapterConstants() {
    }

    public static final String EVENT_ADAPTER_TYPE_FILE = "file-tail";
    public static final String EVENT_ADAPTER_DESCRIPTION_FILE = "file.description";
    public static final String EVENT_ADAPTER_CONF_FILEPATH = "filepath";
    public static final String EVENT_ADAPTER_CONF_FILEPATH_HINT = "filepathHint";
    public static final String EVENT_ADAPTER_DELAY_MILLIS = "delayInMillis";
    public static final String EVENT_ADAPTER_DELAY_MILLIS_HINT = "delayInMillis.hint";
    public static final String EVENT_ADAPTER_START_FROM_END = "startFromEnd";
    public static final String EVENT_ADAPTER_START_FROM_END_HINT = "startFromEnd.hint";
    public static final int DEFAULT_DELAY_MILLIS = 1000;

}
