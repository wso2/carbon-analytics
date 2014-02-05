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

package org.wso2.carbon.event.output.adaptor.manager.core.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.event.output.adaptor.core.OutputEventAdaptorService;
import org.wso2.carbon.event.output.adaptor.manager.core.OutputEventAdaptorManagerService;
import org.wso2.carbon.event.output.adaptor.manager.core.exception.OutputEventAdaptorManagerConfigurationException;
import org.wso2.carbon.event.output.adaptor.manager.core.internal.CarbonOutputEventAdaptorManagerService;
import org.wso2.carbon.event.statistics.EventStatisticsService;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;

/**
 * @scr.component name="output.event.adaptor.manager.component" immediate="true"
 * @scr.reference name="output.event.adaptor.service"
 * interface="org.wso2.carbon.event.output.adaptor.core.OutputEventAdaptorService" cardinality="1..1"
 * policy="dynamic" bind="setEventAdaptorService" unbind="unSetEventAdaptorService"
 * @scr.reference name="eventStatistics.service"
 * interface="org.wso2.carbon.event.statistics.EventStatisticsService" cardinality="1..1"
 * policy="dynamic" bind="setEventStatisticsService" unbind="unsetEventStatisticsService"
 */
public class OutputEventAdaptorManagerDS {

    private static final Log log = LogFactory.getLog(OutputEventAdaptorManagerDS.class);

    /**
     * initialize the Event Adaptor Manager core service here.
     *
     * @param context
     */
    protected void activate(ComponentContext context) {

        try {
            OutputEventAdaptorManagerService outputEventAdaptorProxyService =
                    createEventAdaptorManagerService();
            context.getBundleContext().registerService(OutputEventAdaptorManagerService.class.getName(),
                                                       outputEventAdaptorProxyService, null);
            log.info("Successfully deployed the output event adaptor manager service");
        } catch (RuntimeException e) {
            log.error("Can not create the output event adaptor manager service ", e);
        } catch (OutputEventAdaptorManagerConfigurationException e) {
            log.error("Error occurred when creating the output event adaptor manager configuration ", e);
        }
    }

    protected void setEventAdaptorService(
            OutputEventAdaptorService outputEventAdaptorService) {
        OutputEventAdaptorHolder.getInstance().setOutputEventAdaptorService(outputEventAdaptorService);
    }

    protected void unSetEventAdaptorService(
            OutputEventAdaptorService eventAdaptorService) {
        OutputEventAdaptorHolder.getInstance().unSetTOutputEventAdaptorService();
    }


    public void setEventStatisticsService(EventStatisticsService eventStatisticsService) {
        OutputEventAdaptorManagerValueHolder.registerEventStatisticsService(eventStatisticsService);
    }

    public void unsetEventStatisticsService(EventStatisticsService eventStatisticsService) {
        OutputEventAdaptorManagerValueHolder.registerEventStatisticsService(null);
    }

    private OutputEventAdaptorManagerService createEventAdaptorManagerService()
            throws OutputEventAdaptorManagerConfigurationException {
        CarbonOutputEventAdaptorManagerService carbonEventAdaptorManagerService = new CarbonOutputEventAdaptorManagerService();
        OutputEventAdaptorManagerValueHolder.registerCarbonEventAdaptorManagerService(carbonEventAdaptorManagerService);
        return carbonEventAdaptorManagerService;
    }

}
