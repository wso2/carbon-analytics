package org.wso2.carbon.event.formatter.core.internal.ds;

import org.wso2.carbon.event.formatter.core.EventFormatterService;
import org.wso2.carbon.event.formatter.core.config.OutputMapperFactory;
import org.wso2.carbon.event.formatter.core.internal.CarbonEventFormatterService;
import org.wso2.carbon.event.formatter.core.internal.type.json.JSONOutputMapperFactory;
import org.wso2.carbon.event.formatter.core.internal.type.map.MapOutputMapperFactory;
import org.wso2.carbon.event.formatter.core.internal.type.text.TextOutputMapperFactory;
import org.wso2.carbon.event.formatter.core.internal.type.wso2event.WSO2OutputMapperFactory;
import org.wso2.carbon.event.formatter.core.internal.type.xml.XMLOutputMapperFactory;
import org.wso2.carbon.event.output.adaptor.core.MessageType;
import org.wso2.carbon.event.output.adaptor.core.OutputEventAdaptorService;
import org.wso2.carbon.event.output.adaptor.manager.core.OutputEventAdaptorManagerService;
import org.wso2.carbon.event.processor.api.send.EventProducer;
import org.wso2.carbon.event.statistics.EventStatisticsService;
import org.wso2.carbon.event.stream.manager.core.EventStreamService;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class EventFormatterServiceValueHolder {

    private static OutputEventAdaptorService outputEventAdaptorService;
    private static CarbonEventFormatterService carbonEventFormatterService;
    private static OutputEventAdaptorManagerService outputEventAdaptorManagerService;
    private static ConfigurationContextService configurationContextService;
    private static EventStreamService eventStreamService;
    private static RegistryService registryService;
    private static List<EventProducer> eventProducerList = new ArrayList<EventProducer>();
    private static ConcurrentHashMap<String, OutputMapperFactory> mappingFactoryMap = new ConcurrentHashMap<String, OutputMapperFactory>() {
    };

    static {
        mappingFactoryMap.put(MessageType.MAP, new MapOutputMapperFactory());
        mappingFactoryMap.put(MessageType.TEXT, new TextOutputMapperFactory());
        mappingFactoryMap.put(MessageType.WSO2EVENT, new WSO2OutputMapperFactory());
        mappingFactoryMap.put(MessageType.XML, new XMLOutputMapperFactory());
        mappingFactoryMap.put(MessageType.JSON, new JSONOutputMapperFactory());
    }

    private static EventStatisticsService eventStatisticsService;

    private EventFormatterServiceValueHolder() {

    }

    public static CarbonEventFormatterService getCarbonEventFormatterService() {
        return carbonEventFormatterService;
    }

    public static void registerFormatterService(EventFormatterService eventFormatterService) {
        EventFormatterServiceValueHolder.carbonEventFormatterService = (CarbonEventFormatterService) eventFormatterService;

    }

    public static void registerEventAdaptorService(
            OutputEventAdaptorService eventAdaptorService) {
        EventFormatterServiceValueHolder.outputEventAdaptorService = eventAdaptorService;
    }

    public static OutputEventAdaptorService getOutputEventAdaptorService() {
        return EventFormatterServiceValueHolder.outputEventAdaptorService;
    }

    public static void registerEventAdaptorManagerService(
            OutputEventAdaptorManagerService eventAdaptorManagerService) {
        EventFormatterServiceValueHolder.outputEventAdaptorManagerService = eventAdaptorManagerService;
    }

    public static OutputEventAdaptorManagerService getOutputEventAdaptorManagerService() {
        return EventFormatterServiceValueHolder.outputEventAdaptorManagerService;
    }

    public static void unRegisterEventAdaptorManagerService(
            OutputEventAdaptorManagerService eventAdaptorManagerService) {
        EventFormatterServiceValueHolder.outputEventAdaptorManagerService = null;
    }

    public static void addEventProducer(EventProducer eventProducer) {
        eventProducerList.add(eventProducer);

    }

    public static void removeEventProducer(EventProducer eventProducer) {
        eventProducerList.remove(eventProducer);
    }

    public static List<EventProducer> getEventProducerList() {
        return eventProducerList;
    }

    public static void setRegistryService(RegistryService registryService) {
        EventFormatterServiceValueHolder.registryService = registryService;
    }

    public static void unSetRegistryService() {
        EventFormatterServiceValueHolder.registryService = null;
    }

    public static RegistryService getRegistryService() {
        return EventFormatterServiceValueHolder.registryService;
    }

    public static Registry getRegistry(int tenantId) throws RegistryException {
        return registryService.getConfigSystemRegistry(tenantId);
    }

    public static ConcurrentHashMap<String, OutputMapperFactory> getMappingFactoryMap() {
        return mappingFactoryMap;
    }

    public static void registerEventStatisticsService(
            EventStatisticsService eventStatisticsService) {
        EventFormatterServiceValueHolder.eventStatisticsService = eventStatisticsService;
    }

    public static EventStatisticsService getEventStatisticsService() {
        return eventStatisticsService;
    }

    public static void registerEventStreamService(EventStreamService eventStreamService) {
        EventFormatterServiceValueHolder.eventStreamService = eventStreamService;
    }

    public static EventStreamService getEventStreamService() {
        return EventFormatterServiceValueHolder.eventStreamService;
    }
}
