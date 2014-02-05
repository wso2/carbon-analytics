package org.wso2.carbon.event.processor.admin.internal.ds;


import org.wso2.carbon.event.processor.core.EventProcessorService;

public class EventProcessorAdminValueHolder {

    private static EventProcessorService eventProcessorService;

    public static EventProcessorService getEventProcessorService() {
        return eventProcessorService;
    }

    public static void registerEventProcessorService(EventProcessorService eventProcessorService) {
        EventProcessorAdminValueHolder.eventProcessorService = eventProcessorService;
    }
}
