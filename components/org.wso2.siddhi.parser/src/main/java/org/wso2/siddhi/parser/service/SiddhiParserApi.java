/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.wso2.siddhi.parser.service;

import io.siddhi.core.SiddhiAppRuntime;
import io.siddhi.core.SiddhiManager;
import io.siddhi.core.stream.ServiceDeploymentInfo;
import io.siddhi.core.stream.input.source.Source;
import org.wso2.carbon.streaming.integrator.common.SiddhiAppRuntimeService;
import org.wso2.carbon.streaming.integrator.common.utils.config.FileConfigManager;
import org.wso2.siddhi.parser.SiddhiParserDataHolder;
import org.wso2.siddhi.parser.core.SiddhiAppCreator;
import org.wso2.siddhi.parser.core.SiddhiTopologyCreator;
import org.wso2.siddhi.parser.core.appcreator.DeployableSiddhiQueryGroup;
import org.wso2.siddhi.parser.core.appcreator.NatsSiddhiAppCreator;
import org.wso2.siddhi.parser.core.appcreator.SiddhiQuery;
import org.wso2.siddhi.parser.core.topology.SiddhiTopology;
import org.wso2.siddhi.parser.core.topology.SiddhiTopologyCreatorImpl;
import org.wso2.siddhi.parser.service.model.ApiResponseMessage;
import org.wso2.siddhi.parser.service.model.DeployableSiddhiApp;
import org.wso2.siddhi.parser.service.model.MessagingSystem;
import org.wso2.siddhi.parser.service.model.SiddhiParserRequest;
import org.wso2.siddhi.parser.service.model.SourceDeploymentConfig;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.kernel.config.model.CarbonConfiguration;
import org.wso2.msf4j.MicroservicesRunner;
import org.wso2.msf4j.config.TransportsFileConfiguration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * Siddhi Parser Service used by the Siddhi Kubernetes Operator to parse the Siddhi Apps.
 */
@Component(
        name = "siddhi-parser-service",
        immediate = true
)
@Path("/siddhi-parser")
public class SiddhiParserApi {

    private static final Logger log = LoggerFactory.getLogger(SiddhiParserApi.class);
    private static final String TRANSPORT_ROOT_CONFIG_ELEMENT = "wso2.transport.http";
    private static final String SIDDHI_PARSER_ACTIVATION_SYS_PROPERTY = "siddhi-parser";
    private static TransportsFileConfiguration transportsFileConfiguration;
    private static MicroservicesRunner microservicesRunner;
    private static volatile boolean microserviceActive;
    private static SiddhiAppCreator appCreator = new NatsSiddhiAppCreator();
    private static SiddhiTopologyCreator siddhiTopologyCreator = new SiddhiTopologyCreatorImpl();

    public SiddhiParserApi() {
        SiddhiManager siddhiManager = new SiddhiManager();
        FileConfigManager fileConfigManager = new FileConfigManager(SiddhiParserDataHolder.getConfigProvider());
        siddhiManager.setConfigManager(fileConfigManager);
        SiddhiParserDataHolder.setSiddhiManager(siddhiManager);
    }

    @GET
    @Path("/")
    public String get() {
        return "Siddhi Parser Service is up and running.";
    }

    @POST
    @Path("/parse")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    public Response parseSiddhiApp(SiddhiParserRequest request) throws NotFoundException {
        try {
            List<DeployableSiddhiApp> deployableSiddhiApps = new ArrayList<>();
            List<String> userGivenApps = populateAppWithEnvs(request.getPropertyMap(), request.getSiddhiApps());
            for (String app : userGivenApps) {
                List<SourceDeploymentConfig> sourceDeploymentConfigs = getSourceDeploymentConfigs(app);
                SiddhiTopology topology = siddhiTopologyCreator.createTopology(app);
                boolean isAppStateful = topology.isStatefulApp();
                MessagingSystem messagingSystemConfig = request.getMessagingSystem();

                if (messagingSystemConfig != null && !messagingSystemConfig.isEmpty()) {
                    List<DeployableSiddhiQueryGroup> queryGroupList = appCreator.createApps(topology,
                            messagingSystemConfig);

                    for (DeployableSiddhiQueryGroup deployableSiddhiQueryGroup : queryGroupList) {
                        if (deployableSiddhiQueryGroup.isReceiverQueryGroup()) {
                            for (SiddhiQuery siddhiQuery : deployableSiddhiQueryGroup.getSiddhiQueries()) {
                                deployableSiddhiApps.add(new DeployableSiddhiApp(siddhiQuery.getApp(),
                                        sourceDeploymentConfigs, topology.isUserGiveSourceStateful()));
                            }
                        } else {
                            for (SiddhiQuery siddhiQuery : deployableSiddhiQueryGroup.getSiddhiQueries()) {
                                DeployableSiddhiApp deployableSiddhiApp = new DeployableSiddhiApp(siddhiQuery.getApp(),
                                        isAppStateful);
                                if (deployableSiddhiQueryGroup.isUserGivenSource()) {
                                    deployableSiddhiApp.setSourceDeploymentConfigs(sourceDeploymentConfigs);
                                }
                                deployableSiddhiApps.add(deployableSiddhiApp);
                            }
                        }
                    }
                } else {
                    DeployableSiddhiApp deployableSiddhiApp = new DeployableSiddhiApp(app, isAppStateful);
                    if (sourceDeploymentConfigs != null && sourceDeploymentConfigs.size() != 0) {
                        deployableSiddhiApp.setSourceDeploymentConfigs(sourceDeploymentConfigs);
                    }
                    deployableSiddhiApps.add(deployableSiddhiApp);
                }
            }
            return Response.ok().entity(deployableSiddhiApps).build();
        } catch (Exception e) {
            log.error("Exception caught while parsing the app. " + e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ApiResponseMessage(ApiResponseMessage.ERROR,
                            "Exception caught while parsing the app. " + e.getMessage())).build();
        }
    }

