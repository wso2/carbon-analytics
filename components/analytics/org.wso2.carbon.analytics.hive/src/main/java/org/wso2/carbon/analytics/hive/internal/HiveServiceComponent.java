/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.analytics.hive.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hive.common.ServerUtils;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.service.HiveServer;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.analytics.hive.Utils;
import org.wso2.carbon.analytics.hive.HiveConstants;
import org.wso2.carbon.analytics.hive.ServiceHolder;
import org.wso2.carbon.analytics.hive.exception.AnalyzerConfigException;
import org.wso2.carbon.analytics.hive.extension.util.AnalyzerFactory;
import org.wso2.carbon.analytics.hive.impl.HiveExecutorServiceImpl;
import org.wso2.carbon.analytics.hive.service.HiveExecutorService;
import org.wso2.carbon.analytics.hive.web.HiveScriptStoreService;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.cassandra.dataaccess.DataAccessService;
import org.wso2.carbon.ndatasource.core.DataSourceService;
import org.wso2.carbon.ntask.core.service.TaskService;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.ServerConstants;

import java.io.File;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @scr.component name="bam.hive.component" immediate="true"
 * @scr.reference name="registry.service" interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 * @scr.reference name="config.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="0..1" policy="dynamic"
 * bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 * @scr.reference name="ntask.component" interface="org.wso2.carbon.ntask.core.service.TaskService"
 * cardinality="1..1" policy="dynamic" bind="setTaskService" unbind="unsetTaskService"
 * @scr.reference name="user.realmservice.default" interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic" bind="setRealmService" unbind="unsetRealmService"
 * @scr.reference name="dataaccess.service" interface="org.wso2.carbon.cassandra.dataaccess.DataAccessService"
 * cardinality="1..1" policy="dynamic" bind="setDataAccessService" unbind="unsetDataAccessService"
 * @scr.reference name="datasources.service" interface="org.wso2.carbon.ndatasource.core.DataSourceService"
 * cardinality="1..1" policy="dynamic" bind="setDataSourceService" unbind="unsetDataSourceService"
 */

public class HiveServiceComponent {

    private static final String ENABLE_HIVE_SERVER_SYS_PROP = "enable.hive.thrift.server";

	private static final Log log = LogFactory.getLog(HiveServiceComponent.class);

    private static final String CARBON_HOME_ENV = "CARBON_HOME";

    private static final String LOG4J_LOCATION = "repository" + File.separator + "conf" +
                                                 File.separator + "log4j.properties";

    private static final String LOG4J_PROPERTY = "log4j.properties";

    private ServiceRegistration<HiveExecutorService> hiveExecutorServiceRegistration, hiveScriptStoreServiceRegistration;
        
    private TServer server;

    @SuppressWarnings("unchecked")
	protected void activate(ComponentContext ctx) {
		try {
			if (log.isDebugEnabled()) {
				log.debug("Starting 'HiveServiceComponent'");
			}

			// Set CARBON_HOME if not already set for the use of Hive in order
			// to load hive configurations.
			String carbonHome = System.getProperty(CARBON_HOME_ENV);
			if (carbonHome == null) {
				carbonHome = CarbonUtils.getCarbonHome();
				System.setProperty(CARBON_HOME_ENV, carbonHome);
			}

			// Setting up log4j system property so that forked VM during local
			// mode execution can obtain
			// carbon log4j configurations

			String log4jFile = carbonHome + File.separator + LOG4J_LOCATION;
			System.setProperty(LOG4J_PROPERTY, log4jFile);

			String carbonConf = System.getProperty(ServerConstants.CARBON_CONFIG_DIR_PATH);
			try {
				String path = carbonConf + File.separator + HiveConstants.ANALYZER_CONFIG_XML;
				AnalyzerFactory.loadAnalyzers(path);
			} catch (AnalyzerConfigException e) {
				String errorMessage = "Error loading analyzer-config.xml";
				log.error(errorMessage, e);
			}

			// Set and register HiveExecutorService
			ServiceHolder.setHiveExecutorService(new HiveExecutorServiceImpl());

			ServiceHolder
					.setHiveScriptStoreService(new HiveScriptStoreService());

			hiveExecutorServiceRegistration = (ServiceRegistration<HiveExecutorService>) ctx.getBundleContext().
					registerService(HiveExecutorService.class.getName(),
					ServiceHolder.getHiveExecutorService(), null);

			hiveScriptStoreServiceRegistration = (ServiceRegistration<HiveExecutorService>) ctx.getBundleContext().
					registerService(HiveScriptStoreService.class.getName(),
					ServiceHolder.getHiveScriptStoreService(), null);
			
			String enableHiveServerProp = System.getProperty(ENABLE_HIVE_SERVER_SYS_PROP);
			if (enableHiveServerProp != null && Boolean.parseBoolean(enableHiveServerProp)) {
				Executors.newSingleThreadExecutor().submit(new HiveRunnable());
			}
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
		}
    }

