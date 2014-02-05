/*
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.dashboard.mgt.theme.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.dashboard.mgt.theme.Utils.ThemeMgtContext;
import org.wso2.carbon.dashboard.mgt.theme.handlers.ThemeUploadHandler;
import org.wso2.carbon.registry.core.jdbc.handlers.filters.MediaTypeMatcher;
import org.wso2.carbon.registry.core.service.RegistryService;

/**
 * @scr.component name="org.wso2.carbon.dashboard.mgt.theme" immediate="true"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService" cardinality="1..1"
 * policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 */
public class GSThemeMgtServiceComponent {
    private static final Log log = LogFactory.getLog(GSThemeMgtServiceComponent.class);

    protected void unsetRegistryService(RegistryService registryService) {
        ThemeMgtContext.setRegistryService(null);
    }

    protected void activate(ComponentContext context) {
        try {
            ThemeMgtContext.getRegistryService().getConfigSystemRegistry().getRegistryContext()
                    .getHandlerManager().addHandler(null,
                    new MediaTypeMatcher("application/vnd.wso2.gs.theme"), new ThemeUploadHandler());
        } catch (Exception e) {
            log.error("GS Theme upload Handler registration failed.", e);
        }
        log.info("Successfully started GS Theme upload module.");
    }

    protected void setRegistryService(RegistryService registryService) {
        ThemeMgtContext.setRegistryService(registryService);

    }
}
