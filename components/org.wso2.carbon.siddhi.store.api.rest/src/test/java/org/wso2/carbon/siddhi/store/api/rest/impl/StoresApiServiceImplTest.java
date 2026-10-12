/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.siddhi.store.api.rest.impl;

import io.siddhi.core.SiddhiAppRuntime;
import io.siddhi.core.SiddhiManager;
import io.siddhi.core.util.statistics.metrics.Level;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.siddhi.store.api.rest.SiddhiStoreDataHolder;
import org.wso2.carbon.siddhi.store.api.rest.model.InitAggregationDTO;
import org.wso2.carbon.streaming.integrator.common.SiddhiAppRuntimeService;

import java.util.Collections;
import java.util.Map;
import javax.ws.rs.core.Response;

public class StoresApiServiceImplTest {

    private static final String APP_NAME = "ManualAggregationTestApp";
    private static final String AGGREGATION_NAME = "StockAggregation";

    private SiddhiAppRuntime siddhiAppRuntime;
    private StoresApiServiceImpl storesApiService;

    @BeforeMethod
    public void setUp() {
        String siddhiApp = "@App:name('" + APP_NAME + "') "
                + "define stream StockStream (symbol string, price double, eventTime long); "
                + "define aggregation " + AGGREGATION_NAME + " "
                + "from StockStream "
                + "select symbol, sum(price) as totalPrice "
                + "group by symbol "
                + "aggregate by eventTime every sec ... min;";

        siddhiAppRuntime = new SiddhiManager().createSiddhiAppRuntime(siddhiApp);
        siddhiAppRuntime.start();
        Map<String, SiddhiAppRuntime> runtimes = Collections.singletonMap(APP_NAME, siddhiAppRuntime);
        SiddhiStoreDataHolder.getInstance().setSiddhiAppRuntimeService(new SiddhiAppRuntimeService() {
            @Override
            public Map<String, SiddhiAppRuntime> getActiveSiddhiAppRuntimes() {
                return runtimes;
            }

            @Override
            public void enableSiddhiAppStatistics(Level enabledStatsLevel) {
                // Statistics are not required by the aggregation initialization endpoint.
            }
        });
        storesApiService = new StoresApiServiceImpl();
    }

    @AfterMethod
    public void tearDown() {
        if (siddhiAppRuntime != null) {
            siddhiAppRuntime.shutdown();
        }
        SiddhiStoreDataHolder.getInstance().setSiddhiAppRuntimeService(null);
    }

    @Test
    public void testInitializeAggregation() throws Exception {
        Response response = storesApiService.initAgg(new InitAggregationDTO().trigger(APP_NAME, AGGREGATION_NAME));

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }

    @Test
    public void testInitializeAggregationWithUnknownApp() throws Exception {
        Response response = storesApiService.initAgg(new InitAggregationDTO().trigger("UnknownApp", AGGREGATION_NAME));

        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testInitializeAggregationWithUnknownAggregation() throws Exception {
        Response response = storesApiService.initAgg(new InitAggregationDTO().trigger(APP_NAME, "UnknownAggregation"));

        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
    }
}