    protected void deactivate(ComponentContext ctxt) {
        if (log.isDebugEnabled()) {
            log.debug("Stopping 'HiveServiceComponent'");
        }
        ctxt.getBundleContext().ungetService(hiveExecutorServiceRegistration.getReference());
        ctxt.getBundleContext().ungetService(hiveScriptStoreServiceRegistration.getReference());
        if (this.server != null) {
        	this.server.stop();
        }
    }

    protected void setRegistryService(RegistryService registryService) throws RegistryException {
        ServiceHolder.setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        ServiceHolder.setRegistryService(null);
    }

    protected void setTaskService(TaskService taskService) throws RegistryException {
        ServiceHolder.setTaskService(taskService);
    }

    protected void unsetTaskService(TaskService taskService) {
        ServiceHolder.setTaskService(null);
    }

    protected void setConfigurationContextService(ConfigurationContextService contextService) {
        ServiceHolder.setConfigurationContextService(contextService);
    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
        ServiceHolder.setConfigurationContextService(null);
    }

    protected void setServerConfiguration(ServerConfiguration serverConfiguration) {
        ServiceHolder.setCarbonConfiguration(serverConfiguration);
    }

    protected void unsetServerConfiguration(ServerConfiguration serverConfiguration) {
        ServiceHolder.setCarbonConfiguration(null);
    }

    protected void setRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting the Realm Service");
        }

        ServiceHolder.setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting the Realm Service");
        }

        ServiceHolder.setRealmService(null);
    }

    protected void setDataAccessService(DataAccessService dataAccessService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting the Cassandra Data Access Service");
        }
       ServiceHolder.setCassandraDataAccessService(dataAccessService);
    }

    protected void unsetDataAccessService(DataAccessService dataAccessService) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting Cassandra Data Access Service");
        }
      ServiceHolder.setCassandraDataAccessService(null);
    }

    protected void setDataSourceService(DataSourceService dataSourceService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting the Carbon Data Sources Service");
        }
        ServiceHolder.setDataSourceService(dataSourceService);
    }

    protected void unsetDataSourceService(
            DataSourceService dataSourceService) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting the Carbon Data Sources Service");
        }
        ServiceHolder.setDataSourceService(null);
    }
    
    /* Hive as a server */
	public class HiveRunnable implements Runnable {

		public void run() {
			initialize();
		}

		public void initialize() {
			try {
				HiveServer.HiveServerCli cli = new HiveServer.HiveServerCli();

				cli.parse(null);

				// NOTE: It is critical to do this prior to initializing log4j,
				// otherwise
				// any log specific settings via hiveconf will be ignored
				Properties hiveconf = cli.addHiveconfToSystemProperties();

				// NOTE: It is critical to do this here so that log4j is
				// reinitialized
				// before any of the other core hive classes are loaded
				/*
				 * try { LogUtils.initHiveLog4j(); } catch
				 * (LogUtils.LogInitializationException e) {
				 * HiveServer.HiveServerHandler.LOG.warn(e.getMessage()); }
				 */
				HiveConf conf = new HiveConf(HiveServer.HiveServerHandler.class);
				ServerUtils.cleanUpScratchDir(conf);

				int carbonPortOffset = Utils.getPortOffset();

				TServerTransport serverTransport = new TServerSocket(
						Utils.HIVE_SERVER_DEFAULT_PORT + carbonPortOffset);

				// set all properties specified on the command line
				for (Map.Entry<Object, Object> item : hiveconf.entrySet()) {
					conf.set((String) item.getKey(), (String) item.getValue());
				}

				HiveServer.ThriftHiveProcessorFactory hfactory = new HiveServer.ThriftHiveProcessorFactory(
						null, conf);
				TThreadPoolServer.Args sargs = new TThreadPoolServer.Args(
						serverTransport).processorFactory(hfactory)
						.transportFactory(new TTransportFactory())
						.protocolFactory(new TBinaryProtocol.Factory())
						.minWorkerThreads(cli.minWorkerThreads)
						.maxWorkerThreads(cli.maxWorkerThreads);

				TServer server = new TThreadPoolServer(sargs);

				String msg = "Started Hive Thrift server on port "
						+ (Utils.HIVE_SERVER_DEFAULT_PORT + carbonPortOffset)
						+ "..";

				HiveServer.HiveServerHandler.LOG.info(msg);

				log.info(msg);

				// Start Hive Thrift service
				server.serve();
			} catch (Exception e) {
				log.error("Hive server initialization failed: " + e.getMessage(), e);
			}
		}

	}
    
}
