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
package org.wso2.carbon.das.jobmanager.core;

import org.apache.log4j.Logger;
import org.testng.annotations.Test;
import org.wso2.carbon.das.jobmanager.core.appCreator.DeployableSiddhiQueryGroup;
import org.wso2.carbon.das.jobmanager.core.appCreator.SPSiddhiAppCreator;
import org.wso2.carbon.das.jobmanager.core.topology.SiddhiTopology;
import org.wso2.carbon.das.jobmanager.core.topology.SiddhiTopologyCreatorImpl;
import org.wso2.siddhi.core.SiddhiManager;

import java.util.List;

public class SiddhiTopologyCreatorTestCase {
    private static final Logger log = Logger.getLogger(SiddhiTopologyCreator.class);

    @Test
    public void testSiddhiTopologyCreator() {

        String siddhiApp = "@App:name('TestPlan') \n" +
                "@source(type='http', receiver.url='http://localhost:9055/endpoints/stockQuote', @map(type='xml')) " +
                "Define stream stockStream(symbol string, price float, quantity int, tier string);\n" +
                "\n" +
                "@Sink(type='email', @map(type='json'), username='wso2', address='test@wso2.com',password='****',"
                + "host='smtp.gmail.com',subject='Event from SP',to='towso2@gmail.com')\n"
                +
                "Define stream takingOverStream(symbol string, overtakingSymbol string, avgPrice double);\n" +
                "\n" +
                "@source(type='http', receiver.url='http://localhost:9055/endpoints/trigger', @map(type='xml'))\n" +
                "Define stream companyTriggerStream(symbol string);\n" +
                "\n" +
                "@Store(type='rdbms', jdbc.url='jdbc:mysql://localhost:3306/cepDB',jdbc.driver.name='', "
                + "username='root', password='****',field.length='symbol:254')\n"
                +
                "Define table filteredTable (symbol string, price float, quantity int, tier string);\n" +

                "@Store(type='rdbms', jdbc.url='jdbc:mysql://localhost:3306/spDB',jdbc.driver.name='', "
                + "username='root', password='****',field.length='symbol:254')\n"
                +
                "Define table takingOverTable(symbol string, overtakingSymbol string, avgPrice double);\n" +
                "\n" +
                "@info(name = 'query1')@dist(parallel='2', execGroup='001')\n" +
                "From stockStream[price > 100]\n" +
                "Select *\n" +
                "Insert into filteredStockStream;\n" +
                "\n" +
                "@info(name='query2')@dist(parallel='2',execGroup='002')\n" +
                "Partition with (symbol of filteredStockStream)\n" +
                "begin\n" +
                "From filteredStockStream#window.time(5 min)\n" +
                "Select symbol, avg(price) as avgPrice, quantity\n" +
                "Insert into #avgPriceStream;\n" +
                "\n" +
                "From #avgPriceStream#window.time(5 min) as a right outer join companyTriggerStream#window.length(1)\n"
                +
                "On (companyTriggerStream.symbol == a.symbol)\n" +
                "Select a.symbol, a.avgPrice, a.quantity\n" +
                "Insert into triggeredAvgStream;\n" +
                "End;\n" +
                "\n" +
                "@info(name='query3')@dist(parallel='1', execGroup='003')\n" +
                "From  a1=triggeredAvgStream,  a2=triggeredAvgStream[a1.avgPrice<a2.avgPrice]\n" +
                "Select a1.symbol, a2.symbol as overtakingSymbol, a2.avgPrice \n" +
                "Insert into takingOverStream;\n" +
                "\n" +
                "@info(name='query4')@dist(parallel='4', execGroup='004')\n" +
                "From filteredStockStream\n" +
                "Select *\n" +
                "Insert into filteredTable;\n" +
                "\n" +
                "@info(name='query5')@dist(parallel='4', execGroup='004')\n" +
                "From takingOverStream\n" +
                "Select *\n" +
                "Insert into takingOverTable;\n" +
                "\n" +
                "@info(name='query6')@dist(parallel='3', execGroup='005')\n" +
                "Partition with (tier of filteredStockStream)\n" +
                "begin\n" +
                "From filteredStockStream#log(tier)\n" +
                "Select *\n" +
                "Insert into dumbstream;\n" +
                "End;\n";

        SiddhiTopologyCreatorImpl siddhiTopologyCreator = new SiddhiTopologyCreatorImpl();
        SiddhiTopology topology = siddhiTopologyCreator.createTopology(siddhiApp);
        SiddhiAppCreator appCreator = new SPSiddhiAppCreator();
        List<DeployableSiddhiQueryGroup> queryGroupList = appCreator.createApps(topology);
        for (DeployableSiddhiQueryGroup group : queryGroupList) {
            for (String query : group.getQueryList()) {
                SiddhiManager siddhiManager = new SiddhiManager();
                siddhiManager.createSiddhiAppRuntime(query);
            }
        }
    }

    @Test
    public void testParallelNotGiven(){}


}
