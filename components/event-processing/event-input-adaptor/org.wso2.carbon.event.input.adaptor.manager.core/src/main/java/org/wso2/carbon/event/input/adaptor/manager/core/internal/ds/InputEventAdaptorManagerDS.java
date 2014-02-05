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

package org.wso2.carbon.event.input.adaptor.manager.core.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.event.input.adaptor.core.InputEventAdaptorService;
import org.wso2.carbon.event.input.adaptor.manager.core.InputEventAdaptorManagerService;
import org.wso2.carbon.event.input.adaptor.manager.core.exception.InputEventAdaptorManagerConfigurationException;
import org.wso2.carbon.event.input.adaptor.manager.core.internal.CarbonInputEventAdaptorManagerService;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;

/**
 * @scr.component name="input.event.adaptor.manager.component" immediate="true"
 * @scr.reference name="input.event.adaptor.service"
 * interface="org.wso2.carbon.event.input.adaptor.core.InputEventAdaptorService" cardinality="1..1"
 * policy="dynamic" bind="setEventAdaptorService" unbind="unSetEventAdaptorService"
 */
public class InputEventAdaptorManagerDS {

    private static final Log log = LogFactory.getLog(InputEventAdaptorManagerDS.class);

    /**
     * initialize the event Adaptor Manager core service here.
     *
     * @param context
     */
    protected void activate(ComponentContext context) {

        try {
            InputEventAdaptorManagerService inputEventAdaptorProxyService =
                    createEventAdaptorManagerService();
            context.getBundleContext().registerService(InputEventAdaptorManagerService.class.getName(),
                                                       inputEventAdaptorProxyService, null);
            log.info("Successfully deployed the input event adaptor manager service");
        } catch (RuntimeException e) {
            log.error("Can not create the input event adaptor manager service ", e);
        } catch (InputEventAdaptorManagerConfigurationException e) {
            log.error("Error occurred when creating the input adaptor manager configuration ", e);
        }
    }

    protected void setEventAdaptorService(InputEventAdaptorService inputEventAdaptorService) {
        InputEventAdaptorHolder.getInstance().setInputEventAdaptorService(inputEventAdaptorService);
    }

    protected void unSetEventAdaptorService(InputEventAdaptorService eventAdaptorService) {
        InputEventAdaptorHolder.getInstance().unSetTInputEventAdaptorService();
    }

    private InputEventAdaptorManagerService createEventAdaptorManagerService()
            throws InputEventAdaptorManagerConfigurationException {
        CarbonInputEventAdaptorManagerService carbonEventAdaptorManagerService = new CarbonInputEventAdaptorManagerService();
        InputEventAdaptorManagerValueHolder.registerCarbonEventAdaptorManagerService(carbonEventAdaptorManagerService);
        return carbonEventAdaptorManagerService;
    }

}
