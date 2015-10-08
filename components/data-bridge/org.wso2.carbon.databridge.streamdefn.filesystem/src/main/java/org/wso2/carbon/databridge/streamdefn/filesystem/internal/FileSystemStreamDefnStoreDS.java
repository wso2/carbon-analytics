/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.databridge.streamdefn.filesystem.internal;

import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.databridge.core.definitionstore.AbstractStreamDefinitionStore;
import org.wso2.carbon.databridge.streamdefn.filesystem.FileSystemStreamDefinitionStore;
import org.wso2.carbon.event.stream.core.EventStreamListener;
import org.wso2.carbon.event.stream.core.EventStreamService;

/**
 * @scr.component name="filesystem.streamdefn.store.comp" immediate="true"
 * @scr.reference name="config.context.service"
 * interface="org.wso2.carbon.event.stream.core.EventStreamService" cardinality="1..1" policy="dynamic"
 * bind="setEventStreamService" unbind="unsetEventStreamService"
 */
public class FileSystemStreamDefnStoreDS {
    protected void activate(ComponentContext componentContext) {
        AbstractStreamDefinitionStore abstractStreamDefinitionStore = new FileSystemStreamDefinitionStore();
        componentContext.getBundleContext().registerService(AbstractStreamDefinitionStore.class.getName(),
                abstractStreamDefinitionStore, null);
        ServiceHolder.setStreamDefinitionStore(abstractStreamDefinitionStore);
        componentContext.getBundleContext().registerService(EventStreamListener.class.getName(),
                new EventStreamListenerImpl(), null);
    }

    protected void setEventStreamService(EventStreamService eventStreamService){
        ServiceHolder.setEventStreamService(eventStreamService);
    }

    protected void unsetEventStreamService(EventStreamService eventStreamService){
        ServiceHolder.setEventStreamService(null);
    }
}
