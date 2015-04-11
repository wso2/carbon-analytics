/*
 *
 *  Copyright (c) 2014-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.event.output.adapter.ui.internal.util;

/**
 * This class contains the constants related to ui Output Event Adaptor.
 */
public class UIEventAdapterConstants {

    private UIEventAdapterConstants() {
    }

    public static final String ADAPTER_TYPE_UI = "ui";
    public static final String ADAPTER_UI_OUTPUT_STREAM_NAME = "output.event.stream.name";
    public static final String ADAPTER_UI_OUTPUT_STREAM_VERSION = "output.event.stream.version";
    public static final String ADAPTER_UI_DEFAULT_OUTPUT_STREAM_VERSION = "1.0.0";
    public static final String ADAPTER_UI_COLON = ":";
    public static final int EVENTS_QUEUE_SIZE = 30;
    public static final int INDEX_ZERO = 0;
    public static final int INDEX_ONE = 1;
    public static final int INDEX_TWO = 2;
}
