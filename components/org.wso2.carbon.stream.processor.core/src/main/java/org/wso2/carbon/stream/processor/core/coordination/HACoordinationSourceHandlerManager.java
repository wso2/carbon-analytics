/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.stream.processor.core.coordination;

import org.wso2.siddhi.core.stream.input.source.SourceHandler;
import org.wso2.siddhi.core.stream.input.source.SourceHandlerManager;

/**
 * Implementation of {@link SourceHandlerManager} used for 2 node minimum HA
 */
public class HACoordinationSourceHandlerManager extends SourceHandlerManager {

    /**
     * Parameter that defines the size of the event queue that is stored in each {@link SourceHandler}
     */
    private int queueCapacity;

    public HACoordinationSourceHandlerManager(int queueCapacity) {
        this.queueCapacity = queueCapacity;
    }

    @Override
    public SourceHandler generateSourceHandler() {
        return new HACoordinationSourceHandler(queueCapacity);
    }
}
