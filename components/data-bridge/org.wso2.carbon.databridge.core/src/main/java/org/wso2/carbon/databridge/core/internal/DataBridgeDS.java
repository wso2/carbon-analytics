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

package org.wso2.carbon.databridge.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.utils.EventDefinitionConverterUtils;
import org.wso2.carbon.databridge.core.DataBridge;
import org.wso2.carbon.databridge.core.DataBridgeStreamStore;
import org.wso2.carbon.databridge.core.DataBridgeReceiverService;
import org.wso2.carbon.databridge.core.DataBridgeServiceValueHolder;
import org.wso2.carbon.databridge.core.DataBridgeSubscriberService;
import org.wso2.carbon.databridge.core.conf.DatabridgeConfigurationFileResolver;
import org.wso2.carbon.databridge.core.definitionstore.InMemoryStreamDefinitionStore;
import org.wso2.carbon.databridge.core.internal.authentication.CarbonAuthenticationHandler;
import org.wso2.carbon.databridge.core.internal.utils.DataBridgeConstants;
import org.wso2.carbon.databridge.core.internal.utils.DataBridgeCoreBuilder;
import org.wso2.carbon.kernel.CarbonRuntime;
import org.wso2.carbon.kernel.configprovider.ConfigProvider;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Service component to consume CarbonRuntime instance which has been registered as an OSGi service
 * by Carbon Kernel.
 *
 * @since 1.0.0
 */
@Component(
        name = "org.wso2.carbon.databridge.core.internal.DataBridgeDS",
        immediate = true
)
public class DataBridgeDS {
    private static final Log log = LogFactory.getLog(DataBridgeDS.class);
    private ServiceRegistration receiverServiceRegistration;
    private ServiceRegistration subscriberServiceRegistration;
    private ServiceRegistration dataBridgeEventStreamServiceRegistration;
    private DataBridge databridge;

    /**
     * This is the activation method of DataBridge declarative service. This will be called when its references are
     * satisfied. Agent server is initialized here
     *
     * @param bundleContext the bundle context instance of this bundle.
     * @throws Exception this will be thrown if an issue occurs while executing the activate method
     */
    @Activate
    protected void start(BundleContext bundleContext) throws Exception {
        try {
            if (databridge == null) {
                InMemoryStreamDefinitionStore streamDefinitionStore = new InMemoryStreamDefinitionStore();
                DataBridgeServiceValueHolder.setStreamDefinitionStore(streamDefinitionStore);
                databridge = new DataBridge(new CarbonAuthenticationHandler(),
                        streamDefinitionStore, DatabridgeConfigurationFileResolver.
                        resolveAndSetDatabridgeConfiguration((LinkedHashMap) DataBridgeServiceValueHolder.
                                getConfigProvider().
                                getConfigurationMap(DataBridgeConstants.DATABRIDGE_CONFIG_NAMESPACE)));


                try {
                    List<String> streamDefinitionStrings = DataBridgeCoreBuilder.loadStreamDefinitionXML();
                    for (String streamDefinitionString : streamDefinitionStrings) {
                        try {
                            StreamDefinition streamDefinition = EventDefinitionConverterUtils.convertFromJson(streamDefinitionString);
                            streamDefinitionStore.saveStreamDefinition(streamDefinition);

                        } catch (MalformedStreamDefinitionException e) {
                            log.error("Malformed Stream Definition for " + streamDefinitionString, e);
                        }
                    }
                } catch (Throwable t) {
                    log.error("Cannot load stream definitions ", t);
                }


                receiverServiceRegistration = bundleContext.
                        registerService(DataBridgeReceiverService.class.getName(), databridge, null);
                subscriberServiceRegistration = bundleContext.
                        registerService(DataBridgeSubscriberService.class.getName(), databridge, null);
                dataBridgeEventStreamServiceRegistration = bundleContext.
                        registerService(DataBridgeStreamStore.class.getName(),
                                new DataBridgeStreamStore(), null);

                log.info("Successfully deployed Agent Server ");
            }
        } catch (RuntimeException e) {
            log.error("Error in starting Agent Server ", e);
        }
    }

    /**
     * This is the deactivation method of DataBridge data service. This will be called when this component
     * is being stopped or references are satisfied during runtime.
     *
     * @throws Exception this will be thrown if an issue occurs while executing the de-activate method
     */
    @Deactivate
    protected void stop() throws Exception {
        receiverServiceRegistration.unregister();
        subscriberServiceRegistration.unregister();
        dataBridgeEventStreamServiceRegistration.unregister();
        if (log.isDebugEnabled()) {
            log.debug("Successfully stopped agent server");
        }
    }

    /**
     * This bind method will be called when CarbonRuntime OSGi service is registered.
     *
     * @param carbonRuntime The CarbonRuntime instance registered by Carbon Kernel as an OSGi service
     */
    @Reference(
            name = "carbon.runtime.service",
            service = CarbonRuntime.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetCarbonRuntime"
    )
    protected void setCarbonRuntime(CarbonRuntime carbonRuntime) {
        DataBridgeServiceValueHolder.setCarbonRuntime(carbonRuntime);
    }

    /**
     * This is the unbind method which gets called at the un-registration of CarbonRuntime OSGi service.
     *
     * @param carbonRuntime The CarbonRuntime instance registered by Carbon Kernel as an OSGi service
     */
    protected void unsetCarbonRuntime(CarbonRuntime carbonRuntime) {
        DataBridgeServiceValueHolder.setCarbonRuntime(null);
    }


    @Reference(
            name = "carbon.config.provider",
            service = ConfigProvider.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterConfigProvider"
    )
    protected void registerConfigProvider(ConfigProvider configProvider) {
        DataBridgeServiceValueHolder.setConfigProvider(configProvider);
    }

    protected void unregisterConfigProvider(ConfigProvider configProvider) {
        DataBridgeServiceValueHolder.setConfigProvider(null);
    }

}
