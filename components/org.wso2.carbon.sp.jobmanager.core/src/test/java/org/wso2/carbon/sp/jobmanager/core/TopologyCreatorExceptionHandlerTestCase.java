/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.sp.jobmanager.core;

import org.apache.log4j.Logger;
import org.testng.annotations.Test;
import org.wso2.carbon.sp.jobmanager.core.topology.SiddhiTopologyCreatorImpl;
import org.wso2.siddhi.query.api.exception.SiddhiAppValidationException;

public class TopologyCreatorExceptionHandlerTestCase {
    private static final Logger log = Logger.getLogger(TopologyCreatorExceptionHandlerTestCase.class);

    /**
     * Exception should be thrown when an execGroup is not given a Constant parallel value
     */
    @Test(expectedExceptions = SiddhiAppValidationException.class)
    public void testexecGroupParallelism() {

        String siddhiApp = "@Source(type='http', receiver.url='http://localhost:9055/endpoints/stockQuote', @map"
                + "(type='xml')) "
                + "Define stream stockStream(symbol string, price float, quantity int, tier string);\n"
                + "@Store(type='rdbms', jdbc.url='jdbc:mysql://localhost:3306/spDB',jdbc.driver.name='', "
                + "username='root', password='****',field.length='symbol:254')\n"
                + "Define table takingOverTable(symbol string, price float, quantity int, tier string);\n"
                + "@info(name = 'query1')@dist(parallel='1', execGroup='001')\n"
                + "From stockStream[price > 100]\n"
                + "Select *\n"
                + "Insert into filteredStockStream;\n"
                + "@info(name = 'query2')@dist(parallel='3', execGroup='001')\n"
                + "From filteredStockStream\n"
                + "Select *\n"
                + "Insert into takingOverTable;\n";

        SiddhiTopologyCreatorImpl siddhiTopologyCreator = new SiddhiTopologyCreatorImpl();
        siddhiTopologyCreator.createTopology(siddhiApp);
    }

    /**
     * Exception should be thrown when (Defined)Event window is referenced from more than 1 execGroup.
     */
    @Test(expectedExceptions = SiddhiAppValidationException.class)
    public void testEventWindow() {

        String siddhiApp = "@Source(type = 'tcp', context='TempStream',"
                + "@map(type='binary')) "
                + "define stream TempStream(deviceID long, roomNo int, temp double);"
                + "define stream RegulatorStream(deviceID long, roomNo int, isOn bool);\n"
                + "define window TempWindow(deviceID long, roomNo int, temp double) time(1 min); "
                + "@info(name ='query1') @dist(execGroup='group0', parallel='1')\n"
                + "from TempStream\n"
                + "select *\n"
                + "insert into\n"
                + "TempInternalStream;"
                + "@info(name ='query2') @dist(execGroup='group1')\n"
                + "from TempInternalStream[temp > 30.0] "
                + "insert into TempWindow; "
                + "@info(name = 'query3')  @dist(execGroup='group2')"
                + "from TempWindow "
                + "join RegulatorStream[isOn == false]#window.length(1) as R "
                + "on TempWindow.roomNo == R.roomNo"
                + " select TempWindow.roomNo, R.deviceID, 'start' as action "
                + "insert into RegulatorActionStream;";

        SiddhiTopologyCreatorImpl siddhiTopologyCreator = new SiddhiTopologyCreatorImpl();
        siddhiTopologyCreator.createTopology(siddhiApp);

    }

    /**
     * Exception should be thrown when (Defined)Event window used with a (query/partition) having parallel > 1
     */
    @Test(expectedExceptions = SiddhiAppValidationException.class)
    public void testEventWindowParallelism() {
        String siddhiApp = "@Source(type = 'tcp', context='TempStream',"
                + "@map(type='binary')) "
                + "define stream TempStream(deviceID long, roomNo int, temp double);"
                + "define window TempWindow(deviceID long, roomNo int, temp double) time(1 min); "
                + "@info(name ='query1') @dist(execGroup='group0', parallel='1')\n"
                + "from TempStream\n"
                + "select *\n"
                + "insert into\n"
                + "TempInternalStream;"
                + "@info(name ='query2') @dist(execGroup='group1', parallel='2')\n"
                + "Partition with (deviceID of TempInternalStream)\n"
                + "begin\n"
                + "from TempInternalStream[temp > 30.0] "
                + "insert into TempWindow; "
                + "End;\n";

        SiddhiTopologyCreatorImpl siddhiTopologyCreator = new SiddhiTopologyCreatorImpl();
        siddhiTopologyCreator.createTopology(siddhiApp);
    }

