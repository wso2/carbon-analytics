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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.das.jobmanager.core.appCreator.DeployableSiddhiQueryGroup;
import org.wso2.carbon.das.jobmanager.core.appCreator.SPSiddhiAppCreator;
import org.wso2.carbon.das.jobmanager.core.bean.DeploymentConfig;
import org.wso2.carbon.das.jobmanager.core.internal.ServiceDataHolder;
import org.wso2.carbon.das.jobmanager.core.topology.InputStreamDataHolder;
import org.wso2.carbon.das.jobmanager.core.topology.OutputStreamDataHolder;
import org.wso2.carbon.das.jobmanager.core.topology.PublishingStrategyDataHolder;
import org.wso2.carbon.das.jobmanager.core.topology.SiddhiQueryGroup;
import org.wso2.carbon.das.jobmanager.core.topology.SiddhiTopology;
import org.wso2.carbon.das.jobmanager.core.topology.SubscriptionStrategyDataHolder;
import org.wso2.carbon.das.jobmanager.core.util.EventHolder;
import org.wso2.carbon.das.jobmanager.core.util.TransportStrategy;
import org.wso2.siddhi.core.SiddhiManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SiddhiAppCreatorTestCase {/*
    private static final Logger log = Logger.getLogger(SiddhiAppCreatorTestCase.class);

    @BeforeMethod
    public void setUp() {
        DeploymentConfig deploymentConfig = new DeploymentConfig();
        deploymentConfig.setBootstrapURLs("localhost:9092");
        ServiceDataHolder.setDeploymentConfig(deploymentConfig);
    }

    @Test
    public void testPartitionAllSubscription() {
        String query1 = "@info(name = 'query1')\n"
                + "From stockStream[price > 100] \n"
                + "Select * \n"
                + "Insert into filteredStockStream; \n"
                + "@info(name = 'query2')\n"
                + "From companyTriggerStream \n"
                + "select * \n"
                + "insert into companyTriggerInternalStream";

        String stockStream = "@source(type='http', receiver.url='http://localhost:9055/endpoints/stockQuote', "
                + "@map(type='xml')) Define stream stockStream(symbol string, price float, quantity int, tier "
                + "string)";
        String companyTriggerStream = "@source(type='http', receiver.url='http://localhost:9055/endpoints/trigger', "
                + "@map(type='xml'))\n"
                + "Define stream companyTriggerStream(symbol string)";
        String filteredStockStream = "${filteredStockStream} define stream filteredStockStream (symbol string, "
                + "price float, quantity int, tier string)";
        String companyTriggerInternalStream = "${companyTriggerInternalStream}\n"
                + "Define stream companyTriggerInternalStream (symbol string)";
        SubscriptionStrategyDataHolder app1StockStreamSubscription = new SubscriptionStrategyDataHolder(1,
                TransportStrategy.ROUND_ROBIN);
        SubscriptionStrategyDataHolder app1cmpTgrStreamSubscription = new SubscriptionStrategyDataHolder(1,
                TransportStrategy.ROUND_ROBIN);
        InputStreamDataHolder stockStreamInput = new InputStreamDataHolder("stockStream", stockStream,
                EventHolder.STREAM,
                true, app1StockStreamSubscription);
        InputStreamDataHolder cmpTgrStreamInput = new InputStreamDataHolder("companyTriggerStream",
                companyTriggerStream,
                EventHolder.STREAM,
                true, app1cmpTgrStreamSubscription);
        Map<String, InputStreamDataHolder> app1Input = new HashMap<>();
        app1Input.put("stockStream", stockStreamInput);
        app1Input.put("companyTriggerStream", cmpTgrStreamInput);

        PublishingStrategyDataHolder filStreamPub1 = new PublishingStrategyDataHolder("002", TransportStrategy
                .FIELD_GROUPING, "symbol", 2);
        PublishingStrategyDataHolder filStreamPub2 = new PublishingStrategyDataHolder("005", TransportStrategy
                .FIELD_GROUPING, "tier", 5);
        PublishingStrategyDataHolder filStreamPub3 = new PublishingStrategyDataHolder("004", TransportStrategy
                .ROUND_ROBIN, 1);

        OutputStreamDataHolder filStreamOutput = new OutputStreamDataHolder("filteredStockStream", filteredStockStream,
                EventHolder.STREAM, false);
        filStreamOutput.addPublishingStrategy(filStreamPub1);
        filStreamOutput.addPublishingStrategy(filStreamPub2);
        filStreamOutput.addPublishingStrategy(filStreamPub3);
        PublishingStrategyDataHolder cmpTgrIntStreamPub1 = new PublishingStrategyDataHolder("002", TransportStrategy
                .ALL, 1);
        OutputStreamDataHolder cmpTgrIntStreamOutput = new OutputStreamDataHolder("companyTriggerInternalStream",
                companyTriggerInternalStream,
                EventHolder.STREAM, false);
        cmpTgrIntStreamOutput.addPublishingStrategy(cmpTgrIntStreamPub1);
        Map<String, OutputStreamDataHolder> app1Output = new HashMap<>();
        app1Output.put("filteredStockStream", filStreamOutput);
        app1Output.put("companyTriggerInternalStream", cmpTgrIntStreamOutput);
        SiddhiQueryGroup group1 = new SiddhiQueryGroup("Siddhi-App-001", 2, app1Input, app1Output);
        group1.addQuery(query1);


        String app2 = "@info(name='query2')\n"
                + "Partition with (symbol of filteredStockStream) \n"
                + "Begin \n"
                + "From \n"
                + "filteredStockStream#window.time(5 min) \n"
                + "Select symbol, avg(price) as avgPrice, quantity \n"
                + "Insert into #avgPriceStream; \n"
                + "From #avgPriceStream#window.time(5 min) as a right outer join companyTriggerInternalStream#window"
                + ".length(1) \n"
                + "On (companyTriggerInternalStream.symbol == a.symbol) \n"
                + "Select a.symbol, a.avgPrice, a.quantity \n"
                + "Insert into triggeredAvgStream;\n"
                + "End";
        String triggeredAvgStream = "${triggeredAvgStream}\n"
                + "define stream triggeredAvgStream (symbol string, avgPrice double, quantity int)";
        SubscriptionStrategyDataHolder app2FilStreamSubscription = new SubscriptionStrategyDataHolder(2,
                TransportStrategy.FIELD_GROUPING);
        app2FilStreamSubscription.setPartitionKey("symbol");
        SubscriptionStrategyDataHolder app2cmpTgrIntStreamSubscription = new SubscriptionStrategyDataHolder(1,
                TransportStrategy.ALL);
        InputStreamDataHolder filStreamInput = new InputStreamDataHolder("filteredStockStream", filteredStockStream,
                EventHolder.STREAM, false,
                app2FilStreamSubscription);
        InputStreamDataHolder cmpTgrIntStreamInput = new InputStreamDataHolder("companyTriggerInternalStream",
                companyTriggerInternalStream,
                EventHolder.STREAM, false,
                app2cmpTgrIntStreamSubscription);
        Map<String, InputStreamDataHolder> app2Input = new HashMap<>();
        app2Input.put("filteredStockStream", filStreamInput);
        app2Input.put("companyTriggerInternalStream", cmpTgrIntStreamInput);

        PublishingStrategyDataHolder trgAvgStreamPub1 = new PublishingStrategyDataHolder("003", TransportStrategy
                .ROUND_ROBIN, 1);
        OutputStreamDataHolder trgPubStreamOutput = new OutputStreamDataHolder("triggeredAvgStream", triggeredAvgStream,
                EventHolder.STREAM,
                false);
        trgPubStreamOutput.addPublishingStrategy(trgAvgStreamPub1);
        Map<String, OutputStreamDataHolder> app2Output = new HashMap<>();
        app2Output.put("triggeredAvgStream", trgPubStreamOutput);
        SiddhiQueryGroup group2 = new SiddhiQueryGroup("Siddhi-App-002", 2, app2Input, app2Output);
        group2.addQuery(app2);

        List<SiddhiQueryGroup> queryGroupList = new ArrayList<>();
        queryGroupList.add(group1);
        queryGroupList.add(group2);
        SiddhiTopology topology = new SiddhiTopology("Siddhi-App", queryGroupList);

        SiddhiAppCreator appCreator = new SPSiddhiAppCreator();
        List<DeployableSiddhiQueryGroup> resultList = appCreator.createApps(topology);

        for (DeployableSiddhiQueryGroup group : resultList) {
            for (String query : group.getQueryList()) {
                SiddhiManager siddhiManager = new SiddhiManager();
                siddhiManager.createSiddhiAppRuntime(query);
            }
        }
    }

    @Test
    public void testMultiplePartitionSubscription() {
        String query1 = "@info(name = 'query1')\n"
                + "From stockStream[price > 100] \n"
                + "Select * \n"
                + "Insert into filteredStockStream; \n"
                + "@info(name = 'query2')\n"
                + "From companyTriggerStream \n"
                + "select * \n"
                + "insert into companyTriggerInternalStream";
        String stockStream = "@source(type='http', receiver.url='http://localhost:9055/endpoints/stockQuote', "
                + "@map(type='xml')) Define stream stockStream(symbol string, price float, quantity int, tier "
                + "string)";
        String companyTriggerStream = "@source(type='http', receiver.url='http://localhost:9055/endpoints/trigger', "
                + "@map(type='xml'))\n"
                + "Define stream companyTriggerStream(symbol string)";
        String filteredStockStream = "${filteredStockStream} define stream filteredStockStream (symbol string, "
                + "price float, quantity int, tier string)";
        String companyTriggerInternalStream = "${companyTriggerInternalStream}\n"
                + "Define stream companyTriggerInternalStream (symbol string)";
        SubscriptionStrategyDataHolder app1StockStreamSubscription = new SubscriptionStrategyDataHolder(1,
                TransportStrategy.ROUND_ROBIN);
        SubscriptionStrategyDataHolder app1cmpTgrStreamSubscription = new SubscriptionStrategyDataHolder(1,
                TransportStrategy.ROUND_ROBIN);
        InputStreamDataHolder stockStreamInput = new InputStreamDataHolder("stockStream", stockStream,
                EventHolder.STREAM,
                true, app1StockStreamSubscription);
        InputStreamDataHolder cmpTgrStreamInput = new InputStreamDataHolder("companyTriggerStream",
                companyTriggerStream,
                EventHolder.STREAM,
                true, app1cmpTgrStreamSubscription);
        Map<String, InputStreamDataHolder> app1Input = new HashMap<>();
        app1Input.put("stockStream", stockStreamInput);
        app1Input.put("companyTriggerStream", cmpTgrStreamInput);

        PublishingStrategyDataHolder filStreamPub2 = new PublishingStrategyDataHolder("005", TransportStrategy
                .FIELD_GROUPING, "symbol", 5);
        PublishingStrategyDataHolder filStreamPub3 = new PublishingStrategyDataHolder("004", TransportStrategy
                .ROUND_ROBIN, 1);

        OutputStreamDataHolder filStreamOutput = new OutputStreamDataHolder("filteredStockStream", filteredStockStream,
                EventHolder.STREAM, false);
        filStreamOutput.addPublishingStrategy(filStreamPub2);
        filStreamOutput.addPublishingStrategy(filStreamPub3);
        PublishingStrategyDataHolder cmpTgrIntStreamPub1 = new PublishingStrategyDataHolder("002", TransportStrategy
                .ALL, 1);
        OutputStreamDataHolder cmpTgrIntStreamOutput = new OutputStreamDataHolder("companyTriggerInternalStream",
                companyTriggerInternalStream,
                EventHolder.STREAM, false);
        cmpTgrIntStreamOutput.addPublishingStrategy(cmpTgrIntStreamPub1);
        Map<String, OutputStreamDataHolder> app1Output = new HashMap<>();
        app1Output.put("filteredStockStream", filStreamOutput);
        app1Output.put("companyTriggerInternalStream", cmpTgrIntStreamOutput);
        SiddhiQueryGroup group1 = new SiddhiQueryGroup("Siddhi-App-001", 2, app1Input, app1Output);
        group1.addQuery(query1);


        String app2 = "@info(name='query2')\n"
                + "Partition with (symbol of filteredStockStream) \n"
                + "Begin \n"
                + "From \n"
                + "filteredStockStream#window.time(5 min) \n"
                + "Select symbol, avg(price) as avgPrice, quantity \n"
                + "Insert into #avgPriceStream; \n"
                + "From #avgPriceStream#window.time(5 min) as a right outer join companyTriggerInternalStream#window"
                + ".length(1) \n"
                + "On (companyTriggerInternalStream.symbol == a.symbol) \n"
                + "Select a.symbol, a.avgPrice, a.quantity \n"
                + "Insert into triggeredAvgStream;\n"
                + "End";
        String triggeredAvgStream = "${triggeredAvgStream}\n"
                + "define stream triggeredAvgStream (symbol string, avgPrice double, quantity int)";
        SubscriptionStrategyDataHolder app2FilStreamSubscription = new SubscriptionStrategyDataHolder(5,
                TransportStrategy.FIELD_GROUPING);
        app2FilStreamSubscription.setPartitionKey("symbol");
        SubscriptionStrategyDataHolder app2cmpTgrIntStreamSubscription = new SubscriptionStrategyDataHolder(1,
                TransportStrategy.ALL);
        InputStreamDataHolder filStreamInput = new InputStreamDataHolder("filteredStockStream", filteredStockStream,
                EventHolder.STREAM, false,
                app2FilStreamSubscription);
        InputStreamDataHolder cmpTgrIntStreamInput = new InputStreamDataHolder("companyTriggerInternalStream",
                companyTriggerInternalStream,
                EventHolder.STREAM, false,
                app2cmpTgrIntStreamSubscription);
        Map<String, InputStreamDataHolder> app2Input = new HashMap<>();
        app2Input.put("filteredStockStream", filStreamInput);
        app2Input.put("companyTriggerInternalStream", cmpTgrIntStreamInput);

        PublishingStrategyDataHolder trgAvgStreamPub1 = new PublishingStrategyDataHolder("003", TransportStrategy
                .ROUND_ROBIN, 1);
        OutputStreamDataHolder trgPubStreamOutput = new OutputStreamDataHolder("triggeredAvgStream", triggeredAvgStream,
                EventHolder.STREAM,
                false);
        trgPubStreamOutput.addPublishingStrategy(trgAvgStreamPub1);
        Map<String, OutputStreamDataHolder> app2Output = new HashMap<>();
        app2Output.put("triggeredAvgStream", trgPubStreamOutput);
        SiddhiQueryGroup group2 = new SiddhiQueryGroup("Siddhi-App-002", 2, app2Input, app2Output);
        group2.addQuery(app2);

        List<SiddhiQueryGroup> queryGroupList = new ArrayList<>();
        queryGroupList.add(group1);
        queryGroupList.add(group2);
        SiddhiTopology topology = new SiddhiTopology("Siddhi-App", queryGroupList);

        SiddhiAppCreator appCreator = new SPSiddhiAppCreator();
        List<DeployableSiddhiQueryGroup> resultList = appCreator.createApps(topology);

        for (DeployableSiddhiQueryGroup group : resultList) {
            for (String query : group.getQueryList()) {
                SiddhiManager siddhiManager = new SiddhiManager();
                siddhiManager.createSiddhiAppRuntime(query);
            }
        }

    }

    @Test
    public void testPartitionRoundRobinSubscription() {
        String query1 = "@info(name = 'query1')\n"
                + "From stockStream[price > 100] \n"
                + "Select * \n"
                + "Insert into filteredStockStream; \n"
                + "@info(name = 'query2')\n"
                + "From companyTriggerStream \n"
                + "select * \n"
                + "insert into companyTriggerInternalStream";
        String stockStream = "@source(type='http', receiver.url='http://localhost:9055/endpoints/stockQuote', "
                + "@map(type='xml')) Define stream stockStream(symbol string, price float, quantity int, tier "
                + "string)";
        String companyTriggerStream = "@source(type='http', receiver.url='http://localhost:9055/endpoints/trigger', "
                + "@map(type='xml'))\n"
                + "Define stream companyTriggerStream(symbol string)";
        String filteredStockStream = "${filteredStockStream} define stream filteredStockStream (symbol string, "
                + "price float, quantity int, tier string)";
        String companyTriggerInternalStream = "${companyTriggerInternalStream}\n"
                + "Define stream companyTriggerInternalStream (symbol string)";
        SubscriptionStrategyDataHolder app1StockStreamSubscription = new SubscriptionStrategyDataHolder(1,
                TransportStrategy.ROUND_ROBIN);
        SubscriptionStrategyDataHolder app1cmpTgrStreamSubscription = new SubscriptionStrategyDataHolder(1,
                TransportStrategy.ROUND_ROBIN);
        InputStreamDataHolder stockStreamInput = new InputStreamDataHolder("stockStream", stockStream,
                EventHolder.STREAM,
                true, app1StockStreamSubscription);
        InputStreamDataHolder cmpTgrStreamInput = new InputStreamDataHolder("companyTriggerStream",
                companyTriggerStream,
                EventHolder.STREAM,
                true, app1cmpTgrStreamSubscription);
        Map<String, InputStreamDataHolder> app1Input = new HashMap<>();
        app1Input.put("stockStream", stockStreamInput);
        app1Input.put("companyTriggerStream", cmpTgrStreamInput);

        PublishingStrategyDataHolder filStreamPub1 = new PublishingStrategyDataHolder("002", TransportStrategy
                .FIELD_GROUPING, "symbol", 2);
        PublishingStrategyDataHolder filStreamPub2 = new PublishingStrategyDataHolder("005", TransportStrategy
                .FIELD_GROUPING, "tier", 5);
        PublishingStrategyDataHolder filStreamPub3 = new PublishingStrategyDataHolder("004", TransportStrategy
                .ROUND_ROBIN, 1);

        OutputStreamDataHolder filStreamOutput = new OutputStreamDataHolder("filteredStockStream", filteredStockStream,
                EventHolder.STREAM, false);
        filStreamOutput.addPublishingStrategy(filStreamPub1);
        filStreamOutput.addPublishingStrategy(filStreamPub2);
        filStreamOutput.addPublishingStrategy(filStreamPub3);
        PublishingStrategyDataHolder cmpTgrIntStreamPub1 = new PublishingStrategyDataHolder("002", TransportStrategy
                .ROUND_ROBIN, 1);
        OutputStreamDataHolder cmpTgrIntStreamOutput = new OutputStreamDataHolder("companyTriggerInternalStream",
                companyTriggerInternalStream,
                EventHolder.STREAM, false);
        cmpTgrIntStreamOutput.addPublishingStrategy(cmpTgrIntStreamPub1);
        Map<String, OutputStreamDataHolder> app1Output = new HashMap<>();
        app1Output.put("filteredStockStream", filStreamOutput);
        app1Output.put("companyTriggerInternalStream", cmpTgrIntStreamOutput);
        SiddhiQueryGroup group1 = new SiddhiQueryGroup("Siddhi-App-001", 2, app1Input, app1Output);
        group1.addQuery(query1);


        String app2 = "@info(name='query2')\n"
                + "Partition with (symbol of filteredStockStream) \n"
                + "Begin \n"
                + "From \n"
                + "filteredStockStream#window.time(5 min) \n"
                + "Select symbol, avg(price) as avgPrice, quantity \n"
                + "Insert into #avgPriceStream; \n"
                + "From #avgPriceStream#window.time(5 min) as a right outer join companyTriggerInternalStream#window"
                + ".length(1) \n"
                + "On (companyTriggerInternalStream.symbol == a.symbol) \n"
                + "Select a.symbol, a.avgPrice, a.quantity \n"
                + "Insert into triggeredAvgStream;\n"
                + "End";
        String triggeredAvgStream = "${triggeredAvgStream}\n"
                + "define stream triggeredAvgStream (symbol string, avgPrice double, quantity int)";
        SubscriptionStrategyDataHolder app2FilStreamSubscription = new SubscriptionStrategyDataHolder(2,
                TransportStrategy.FIELD_GROUPING);
        app2FilStreamSubscription.setPartitionKey("symbol");
        SubscriptionStrategyDataHolder app2cmpTgrIntStreamSubscription = new SubscriptionStrategyDataHolder(1,
                TransportStrategy.ROUND_ROBIN);
        InputStreamDataHolder filStreamInput = new InputStreamDataHolder("filteredStockStream", filteredStockStream,
                EventHolder.STREAM, false,
                app2FilStreamSubscription);
        InputStreamDataHolder cmpTgrIntStreamInput = new InputStreamDataHolder("companyTriggerInternalStream",
                companyTriggerInternalStream,
                EventHolder.STREAM, false,
                app2cmpTgrIntStreamSubscription);
        Map<String, InputStreamDataHolder> app2Input = new HashMap<>();
        app2Input.put("filteredStockStream", filStreamInput);
        app2Input.put("companyTriggerInternalStream", cmpTgrIntStreamInput);

        PublishingStrategyDataHolder trgAvgStreamPub1 = new PublishingStrategyDataHolder("003", TransportStrategy
                .ROUND_ROBIN, 1);
        OutputStreamDataHolder trgPubStreamOutput = new OutputStreamDataHolder("triggeredAvgStream", triggeredAvgStream,
                EventHolder.STREAM,
                false);
        trgPubStreamOutput.addPublishingStrategy(trgAvgStreamPub1);
        Map<String, OutputStreamDataHolder> app2Output = new HashMap<>();
        app2Output.put("triggeredAvgStream", trgPubStreamOutput);
        SiddhiQueryGroup group2 = new SiddhiQueryGroup("Siddhi-App-002", 2, app2Input, app2Output);
        group2.addQuery(app2);

        List<SiddhiQueryGroup> queryGroupList = new ArrayList<>();
        queryGroupList.add(group1);
        queryGroupList.add(group2);
        SiddhiTopology topology = new SiddhiTopology("Siddhi-App", queryGroupList);

        SiddhiAppCreator appCreator = new SPSiddhiAppCreator();
        List<DeployableSiddhiQueryGroup> resultList = appCreator.createApps(topology);

        for (DeployableSiddhiQueryGroup group : resultList) {
            for (String query : group.getQueryList()) {
                SiddhiManager siddhiManager = new SiddhiManager();
                siddhiManager.createSiddhiAppRuntime(query);
            }
        }

    }*/
}
