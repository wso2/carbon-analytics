/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.analytics.eventsink.template.deployer.internal.util;

import org.wso2.carbon.event.template.manager.core.internal.util.TemplateManagerConstants;
import org.wso2.carbon.registry.core.RegistryConstants;

public class EventSinkTemplateDeployerConstants {

    public static final char META_INFO_STREAM_NAME_SEPARATER = ',';

    public static final String EVENT_SINK_DEPLOYER_TYPE = "eventsink";

    public static final String META_INFO_COLLECTION_PATH = TemplateManagerConstants.DEPLOYER_META_INFO_PATH
                                                           + RegistryConstants.PATH_SEPARATOR + EVENT_SINK_DEPLOYER_TYPE;

    public static final String ARTIFACT_ID_TO_COLUMN_DEF_KEYS_COLLECTION_PATH = META_INFO_COLLECTION_PATH
                                                                                + RegistryConstants.PATH_SEPARATOR + "artifactID-to-column-def-keys";

    public static final String ARTIFACT_ID_TO_STREAM_IDS_COLLECTION_PATH = META_INFO_COLLECTION_PATH
                                                                                + RegistryConstants.PATH_SEPARATOR + "artifactID-to-streamIDs";

    public static final String STREAM_ID_TO_ARTIFACT_IDS_COLLECTION_PATH = META_INFO_COLLECTION_PATH
                                                                           + RegistryConstants.PATH_SEPARATOR + "streamID-to-artifactIDs";

    public static final String COLUMN_DEF_KEY_TO_ARTIFACT_IDS_COLLECTION_PATH = META_INFO_COLLECTION_PATH
                                                                           + RegistryConstants.PATH_SEPARATOR + "column-def-key-to-artifactIDs";

    public static final String COLUMN_DEF_KEY_TO_COLUMN_DEF_HASH_RESOURCE_PATH = META_INFO_COLLECTION_PATH
                                                                                + RegistryConstants.PATH_SEPARATOR + "column-def-key-to-column-def-hash";

    public static final String COLUMN_DEF_KEY_SEPARATOR = "  ";     //double space

    //single space. Using space because this needs to be a restricted char for stream names. At the same time, should be allowed as a registry property value.
    public static final String COLUMN_KEY_COMPONENT_SEPARATOR = " ";

    public static final String SEPARATOR = ",";
}
