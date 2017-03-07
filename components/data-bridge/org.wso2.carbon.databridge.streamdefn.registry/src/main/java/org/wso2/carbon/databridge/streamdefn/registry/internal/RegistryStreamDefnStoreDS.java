/*
 * Copyright 2012 WSO2, Inc. (http://wso2.com)
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

package org.wso2.carbon.databridge.streamdefn.registry.internal;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.databridge.core.definitionstore.AbstractStreamDefinitionStore;
import org.wso2.carbon.databridge.streamdefn.registry.datastore.RegistryStreamDefinitionStore;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;

/**
 * @scr.component name="registry.streamdefn.comp" immediate="true"
 * @scr.reference name="registry.service" interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 */
public class RegistryStreamDefnStoreDS {
    private static Log log = LogFactory.getLog(RegistryStreamDefnStoreDS.class);

    protected void activate(ComponentContext componentContext) {
        AbstractStreamDefinitionStore abstractStreamDefinitionStore = new RegistryStreamDefinitionStore();
        componentContext.getBundleContext().registerService(AbstractStreamDefinitionStore.class.getName(), abstractStreamDefinitionStore, null);
        if (log.isDebugEnabled()) {
            log.debug("Started the Data bridge Registry stream definition store component");
        }
    }

    protected void deactivate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Stopped the Data bridge Registry stream definition store component");
        }
    }

    protected void setRegistryService(RegistryService registryService) throws
                                                                       RegistryException {
        ServiceHolder.setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        ServiceHolder.setRegistryService(null);
    }


}
