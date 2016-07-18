/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.jaggeryapp.template.deployer.internal;

import org.wso2.carbon.event.template.manager.core.internal.util.TemplateManagerConstants;
import org.wso2.carbon.registry.core.RegistryConstants;

public class JaggeryappTemplateDeployerConstants {

    private JaggeryappTemplateDeployerConstants(){

    }

    public static final String ARTIFACT_TYPE = "jaggeryapp";

    public static final String CONFIG_TAG = "config";

    public static final String PROPERTIES_TAG = "properties";

    public static final String PROPERTY_TAG = "property";

    public static final String TEMPLATE_MANAGER = "template-manager";

    public static final String JAGGERYAPP_TEMPLATES = "jaggeryapp-templates";

    public static final String ARTIFACTS_TAG = "artifacts";

    public static final String ARTIFACT_TAG = "artifact";

    public static final String NAME_ATTRIBUTE = "name";

    public static final String FILE_ATTRIBUTE = "file";

    public static final String DIRECTORY_NAME = "directoryName";

    public static final String TEMPLATE_DIRECTORY = "templateDirectory";

    public static final String META_INFO_COLLECTION_PATH = TemplateManagerConstants.DEPLOYER_META_INFO_PATH
            + RegistryConstants.PATH_SEPARATOR
            + JaggeryappTemplateDeployerConstants.ARTIFACT_TYPE;

    public static final int ENTITY_EXPANSION_LIMIT = 0;

}
