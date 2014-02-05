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
import org.wso2.carbon.event.input.adaptor.core.AbstractInputEventAdaptor;
import org.wso2.carbon.event.input.adaptor.manager.core.exception.InputEventAdaptorManagerConfigurationException;


/**
 * @scr.component name="input.event.adaptor.type.service.tracker.component" immediate="true"
 * @scr.reference name="abstract.input.event.adaptor.type.service"
 * interface="org.wso2.carbon.event.input.adaptor.core.AbstractInputEventAdaptor" cardinality="0..n"
 * policy="dynamic" bind="notifyNewEventAdaptorType" unbind="notifyRemovalEventAdaptorType"
 */
public class InputEventAdaptorTypeServiceTrackerDS {

    private static final Log log = LogFactory.getLog(InputEventAdaptorTypeServiceTrackerDS.class);

    protected void activate(ComponentContext context) {

        try {
            log.info("Successfully deployed the input event adaptor type tracker service");
        } catch (RuntimeException e) {
            log.error("Can not create the input event adaptor type tracker service ", e);
        }
    }

    protected void notifyNewEventAdaptorType(
            AbstractInputEventAdaptor abstractInputEventAdaptor)
            throws InputEventAdaptorManagerConfigurationException {

        InputEventAdaptorManagerValueHolder.getCarbonEventAdaptorManagerService().activateInactiveInputEventAdaptorConfiguration();

    }

    protected void notifyRemovalEventAdaptorType(
            AbstractInputEventAdaptor abstractInputEventAdaptor) {

    }
}