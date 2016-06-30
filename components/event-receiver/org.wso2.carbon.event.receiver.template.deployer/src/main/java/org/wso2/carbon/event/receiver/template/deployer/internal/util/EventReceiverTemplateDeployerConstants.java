package org.wso2.carbon.event.receiver.template.deployer.internal.util;

import org.wso2.carbon.event.execution.manager.core.internal.util.ExecutionManagerConstants;
import org.wso2.carbon.registry.core.RegistryConstants;

public class EventReceiverTemplateDeployerConstants {
    public static final String META_INFO_COLLECTION_PATH = ExecutionManagerConstants.DEPLOYER_META_INFO_PATH
                                                           + RegistryConstants.PATH_SEPARATOR
                                                           + EventReceiverTemplateDeployerConstants.EVENT_RECEIVER_DEPLOYER_TYPE;
    public static final String META_INFO_STREAM_NAME_SEPARATER = ",";

    public static final String EVENT_RECEIVER_DEPLOYER_TYPE = "eventreceiver";
}
