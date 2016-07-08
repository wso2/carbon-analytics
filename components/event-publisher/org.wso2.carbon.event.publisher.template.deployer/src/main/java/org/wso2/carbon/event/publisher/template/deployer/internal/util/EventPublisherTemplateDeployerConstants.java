package org.wso2.carbon.event.publisher.template.deployer.internal.util;

import org.wso2.carbon.event.template.manager.core.internal.util.TemplateManagerConstants;
import org.wso2.carbon.registry.core.RegistryConstants;

public class EventPublisherTemplateDeployerConstants {
    public static final String META_INFO_COLLECTION_PATH = TemplateManagerConstants.DEPLOYER_META_INFO_PATH
                                                           + RegistryConstants.PATH_SEPARATOR
                                                           + EventPublisherTemplateDeployerConstants.EVENT_PUBLISHER_DEPLOYER_TYPE;
    public static final String META_INFO_PUBLISHER_NAME_SEPARATER = ",";

    public static final String EVENT_PUBLISHER_DEPLOYER_TYPE = "eventpublisher";
}
