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
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.das.jobmanager.core.appCreator.DeployableSiddhiQueryGroup;
import org.wso2.carbon.das.jobmanager.core.appCreator.SPSiddhiAppCreator;
import org.wso2.carbon.das.jobmanager.core.topology.InputStreamDataHolder;
import org.wso2.carbon.das.jobmanager.core.topology.OutputStreamDataHolder;
import org.wso2.carbon.das.jobmanager.core.topology.PublishingStrategyDataHolder;
import org.wso2.carbon.das.jobmanager.core.topology.SiddhiQueryGroup;
import org.wso2.carbon.das.jobmanager.core.topology.SiddhiTopology;
import org.wso2.carbon.das.jobmanager.core.topology.SubscriptionStrategyDataHolder;
import org.wso2.carbon.das.jobmanager.core.util.TransportStrategy;

import java.util.ArrayList;
import java.util.List;

public class SiddhiAppCreatorTestCase {
    private static final Logger log = Logger.getLogger(SiddhiAppCreatorTestCase.class);

    @Test
    public void testPartitionAllSubscription() {
        String app1 = "@App:name('${appName}') @source(type='http', receiver"
                + ".url='http://localhost:9055/endpoints/stockQuote', @map(type='xml')) \n"
                + "Define stream stockStream(symbol string, price float, quantity int, tier string); \n"
                + "@source(type='http', receiver.url='http://localhost:9055/endpoints/trigger', @map(type='xml'))\n"
                + "Define stream companyTriggerStream(symbol string);\n"
                + "${filteredStockStream} define stream filteredStockStream (symbol string, price float, quantity "
                + "int, tier string); \n"
                + "${companyTriggerInternalStream}\n"
                + "Define stream companyTriggerInternalStream (symbol string); \n"
                + "@info(name = 'query1')\n"
                + "From stockStream[price > 100] \n"
                + "Select * \n"
                + "Insert into filteredStockStream; \n"
                + "@info(name = 'query2')\n"
                + "From companyTriggerStream \n"
                + "select * \n"
                + "insert into companyTriggerInternalStream;\n";
        SubscriptionStrategyDataHolder app1StockStreamSubscription = new SubscriptionStrategyDataHolder(1,
                                                                                                        TransportStrategy.ROUND_ROBIN);
        SubscriptionStrategyDataHolder app1cmpTgrStreamSubscription = new SubscriptionStrategyDataHolder(1,
                                                                                                         TransportStrategy.ROUND_ROBIN);
        InputStreamDataHolder stockStreamInput = new InputStreamDataHolder("stockStream",
                                                                           app1StockStreamSubscription, true);
        InputStreamDataHolder cmpTgrStreamInput = new InputStreamDataHolder("companyTriggerStream",
                                                                            app1cmpTgrStreamSubscription, true);
        List<InputStreamDataHolder> app1Input = new ArrayList<>();
        app1Input.add(stockStreamInput);
        app1Input.add(cmpTgrStreamInput);

        PublishingStrategyDataHolder filStreamPub1 = new PublishingStrategyDataHolder("002", TransportStrategy
                .FIELD_GROUPING, "symbol", 2);
        PublishingStrategyDataHolder filStreamPub2 = new PublishingStrategyDataHolder("005", TransportStrategy
                .FIELD_GROUPING, "tier", 5);
        PublishingStrategyDataHolder filStreamPub3 = new PublishingStrategyDataHolder("004", TransportStrategy
                .ROUND_ROBIN, 1);
        List<PublishingStrategyDataHolder> filPubStrategies = new ArrayList<>(3);
        filPubStrategies.add(filStreamPub1);
        filPubStrategies.add(filStreamPub2);
        filPubStrategies.add(filStreamPub3);
        OutputStreamDataHolder filStreamOutput = new OutputStreamDataHolder("filteredStockStream", filPubStrategies,
                                                                            false);

        PublishingStrategyDataHolder cmpTgrIntStreamPub1 = new PublishingStrategyDataHolder("002", TransportStrategy
                .ALL, 1);
        List<PublishingStrategyDataHolder> cmpTgrIntPubStrategies = new ArrayList<>(1);
        cmpTgrIntPubStrategies.add(cmpTgrIntStreamPub1);
        OutputStreamDataHolder cmpTgrIntStreamOutput = new OutputStreamDataHolder("companyTriggerInternalStream",
                                                                                  cmpTgrIntPubStrategies, false);
        List<OutputStreamDataHolder> app1Output = new ArrayList<>();
        app1Output.add(filStreamOutput);
        app1Output.add(cmpTgrIntStreamOutput);
        SiddhiQueryGroup group1 = new SiddhiQueryGroup("Siddhi-App-001", 2, app1, app1Input, app1Output);


        String app2 = "@App:name('${appName}') ${filteredStockStream}\n"
                + "define stream filteredStockStream (symbol string, price float, quantity int, tier string); "
                + "${companyTriggerInternalStream}\n"
                + "define stream companyTriggerInternalStream (symbol string);\n"
                + "${triggeredAvgStream}\n"
                + "define stream triggeredAvgStream (symbol string, avgPrice double, quantity int);\n"
                + "@info(name='query2')\n"
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
                + "End; \n";

        SubscriptionStrategyDataHolder app2FilStreamSubscription = new SubscriptionStrategyDataHolder(2,
                                                                                                      TransportStrategy.FIELD_GROUPING);
        app2FilStreamSubscription.setPartitionKey("symbol");
        SubscriptionStrategyDataHolder app2cmpTgrIntStreamSubscription = new SubscriptionStrategyDataHolder(1,
                                                                                                            TransportStrategy.ALL);
        InputStreamDataHolder filStreamInput = new InputStreamDataHolder("filteredStockStream",
                                                                         app2FilStreamSubscription, false);
        InputStreamDataHolder cmpTgrIntStreamInput = new InputStreamDataHolder("companyTriggerInternalStream",
                                                                               app2cmpTgrIntStreamSubscription, false);
        List<InputStreamDataHolder> app2Input = new ArrayList<>();
        app2Input.add(filStreamInput);
        app2Input.add(cmpTgrIntStreamInput);

        PublishingStrategyDataHolder trgAvgStreamPub1 = new PublishingStrategyDataHolder("003", TransportStrategy
                .ROUND_ROBIN, 1);
        List<PublishingStrategyDataHolder> trgPubPubStrategies = new ArrayList<>(3);
        trgPubPubStrategies.add(trgAvgStreamPub1);
        OutputStreamDataHolder trgPubStreamOutput = new OutputStreamDataHolder("triggeredAvgStream",
                                                                               trgPubPubStrategies,
                                                                               false);

        List<OutputStreamDataHolder> app2Output = new ArrayList<>();
        app2Output.add(trgPubStreamOutput);
        SiddhiQueryGroup group2 = new SiddhiQueryGroup("Siddhi-App-002", 2, app2, app2Input, app2Output);

        List<SiddhiQueryGroup> queryGroupList = new ArrayList<>();
        queryGroupList.add(group1);
        queryGroupList.add(group2);
        SiddhiTopology topology = new SiddhiTopology("Siddhi-App", queryGroupList);

        SiddhiAppCreator appCreator = new SPSiddhiAppCreator();
        List<DeployableSiddhiQueryGroup> resultList = appCreator.createApps(topology);

        for (DeployableSiddhiQueryGroup group : resultList) {
            for (String query : group.getQueryList()) {
                log.info(query + "\n");
            }
        }

    }

