/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.sp.jobmanager.core.util;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Class to retain results received by JMS client so that tests can poll the result and assert against.
 */
public class ResultContainer {
    private static Log log = LogFactory.getLog(ResultContainer.class);
    private int eventCount;
    private List<String> results;
    private CountDownLatch latch;
    private int timeout = 90;


    public ResultContainer(int expectedEventCount) {
        eventCount = 0;
        results = new ArrayList<>(expectedEventCount);
        latch = new CountDownLatch(expectedEventCount);
    }

    public ResultContainer(int expectedEventCount, int timeoutInSeconds) {
        eventCount = 0;
        results = new ArrayList<>(expectedEventCount);
        latch = new CountDownLatch(expectedEventCount);
        timeout = timeoutInSeconds;
    }

    public void eventReceived(String message) {
        eventCount++;
        results.add(message.toString());
        latch.countDown();
    }

    public void waitForResult() {
        try {
            latch.await(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public Boolean assertMessageContent(String content) {
        try {
            if (latch.await(timeout, TimeUnit.SECONDS)) {
                for (String message : results) {
                    if (message.contains(content)) {
                        return true;
                    }
                }
                return false;
            } else {
                log.error("Expected number of results not received. Only received " + eventCount + " events.");
                return false;
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return false;
    }
}


