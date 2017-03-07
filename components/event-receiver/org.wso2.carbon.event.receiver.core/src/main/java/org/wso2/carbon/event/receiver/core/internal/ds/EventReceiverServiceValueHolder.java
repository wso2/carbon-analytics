/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.event.receiver.core.internal.ds;

import org.wso2.carbon.event.input.adapter.core.InputEventAdapterService;
import org.wso2.carbon.event.input.adapter.core.MessageType;
import org.wso2.carbon.event.processor.manager.core.EventManagementService;
import org.wso2.carbon.event.receiver.core.InputMapperFactory;
import org.wso2.carbon.event.receiver.core.internal.CarbonEventReceiverManagementService;
import org.wso2.carbon.event.receiver.core.internal.CarbonEventReceiverService;
import org.wso2.carbon.event.receiver.core.internal.type.json.JSONInputMapperFactory;
import org.wso2.carbon.event.receiver.core.internal.type.map.MapInputMapperFactory;
import org.wso2.carbon.event.receiver.core.internal.type.text.TextInputMapperFactory;
import org.wso2.carbon.event.receiver.core.internal.type.wso2event.WSO2EventInputMapperFactory;
import org.wso2.carbon.event.receiver.core.internal.type.xml.XMLInputMapperFactory;
import org.wso2.carbon.event.stream.core.EventStreamService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class EventReceiverServiceValueHolder {

    private static InputEventAdapterService inputEventAdapterService;
    private static CarbonEventReceiverService carbonEventReceiverService;
    private static EventManagementService eventManagementService;
    private static RegistryService registryService;
    private static EventStreamService eventStreamService;
    private static ConcurrentHashMap<String, InputMapperFactory> mappingFactoryMap;
    private static ConfigurationContextService configurationContextService;
    private static CarbonEventReceiverManagementService carbonEventReceiverManagementService;
    public static Set<String> inputEventAdapterTypes = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
    private static RealmService realmService;
    private static boolean globalStatisticsEnabled = false;

    static {
        mappingFactoryMap = new ConcurrentHashMap<String, InputMapperFactory>();
        mappingFactoryMap.put(MessageType.MAP, new MapInputMapperFactory());
        mappingFactoryMap.put(MessageType.TEXT, new TextInputMapperFactory());
        mappingFactoryMap.put(MessageType.WSO2EVENT, new WSO2EventInputMapperFactory());
        mappingFactoryMap.put(MessageType.XML, new XMLInputMapperFactory());
        mappingFactoryMap.put(MessageType.JSON, new JSONInputMapperFactory());
    }


    private EventReceiverServiceValueHolder() {

    }

    public static ConcurrentHashMap<String, InputMapperFactory> getMappingFactoryMap() {
        return mappingFactoryMap;
    }

    public static CarbonEventReceiverService getCarbonEventReceiverService() {
        return carbonEventReceiverService;
    }

    public static void registerEventReceiverService(CarbonEventReceiverService carbonEventReceiverService) {
        EventReceiverServiceValueHolder.carbonEventReceiverService = (CarbonEventReceiverService) carbonEventReceiverService;
    }

    public static void registerInputEventAdapterService(
            InputEventAdapterService inputEventAdapterService) {
        EventReceiverServiceValueHolder.inputEventAdapterService = inputEventAdapterService;
    }

    public static InputEventAdapterService getInputEventAdapterService() {
        return EventReceiverServiceValueHolder.inputEventAdapterService;
    }

    public static void registerRegistryService(RegistryService registryService) {
        EventReceiverServiceValueHolder.registryService = registryService;
    }

    public static RegistryService getRegistryService() {
        return registryService;
    }

    public static void registerEventStreamService(EventStreamService eventStreamService) {
        EventReceiverServiceValueHolder.eventStreamService = eventStreamService;
    }

    public static EventStreamService getEventStreamService() {
        return EventReceiverServiceValueHolder.eventStreamService;
    }

    public static ConfigurationContextService getConfigurationContextService() {
        return configurationContextService;
    }

    public static void setConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        EventReceiverServiceValueHolder.configurationContextService = configurationContextService;
    }

    public static CarbonEventReceiverManagementService getCarbonEventReceiverManagementService() {
        return carbonEventReceiverManagementService;
    }

    public static void registerEventManagementService(EventManagementService eventManagementService) {
        EventReceiverServiceValueHolder.eventManagementService = eventManagementService;
    }

    public static EventManagementService getEventManagementService() {
        return eventManagementService;
    }

    public static void registerReceiverManagementService(CarbonEventReceiverManagementService eventReceiverManagementService) {
        EventReceiverServiceValueHolder.carbonEventReceiverManagementService = eventReceiverManagementService;
    }

    public static Set<String> getInputEventAdapterTypes() {
        return inputEventAdapterTypes;
    }

    public static void addInputEventAdapterType(String inputEventAdapterType) {
        EventReceiverServiceValueHolder.inputEventAdapterTypes.add(inputEventAdapterType);
    }

    public static void removeInputEventAdapterType(String inputEventAdapterType) {
        EventReceiverServiceValueHolder.inputEventAdapterTypes.remove(inputEventAdapterType);
    }

    public static RealmService getRealmService() {
        return realmService;
    }

    public static void setRealmService(RealmService realmService) {
        EventReceiverServiceValueHolder.realmService = realmService;
    }

    public static void setGlobalStatisticsEnabled(boolean globalStatisticsEnabled) {
        EventReceiverServiceValueHolder.globalStatisticsEnabled = globalStatisticsEnabled;
    }

    public static boolean isGlobalStatisticsEnabled() {
        return globalStatisticsEnabled;
    }
}