    /**
     * Exception should be thrown when In-Memory table is referenced from more than 1 execGroup.
     */
    @Test(expectedExceptions = SiddhiAppValidationException.class)
    public void testInMemoryEventTable() {

        String siddhiApp = "@Source(type='http', receiver.url='http://localhost:9055/endpoints/stockQuote', @map"
                + "(type='xml')) "
                + "Define stream stockStream(symbol string, price float, quantity int, tier string);\n"
                + "@Source(type = 'tcp', context='TempStream',"
                + "@map(type='binary')) "
                + "define stream TempStream(symbol string, roomNo int, price float);"
                + "Define table takingOverTable(symbol string, price float, quantity int, tier string);\n"
                + "@info(name = 'query1')@dist(parallel='1', execGroup='001')\n"
                + "from TempStream\n"
                + "select *\n"
                + "insert into\n"
                + "TempInternalStream;"
                + "@info(name = 'query2')@dist(parallel='1', execGroup='001')\n"
                + "From stockStream[price > 100]\n"
                + "Select *\n"
                + "Insert into takingOverTable;\n"
                + "@info(name = 'query3')@dist(parallel='1', execGroup='002')\n"
                + "from TempInternalStream join takingOverTable\n"
                + "on takingOverTable.price == TempInternalStream.price\n"
                + "select TempInternalStream.symbol, takingOverTable.price as roomPrice,roomNo\n"
                + "having symbol  == 'pi'\n"
                + "insert into ServerRoomTempStream;";

        SiddhiTopologyCreatorImpl siddhiTopologyCreator = new SiddhiTopologyCreatorImpl();
        siddhiTopologyCreator.createTopology(siddhiApp);

    }

    /**
     * Exception should be thrown when In-Memory table is used with a (query/partition) having parallel > 1
     */
    @Test(expectedExceptions = SiddhiAppValidationException.class)
    public void testInMemoryTableParallelism() {

        String siddhiApp = "@Source(type='http', receiver.url='http://localhost:9055/endpoints/stockQuote', @map"
                + "(type='xml')) "
                + "Define stream stockStream(symbol string, price float, quantity int, tier string);\n"
                + "@Source(type = 'tcp', context='TempStream',"
                + "@map(type='binary')) "
                + "define stream TempStream(symbol string, roomNo int, price float);"
                + "Define table takingOverTable(symbol string, price float, quantity int, tier string);\n"
                + "@info(name = 'query1')@dist(parallel='1', execGroup='001')\n"
                + "from TempStream\n"
                + "select *\n"
                + "insert into\n"
                + "TempInternalStream;"
                + "@info(name = 'query2')@dist(parallel='1', execGroup='001')\n"
                + "From stockStream[price > 100]\n"
                + "Select *\n"
                + "Insert into takingOverTable;\n"
                + "@info(name = 'query3')@dist(parallel='2', execGroup='002')\n"
                + "Partition with (symbol of TempInternalStream)\n"
                + "Begin\n"
                + "from TempInternalStream join takingOverTable\n"
                + "on takingOverTable.price == TempInternalStream.price\n"
                + "select TempInternalStream.symbol, takingOverTable.price as roomPrice,roomNo\n"
                + "having symbol  == 'pi'\n"
                + "insert into ServerRoomTempStream;\n"
                + "End;";

        SiddhiTopologyCreatorImpl siddhiTopologyCreator = new SiddhiTopologyCreatorImpl();
        siddhiTopologyCreator.createTopology(siddhiApp);
    }


