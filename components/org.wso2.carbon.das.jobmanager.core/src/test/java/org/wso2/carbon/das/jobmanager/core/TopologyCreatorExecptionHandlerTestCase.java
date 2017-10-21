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
 *
 */

package org.wso2.carbon.das.jobmanager.core;

import org.apache.log4j.Logger;
import org.testng.annotations.Test;
import org.wso2.carbon.das.jobmanager.core.topology.SiddhiTopologyCreatorImpl;
import org.wso2.siddhi.query.api.exception.SiddhiAppValidationException;

public class TopologyCreatorExecptionHandlerTestCase {
    private static final Logger log = Logger.getLogger(TopologyCreatorExecptionHandlerTestCase.class);

    /**
     * An execGroup should have constant parallel value
     */
    @Test(expectedExceptions = SiddhiAppValidationException.class)
    public void testexecGroupParallelism() {

        String siddhiApp = "@Source(type='http', receiver.url='http://localhost:9055/endpoints/stockQuote', @map"
                + "(type='xml')) "
                + "Define stream stockStream(symbol string, price float, quantity int, tier string);\n"
                + "@Store(type='rdbms', jdbc.url='jdbc:mysql://localhost:3306/spDB',jdbc.driver.name='', "
                + "username='root', password='****',field.length='symbol:254')\n"
                + "Define table takingOverTable(symbol string, price float, quantity int, tier string);\n"
                + "@info(name = 'query1')@dist(parallel='2', execGroup='001')\n"
                + "From stockStream[price > 100]\n"
                + "Select *\n"
                + "Insert into filteredStockStream;\n"
                + "@info(name = 'query2')@dist(parallel='3', execGroup='002')\n"
                + "From filteredStockStream\n"
                + "Select *\n"
                + "Insert into takingOverTable;\n";

        SiddhiTopologyCreatorImpl siddhiTopologyCreator = new SiddhiTopologyCreatorImpl();
        siddhiTopologyCreator.createTopology(siddhiApp);
    }

    /**
     * Event window can not have parallel > 1 and can not exist in more than 1 execGroup
     */
    @Test(expectedExceptions = SiddhiAppValidationException.class)
    public void testEventWindow() {

        String siddhiApp = "@Source(type = 'tcp', context='TempStream',"
                + "@map(type='binary')) "
                + "define stream TempStream(deviceID long, roomNo int, temp double);"
                + "define stream RegulatorStream(deviceID long, roomNo int, isOn bool);"
                + "define window TempWindow(deviceID long, roomNo int, temp double) time(1 min);"
                + "@info(name = 'query1') @dist(execGroup='group1')"
                + "from TempStream[temp > 30.0]"
                + "insert into TempWindow; "
                + "@info(name = 'query2')  @dist(execGroup='group2')"
                + "from TempWindow "
                + "join RegulatorStream[isOn == false]#window.length(1) as R "
                + "on TempWindow.roomNo == R.roomNo"
                + " select TempWindow.roomNo, R.deviceID, 'start' as action "
                + "insert into RegulatorActionStream;";

        SiddhiTopologyCreatorImpl siddhiTopologyCreator = new SiddhiTopologyCreatorImpl();
        siddhiTopologyCreator.createTopology(siddhiApp);

    }

    /**
     * In-Memory Event table can not have parallel > 1 and can not exist in more than 1 execGroup
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
                + "@info(name = 'query1')@dist(parallel='2', execGroup='001')\n"
                + "From stockStream[price > 100]\n"
                + "Select *\n"
                + "Insert into takingOverTable;\n"
                + "@info(name = 'query2')@dist(parallel='1', execGroup='002')\n"
                + "from TempStream join takingOverTable\n"
                + "on takingOverTable.price == TempStream.price\n"
                + "select TempStream.symbol, takingOverTable.price as roomPrice,roomNo\n"
                + "having symbol  == 'pi'\n"
                + "insert into ServerRoomTempStream;";

        SiddhiTopologyCreatorImpl siddhiTopologyCreator = new SiddhiTopologyCreatorImpl();
        siddhiTopologyCreator.createTopology(siddhiApp);

    }


}
