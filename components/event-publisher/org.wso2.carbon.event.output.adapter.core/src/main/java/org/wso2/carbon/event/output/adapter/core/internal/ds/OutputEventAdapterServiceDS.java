/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.wso2.carbon.event.output.adapter.core.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterFactory;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterService;
import org.wso2.carbon.event.output.adapter.core.internal.CarbonOutputEventAdapterService;
import org.wso2.carbon.event.output.adapter.core.internal.EventAdapterConstants;
import org.wso2.carbon.event.output.adapter.core.internal.config.AdapterConfigs;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @scr.component name="event.output.adapter.service" immediate="true"
 * @scr.reference name="output.event.adapter.tracker.service"
 * interface="org.wso2.carbon.event.output.adapter.core.OutputEventAdapterFactory" cardinality="0..n"
 * policy="dynamic" bind="setEventAdapterType" unbind="unSetEventAdapterType"
 * @scr.reference name="config.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="0..1" policy="dynamic"
 * bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 */
public class OutputEventAdapterServiceDS {

    private static final Log log = LogFactory.getLog(OutputEventAdapterServiceDS.class);
    private final static List<OutputEventAdapterFactory> outputEventAdapterFactories = new ArrayList<OutputEventAdapterFactory>();

    /**
     * initialize the Event Adapter Manager core service here.
     *
     * @param context the component context that will be passed in from the OSGi environment at activation
     */
    protected void activate(ComponentContext context) {


        OutputEventAdapterServiceValueHolder.setGlobalAdapterConfigs(loadGlobalConfigs());

        CarbonOutputEventAdapterService outputEventAdapterService = new CarbonOutputEventAdapterService();
        OutputEventAdapterServiceValueHolder.setCarbonOutputEventAdapterService(outputEventAdapterService);

        registerOutputEventAdapterFactories();

        context.getBundleContext().registerService(OutputEventAdapterService.class.getName(), outputEventAdapterService, null);

        try {
            if (log.isDebugEnabled()) {
                log.debug("Successfully deployed the output event adapter service");
            }
        } catch (RuntimeException e) {
            log.error("Can not create the output event adapter service ", e);
        }
    }

    private void registerOutputEventAdapterFactories() {
        CarbonOutputEventAdapterService carbonOutputEventAdapterService = OutputEventAdapterServiceValueHolder.getCarbonOutputEventAdapterService();

        for (OutputEventAdapterFactory outputEventAdapterFactory : outputEventAdapterFactories) {
            carbonOutputEventAdapterService.registerEventAdapterFactory(outputEventAdapterFactory);
        }

    }

    protected void setEventAdapterType(OutputEventAdapterFactory outputEventAdapterFactory) {
        try {
            if (OutputEventAdapterServiceValueHolder.getCarbonOutputEventAdapterService() != null) {
                OutputEventAdapterServiceValueHolder.getCarbonOutputEventAdapterService().registerEventAdapterFactory(outputEventAdapterFactory);
//                OutputEventAdapterServiceValueHolder.getCarbonEventPublisherService().activateInactiveEventFormatterConfigurationForAdapter(abstractOutputEventAdapter.getOutputEventAdapterDto().getType());
            } else {
                outputEventAdapterFactories.add(outputEventAdapterFactory);
            }
        } catch (Throwable t) {
            String outputEventAdapterFactoryClassName = "Unknown";
            if (outputEventAdapterFactory != null) {
                outputEventAdapterFactoryClassName = outputEventAdapterFactory.getClass().getName();
            }
            log.error("Unexpected error at initializing output event adapter factory "
                    + outputEventAdapterFactoryClassName + ": " + t.getMessage(), t);
        }
    }

    protected void unSetEventAdapterType(OutputEventAdapterFactory outputEventAdapterFactory) {
        OutputEventAdapterServiceValueHolder.getCarbonOutputEventAdapterService().unRegisterEventAdapter(outputEventAdapterFactory);
    }

    private AdapterConfigs loadGlobalConfigs() {

        String path = CarbonUtils.getCarbonConfigDirPath() + File.separator + EventAdapterConstants.GLOBAL_CONFIG_FILE_NAME;
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(AdapterConfigs.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

            File configFile = new File(path);
            if (!configFile.exists()) {
                log.warn(EventAdapterConstants.GLOBAL_CONFIG_FILE_NAME + " can not found in " + path + ", hence Output Event Adapters will be running with default global configs.");
            }
            return (AdapterConfigs) unmarshaller.unmarshal(configFile);
        } catch (JAXBException e) {
            log.error("Error in loading " + EventAdapterConstants.GLOBAL_CONFIG_FILE_NAME + " from " + path + ", hence Output Event Adapters will be running with default global configs.");
        }
        return new AdapterConfigs();
    }

    protected void setConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        OutputEventAdapterServiceValueHolder.setConfigurationContextService(configurationContextService);
    }

    protected void unsetConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        OutputEventAdapterServiceValueHolder.setConfigurationContextService(null);

    }

}