    /**
     * Exception should be thrown when window is used in a (query/partition) without a partitioned stream or inner
     * stream having parallel >1
     */
    @Test(expectedExceptions = SiddhiAppValidationException.class)
    public void testWindowParallelism() {
        String siddhiApp = "@App:name('TestPlan') "
                + "@Source(type = 'tcp', context='TempStream',"
                + "@map(type='binary')) "
                + "define stream TempStream(deviceID long, roomNo int, temp double); "
                + "@info(name = 'query1') @dist(parallel ='1', execGroup='group1')\n "
                + "from TempStream\n"
                + "select *\n"
                + "insert into TempInternalStream;"
                + "@info(name = 'query2') @dist(parallel ='2', execGroup='group2')\n "
                + "from TempInternalStream#window.length(10)\n"
                + "select roomNo, deviceID, max(temp) as maxTemp\n"
                + "insert into DeviceTempStream\n";

        SiddhiTopologyCreatorImpl siddhiTopologyCreator = new SiddhiTopologyCreatorImpl();
        siddhiTopologyCreator.createTopology(siddhiApp);
    }


    /**
     * Exception should be thrown when Joined without a partitioned stream or inner stream having parallel >1
     */
    @Test(expectedExceptions = SiddhiAppValidationException.class)
    public void testJoinParallelism() {
        String siddhiApp = "define stream TempStream(deviceID long, roomNo int, temp double);\n"
                + "define stream RegulatorStream(deviceID long, roomNo int, isOn bool);\n"
                + "@info(name ='query1') @dist(execGroup='group1', parallel='2')\n"
                + "from TempStream[temp > 30.0]#window.time(1 min) as T\n"
                + "  join RegulatorStream[isOn == false]#window.length(1) as R\n"
                + "  on T.roomNo == R.roomNo\n"
                + "select T.roomNo, R.deviceID, 'start' as action\n"
                + "insert into RegulatorActionStream;";

        SiddhiTopologyCreatorImpl siddhiTopologyCreator = new SiddhiTopologyCreatorImpl();
        siddhiTopologyCreator.createTopology(siddhiApp);
    }


    /**
     * Exception should be thrown when Patterned without a partitioned stream or inner stream having parallel >1
     */
    @Test(expectedExceptions = SiddhiAppValidationException.class)
    public void testPatternParallelism() {
        String siddhiApp = "define stream TempStream (deviceID long, roomNo int, temp double);\n"
                + "define stream RegulatorStream (deviceID long, roomNo int, tempSet double, isOn bool);\n"
                + "@info(name ='query1') @dist(execGroup='group1', parallel='2')\n"
                + "from every( e1=RegulatorStream) -> e2=TempStream[e1.roomNo==roomNo]<1:> -> "
                + "e3=RegulatorStream[e1.roomNo==roomNo]\n"
                + "select e1.roomNo, e2[0].temp - e2[last].temp as tempDiff\n"
                + "insert into TempDiffStream;";

        SiddhiTopologyCreatorImpl siddhiTopologyCreator = new SiddhiTopologyCreatorImpl();
        siddhiTopologyCreator.createTopology(siddhiApp);

    }


    /**
     * Exception should be thrown when Sequenced without a partitioned stream or inner stream having parallel >1
     */
    @Test(expectedExceptions = SiddhiAppValidationException.class)
    public void testSequenceParallelism() {
        String siddhiApp = "define stream TempStream(deviceID long, roomNo int, temp double);\n"
                + "@info(name ='query1') @dist(execGroup='group1', parallel='2')\n"
                + "from every e1=TempStream, e2=TempStream[e1.temp <= temp]+, e3=TempStream[e2[last].temp > temp]\n"
                + "select e1.temp as initialTemp, e2[last].temp as peakTemp\n"
                + "insert into PeekTempStream;";

        SiddhiTopologyCreatorImpl siddhiTopologyCreator = new SiddhiTopologyCreatorImpl();
        siddhiTopologyCreator.createTopology(siddhiApp);
    }


