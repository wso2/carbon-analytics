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
import org.wso2.carbon.siddhi.extensions.installer.core.models.enums.ExtensionInstallationStatus;

import java.util.List;
import java.util.Map;

/**
 * Interface which describes methods, that are used to read information related to extension dependencies.
 */
public interface DependencyRetriever {

    /**
     * Retrieves installation statuses of all the extensions that have been configured in the configuration file.
     * Possible installation statuses are
     * {@link ExtensionInstallationStatus}
     *
     * @return Map containing extension information and their installation statuses.
     * @throws ExtensionsInstallerException Failed to retrieve installation statuses.
     */
    Map<String, Map<String, Object>> getAllExtensionStatuses() throws ExtensionsInstallerException;

    /**
     * Retrieves installation status of the extension which has the given id.
     * Possible installation statuses are
     * {@link ExtensionInstallationStatus}
     *
     * @param extensionId Id of the extension.
     * @return Map containing extension information and its installation status.
     * @throws ExtensionsInstallerException Failed to retrieve installation status.
     */
    Map<String, Object> getExtensionStatusFor(String extensionId) throws ExtensionsInstallerException;

    /**
     * Retrieves statuses of all the dependencies, of the extension that has the given id.
     *
     * @param extensionId Id of the extension.
     * @return Statuses of dependencies.
     * @throws ExtensionsInstallerException Failed to retrieve dependency statuses.
     */
    List<Map<String, Object>> getDependencyStatusesFor(String extensionId) throws ExtensionsInstallerException;

}
