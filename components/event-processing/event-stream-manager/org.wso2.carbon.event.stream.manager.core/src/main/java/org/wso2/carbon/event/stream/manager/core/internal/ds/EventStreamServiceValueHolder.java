package org.wso2.carbon.event.stream.manager.core.internal.ds;

import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.processor.api.passthrough.PassthroughReceiverConfigurator;
import org.wso2.carbon.event.stream.manager.core.internal.CarbonEventStreamService;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.ArrayList;
import java.util.List;

public class EventStreamServiceValueHolder {

    private static RegistryService registryService;
    private static CarbonEventStreamService carbonEventStreamService;
    private static PassthroughReceiverConfigurator passthroughReceiverConfigurator;
    private static RealmService realmService;
    private static List<StreamDefinition> pendingStreamIdList = new ArrayList<StreamDefinition>();

    private EventStreamServiceValueHolder() {

    }

    public static void unSetRegistryService() {
        EventStreamServiceValueHolder.registryService = null;
    }

    public static RegistryService getRegistryService() {
        return EventStreamServiceValueHolder.registryService;
    }

    public static void setRegistryService(RegistryService registryService) {
        EventStreamServiceValueHolder.registryService = registryService;
    }

    public static Registry getRegistry(int tenantId) throws RegistryException {
        return registryService.getConfigSystemRegistry(tenantId);
    }

    public static void unSetEventStreamService() {
        EventStreamServiceValueHolder.carbonEventStreamService = null;
    }

    public static CarbonEventStreamService getCarbonEventStreamService() {
        return EventStreamServiceValueHolder.carbonEventStreamService;
    }

    public static void setCarbonEventStreamService(
            CarbonEventStreamService carbonEventStreamService) {
        EventStreamServiceValueHolder.carbonEventStreamService = carbonEventStreamService;
    }

    public static PassthroughReceiverConfigurator getPassthroughReceiverConfigurator() {
        return passthroughReceiverConfigurator;
    }

    public static void registerPassthroughReceiverConfigurator(PassthroughReceiverConfigurator eventBuilderService) {
        EventStreamServiceValueHolder.passthroughReceiverConfigurator = eventBuilderService;
    }

    public static List<StreamDefinition> getPendingStreamIdList() {
        return EventStreamServiceValueHolder.pendingStreamIdList;
    }

    public static void setPendingStreamIdList(List<StreamDefinition> pendingStreamIdList) {
        EventStreamServiceValueHolder.pendingStreamIdList = pendingStreamIdList;
    }

    public static void addEventStreamToPendingList(StreamDefinition streamId) {
        EventStreamServiceValueHolder.pendingStreamIdList.add(streamId);
    }

    public static RealmService getRealmService() {
        return realmService;
    }

    public static void setRealmService(RealmService realmService) {
        EventStreamServiceValueHolder.realmService = realmService;
    }
}