    @Test
    public void testMultiplePartitionSubscription() {
        String app1 = "@App:name('${appName}') @source(type='http', receiver"
                + ".url='http://localhost:9055/endpoints/stockQuote', @map(type='xml')) \n"
                + "Define stream stockStream(symbol string, price float, quantity int, tier string); \n"
                + "@source(type='http', receiver.url='http://localhost:9055/endpoints/trigger', @map(type='xml'))\n"
                + "Define stream companyTriggerStream(symbol string);\n"
                + "${filteredStockStream} define stream filteredStockStream (symbol string, price float, quantity "
                + "int, tier string); \n"
                + "${companyTriggerInternalStream}\n"
                + "Define stream companyTriggerInternalStream (symbol string); \n"
                + "@info(name = 'query1')\n"
                + "From stockStream[price > 100] \n"
                + "Select * \n"
                + "Insert into filteredStockStream; \n"
                + "@info(name = 'query2')\n"
                + "From companyTriggerStream \n"
                + "select * \n"
                + "insert into companyTriggerInternalStream;\n";
        SubscriptionStrategyDataHolder app1StockStreamSubscription = new SubscriptionStrategyDataHolder(1,
                                                                                                        TransportStrategy.ROUND_ROBIN);
        SubscriptionStrategyDataHolder app1cmpTgrStreamSubscription = new SubscriptionStrategyDataHolder(1,
                                                                                                         TransportStrategy.ROUND_ROBIN);
        InputStreamDataHolder stockStreamInput = new InputStreamDataHolder("stockStream",
                                                                           app1StockStreamSubscription, true);
        InputStreamDataHolder cmpTgrStreamInput = new InputStreamDataHolder("companyTriggerStream",
                                                                            app1cmpTgrStreamSubscription, true);
        List<InputStreamDataHolder> app1Input = new ArrayList<>();
        app1Input.add(stockStreamInput);
        app1Input.add(cmpTgrStreamInput);

        PublishingStrategyDataHolder filStreamPub1 = new PublishingStrategyDataHolder("002", TransportStrategy
                .FIELD_GROUPING, "symbol", 2);
        PublishingStrategyDataHolder filStreamPub2 = new PublishingStrategyDataHolder("005", TransportStrategy
                .FIELD_GROUPING, "symbol", 5);
        PublishingStrategyDataHolder filStreamPub3 = new PublishingStrategyDataHolder("004", TransportStrategy
                .ROUND_ROBIN, 1);
        List<PublishingStrategyDataHolder> filPubStrategies = new ArrayList<>(3);
        filPubStrategies.add(filStreamPub1);
        filPubStrategies.add(filStreamPub2);
        filPubStrategies.add(filStreamPub3);
        OutputStreamDataHolder filStreamOutput = new OutputStreamDataHolder("filteredStockStream", filPubStrategies,
                                                                            false);

        PublishingStrategyDataHolder cmpTgrIntStreamPub1 = new PublishingStrategyDataHolder("002", TransportStrategy
                .ALL, 1);
        List<PublishingStrategyDataHolder> cmpTgrIntPubStrategies = new ArrayList<>(1);
        cmpTgrIntPubStrategies.add(cmpTgrIntStreamPub1);
        OutputStreamDataHolder cmpTgrIntStreamOutput = new OutputStreamDataHolder("companyTriggerInternalStream",
                                                                                  cmpTgrIntPubStrategies, false);
        List<OutputStreamDataHolder> app1Output = new ArrayList<>();
        app1Output.add(filStreamOutput);
        app1Output.add(cmpTgrIntStreamOutput);
        SiddhiQueryGroup group1 = new SiddhiQueryGroup("Siddhi-App-001", 2, app1, app1Input, app1Output);


        String app2 = "@App:name(${appName}) ${filteredStockStream}\n"
                + "define stream filteredStockStream (symbol string, price float, quantity int, tier string); "
                + "${companyTriggerInternalStream}\n"
                + "define stream companyTriggerInternalStream (symbol string);\n"
                + "${triggeredAvgStream}\n"
                + "define stream triggeredAvgStream (symbol string, avgPrice double, quantity int);\n"
                + "@info(name='query2')\n"
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
                + "End; \n";

        SubscriptionStrategyDataHolder app2FilStreamSubscription = new SubscriptionStrategyDataHolder(5,
                                                                                                      TransportStrategy.FIELD_GROUPING);
        app2FilStreamSubscription.setPartitionKey("symbol");
        SubscriptionStrategyDataHolder app2cmpTgrIntStreamSubscription = new SubscriptionStrategyDataHolder(1,
                                                                                                            TransportStrategy.ALL);
        InputStreamDataHolder filStreamInput = new InputStreamDataHolder("filteredStockStream",
                                                                         app2FilStreamSubscription, false);
        InputStreamDataHolder cmpTgrIntStreamInput = new InputStreamDataHolder("companyTriggerInternalStream",
                                                                               app2cmpTgrIntStreamSubscription, false);
        List<InputStreamDataHolder> app2Input = new ArrayList<>();
        app2Input.add(filStreamInput);
        app2Input.add(cmpTgrIntStreamInput);

        PublishingStrategyDataHolder trgAvgStreamPub1 = new PublishingStrategyDataHolder("003", TransportStrategy
                .ROUND_ROBIN, 1);
        List<PublishingStrategyDataHolder> trgPubPubStrategies = new ArrayList<>(3);
        trgPubPubStrategies.add(trgAvgStreamPub1);
        OutputStreamDataHolder trgPubStreamOutput = new OutputStreamDataHolder("triggeredAvgStream",
                                                                               trgPubPubStrategies,
                                                                               false);

        List<OutputStreamDataHolder> app2Output = new ArrayList<>();
        app2Output.add(trgPubStreamOutput);
        SiddhiQueryGroup group2 = new SiddhiQueryGroup("Siddhi-App-002", 2, app2, app2Input, app2Output);

        List<SiddhiQueryGroup> queryGroupList = new ArrayList<>();
        queryGroupList.add(group1);
        queryGroupList.add(group2);
        SiddhiTopology topology = new SiddhiTopology("Siddhi-App", queryGroupList);

        SiddhiAppCreator appCreator = new SPSiddhiAppCreator();
        List<DeployableSiddhiQueryGroup> resultList = appCreator.createApps(topology);

        for (DeployableSiddhiQueryGroup group : resultList) {
            for (String query : group.getQueryList()) {
                log.info(query + "\n");
            }
        }

    }

