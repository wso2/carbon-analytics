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
import org.wso2.carbon.event.statistics.EventStatisticsService;
import org.wso2.carbon.event.input.adaptor.core.InputEventAdaptorService;
import org.wso2.carbon.event.input.adaptor.core.internal.CarbonInputEventAdaptorService;

import java.util.List;


/**
 * @scr.component name="input.event.adaptor.service.component" immediate="true"
 * @scr.reference name="eventStatistics.service"
 * interface="org.wso2.carbon.event.statistics.EventStatisticsService" cardinality="1..1"
 * policy="dynamic" bind="setEventStatisticsService" unbind="unsetEventStatisticsService"
 */


public class InputEventAdaptorServiceDS {

    private static final Log log = LogFactory.getLog(InputEventAdaptorServiceDS.class);

    /**
     * initialize the cep service here.
     *
     * @param context
     */
    protected void activate(ComponentContext context) {

        try {
            InputEventAdaptorService inputEventAdaptorService = new CarbonInputEventAdaptorService();
            InputEventAdaptorServiceValueHolder.registerCarbonEventService(inputEventAdaptorService);
            context.getBundleContext().registerService(InputEventAdaptorService.class.getName(), inputEventAdaptorService, null);
            InputEventAdaptorServiceValueHolder.registerComponentContext(context);
            log.info("Successfully deployed the input event adaptor service");
            registerEventAdaptorTypes();
        } catch (RuntimeException e) {
            log.error("Can not create the input event adaptor service ", e);
        }
    }

    public void setEventStatisticsService(EventStatisticsService eventStatisticsService) {
        InputEventAdaptorServiceValueHolder.registerEventStatisticsService(eventStatisticsService);
    }

    public void unsetEventStatisticsService(EventStatisticsService eventStatisticsService) {
        InputEventAdaptorServiceValueHolder.registerEventStatisticsService(null);
    }

    private void registerEventAdaptorTypes(){

        List<InputEventAdaptorFactory> inputEventAdaptorFactories = InputEventAdaptorServiceTrackerDS.inputEventAdaptorFactories;

        for(InputEventAdaptorFactory inputEventAdaptorFactory : inputEventAdaptorFactories){
                ((CarbonInputEventAdaptorService) InputEventAdaptorServiceValueHolder.getCarbonInputEventAdaptorService()).registerEventAdaptor(inputEventAdaptorFactory.getEventAdaptor());
        }
    }


}
