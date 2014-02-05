/**
 * Copyright (c) 2005 - 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.event.processor.storm.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.cassandra.dataaccess.DataAccessService;
import org.wso2.carbon.event.processor.storm.StormProcessorService;
import org.wso2.carbon.event.processor.storm.internal.CarbonStormProcessorService;
import org.wso2.carbon.event.statistics.EventStatisticsService;
import org.wso2.carbon.ndatasource.core.DataSourceService;
import org.wso2.carbon.user.core.UserRealm;

/**
 * @scr.component name="stormProcessorService.component" immediate="true"
 * @scr.reference name="eventStatistics.service"
 * interface="org.wso2.carbon.event.statistics.EventStatisticsService" cardinality="1..1"
 * policy="dynamic" bind="setEventStatisticsService" unbind="unsetEventStatisticsService"
 * @scr.reference name="dataaccess.service" interface="org.wso2.carbon.cassandra.dataaccess.DataAccessService"
 * cardinality="1..1" policy="dynamic" bind="setDataAccessService" unbind="unsetDataAccessService"
 * @scr.reference name="org.wso2.carbon.ndatasource" interface="org.wso2.carbon.ndatasource.core.DataSourceService"
 * cardinality="1..1" policy="dynamic" bind="setDataSourceService" unbind="unsetDataSourceService"
 */
public class StormProcessorServiceDS {
    private static final Log log = LogFactory.getLog(StormProcessorServiceDS.class);

    protected void activate(ComponentContext context) {
        try {

            StormProcessorService carbonStormProcessorService = new CarbonStormProcessorService();
            StormProcessorValueHolder.registerStormProcessorService((CarbonStormProcessorService) carbonStormProcessorService);

            log.info("Successfully deployed StormProcessorService");

        } catch (RuntimeException e) {
            log.error("Could not create StormProcessorService");
        }

    }

    public void setEventStatisticsService(EventStatisticsService eventStatisticsService) {
        StormProcessorValueHolder.registerEventStatisticsService(eventStatisticsService);
    }

    public void unsetEventStatisticsService(EventStatisticsService eventStatisticsService) {
        StormProcessorValueHolder.registerEventStatisticsService(null);
    }

    protected void setDataAccessService(DataAccessService dataAccessService) {
        StormProcessorValueHolder.setDataAccessService(dataAccessService);
    }

    protected void unsetDataAccessService(DataAccessService dataAccessService) {
        StormProcessorValueHolder.setDataAccessService(null);
    }

    protected void setUserRealm(UserRealm userRealm) {
        StormProcessorValueHolder.setUserRealm(userRealm);
    }

    protected void unsetUserRealm(UserRealm userRealm) {
        StormProcessorValueHolder.setUserRealm(null);

    }

    protected void setDataSourceService(DataSourceService dataSourceService) {
        StormProcessorValueHolder.setDataSourceService(dataSourceService);
    }

    protected void unsetDataSourceService(DataSourceService dataSourceService) {
        StormProcessorValueHolder.setDataSourceService(null);
    }
}
