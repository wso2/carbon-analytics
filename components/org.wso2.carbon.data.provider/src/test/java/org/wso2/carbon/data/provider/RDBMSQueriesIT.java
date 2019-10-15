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

package org.wso2.carbon.data.provider;

import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.data.provider.bean.DataModel;
import org.wso2.carbon.data.provider.bean.DataSetMetadata;
import org.wso2.carbon.data.provider.endpoint.DataProviderEndPoint;
import org.wso2.carbon.data.provider.rdbms.RDBMSBatchDataProvider;
import org.wso2.carbon.data.provider.rdbms.RDBMSStreamingDataProvider;
import org.wso2.carbon.data.provider.rdbms.bean.RDBMSDataProviderConfBean;
import org.wso2.carbon.data.provider.utils.DataProviderValueHolder;
import org.wso2.carbon.data.provider.utils.RDBMSTableTestUtils;
import org.wso2.carbon.data.provider.utils.WebSocketClient;
import org.wso2.carbon.database.query.manager.config.Queries;
import org.wso2.carbon.datasource.core.api.DataSourceService;
import org.wso2.carbon.datasource.core.beans.DataSourceDefinition;
import org.wso2.carbon.datasource.core.exception.DataSourceException;
import org.wso2.msf4j.MicroservicesRunner;

