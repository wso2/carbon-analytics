/*
 * Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.event.output.adapter.websocket.local.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterFactory;
import org.wso2.carbon.event.output.adapter.websocket.local.WebsocketLocalEventAdapterFactory;
import org.wso2.carbon.event.output.adapter.websocket.local.WebsocketLocalOutputCallbackRegisterService;
import org.wso2.carbon.event.output.adapter.websocket.local.internal.WebsocketLocalOutputCallbackRegisterServiceImpl;

/**
 * @scr.component component.name="output.Websocket.AdapterService.component" immediate="true"
 */
public class WebsocketLocalEventAdapterServiceDS {

    private static final Log log = LogFactory.getLog(WebsocketLocalEventAdapterServiceDS.class);

    /**
     * initialize the websocket adapter service here service here.
     *
     * @param context
     */
    protected void activate(ComponentContext context) {

        try {
            OutputEventAdapterFactory websocketLocalEventAdapterFactory = new WebsocketLocalEventAdapterFactory();
            context.getBundleContext().registerService(OutputEventAdapterFactory.class.getName(), websocketLocalEventAdapterFactory, null);

            WebsocketLocalOutputCallbackRegisterServiceImpl websocketLocalOutputCallbackRegisterServiceImpl = new WebsocketLocalOutputCallbackRegisterServiceImpl();
            context.getBundleContext().registerService(WebsocketLocalOutputCallbackRegisterService.class.getName(), websocketLocalOutputCallbackRegisterServiceImpl, null);

            WebsocketLocalEventAdaptorServiceInternalValueHolder.registerWebsocketOutputCallbackRegisterServiceInternal(websocketLocalOutputCallbackRegisterServiceImpl);

            if (log.isDebugEnabled()) {
                log.debug("Successfully deployed the output websocket-local adapter service");
            }
        } catch (RuntimeException e) {
            log.error("Can not create the output websocket-local adapter service ", e);
        }
    }

}
