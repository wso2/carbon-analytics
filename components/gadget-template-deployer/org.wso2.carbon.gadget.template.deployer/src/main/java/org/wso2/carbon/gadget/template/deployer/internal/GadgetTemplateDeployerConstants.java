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

package org.wso2.carbon.gadget.template.deployer.internal;

import org.wso2.carbon.registry.core.RegistryConstants;

public class GadgetTemplateDeployerConstants {

    private GadgetTemplateDeployerConstants() {
    }

    public static final String ARTIFACT_TYPE = "gadget";

    public static final String CONFIG_TAG = "config";

    public static final String PROPERTIES_TAG = "properties";

    public static final String PROPERTY_TAG = "property";

    public static final String TEMPLATE_MANAGER = "template-manager";

    public static final String GADGET_TEMPLATES = "gadget-templates";

    public static final String ARTIFACTS_TAG = "artifacts";

    public static final String ARTIFACT_TAG = "artifact";

    public static final String NAME_ATTRIBUTE = "name";

    public static final String FILE_ATTRIBUTE = "file";

    public static final String APP_NAME = "portal";

    public static final String DIRECTORY_NAME = "directoryName";

    public static final String TEMPLATE_DIRECTORY = "templateDirectory";

    public static final String DEFAULT_STORE_TYPE = "fs";

    public static final String ARTIFACT_DIRECTORY_MAPPING_PATH = "repository" + RegistryConstants.PATH_SEPARATOR + "components" + RegistryConstants.PATH_SEPARATOR +
            "org.wso2.carbon.gadget.template.deployer" + RegistryConstants.PATH_SEPARATOR + "artifact.directory.mapping";

    public static final int ENTITY_EXPANSION_LIMIT = 0;
}
