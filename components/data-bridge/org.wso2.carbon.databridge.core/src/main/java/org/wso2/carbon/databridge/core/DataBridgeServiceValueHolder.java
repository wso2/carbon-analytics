package org.wso2.carbon.databridge.core;

/*
* Copyright 2004,2005 The Apache Software Foundation.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import org.wso2.carbon.databridge.core.definitionstore.AbstractStreamDefinitionStore;
import org.wso2.carbon.databridge.core.definitionstore.InMemoryStreamDefinitionStore;

public class DataBridgeServiceValueHolder {

    // TODO: 1/24/17 no realmservice, ConfigurationContextService in c5. get methods not used either
    //private static RealmService realmService;
//    private static AbstractStreamDefinitionStore streamDefinitionStore;
    // TODO: 1/31/17 temporarily converted above to InMemoryStreamDefinitionStore
    private static InMemoryStreamDefinitionStore streamDefinitionStore;
//    private static ConfigurationContextService configurationContextService;

    /*public static void setRealmService(RealmService realmService) {
        DataBridgeServiceValueHolder.realmService = realmService;
    }*/

    /*public static RealmService getRealmService() {
        return realmService;
    }*/

    public static void setStreamDefinitionStore(InMemoryStreamDefinitionStore streamDefinitionStore) {
        DataBridgeServiceValueHolder.streamDefinitionStore = streamDefinitionStore;
    }

    public static InMemoryStreamDefinitionStore getStreamDefinitionStore() {
        return streamDefinitionStore;
    }

    /*public static void setConfigurationContextService(ConfigurationContextService configurationContextService) {
        DataBridgeServiceValueHolder.configurationContextService = configurationContextService;
    }

    public static ConfigurationContextService getConfigurationContextService() {
        return configurationContextService;
    }*/
}
