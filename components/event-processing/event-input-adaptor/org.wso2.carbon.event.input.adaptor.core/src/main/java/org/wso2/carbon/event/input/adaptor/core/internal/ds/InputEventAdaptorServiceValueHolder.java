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

import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.event.input.adaptor.core.InputEventAdaptorService;
import org.wso2.carbon.event.statistics.EventStatisticsService;

/**
 * common place to hold some OSGI bundle references.
 */
public final class InputEventAdaptorServiceValueHolder {

    private static InputEventAdaptorService carbonInputEventAdaptorService;
    private static ComponentContext  componentContext;
    private static EventStatisticsService eventStatisticsService;

    private InputEventAdaptorServiceValueHolder() {
    }

    public static void registerCarbonEventService(
            InputEventAdaptorService carbonInputEventAdaptorService) {

        InputEventAdaptorServiceValueHolder.carbonInputEventAdaptorService = carbonInputEventAdaptorService;
    }

    public static InputEventAdaptorService getCarbonInputEventAdaptorService() {
        return InputEventAdaptorServiceValueHolder.carbonInputEventAdaptorService;
    }

    public static void registerComponentContext(
            ComponentContext componentContext) {

        InputEventAdaptorServiceValueHolder.componentContext = componentContext;
    }

    public static ComponentContext getComponentContext() {
        return InputEventAdaptorServiceValueHolder.componentContext;
    }


    public static void registerEventStatisticsService(EventStatisticsService eventStatisticsService) {
        InputEventAdaptorServiceValueHolder.eventStatisticsService = eventStatisticsService;
    }

    public static EventStatisticsService getEventStatisticsService() {
        return eventStatisticsService;
    }
}
