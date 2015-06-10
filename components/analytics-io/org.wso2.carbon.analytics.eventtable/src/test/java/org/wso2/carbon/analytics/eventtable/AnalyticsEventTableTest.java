/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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
package org.wso2.carbon.analytics.eventtable;

import java.util.List;

import javax.naming.NamingException;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.analytics.dataservice.AnalyticsDataService;
import org.wso2.carbon.analytics.dataservice.AnalyticsServiceHolder;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsTableNotAvailableException;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;
import org.wso2.carbon.analytics.eventtable.internal.ServiceHolder;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.stream.input.InputHandler;

/**
 * Analytics event table tests.
 */
public class AnalyticsEventTableTest {

    private AnalyticsDataService service;
    
    @BeforeClass
    public void setup() throws NamingException, AnalyticsException {
        GenericUtils.clearGlobalCustomDataSourceRepo();
        System.setProperty(GenericUtils.WSO2_ANALYTICS_CONF_DIRECTORY_SYS_PROP, "src/test/resources/conf1");
        AnalyticsServiceHolder.setHazelcastInstance(null);
        AnalyticsServiceHolder.setAnalyticsClusterManager(null);
        System.setProperty(AnalyticsServiceHolder.FORCE_INDEXING_ENV_PROP, Boolean.TRUE.toString());
        this.service = AnalyticsServiceHolder.getAnalyticsDataService();
        ServiceHolder.setAnalyticsDataService(service);
    }
    
    @AfterClass
    public void destroy() throws AnalyticsException {
        this.service.destroy();
        System.clearProperty(AnalyticsServiceHolder.FORCE_INDEXING_ENV_PROP);
        AnalyticsServiceHolder.setAnalyticsDataService(null);
    }
    
    @Test
    public void testInsert() throws InterruptedException, AnalyticsTableNotAvailableException, AnalyticsException {
        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "@from(eventtable = 'analytics.table' , table.name = 'stocks', schema = 'symbol string, price float, volume long', primary.keys = 'symbol') " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1')" +
                "from StockStream" +
                "insert into StockTable;";
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);
        InputHandler stockStream = executionPlanRuntime.getInputHandler("StockStream");
        executionPlanRuntime.start();
        stockStream.send(new Object[]{"WSO2", 55.6f, 100l});
        stockStream.send(new Object[]{"IBM", 75.6f, 100l});
        stockStream.send(new Object[]{"WSO2", 57.6f, 100l});
        Thread.sleep(1000);
        List<Record> recordsIn = GenericUtils.listRecords(this.service,
                this.service.get(-1, "stocks", 1, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        Assert.assertEquals(recordsIn.size(), 2);
    }
    
}
