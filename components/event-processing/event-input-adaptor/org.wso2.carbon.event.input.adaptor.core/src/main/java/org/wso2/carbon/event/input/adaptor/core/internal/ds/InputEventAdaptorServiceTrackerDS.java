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

package org.wso2.carbon.event.input.adaptor.core.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.event.input.adaptor.core.InputEventAdaptorFactory;
import org.wso2.carbon.event.input.adaptor.core.internal.CarbonInputEventAdaptorService;

import java.util.ArrayList;
import java.util.List;

/**
 * @scr.component name="input.event.adaptor.service.tracker.component" immediate="true"
 * @scr.reference name="input.event.adaptor.tracker.service"
 * interface="org.wso2.carbon.event.input.adaptor.core.InputEventAdaptorFactory" cardinality="0..n"
 * policy="dynamic" bind="setEventAdaptorType" unbind="unSetEventAdaptorType"
 */

public class InputEventAdaptorServiceTrackerDS {

    private static final Log log = LogFactory.getLog(InputEventAdaptorServiceTrackerDS.class);
    private boolean adaptorServiceInitialised = false;
    public static List<InputEventAdaptorFactory> inputEventAdaptorFactories = new ArrayList<InputEventAdaptorFactory>();

    /**
     * initialize the Event Adaptor Manager core service here.
     *
     * @param context
     */
    protected void activate(ComponentContext context) {
        try {
            log.info("Successfully deployed the input event adaptor tracker service");
        } catch (RuntimeException e) {
            log.error("Can not create the input event adaptor tracker service ", e);
        }
    }

    protected void setEventAdaptorType(
            InputEventAdaptorFactory inputEventAdaptorFactory) {
        try {
            if (InputEventAdaptorServiceValueHolder.getCarbonInputEventAdaptorService() != null) {
                ((CarbonInputEventAdaptorService) InputEventAdaptorServiceValueHolder.getCarbonInputEventAdaptorService()).registerEventAdaptor(inputEventAdaptorFactory.getEventAdaptor());
                adaptorServiceInitialised = true;
            }
            else{
                inputEventAdaptorFactories.add(inputEventAdaptorFactory);
            }
        } catch (Throwable t) {
            log.error(t);
        }
    }

    protected void unSetEventAdaptorType(
            InputEventAdaptorFactory inputEventAdaptorFactory) {
        ((CarbonInputEventAdaptorService) InputEventAdaptorServiceValueHolder.getCarbonInputEventAdaptorService()).unRegisterEventAdaptor(inputEventAdaptorFactory.getEventAdaptor());
    }
}

