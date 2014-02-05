package org.wso2.carbon.event.processor.admin.internal.ds;


import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.event.processor.core.EventProcessorService;

/**
 * This class is used to get the EventProcessor service.
 *
 * @scr.component name="eventProcessorAdmin.component" immediate="true"
 * @scr.reference name="eventProcessorService.service"
 * interface="org.wso2.carbon.event.processor.core.EventProcessorService" cardinality="1..1"
 * policy="dynamic" bind="setEventProcessorService" unbind="unsetEventProcessorService"
 */
public class EventProcessorAdminServiceDS {

    protected void activate(ComponentContext context) {

    }

    public void setEventProcessorService(EventProcessorService eventProcessorService) {
        EventProcessorAdminValueHolder.registerEventProcessorService(eventProcessorService);
    }

    public void unsetEventProcessorService(EventProcessorService eventProcessorService) {
        EventProcessorAdminValueHolder.registerEventProcessorService(null);

    }

}
