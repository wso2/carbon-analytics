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

package org.wso2.carbon.event.input.adaptor.local.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.event.input.adaptor.core.InputEventAdaptorFactory;
import org.wso2.carbon.event.input.adaptor.local.LocalEventAdaptorFactory;
import org.wso2.carbon.event.input.adaptor.local.LocalEventAdaptorType;


/**
 * @scr.component name="input.LocalEventAdaptorService.component" immediate="true"
 */


public class LocalEventAdaptorServiceDS {

    private static final Log log = LogFactory.getLog(LocalEventAdaptorServiceDS.class);

    /**
     * initialize the agent service here service here.
     *
     * @param context
     */
    protected void activate(ComponentContext context) {

        try {
            InputEventAdaptorFactory localEventAdaptorFactory = new LocalEventAdaptorFactory();
            context.getBundleContext().registerService(InputEventAdaptorFactory.class.getName(), localEventAdaptorFactory, null);

            LocalEventAdaptorType localEventAdaptorType = LocalEventAdaptorType.getInstance();
            context.getBundleContext().registerService(LocalEventAdaptorType.class.getName(), localEventAdaptorType, null);

            log.info("Successfully deployed the input Local event service");
        } catch (RuntimeException e) {
            log.error("Can not create the input Local event service ", e);
        }
    }


}
