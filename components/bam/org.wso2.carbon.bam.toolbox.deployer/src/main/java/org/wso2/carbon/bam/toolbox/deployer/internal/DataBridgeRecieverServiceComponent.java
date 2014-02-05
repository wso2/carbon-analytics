package org.wso2.carbon.bam.toolbox.deployer.internal;

/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.bam.toolbox.deployer.ServiceHolder;
import org.wso2.carbon.databridge.core.DataBridgeReceiverService;

/**
 * @scr.component name="org.wso2.carbon.bam.toolbox.databridge.service.component" immediate="true"
 * @scr.reference name="databridge.reciever"
 * interface="org.wso2.carbon.databridge.core.DataBridgeReceiverService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setDataBridgeReceiverService"
 * unbind="unsetDataBridgeReceiverService"
 */

public class DataBridgeRecieverServiceComponent {
    private static final Log log = LogFactory.getLog(DataBridgeRecieverServiceComponent.class);

    protected void activate(ComponentContext context) {
        log.info("Successfully setted data bridge reciever service");
    }

    protected void setDataBridgeReceiverService(DataBridgeReceiverService dataBridgeReceiverService) {
        ServiceHolder.setDataBridgeReceiverService(dataBridgeReceiverService);
    }

    protected void unsetDataBridgeReceiverService(DataBridgeReceiverService dataBridgeReceiverService) {
        ServiceHolder.setDataBridgeReceiverService(null);
    }


}
