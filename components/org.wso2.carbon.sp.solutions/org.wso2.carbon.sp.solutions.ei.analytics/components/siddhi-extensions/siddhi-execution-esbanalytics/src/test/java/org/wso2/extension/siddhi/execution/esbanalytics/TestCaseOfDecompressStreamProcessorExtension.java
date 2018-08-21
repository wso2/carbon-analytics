/*
 *  Copyright (c)  2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.extension.siddhi.execution.esbanalytics;

import org.apache.log4j.Logger;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.extension.siddhi.execution.esbanalytics.util.ESBAnalyticsDecompressConstants;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.output.StreamCallback;
import org.wso2.siddhi.core.util.SiddhiTestHelper;

import java.util.concurrent.atomic.AtomicInteger;

public class TestCaseOfDecompressStreamProcessorExtension {

    private static Logger log = Logger.getLogger(TestCaseOfDecompressStreamProcessorExtension.class);
    private AtomicInteger count = new AtomicInteger(0);
    private volatile boolean decompressedAttributesRetrieved;

    @BeforeMethod
    public void init() {

        count.set(0);
        decompressedAttributesRetrieved = false;
    }

    @Test
    public void testDecompressSimpleMessage() throws InterruptedException {

        log.info("DecompressSimpleMessage TestCase");
        SiddhiManager siddhiManager = new SiddhiManager();
        //siddhiManager.setExtension("str:concat", ConcatFunctionExtension.class);

        String inStreamDefinition = "\ndefine stream inputStream(meta_compressed bool, meta_tenantId int," +
                " messageId string, flowData string);";
        String query = ("@info( name = 'query') from inputStream#esbAnalytics:decompress(meta_compressed, " +
                "meta_tenantId, messageId, flowData) insert all events into outputStream;");
        log.info("Preparing siddhi app runtime: " + inStreamDefinition + "\n" + query);

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(
                inStreamDefinition + query);

        siddhiAppRuntime.addCallback("outputStream", new StreamCallback() {
            @Override
            public void receive(Event[] inEvents) {

                for (Event event : inEvents) {
                    count.incrementAndGet();
                    if (count.get() == 1) {
                        String componentName = (String) event.getData(5);
                        log.info("Asserting decompressed events..");
                        AssertJUnit.assertEquals("Proxy Service", componentName);
                        decompressedAttributesRetrieved = true;
                    }
                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("inputStream");
        siddhiAppRuntime.start();
        Object[] esbAnalyticsEvent = new Object[]{
                ESBAnalyticsDecompressConstants.TestData.META_COMPRESSED,
                ESBAnalyticsDecompressConstants.TestData.META_TENANT_ID,
                ESBAnalyticsDecompressConstants.TestData.MESSAGE_ID,
                ESBAnalyticsDecompressConstants.TestData.FLOW_DATA
        };
        log.info("Simulating esbAnalytics message flow..");
        inputHandler.send(esbAnalyticsEvent);
        SiddhiTestHelper.waitForEvents(200, 1, count, 60000);
        AssertJUnit.assertTrue(decompressedAttributesRetrieved);
        siddhiAppRuntime.shutdown();
    }
}