    /**
     * Exception should be thrown when More than 1 partition of (same/different) partition keys residing on the same
     * execGroup
     */
    @Test(expectedExceptions = SiddhiAppValidationException.class)
    public void testMultiPartition() {
        String siddhiApp = "@App:name('TestPlan') \n"
                + "@source(type='http', receiver.url='http://localhost:9055/endpoints/stockQuote', @map(type='xml')) "
                + "Define stream stockStream(symbol string, price float, quantity int, tier string);\n"
                + "@source(type='http', receiver.url='http://localhost:9055/endpoints/trigger', @map(type='xml'))\n"
                + "Define stream companyTriggerStream(symbol string);\n"
                + "@info(name = 'query1')@dist(parallel='1', execGroup='001')\n"
                + "From stockStream[price > 100]\n"
                + "Select *\n"
                + "Insert into filteredStockStream;\n"
                + "@info(name = 'query2')@dist(parallel='1', execGroup='001')\n"
                + "from companyTriggerStream\n"
                + "select *\n"
                + "insert into\n"
                + "companyTriggerInternalStream;\n"
                + "@info(name='query3')@dist(parallel='2',execGroup='002')\n"
                + "Partition with (symbol of filteredStockStream)\n"
                + "begin\n"
                + "From filteredStockStream#window.time(5 min)\n"
                + "Select symbol, avg(price) as avgPrice, quantity\n"
                + "Insert into #avgPriceStream;\n"
                + "From #avgPriceStream#window.time(5 min) as a right outer join companyTriggerInternalStream#window"
                + ".length"
                + "(1)\n"
                + "On (companyTriggerInternalStream.symbol == a.symbol)\n"
                + "Select a.symbol, a.avgPrice, a.quantity\n"
                + "Insert into triggeredAvgStream;\n"
                + "End;\n"
                + "@info(name='query4')@dist(parallel='2', execGroup='002')\n"
                + "Partition with (symbol of filteredStockStream)\n"
                + "begin\n"
                + "From filteredStockStream#log(symbol)\n"
                + "Select *\n"
                + "Insert into dumbstream;\n"
                + "End;\n";

        SiddhiTopologyCreatorImpl siddhiTopologyCreator = new SiddhiTopologyCreatorImpl();
        siddhiTopologyCreator.createTopology(siddhiApp);
    }


    /**
     * Exception should be thrown when an Unpartitioned stream is used in a partition and query belonging to same
     * execGroup.
     */
    @Test(expectedExceptions = SiddhiAppValidationException.class)
    public void testConflictingStrategies() {
        String siddhiApp = "@App:name('TestPlan') \n"
                + "@source(type='http', receiver.url='http://localhost:9055/endpoints/stockQuote', @map(type='xml')) "
                + "Define stream stockStream(symbol string, price float, quantity int, tier string);\n"
                + "@source(type='http', receiver.url='http://localhost:9055/endpoints/trigger', @map(type='xml'))\n"
                + "Define stream companyTriggerStream(symbol string);\n"
                + "@info(name = 'query1')@dist(parallel='1', execGroup='001')\n"
                + "From stockStream[price > 100]\n"
                + "Select *\n"
                + "Insert into filteredStockStream;\n"
                + "@info(name = 'query2')@dist(parallel='1', execGroup='001')\n"
                + "from companyTriggerStream\n"
                + "select *\n"
                + "insert into\n"
                + "companyTriggerInternalStream;\n"
                + "@info(name='query3')@dist(parallel='2',execGroup='002')\n"
                + "Partition with (symbol of filteredStockStream)\n"
                + "begin\n"
                + "From filteredStockStream#window.time(5 min)\n"
                + "Select symbol, avg(price) as avgPrice, quantity\n"
                + "Insert into #avgPriceStream;\n"
                + "From #avgPriceStream#window.time(5 min) as a right outer join companyTriggerInternalStream#window"
                + ".length"
                + "(1)\n"
                + "On (companyTriggerInternalStream.symbol == a.symbol)\n"
                + "Select a.symbol, a.avgPrice, a.quantity\n"
                + "Insert into triggeredAvgStream;\n"
                + "End;\n"
                + "@info(name='query4')@dist(parallel='2', execGroup='002')\n"
                + "from companyTriggerInternalStream select *\n"
                + "insert into outputStream";

        SiddhiTopologyCreatorImpl siddhiTopologyCreator = new SiddhiTopologyCreatorImpl();
        siddhiTopologyCreator.createTopology(siddhiApp);
    }

    // TODO: 11/1/17 test for @dist inside partition queries


}
