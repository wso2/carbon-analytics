/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.streaming.integrator.core.ha;

import io.siddhi.core.table.record.RecordTableHandler;
import io.siddhi.core.table.record.RecordTableHandlerManager;

/**
 * Implementation of {@link RecordTableHandlerManager} used for two node minimum HA
 */
public class HACoordinationRecordTableHandlerManager extends RecordTableHandlerManager {

    public HACoordinationRecordTableHandlerManager() {
    }

    @Override
    public RecordTableHandler generateRecordTableHandler() {
        return new HACoordinationRecordTableHandler();
    }
}
