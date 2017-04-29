package org.wso2.carbon.event.simulator.core.internal.resourceManager.util;

import org.wso2.carbon.event.simulator.core.internal.resourceManager.CSVResourceManager;
import org.wso2.carbon.event.simulator.core.internal.resourceManager.ExecutionPlanResourceManager;
import org.wso2.carbon.event.simulator.core.internal.resourceManager.ResourceManager;
import org.wso2.carbon.event.simulator.core.internal.resourceManager.StreamResourceManager;
import org.wso2.carbon.stream.processor.common.Resources;

/**
 * ResourceManagerFactoryImpl is used for ResourceManager creation
 */
public class ResourceManagerFactoryImpl implements ResourceManagerFactory {

    @Override
    public ResourceManager createResourceManager(Resources.ResourceType resourceType) {
        switch (resourceType) {
            case CSV_FILE:
                return CSVResourceManager.getInstance();
            case EXECUTION_PLAN:
                return ExecutionPlanResourceManager.getInstance();
            case STREAM:
                return StreamResourceManager.getInstance();
            default:
                /*
                 * this statement is never reached since resource type is an enum. Nevertheless a null return is added
                 * since the method signature mandates it
                 */
                return null;
        }
    }
}
