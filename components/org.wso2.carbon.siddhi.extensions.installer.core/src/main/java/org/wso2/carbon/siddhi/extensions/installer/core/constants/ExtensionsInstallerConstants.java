/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.siddhi.extensions.installer.core.constants;

import org.wso2.carbon.utils.Utils;

/**
 * Contains constants used in Siddhi Extensions Installer.
 */
public class ExtensionsInstallerConstants {

    private ExtensionsInstallerConstants() {
        // Prevents instantiation.
    }

    public static final String CONFIG_FILE_LOCATION =
        Utils.getRuntimePath().normalize().toString() + "/resources/extensionsInstaller/extensionDependencies.json";
    private static final String CARBON_HOME = Utils.getCarbonHome().normalize().toString();
    public static final String RUNTIME_JARS_LOCATION = CARBON_HOME + "/jars";
    public static final String RUNTIME_LIB_LOCATION = CARBON_HOME + "/lib";
    public static final String SAMPLES_LIB_LOCATION = CARBON_HOME + "/samples/sample-clients/lib";

}
