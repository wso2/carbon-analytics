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

package org.wso2.carbon.siddhi.extensions.installer.core.execution;

import org.wso2.carbon.siddhi.extensions.installer.core.exceptions.ExtensionsInstallerException;

import java.util.Map;

/**
 * Interface which describes methods that are used to install or un-install extension dependencies.
 */
public interface DependencyInstaller {

    /**
     * Installs dependencies of the extension, that is denoted by the given id.
     *
     * @param extensionId Id of the extension.
     * @return Information about artifacts related to the installation's completion.
     * @throws ExtensionsInstallerException Failure occurred when installing dependencies for the extension.
     */
    Map<String, Object> installDependenciesFor(String extensionId) throws ExtensionsInstallerException;

    /**
     * Un-installs dependencies of the extension, that is denoted by the given id.
     *
     * @param extensionId Id of the extension.
     * @return Information about artifacts related to the un-installation's completion.
     * @throws ExtensionsInstallerException Failure occurred when un-installing dependencies of the extension.
     */
    Map<String, Object> unInstallDependenciesFor(String extensionId) throws ExtensionsInstallerException;

}
