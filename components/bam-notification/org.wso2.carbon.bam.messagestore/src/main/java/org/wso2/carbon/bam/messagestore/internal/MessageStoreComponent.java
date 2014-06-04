/*
 *  Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.bam.messagestore.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * @scr.component name="bam.message.store.component" immediate="true"
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService" cardinality="1..1"
 * policy="dynamic" bind="setRealmService" unbind="unsetRealmService"
 */
public class MessageStoreComponent {

    private static RealmService realmService;
    
    private static Log log = LogFactory.getLog(MessageStoreComponent.class);
    
    protected void activate(ComponentContext ctxt) {
        try {
            log.debug("BAM Message Store bundle is activated ");
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
        }
    }

    protected void deactivate(ComponentContext ctxt) {
        log.debug("BAM Message Store bundle is deactivated ");
    }

    protected void setRealmService(RealmService realmService) {
        MessageStoreComponent.realmService = realmService;
    }

    protected void unsetRealmService(RealmService realmService) {
        MessageStoreComponent.realmService = null;
    }
    
    public static RealmService getRealmService() {
        return realmService;
    }
    
}