import javax.net.ssl.SSLException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RDBMSQueriesIT {
    private static final Log log = LogFactory.getLog(RDBMSQueriesIT.class);
    private final int sleepTime = 10000;
    private final String host = "localhost";
    private final String port = "9090";
    private String dataproviderUrl = "ws://" + host + ":" + port + "/data-provider";

    @BeforeClass
    public void startTest() {
        log.info("== RDBMS Queries tests started ==");
        MicroservicesRunner microservicesRunner = new MicroservicesRunner();
        microservicesRunner.deployWebSocketEndpoint(new DataProviderEndPoint());
        microservicesRunner.start();
    }

    @AfterClass
    public static void shutdown() {
        log.info("== RDBMS Queries tests completed ==");
    }

    @BeforeMethod
    public void init() {
        try {
            RDBMSTableTestUtils.initDatabaseTable();
            RDBMSDataProviderConfBean rdbmsDataProviderConfBean = new RDBMSDataProviderConfBean();
            DataProviderValueHolder.getDataProviderHelper().setDataProvider(RDBMSBatchDataProvider.class
                    .getSimpleName(), new RDBMSBatchDataProvider());
            DataProviderValueHolder.getDataProviderHelper().setDataProvider(RDBMSStreamingDataProvider.class
                    .getSimpleName(), new RDBMSStreamingDataProvider());
            DataSourceService dataSourceService = new DataSourceService() {
                public Object getDataSource(String s) throws DataSourceException {
                    return RDBMSTableTestUtils.getDataSource();
                }

                public Object createDataSource(DataSourceDefinition dataSourceDefinition) throws DataSourceException {
                    return RDBMSTableTestUtils.getDataSource();
                }
            };
            Queries queries = new Queries();
            queries.setVersion("default");
            queries.setType("H2");
            Map<String, String> queriesMap = new HashMap<>();
            queriesMap.put("record_limit", "{{CUSTOM_QUERY}} ORDER BY {{INCREMENTAL_COLUMN}} DESC " +
                    "LIMIT {{LIMIT_VALUE}}");
            queriesMap.put("record_greater_than", "SELECT * FROM ({{CUSTOM_QUERY}} ORDER BY {{INCREMENTAL_COLUMN}} " +
                    "DESC ) WHERE {{INCREMENTAL_COLUMN}} > {{LAST_RECORD_VALUE}}");
            queriesMap.put("total_record_count", "SELECT COUNT(*) FROM {{TABLE_NAME}}");
            queriesMap.put("record_delete", "DELETE TOP {{LIMIT_VALUE}} FROM {{TABLE_NAME}}");
            queries.setMappings(queriesMap);
            ArrayList<Queries> arrayList = new ArrayList<>();
            arrayList.add(queries);
            rdbmsDataProviderConfBean.setQueries(arrayList);
            ConfigProvider configProvider = new ConfigProvider() {
                @Override
                public <T> T getConfigurationObject(Class<T> aClass) throws ConfigurationException {
                    return (T) rdbmsDataProviderConfBean;
                }

                @Override
                public Object getConfigurationObject(String s) throws ConfigurationException {
                    return rdbmsDataProviderConfBean;
                }

                @Override
                public <T> T getConfigurationObject(String s, Class<T> aClass) throws ConfigurationException {
                    return (T) rdbmsDataProviderConfBean;
                }

                @Override
                public <T> ArrayList<T> getConfigurationObjectList(String s, Class<T> aClass) throws ConfigurationException {
                    return null;
                }
            };
            DataProviderValueHolder.getDataProviderHelper().setDataSourceService(dataSourceService);
            DataProviderValueHolder.getDataProviderHelper().setConfigProvider(configProvider);
            DataProviderValueHolder.getDataProviderHelper().setDataProviderAuthorizer(
                    DefaultDataProviderAuthorizer.class.getName(),
                    new DefaultDataProviderAuthorizer());
        } catch (SQLException e) {
            log.info("Test case ignored due to " + e.getMessage());
        }
    }

    @Test(description = "RDBMS Batch Data provider table test.")
    public void batchDataProviderTest() throws InterruptedException, SQLException, URISyntaxException, SSLException {
        WebSocketClient webSocketClient = new WebSocketClient(dataproviderUrl);
        Assert.assertTrue(webSocketClient.handhshake());

        String message = "{" +
                "\"providerName\": \"RDBMSBatchDataProvider\"," +
                "\"dataProviderConfiguration\": {" +
                "\"datasourceName\": \"DEMO_DB\"," +
                "\"queryData\": {\"queryFunction\":\"this.getQuery = function (username,country){return \\\"Select *" +
                " from TRANSACTIONS_TABLE where country = '\\\"+country+\\\"'\\\";}\",\"query\":" +
                "\"select * from Foo_Table\",\"customWidgetInputs\":[{\"name\":\"country\",\"defaultValue\":" +
                "\"Ireland\"}],\"systemWidgetInputs\":[{\"name\":\"username\",\"defaultValue\":\"admin\"}]}," +
                "\"tableName\": \"Foo_Table\"," +
                "\"incrementalColumn\": \"recipe_id\"," +
                "\"publishingInterval\": 1000," +
                "\"purgingInterval\": 1000," +
                "\"publishingLimit\": 1000," +
                "\"purgingLimit\": 1000," +
                "\"isPurgingEnable\": false" +
                "}," +
                "\"topic\": \"test-topic\"," +
                "\"action\": \"subscribe\"" +
                "}";
        DataSetMetadata dataSetMetadata = new DataSetMetadata(2);
        dataSetMetadata.put(0, "RECIPE_ID", DataSetMetadata.Types.LINEAR);
        dataSetMetadata.put(1, "RECIPE_NAME", DataSetMetadata.Types.ORDINAL);

        String actualMessage;
        webSocketClient.sendText(message);
        Thread.sleep(sleepTime);
        actualMessage = webSocketClient.getTextReceived();

        DataModel dataModel = new Gson().fromJson(actualMessage, DataModel.class);
        Object[][] data = {{3.0, "Grilled Cheese"}, {2.0, "Tomato Soup"}, {1.0, "Tacos"}};
        DataModel expected = new DataModel(dataSetMetadata, data, -1, "test-topic");

        Assert.assertNotNull(expected);
        Assert.assertEquals(dataModel.getTopic(), expected.getTopic());
        Assert.assertEquals(dataModel.getData(), expected.getData());
        for (int i = 0; i < dataModel.getMetadata().getNames().length; i++) {
            Assert.assertEquals(dataModel.getMetadata().getNames()[i].equalsIgnoreCase(expected.getMetadata()
                    .getNames()[i]), true);
        }
        Assert.assertEquals(dataModel.getMetadata().getTypes(), expected.getMetadata().getTypes());
        Assert.assertEquals(dataModel.getLastRow(), expected.getLastRow());
        webSocketClient.shutDown();
    }

    @Test(description = "RDBMS Stream Data provider table test.", dependsOnMethods = "batchDataProviderTest")
    public void streamDataProviderTest() throws InterruptedException, SQLException, URISyntaxException, SSLException {
        WebSocketClient webSocketClient = new WebSocketClient(dataproviderUrl);
        Assert.assertTrue(webSocketClient.handhshake());

        String message = "{" +
                "\"providerName\": \"RDBMSStreamingDataProvider\"," +
                "\"dataProviderConfiguration\": {" +
                "\"datasourceName\": \"DEMO_DB\"," +
                "\"queryData\": {\"queryFunction\":\"this.getQuery = function (username,country){" +
                "return \\\"Select * from TRANSACTIONS_TABLE where country = '\\\"+country+\\\"'\\\";}\",\"query\":" +
                "\"select * from Foo_Table\",\"customWidgetInputs\":[{\"name\":\"country\",\"defaultValue\":" +
                "\"Ireland\"}],\"systemWidgetInputs\":[{\"name\":\"username\",\"defaultValue\":\"admin\"}]}," +
                "\"tableName\": \"Foo_Table\"," +
                "\"incrementalColumn\": \"recipe_id\"," +
                "\"publishingInterval\": 1000," +
                "\"purgingInterval\": 1000," +
                "\"publishingLimit\": 1000," +
                "\"purgingLimit\": 1000," +
                "\"isPurgingEnable\": false" +
                "}," +
                "\"topic\": \"test-topic\"," +
                "\"action\": \"subscribe\"" +
                "}";
        DataSetMetadata dataSetMetadata = new DataSetMetadata(2);
        dataSetMetadata.put(0, "RECIPE_ID", DataSetMetadata.Types.LINEAR);
        dataSetMetadata.put(1, "RECIPE_NAME", DataSetMetadata.Types.ORDINAL);

        String actualMessage;
        webSocketClient.sendText(message);
        Thread.sleep(sleepTime);
        actualMessage = webSocketClient.getTextReceived();

        Object[][] data = {{3.0, "Grilled Cheese"}, {2.0, "Tomato Soup"}, {1.0, "Tacos"}};
        DataModel expected = new DataModel(dataSetMetadata, data, -1, "test-topic");

        DataModel dataModel = new Gson().fromJson(actualMessage, DataModel.class);
        Assert.assertEquals(dataModel.getTopic(), expected.getTopic());
        Assert.assertEquals(dataModel.getData(), expected.getData());
        for (int i = 0; i < dataModel.getMetadata().getNames().length; i++) {
            Assert.assertEquals(dataModel.getMetadata().getNames()[i].equalsIgnoreCase(expected.getMetadata()
                    .getNames()[i]), true);
        }
        Assert.assertEquals(dataModel.getMetadata().getTypes(), expected.getMetadata().getTypes());
        Assert.assertEquals(dataModel.getLastRow(), expected.getLastRow());
        webSocketClient.shutDown();
    }

    @Test(description = "RDBMS Stream Data provider table test2.", dependsOnMethods = "streamDataProviderTest")
    public void streamDataProviderTest2() throws InterruptedException, SQLException, URISyntaxException, SSLException {
        WebSocketClient webSocketClient = new WebSocketClient(dataproviderUrl);
        Assert.assertTrue(webSocketClient.handhshake());

        String message = "{" +
                "\"providerName\": \"RDBMSStreamingDataProvider\"," +
                "\"dataProviderConfiguration\": {" +
                "\"datasourceName\": \"DEMO_DB\"," +
                "\"queryData\": {\"queryFunction\":\"this.getQuery = function (username,country){" +
                "return \\\"Select * from TRANSACTIONS_TABLE where country = '\\\"+country+\\\"'\\\";}\",\"query\":" +
                "\"select * from Foo_Table\",\"customWidgetInputs\":[{\"name\":\"country\",\"defaultValue\":" +
                "\"Ireland\"}],\"systemWidgetInputs\":[{\"name\":\"username\",\"defaultValue\":\"admin\"}]}," +
                "\"tableName\": \"Foo_Table\"," +
                "\"incrementalColumn\": \"recipe_id\"," +
                "\"publishingInterval\": 3," +
                "\"purgingInterval\": 1000," +
                "\"publishingLimit\": 1000," +
                "\"purgingLimit\": 1000," +
                "\"isPurgingEnable\": false" +
                "}," +
                "\"topic\": \"test-topic\"," +
                "\"action\": \"subscribe\"" +
                "}";
        DataSetMetadata dataSetMetadata = new DataSetMetadata(2);
        dataSetMetadata.put(0, "RECIPE_ID", DataSetMetadata.Types.LINEAR);
        dataSetMetadata.put(1, "RECIPE_NAME", DataSetMetadata.Types.ORDINAL);

        String actualMessage1;
        String actualMessage2;

        webSocketClient.sendText(message);
        Thread.sleep(sleepTime);
        actualMessage1 = webSocketClient.getTextReceived();
        Assert.assertNotNull(actualMessage1);

        RDBMSTableTestUtils.insertRecords(4, "Pizza");
        Thread.sleep(sleepTime);
        actualMessage2 = webSocketClient.getTextReceived();

        DataModel dataModel = new Gson().fromJson(actualMessage2, DataModel.class);

        Object[][] data = {{4.0, "Pizza"}};
        DataModel expected = new DataModel(dataSetMetadata, data, -1, "test-topic");
        Assert.assertEquals(dataModel.getTopic(), expected.getTopic());
        Assert.assertEquals(dataModel.getData(), expected.getData());
        for (int i = 0; i < dataModel.getMetadata().getNames().length; i++) {
            Assert.assertEquals(dataModel.getMetadata().getNames()[i].equalsIgnoreCase(expected.getMetadata()
                    .getNames()[i]), true);
        }
        Assert.assertEquals(dataModel.getMetadata().getTypes(), expected.getMetadata().getTypes());
        Assert.assertEquals(dataModel.getLastRow(), expected.getLastRow());
        webSocketClient.shutDown();
    }

    @Test(description = "RDBMS Data provider data purging test.", dependsOnMethods = "streamDataProviderTest2")
    public void dataBatchProviderPurgingTest() throws InterruptedException, SQLException, URISyntaxException,
            SSLException {
        WebSocketClient webSocketClient = new WebSocketClient(dataproviderUrl);
        Assert.assertTrue(webSocketClient.handhshake());

        String message = "{" +
                "\"providerName\": \"RDBMSBatchDataProvider\"," +
                "\"dataProviderConfiguration\": {" +
                "\"datasourceName\": \"DEMO_DB\"," +
                "\"queryData\": {\"queryFunction\":\"this.getQuery = function (username,country){" +
                "return \\\"Select * from TRANSACTIONS_TABLE where country = '\\\"+country+\\\"'\\\";}\",\"query\":" +
                "\"select * from Foo_Table\",\"customWidgetInputs\":[{\"name\":\"country\",\"defaultValue\":" +
                "\"Ireland\"}],\"systemWidgetInputs\":[{\"name\":\"username\",\"defaultValue\":\"admin\"}]}," +
                "\"tableName\": \"Foo_Table\"," +
                "\"incrementalColumn\": \"recipe_id\"," +
                "\"publishingInterval\": 3," +
                "\"purgingInterval\": 1000," +
                "\"publishingLimit\": 1000," +
                "\"purgingLimit\": 2," +
                "\"isPurgingEnable\": true" +
                "}," +
                "\"topic\": \"test-topic\"," +
                "\"action\": \"subscribe\"" +
                "}";
        DataSetMetadata dataSetMetadata = new DataSetMetadata(2);
        dataSetMetadata.put(0, "RECIPE_ID", DataSetMetadata.Types.LINEAR);
        dataSetMetadata.put(1, "RECIPE_NAME", DataSetMetadata.Types.ORDINAL);

        webSocketClient.sendText(message);
        Thread.sleep(sleepTime);
        String actualMessage = webSocketClient.getTextReceived();
        Assert.assertNotNull(actualMessage);

        Object[][] data = {{3.0, "Grilled Cheese"}, {2.0, "Tomato Soup"}};
        DataModel expected = new DataModel(dataSetMetadata, data, -1, "test-topic");
        DataModel dataModel = new Gson().fromJson(actualMessage, DataModel.class);
        Assert.assertEquals(dataModel.getTopic(), expected.getTopic());
        Assert.assertEquals(dataModel.getData(), expected.getData());
        for (int i = 0; i < dataModel.getMetadata().getNames().length; i++) {
            Assert.assertEquals(dataModel.getMetadata().getNames()[i].equalsIgnoreCase(expected.getMetadata()
                    .getNames()[i]), true);
        }
        Assert.assertEquals(dataModel.getMetadata().getTypes(), expected.getMetadata().getTypes());
        Assert.assertEquals(dataModel.getLastRow(), expected.getLastRow());
        webSocketClient.shutDown();
    }
}
