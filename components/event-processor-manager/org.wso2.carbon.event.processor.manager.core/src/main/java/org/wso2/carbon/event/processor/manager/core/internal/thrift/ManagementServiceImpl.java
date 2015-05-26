/*
 * Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.event.processor.manager.core.internal.thrift;

import org.apache.log4j.Logger;
import org.wso2.carbon.event.processor.manager.core.internal.ds.EventManagementServiceValueHolder;
import org.wso2.carbon.event.processor.manager.core.internal.thrift.data.SnapshotData;
import org.wso2.carbon.event.processor.manager.core.internal.thrift.exception.InternalServerException;
import org.wso2.carbon.event.processor.manager.core.internal.thrift.service.ManagementService;


public class ManagementServiceImpl implements ManagementService.Iface {

    private static final Logger log = Logger.getLogger(ManagementServiceImpl.class);

    @Override
    public SnapshotData takeSnapshot() throws InternalServerException {
        try {
            byte[] state = EventManagementServiceValueHolder.getCarbonEventManagementService().getState();
            SnapshotData snapshotData = new SnapshotData();
            snapshotData.setState(state);
            return snapshotData;
        } catch (Throwable t) {
            throw new InternalServerException(t.getMessage());
        }
    }
}
