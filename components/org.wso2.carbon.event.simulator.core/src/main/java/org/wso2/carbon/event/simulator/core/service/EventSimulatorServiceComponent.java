package org.wso2.carbon.event.simulator.core.service;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.analytics.permissions.PermissionManager;
import org.wso2.carbon.stream.processor.core.internal.StreamProcessorDataHolder;

@Component(
        name = "Event-Simulator-Service Component",
        immediate = true
)
public class EventSimulatorServiceComponent {
    @Reference(
            name = "permission-manager",
            service = PermissionManager.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetPermissionManager"
    )
    protected void setPermissionManager(PermissionManager permissionManager) {
        EventSimulatorDataHolder.setPermissionProvider(permissionManager.getProvider());
    }

    protected void unsetPermissionManager(PermissionManager permissionManager) {
        EventSimulatorDataHolder.setPermissionProvider(null);
    }
}
