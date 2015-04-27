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
package org.wso2.carbon.event.input.adapter.core.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterFactory;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterService;
import org.wso2.carbon.event.input.adapter.core.internal.CarbonInputEventAdapterService;
import org.wso2.carbon.event.input.adapter.core.internal.EventAdapterConstants;
import org.wso2.carbon.event.input.adapter.core.internal.config.AdapterConfigs;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @scr.component name="event.input.adapter.service" immediate="true"
 * @scr.reference name="input.event.adapter.tracker.service"
 * interface="org.wso2.carbon.event.input.adapter.core.InputEventAdapterFactory" cardinality="0..n"
 * policy="dynamic" bind="setEventAdapterType" unbind="unSetEventAdapterType"
 * @scr.reference name="config.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="0..1" policy="dynamic"
 * bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 */
public class InputEventAdapterServiceDS {

    private static final Log log = LogFactory.getLog(InputEventAdapterServiceDS.class);
    public static List<InputEventAdapterFactory> inputEventAdapterFactories = new ArrayList<InputEventAdapterFactory>();

    /**
     * initialize the Event Adapter Manager core service here.
     *
     * @param context the component context that will be passed in from the OSGi environment at activation
     */
    protected void activate(ComponentContext context) {


        InputEventAdapterServiceValueHolder.setGlobalAdapterConfigs(loadGlobalConfigs());

        CarbonInputEventAdapterService inputEventAdapterService = new CarbonInputEventAdapterService();
        InputEventAdapterServiceValueHolder.setCarbonInputEventAdapterService(inputEventAdapterService);

        registerInputEventAdapterFactories();

        context.getBundleContext().registerService(InputEventAdapterService.class.getName(), inputEventAdapterService, null);

        try {
            if (log.isDebugEnabled()) {
                log.debug("Successfully deployed the input event adapter service");
            }
        } catch (RuntimeException e) {
            log.error("Can not create the input event adapter service ", e);
        }
    }

    private void registerInputEventAdapterFactories() {
        CarbonInputEventAdapterService carbonInputEventAdapterService = InputEventAdapterServiceValueHolder.getCarbonInputEventAdapterService();

        for (InputEventAdapterFactory inputEventAdapterFactory : inputEventAdapterFactories) {
            carbonInputEventAdapterService.registerEventAdapterFactory(inputEventAdapterFactory);
        }

    }

    protected void setEventAdapterType(InputEventAdapterFactory inputEventAdapterFactory) {
        try {
            if (InputEventAdapterServiceValueHolder.getCarbonInputEventAdapterService() != null) {
                InputEventAdapterServiceValueHolder.getCarbonInputEventAdapterService().registerEventAdapterFactory(inputEventAdapterFactory);
            } else {
                inputEventAdapterFactories.add(inputEventAdapterFactory);
            }
        } catch (Throwable t) {
            String inputEventAdapterFactoryClassName = "Unknown";
            if (inputEventAdapterFactory != null) {
                inputEventAdapterFactoryClassName = inputEventAdapterFactory.getClass().getName();
            }
            log.error("Unexpected error at initializing input event adapter factory "
                    + inputEventAdapterFactoryClassName + ": " + t.getMessage(), t);
        }
    }

    protected void unSetEventAdapterType(InputEventAdapterFactory inputEventAdapterFactory) {
        InputEventAdapterServiceValueHolder.getCarbonInputEventAdapterService().unRegisterEventAdapter(inputEventAdapterFactory);
    }

    protected void setConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        InputEventAdapterServiceValueHolder.setConfigurationContextService(configurationContextService);
    }

    protected void unsetConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        InputEventAdapterServiceValueHolder.setConfigurationContextService(null);

    }

    private AdapterConfigs loadGlobalConfigs() {

        String path = CarbonUtils.getCarbonConfigDirPath() + File.separator + EventAdapterConstants.GLOBAL_CONFIG_FILE_NAME;
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(AdapterConfigs.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

            File configFile = new File(path);
            if (!configFile.exists()) {
                log.warn(EventAdapterConstants.GLOBAL_CONFIG_FILE_NAME + " can not found in " + path + ", hence Input Event Adapters will be running with default global configs.");
            }
            return (AdapterConfigs) unmarshaller.unmarshal(configFile);
        } catch (JAXBException e) {
            log.error("Error in loading " + EventAdapterConstants.GLOBAL_CONFIG_FILE_NAME + " from " + path + ", hence Input Event Adapters will be running with default global configs.");
        }
        return new AdapterConfigs();
    }
}

