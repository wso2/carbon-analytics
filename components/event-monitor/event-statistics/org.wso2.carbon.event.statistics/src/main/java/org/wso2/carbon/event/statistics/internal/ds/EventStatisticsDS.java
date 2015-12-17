/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.event.statistics.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.event.statistics.EventStatisticsObserver;
import org.wso2.carbon.event.statistics.EventStatisticsService;
import org.wso2.carbon.event.statistics.internal.Constants;
import org.wso2.carbon.event.statistics.internal.EventStatisticsManager;
import org.wso2.carbon.event.statistics.internal.StatisticsReporterThread;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;


/**
 * @scr.component name="mediation.statistics" immediate="true"
 */
public class EventStatisticsDS {

    private static final Log log = LogFactory.getLog(EventStatisticsDS.class);
    private StatisticsReporterThread reporterThread;

    protected void activate(ComponentContext context) throws Exception {
        try {


            createStatisticsManager();
            EventStatisticsServiceHolder.getInstance().setEventStatisticsService(new EventStatisticsService());
            context.getBundleContext().registerService(EventStatisticsService.class.getName(),
                                                       EventStatisticsServiceHolder.getInstance().getEventStatisticsService(), null);
            if (log.isDebugEnabled()) {
                log.debug("Successfully deployed the event statistics monitoring service");
            }
        } catch (Throwable e) {
            log.error("Can not create the event statistics monitoring service ", e);
        }
    }

    /**
     * Create the statistics store using the synapse environment and configuration context.
     */
    private void createStatisticsManager() {

//        EventStatisticsManager eventStatisticsManager;
        //Ignoring statistics report observers registration if {StatisticsReporterDisabled =true}
        ServerConfiguration config = ServerConfiguration.getInstance();
        String confStatisticsReporterDisabled = config.getFirstProperty("StatisticsReporterDisabled");
        if (!"".equals(confStatisticsReporterDisabled)) {
            boolean disabled = Boolean.valueOf(confStatisticsReporterDisabled);
            if (disabled) {
                return;
            }

        }

        EventStatisticsServiceHolder.getInstance().setEventStatisticsManager(new EventStatisticsManager());

        //JMX-based event-stat monitoring is supported only for super tenant because other tenants cannot use it to monitor event-stats.
        reporterThread = new StatisticsReporterThread(MultitenantConstants.SUPER_TENANT_ID);

        // Set a custom interval value if required
        ServerConfiguration serverConf = ServerConfiguration.getInstance();
        String interval = serverConf.getFirstProperty(Constants.STAT_REPORTING_INTERVAL);
        if (interval != null) {
            reporterThread.setDelay(Long.parseLong(interval));
        }

        // Engage custom observer implementations (user written extensions)
        String observers = serverConf.getFirstProperty(Constants.STAT_OBSERVERS);
        if (observers != null && !"".equals(observers)) {
            String[] classNames = observers.split(",");
            for (String className : classNames) {
                try {
                    Class clazz = this.getClass().getClassLoader().loadClass(className.trim());
                    EventStatisticsObserver o = (EventStatisticsObserver)
                            clazz.newInstance();
                    EventStatisticsServiceHolder.getInstance().getEventStatisticsManager().registerObserver(o);
                } catch (Exception e) {
                    log.error("Error while initializing the event statistics " + "observer : " + className, e);
                }
            }
        }

        reporterThread.start();

        if (log.isDebugEnabled()) {
            log.debug("Registering the mediation statistics service");
        }

    }


    protected void deactivate(ComponentContext compCtx) throws Exception {

        reporterThread.shutdown();
        reporterThread.interrupt(); // This should wake up the thread if it is asleep

        // Wait for the reporting thread to gracefully terminate
        // Observers should not be disengaged before this thread halts
        // Otherwise some of the collected data may not be sent to the observers
        while (reporterThread.isAlive()) {
            if (log.isDebugEnabled()) {
                log.debug("Waiting for the event statistics reporter thread to terminate");
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException ignore) {

            }
        }

        EventStatisticsServiceHolder.getInstance().getEventStatisticsManager().unregisterObservers();
    }


}
