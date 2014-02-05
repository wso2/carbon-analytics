package org.wso2.carbon.bam.toolbox.deployer.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.toolbox.deployer.ServiceHolder;
import org.wso2.carbon.bam.toolbox.deployer.exception.BAMToolboxDeploymentException;
import org.wso2.carbon.databridge.commons.exception.*;
import org.wso2.carbon.databridge.core.DataBridgeReceiverService;

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
public class DataPublisher {
    private static DataPublisher instance;
    private static DataBridgeReceiverService receiverService;

    private static Log log = LogFactory.getLog(DataPublisher.class);

    private DataPublisher() {
        receiverService = ServiceHolder.getDataBridgeReceiverService();
        if (null == receiverService) {
            log.warn("Databridge reciever service is not available...");
        }
    }

    public static DataPublisher getInstance() {
        if (null == instance) {
            instance = new DataPublisher();
        }
        return instance;
    }

    public void createEventDefn(String defn, String username, String password)
            throws BAMToolboxDeploymentException {
        try {
            String session = receiverService.login(username, password);
            try {
                receiverService.defineStream(session, defn);
            } catch (DifferentStreamDefinitionAlreadyDefinedException e1) {
                log.warn(e1.getMessage());
            } catch (MalformedStreamDefinitionException e1) {
                log.error(e1);
                throw new BAMToolboxDeploymentException(e1.getMessage(), e1);
            } catch (SessionTimeoutException e1) {
                log.error(e1);
            }
        } catch (Exception e) {
            log.warn("Logout in DataReceiver is not successful...");
        }
    }

    public void createEventDefn(String defn, String username, String password, String indexDefn)
            throws BAMToolboxDeploymentException {
        try {
            String session = receiverService.login(username, password);
            try {
                receiverService.defineStream(session, defn, indexDefn);
            } catch (DifferentStreamDefinitionAlreadyDefinedException e1) {
                log.warn(e1.getMessage());
            } catch (MalformedStreamDefinitionException e1) {
                log.error(e1);
                throw new BAMToolboxDeploymentException(e1.getMessage(), e1);
            } catch (SessionTimeoutException e1) {
                log.error(e1);
            }
        } catch (Exception e) {
            log.warn("Logout in DataReceiver is not successful...");
        }
    }

}
