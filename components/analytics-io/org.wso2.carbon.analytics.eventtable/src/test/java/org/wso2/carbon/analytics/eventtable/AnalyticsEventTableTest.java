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
import org.wso2.carbon.analytics.dataservice.AnalyticsDataServiceUtils;
import org.wso2.carbon.analytics.dataservice.AnalyticsServiceHolder;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsTableNotAvailableException;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;
import org.wso2.carbon.analytics.eventtable.internal.ServiceHolder;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;

/**
 * Analytics event table tests.
 */
public class AnalyticsEventTableTest {

    private AnalyticsDataService service;
    
    private int inEventCount = 0;
    
    private int removeEventCount = 0;
    
    private boolean eventArrived = false;
    
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
        this.service.deleteTable(-1, "stocks");
        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "@from(eventtable = 'analytics.table' , table.name = 'stocks', primary.keys = 'symbol') " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable;";
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);
        InputHandler stockStream = executionPlanRuntime.getInputHandler("StockStream");
        executionPlanRuntime.start();
        stockStream.send(new Object[] { "WSO2", 55.6f, 100l });
        stockStream.send(new Object[] { "IBM", 75.6f, 100l });
        stockStream.send(new Object[] { "WSO2", 57.6f, 100l });
        Thread.sleep(2000);
        List<Record> recordsIn = AnalyticsDataServiceUtils.listRecords(this.service,
                this.service.get(-1, "stocks", 1, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        Assert.assertEquals(recordsIn.size(), 2);
        this.service.deleteTable(-1, "stocks");
    }
    
    @Test
    public void testInsertWithIndices() throws InterruptedException, AnalyticsTableNotAvailableException, AnalyticsException {
        this.service.deleteTable(-1, "stocks");
        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "@from(eventtable = 'analytics.table' , table.name = 'stocks', primary.keys = 'symbol', indices = 'symbol') " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable;";
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);
        InputHandler stockStream = executionPlanRuntime.getInputHandler("StockStream");
        executionPlanRuntime.start();
        stockStream.send(new Object[] { "WSO2", 55.6f, 100l });
        stockStream.send(new Object[] { "IBM", 75.6f, 100l });
        stockStream.send(new Object[] { "WSO2", 57.6f, 100l });
        Thread.sleep(2000);
        List<Record> recordsIn = AnalyticsDataServiceUtils.listRecords(this.service,
                this.service.get(-1, "stocks", 1, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        Assert.assertEquals(recordsIn.size(), 2);
        Assert.assertEquals(this.service.search(-1, "stocks", "symbol: 'WSO2'", 0, 10).size(), 1);
        this.service.deleteTable(-1, "stocks");
    }
    
    @Test
    public void testInsertWithIndicesAndScoreParams() throws InterruptedException, AnalyticsException {
        this.service.deleteTable(-1, "stocks");
        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "@from(eventtable = 'analytics.table' , table.name = 'stocks', primary.keys = 'symbol', indices = 'symbol, price -sp') " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable;";
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);
        InputHandler stockStream = executionPlanRuntime.getInputHandler("StockStream");
        executionPlanRuntime.start();
        stockStream.send(new Object[] { "WSO2", 55.6f, 100l });
        stockStream.send(new Object[] { "IBM", 75.6f, 100l });
        stockStream.send(new Object[] { "WSO2", 57.6f, 100l });
        Thread.sleep(2000);
        List<Record> recordsIn = AnalyticsDataServiceUtils.listRecords(this.service,
                this.service.get(-1, "stocks", 1, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        Assert.assertEquals(recordsIn.size(), 2);
        Assert.assertEquals(this.service.search(-1, "stocks", "price: [60 TO 80]", 0, 10).size(), 1);
        this.service.deleteTable(-1, "stocks");
    }
    
    @Test
    public void testJoin1() throws InterruptedException, AnalyticsException {
        this.cleanupCommonProps();
        this.service.deleteTable(-1, "stocks");
        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string); " +
                "@from(eventtable = 'analytics.table' , table.name = 'stocks') " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query2') " +
                "from CheckStockStream#window.length(1) join StockTable " +
                "select CheckStockStream.symbol as checkSymbol, StockTable.symbol as symbol, StockTable.volume as volume  " +
                "insert into OutputStream ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);

        executionPlanRuntime.addCallback("query2", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    for (Event event : inEvents) {
                        inEventCount++;
                        switch (inEventCount) {
                            case 1:
                                Assert.assertEquals(new Object[]{"WSO2", "WSO2", 100l}, event.getData());
                                break;
                            case 2:
                                Assert.assertEquals(new Object[]{"WSO2", "IBM", 10l}, event.getData());
                                break;
                            default:
                                Assert.assertSame(2, inEventCount);
                        }
                    }
                    eventArrived = true;
                }
                if (removeEvents != null) {
                    removeEventCount = removeEventCount + removeEvents.length;
                }
                eventArrived = true;
            }

        });

        InputHandler stockStream = executionPlanRuntime.getInputHandler("StockStream");
        InputHandler checkStockStream = executionPlanRuntime.getInputHandler("CheckStockStream");

        executionPlanRuntime.start();

        stockStream.send(new Object[] { "WSO2", 55.6f, 100l });
        stockStream.send(new Object[] { "IBM", 75.6f, 10l });
        checkStockStream.send(new Object[] { "WSO2" });

        Thread.sleep(2000);

        Assert.assertEquals(2, this.inEventCount);
        Assert.assertEquals(0, this.removeEventCount);
        Assert.assertEquals(true, this.eventArrived);

        executionPlanRuntime.shutdown();
        this.service.deleteTable(-1, "stocks");
        this.cleanupCommonProps();
    }
    
    @Test
    public void testJoin2() throws InterruptedException, AnalyticsException {
        this.cleanupCommonProps();
        this.service.deleteTable(-1, "stocks");
        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string); " +
                "@from(eventtable = 'analytics.table' , table.name = 'stocks', indices = 'symbol') " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query2') " +
                "from CheckStockStream#window.length(1) join StockTable " +
                " on StockTable.symbol==CheckStockStream.symbol " +
                "select CheckStockStream.symbol as checkSymbol, StockTable.symbol as symbol, StockTable.volume as volume  " +
                "insert into OutputStream ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);

        executionPlanRuntime.addCallback("query2", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    for (Event event : inEvents) {
                        inEventCount++;
                        switch (inEventCount) {
                            case 1:
                                Assert.assertEquals(new Object[]{"WSO2", "WSO2", 100l}, event.getData());
                                break;
                            case 2:
                                Assert.assertEquals(new Object[]{"WSO2", "IBM", 10l}, event.getData());
                                break;
                            default:
                                Assert.assertSame(1, inEventCount);
                        }
                    }
                    eventArrived = true;
                }
                if (removeEvents != null) {
                    removeEventCount = removeEventCount + removeEvents.length;
                }
                eventArrived = true;
            }

        });

        InputHandler stockStream = executionPlanRuntime.getInputHandler("StockStream");
        InputHandler checkStockStream = executionPlanRuntime.getInputHandler("CheckStockStream");

        executionPlanRuntime.start();

        stockStream.send(new Object[] { "WSO2", 55.6f, 100l });
        stockStream.send(new Object[] { "IBM", 75.6f, 10l });
        checkStockStream.send(new Object[] { "WSO2" });

        Thread.sleep(2000);
        
        Assert.assertEquals(this.inEventCount, 1);
        Assert.assertEquals(this.removeEventCount, 0);
        Assert.assertEquals(this.eventArrived, true);

        executionPlanRuntime.shutdown();
        this.service.deleteTable(-1, "stocks");
        this.cleanupCommonProps();
    }
    
    private void cleanupCommonProps() {
        this.inEventCount = 0;
        this.removeEventCount = 0;
        this.eventArrived = false;
    }
    
    public static void main(String[] args) throws Exception {
        AnalyticsEventTableTest x = new AnalyticsEventTableTest();
        x.setup();
        x.testJoin2();
        System.exit(0);
    }
    
}
