/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.analytics.eventsink.internal.jmx;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created on 6/6/16.
 *
 * This class will count total number of event received to the server from the server startup.
 * This will count event only receive to stream as marked as persist.
 */
public class EventCounter {

    private static AtomicInteger counter = new AtomicInteger(0);

    public static int incrementAndGet(int delta) {
        return counter.addAndGet(delta);
    }

    public static int getCurrentCount() {
        return counter.get();
    }
}
