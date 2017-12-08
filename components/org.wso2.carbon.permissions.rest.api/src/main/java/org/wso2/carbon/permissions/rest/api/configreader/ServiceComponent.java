/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.permissions.rest.api.configreader;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.analytics.permissions.PermissionProvider;

/**
 * This is OSGi-components to register config provider class.
 */
@Component(
        name = "org.wso2.carbon.permissions.rest.api",
        service = ServiceComponent.class,
        immediate = true
)
public class ServiceComponent {
    @Reference(
            name = "carbon.permission.provider",
            service = PermissionProvider.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterPermissionProvider"
    )
    protected void registerPermissionProvider(PermissionProvider permissionProvider) {
        DataHolder.getInstance().setPermissionProvider(permissionProvider);
    }

    protected void unregisterPermissionProvider(PermissionProvider permissionProvider) {
        DataHolder.getInstance().setPermissionProvider(null);
    }
}
