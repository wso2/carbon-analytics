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
package org.wso2.carbon.event.execution.manager.core.internal.util;

import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;

/**
 * Consist of the constants required for EventManagerService
 */
public class ExecutionManagerConstants {

    // path canged for both cep/spark.
    public static final String TEMPLATE_DOMAIN_PATH = CarbonUtils.getCarbonConfigDirPath()
            + File.separator + "template-manager" + File.separator + "domain-template";

    public static final String TEMPLATE_CONFIG_PATH = RegistryConstants.PATH_SEPARATOR + "repository"
            + RegistryConstants.PATH_SEPARATOR + "components" + RegistryConstants.PATH_SEPARATOR
            + "org.wso2.carbon.event.execution.manager.core" + RegistryConstants.PATH_SEPARATOR + "template-config";

    //this is the registry path where specific deployers save their meta information.
    public static final String DEPLOYER_META_INFO_PATH = RegistryConstants.PATH_SEPARATOR + "repository"
                                                         + RegistryConstants.PATH_SEPARATOR + "components" + RegistryConstants.PATH_SEPARATOR
                                                         + "execution.manager.template.deployers";

    public static final String CONFIG_NAME_SEPARATOR = "-";

    public static final String CONFIG_FILE_EXTENSION = ".xml";

    public static final String DEFAULT_CHARSET = "UTF-8";

    public static final String REGEX_NAME_VALUE = "\\$";

    public static final String STREAM_MAPPING_PLAN_SUFFIX = "StreamMappingPlan";

    public static final String DEPLOYER_TYPE_REALTIME = "realtime";

    public static final String META_PREFIX = "meta_";

    public static final String CORRELATION_PREFIX = "correlation_";


    /**
     * To avoid instantiating
     */
    private ExecutionManagerConstants() {
    }
}