    @Test
    public void testPartitionRoundRobinSubscription() {
        String app1 = "@App:name(${appName}) @source(type='http', receiver"
                + ".url='http://localhost:9055/endpoints/stockQuote', @map(type='xml')) \n"
                + "Define stream stockStream(symbol string, price float, quantity int, tier string); \n"
                + "@source(type='http', receiver.url='http://localhost:9055/endpoints/trigger', @map(type='xml'))\n"
                + "Define stream companyTriggerStream(symbol string);\n"
                + "${filteredStockStream} define stream filteredStockStream (symbol string, price float, quantity "
                + "int, tier string); \n"
                + "${companyTriggerInternalStream}\n"
                + "Define stream companyTriggerInternalStream (symbol string); \n"
                + "@info(name = 'query1')\n"
                + "From stockStream[price > 100] \n"
                + "Select * \n"
                + "Insert into filteredStockStream; \n"
                + "@info(name = 'query2')\n"
                + "From companyTriggerStream \n"
                + "select * \n"
                + "insert into companyTriggerInternalStream;\n";
        SubscriptionStrategyDataHolder app1StockStreamSubscription = new SubscriptionStrategyDataHolder(1,
                                                                                                        TransportStrategy.ROUND_ROBIN);
        SubscriptionStrategyDataHolder app1cmpTgrStreamSubscription = new SubscriptionStrategyDataHolder(1,
                                                                                                         TransportStrategy.ROUND_ROBIN);
        InputStreamDataHolder stockStreamInput = new InputStreamDataHolder("stockStream",
                                                                           app1StockStreamSubscription, true);
        InputStreamDataHolder cmpTgrStreamInput = new InputStreamDataHolder("companyTriggerStream",
                                                                            app1cmpTgrStreamSubscription, true);
        List<InputStreamDataHolder> app1Input = new ArrayList<>();
        app1Input.add(stockStreamInput);
        app1Input.add(cmpTgrStreamInput);

        PublishingStrategyDataHolder filStreamPub1 = new PublishingStrategyDataHolder("002", TransportStrategy
                .FIELD_GROUPING, "symbol", 2);
        PublishingStrategyDataHolder filStreamPub2 = new PublishingStrategyDataHolder("005", TransportStrategy
                .FIELD_GROUPING, "symbol", 5);
        PublishingStrategyDataHolder filStreamPub3 = new PublishingStrategyDataHolder("004", TransportStrategy
                .ROUND_ROBIN, 1);
        List<PublishingStrategyDataHolder> filPubStrategies = new ArrayList<>(3);
        filPubStrategies.add(filStreamPub1);
        filPubStrategies.add(filStreamPub2);
        filPubStrategies.add(filStreamPub3);
        OutputStreamDataHolder filStreamOutput = new OutputStreamDataHolder("filteredStockStream", filPubStrategies,
                                                                            false);

        PublishingStrategyDataHolder cmpTgrIntStreamPub1 = new PublishingStrategyDataHolder("002", TransportStrategy
                .ROUND_ROBIN, 1);
        List<PublishingStrategyDataHolder> cmpTgrIntPubStrategies = new ArrayList<>(1);
        filPubStrategies.add(cmpTgrIntStreamPub1);
        OutputStreamDataHolder cmpTgrIntStreamOutput = new OutputStreamDataHolder("companyTriggerInternalStream",
                                                                                  cmpTgrIntPubStrategies, false);
        List<OutputStreamDataHolder> app1Output = new ArrayList<>();
        app1Output.add(filStreamOutput);
        app1Output.add(cmpTgrIntStreamOutput);
        SiddhiQueryGroup group1 = new SiddhiQueryGroup("Siddhi-App-001", 2, app1, app1Input, app1Output);


        String app2 = "@App:name(${appName}) ${filteredStockStream}\n"
                + "define stream filteredStockStream (symbol string, price float, quantity int, tier string); "
                + "${companyTriggerInternalStream}\n"
                + "define stream companyTriggerInternalStream (symbol string);\n"
                + "${triggeredAvgStream}\n"
                + "define stream triggeredAvgStream (symbol string, avgPrice double, quantity int);\n"
                + "@info(name='query2')\n"
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
                + "End; \n";

        SubscriptionStrategyDataHolder app2FilStreamSubscription = new SubscriptionStrategyDataHolder(5,
                                                                                                      TransportStrategy.FIELD_GROUPING);
        app2FilStreamSubscription.setPartitionKey("symbol");
        SubscriptionStrategyDataHolder app2cmpTgrIntStreamSubscription = new SubscriptionStrategyDataHolder(1,
                                                                                                            TransportStrategy.ROUND_ROBIN);
        InputStreamDataHolder filStreamInput = new InputStreamDataHolder("filteredStockStream",
                                                                         app2FilStreamSubscription, false);
        InputStreamDataHolder cmpTgrIntStreamInput = new InputStreamDataHolder("companyTriggerInternalStream",
                                                                               app2cmpTgrIntStreamSubscription, false);
        List<InputStreamDataHolder> app2Input = new ArrayList<>();
        app2Input.add(filStreamInput);
        app2Input.add(cmpTgrIntStreamInput);

        PublishingStrategyDataHolder trgAvgStreamPub1 = new PublishingStrategyDataHolder("003", TransportStrategy
                .ROUND_ROBIN, 1);
        List<PublishingStrategyDataHolder> trgPubPubStrategies = new ArrayList<>(3);
        trgPubPubStrategies.add(trgAvgStreamPub1);
        OutputStreamDataHolder trgPubStreamOutput = new OutputStreamDataHolder("triggeredAvgStream",
                                                                               trgPubPubStrategies,
                                                                               false);

        List<OutputStreamDataHolder> app2Output = new ArrayList<>();
        app2Output.add(trgPubStreamOutput);
        SiddhiQueryGroup group2 = new SiddhiQueryGroup("Siddhi-App-002", 2, app2, app2Input, app2Output);

        List<SiddhiQueryGroup> queryGroupList = new ArrayList<>();
        queryGroupList.add(group1);
        queryGroupList.add(group2);
        SiddhiTopology topology = new SiddhiTopology("Siddhi-App", queryGroupList);

        SiddhiAppCreator appCreator = new SPSiddhiAppCreator();
        List<DeployableSiddhiQueryGroup> resultList = appCreator.createApps(topology);

        for (DeployableSiddhiQueryGroup group : resultList) {
            for (String query : group.getQueryList()) {
                log.info(query + "\n");
            }
        }

    }
}