    private List<String> populateAppWithEnvs(Map<String, String> envMap, List<String> siddhiApps) {
        List<String> populatedApps = new ArrayList<>();
        if (siddhiApps != null) {
            for (String siddhiApp : siddhiApps) {
                if (siddhiApp.contains("$")) {
                    if (envMap != null) {
                        String envPattern = "\\$\\{(\\w+)\\}";
                        Pattern expr = Pattern.compile(envPattern);
                        Matcher matcher = expr.matcher(siddhiApp);
                        while (matcher.find()) {
                            for (int i = 1; i <= matcher.groupCount(); i++) {
                                String envValue = envMap.getOrDefault(matcher.group(i), "");
                                envValue = envValue.replace("\\", "\\\\");
                                Pattern subexpr = Pattern.compile("\\$\\{" + matcher.group(i) + "\\}");
                                siddhiApp = subexpr.matcher(siddhiApp).replaceAll(envValue);
                            }
                        }
                    }
                }
                populatedApps.add(siddhiApp);
            }
        }
        return populatedApps;
    }

    private List<SourceDeploymentConfig> getSourceDeploymentConfigs(String siddhiApp) {
        List<SourceDeploymentConfig> sourceDeploymentConfigs = new ArrayList<>();
        SiddhiAppRuntime siddhiAppRuntime = SiddhiParserDataHolder.getSiddhiManager().createSiddhiAppRuntime(siddhiApp);
        Collection<List<Source>> sources = siddhiAppRuntime.getSources();
        for (List<Source> sourceList : sources) {
            for (Source source : sourceList) {
                SourceDeploymentConfig response;
                ServiceDeploymentInfo serviceDeploymentInfo = source.getServiceDeploymentInfo();
                if (serviceDeploymentInfo != null) {
                    response = new SourceDeploymentConfig(serviceDeploymentInfo.getPort(),
                            serviceDeploymentInfo.getServiceProtocol().name(),
                            serviceDeploymentInfo.isSecured(),
                            serviceDeploymentInfo.isPulling(),
                            serviceDeploymentInfo.getDeploymentProperties());
                    sourceDeploymentConfigs.add(response);
                }
            }
        }
        return sourceDeploymentConfigs;
    }

    /**
     * This is the activation method of Siddhi Parser Api Service Component. This will be called when its references are
     * satisfied.
     *
     * @param bundleContext the bundle context instance of this bundle.
     * @throws Exception this will be thrown if an issue occurs while executing the activate method
     */
    @Activate
    protected void start(BundleContext bundleContext) throws Exception {
        if (transportsFileConfiguration != null) {
            microservicesRunner = new MicroservicesRunner(transportsFileConfiguration);
        }
        String toolIdentifier = System.getProperty(SIDDHI_PARSER_ACTIVATION_SYS_PROPERTY);
        Optional.ofNullable(toolIdentifier)
                .ifPresent(identifier -> {
                    startStoresApiMicroservice();
                });
    }

    /**
     * This is the deactivation method of Siddhi Parser Api Service Component. This will be called when this component
     * is being stopped or references are satisfied during runtime.
     *
     * @throws Exception this will be thrown if an issue occurs while executing the de-activate method
     */
    @Deactivate
    protected void stop() throws Exception {
        log.debug("Siddhi Parser API deactivated.");
        stopStoresApiMicroservice();
    }

    /**
     * This is the activation method of Parser Api Microservice.
     */
    public static void startStoresApiMicroservice() {
        if (microservicesRunner != null && !microserviceActive) {
            microservicesRunner.deploy(new SiddhiParserApi());
            microservicesRunner.start();
            microserviceActive = true;
        }
        log.info("Siddhi Parser REST API activated.");
    }

    /**
     * This is the deactivate method of Parser Api Microservice.
     */
    public static void stopStoresApiMicroservice() {
        if (microservicesRunner != null && microserviceActive) {
            microservicesRunner.stop();
            microserviceActive = false;
        }
    }

    @Reference(
            name = "carbon.config.provider",
            service = ConfigProvider.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterConfigProvider"
    )
    protected void registerConfigProvider(ConfigProvider configProvider) {
        SiddhiParserDataHolder.setConfigProvider(configProvider);
        try {
            transportsFileConfiguration = configProvider.getConfigurationObject(TRANSPORT_ROOT_CONFIG_ELEMENT,
                    TransportsFileConfiguration.class);
            CarbonConfiguration carbonConfig = configProvider.getConfigurationObject(CarbonConfiguration.class);
            transportsFileConfiguration.getListenerConfigurations().forEach(
                    listenerConfiguration -> listenerConfiguration.setPort(
                            listenerConfiguration.getPort() + carbonConfig.getPortsConfig().getOffset()));
        } catch (ConfigurationException e) {
            log.error("Error while loading TransportsConfiguration for " + TRANSPORT_ROOT_CONFIG_ELEMENT, e);
        }
    }

    protected void unregisterConfigProvider(ConfigProvider configProvider) {
        SiddhiParserDataHolder.setConfigProvider(null);
    }

    @Reference(
            name = "SiddhiAppRuntimeService",
            service = SiddhiAppRuntimeService.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetSiddhiAppRuntimeService"
    )
    protected void setSiddhiAppRuntimeService(SiddhiAppRuntimeService siddhiAppRuntimeService) {
    }

    protected void unsetSiddhiAppRuntimeService(SiddhiAppRuntimeService siddhiAppRuntimeService) {
    }
}
