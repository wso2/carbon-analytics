package org.wso2.carbon.databridge.streamdefn.registry.util;

import org.wso2.carbon.registry.core.RegistryConstants;

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
public class RegistryStreamDefinitionStoreUtil {
    private static final String STREAM_DEFINITION_STORE = "/StreamDefinitions";
    private static final String STREAM_INDEX_DEFINITION_STORE = "/StreamIndexDefinitions";

    public static String getStreamDefinitionPath(String streamName, String streamVersion) {
        return STREAM_DEFINITION_STORE + RegistryConstants.PATH_SEPARATOR +
                streamName + RegistryConstants.PATH_SEPARATOR + streamVersion;
    }

    public static String getStreamIndexDefinitionPath(String streamName, String streamVersion) {
        return STREAM_INDEX_DEFINITION_STORE + RegistryConstants.PATH_SEPARATOR +
                streamName + RegistryConstants.PATH_SEPARATOR + streamVersion;
    }



    public static String getStreamDefinitionStorePath() {
        return STREAM_DEFINITION_STORE;
    }
}
