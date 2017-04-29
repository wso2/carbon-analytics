package org.wso2.carbon.event.simulator.core.internal.resourceManager.util;

import org.wso2.carbon.event.simulator.core.internal.resourceManager.ResourceManager;
import org.wso2.carbon.stream.processor.common.Resources;

/**
 * ResourceManagerFactory is the factory class used for creating ResourceManagers
 */
public interface ResourceManagerFactory {

    ResourceManager createResourceManager(Resources.ResourceType resourceType);
}
