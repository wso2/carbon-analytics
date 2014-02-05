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

package org.wso2.carbon.event.input.adaptor.wsevent.local.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.event.core.EventBroker;
import org.wso2.carbon.event.input.adaptor.core.InputEventAdaptorFactory;
import org.wso2.carbon.event.input.adaptor.wsevent.local.WSEventLocalEventAdaptorFactory;


/**
 * @scr.component name="input.WSEventLocalEventAdaptorService.component" immediate="true"
 * @scr.reference name="eventbroker.service"
 * interface="org.wso2.carbon.event.core.EventBroker" cardinality="1..1"
 * policy="dynamic" bind="setWSEventAdaptorService" unbind="unSetWSEventAdaptorService"
 */


public class WSEventLocalEventAdaptorServiceDS {

    private static final Log log = LogFactory.getLog(WSEventLocalEventAdaptorServiceDS.class);

    /**
     * initialize the agent service here service here.
     *
     * @param context
     */
    protected void activate(ComponentContext context) {

        try {
            InputEventAdaptorFactory wsEventLocalEventAdaptorFactory = new WSEventLocalEventAdaptorFactory();
            context.getBundleContext().registerService(InputEventAdaptorFactory.class.getName(), wsEventLocalEventAdaptorFactory, null);
            log.info("Successfully deployed the input WSEventLocal adaptor service");
        } catch (RuntimeException e) {
            log.error("Can not create the input WSEventLocal adaptor service ", e);
        }
    }

    protected void setWSEventAdaptorService(EventBroker eventBroker) {
        WSEventLocalAdaptorServiceValueHolder.registerEventBroker(eventBroker);
    }

    protected void unSetWSEventAdaptorService(EventBroker eventBroker) {

    }

}
