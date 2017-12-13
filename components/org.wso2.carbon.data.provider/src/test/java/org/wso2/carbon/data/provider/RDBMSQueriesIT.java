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
import org.awaitility.Awaitility;
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
import org.wso2.carbon.database.query.manager.config.Queries;
import org.wso2.carbon.datasource.core.api.DataSourceService;
import org.wso2.carbon.datasource.core.beans.DataSourceDefinition;
import org.wso2.carbon.datasource.core.exception.DataSourceException;

import javax.websocket.CloseReason;
import javax.websocket.EncodeException;
import javax.websocket.Extension;
import javax.websocket.MessageHandler;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URI;
import java.nio.ByteBuffer;
import java.security.Principal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class RDBMSQueriesIT {
    private static final Log log = LogFactory.getLog(RDBMSQueriesIT.class);
    private Session session = null;
    private RemoteEndpoint.Basic basic = null;
    private ResponseCallBack responseCallBack;
    private ResponseCallBackListener responseCallBackListener;

    @BeforeClass
    public static void startTest() {
        log.info("== RDBMS Queries tests started ==");
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
            Map queriesMap = new HashMap();
            queriesMap.put("record_delete", "DELETE TOP {{LIMIT_VALUE}} FROM {{TABLE_NAME}}" +
                    " {{INCREMENTAL_COLUMN}} ASC LIMIT {{LIMIT_VALUE}}");
            queriesMap.put("total_record_count", "SELECT COUNT(*) FROM {{TABLE_NAME}}");
            queriesMap.put("record_limit", " ORDER BY {{INCREMENTAL_COLUMN}} ASC LIMIT {{LIMIT_VALUE}}");
            queriesMap.put("record_greater_than", " WHERE {{INCREMENTAL_COLUMN}} > {{LAST_RECORD_VALUE}}");
            queries.setMappings(queriesMap);
            ArrayList arrayList = new ArrayList();
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
            };

            basic = new RemoteEndpoint.Basic() {
                @Override
                public void sendText(String s) throws IOException {
                    if (rdbmsDataProviderConfBean != null) {
                        responseCallBackListener.responseListener(s);
                    }
                }

                @Override
                public void sendBinary(ByteBuffer byteBuffer) throws IOException {

                }

                @Override
                public void sendText(String s, boolean b) throws IOException {

                }

                @Override
                public void sendBinary(ByteBuffer byteBuffer, boolean b) throws IOException {

                }

                @Override
                public OutputStream getSendStream() throws IOException {
                    return null;
                }

                @Override
                public Writer getSendWriter() throws IOException {
                    return null;
                }

                @Override
                public void sendObject(Object o) throws IOException, EncodeException {

                }

                @Override
                public void setBatchingAllowed(boolean b) throws IOException {

                }

                @Override
                public boolean getBatchingAllowed() {
                    return false;
                }

                @Override
                public void flushBatch() throws IOException {

                }

                @Override
                public void sendPing(ByteBuffer byteBuffer) throws IOException, IllegalArgumentException {

                }

                @Override
                public void sendPong(ByteBuffer byteBuffer) throws IOException, IllegalArgumentException {

                }
            };
            session = new Session() {
                @Override
                public WebSocketContainer getContainer() {
                    return null;
                }

                @Override
                public void addMessageHandler(MessageHandler messageHandler) throws IllegalStateException {

                }

                @Override
                public <T> void addMessageHandler(Class<T> aClass, MessageHandler.Whole<T> whole) {

                }

                @Override
                public <T> void addMessageHandler(Class<T> aClass, MessageHandler.Partial<T> partial) {

                }

                @Override
                public Set<MessageHandler> getMessageHandlers() {
                    return null;
                }

                @Override
                public void removeMessageHandler(MessageHandler messageHandler) {

                }

                @Override
                public String getProtocolVersion() {
                    return null;
                }

                @Override
                public String getNegotiatedSubprotocol() {
                    return null;
                }

                @Override
                public List<Extension> getNegotiatedExtensions() {
                    return null;
                }

                @Override
                public boolean isSecure() {
                    return false;
                }

                @Override
                public boolean isOpen() {
                    return false;
                }

                @Override
                public long getMaxIdleTimeout() {
                    return 0;
                }

                @Override
                public void setMaxIdleTimeout(long l) {

                }

                @Override
                public void setMaxBinaryMessageBufferSize(int i) {

                }

                @Override
                public int getMaxBinaryMessageBufferSize() {
                    return 0;
                }

                @Override
                public void setMaxTextMessageBufferSize(int i) {

                }

                @Override
                public int getMaxTextMessageBufferSize() {
                    return 0;
                }

                @Override
                public RemoteEndpoint.Async getAsyncRemote() {
                    return null;
                }

                @Override
                public RemoteEndpoint.Basic getBasicRemote() {
                    return basic;
                }

                @Override
                public String getId() {
                    return "foo-session";
                }

                @Override
                public void close() throws IOException {

                }

                @Override
                public void close(CloseReason closeReason) throws IOException {

                }

                @Override
                public URI getRequestURI() {
                    return null;
                }

                @Override
                public Map<String, List<String>> getRequestParameterMap() {
                    return null;
                }

                @Override
                public String getQueryString() {
                    return null;
                }

                @Override
                public Map<String, String> getPathParameters() {
                    return null;
                }

                @Override
                public Map<String, Object> getUserProperties() {
                    return null;
                }

                @Override
                public Principal getUserPrincipal() {
                    return null;
                }

                @Override
                public Set<Session> getOpenSessions() {
                    return null;
                }
            };
            DataProviderValueHolder.getDataProviderHelper().setDataSourceService(dataSourceService);
            DataProviderValueHolder.getDataProviderHelper().setConfigProvider(configProvider);
        } catch (SQLException e) {
            log.info("Test case ignored due to " + e.getMessage());
        }
    }

    @Test(description = "RDBMS Batch Data provider table test.")
    public void batchDataProviderTest() throws InterruptedException, SQLException {
        DataProviderEndPoint.getSessionMap().put(session.getId(), session);
        DataProviderEndPoint dataProviderEndPoint = new DataProviderEndPoint();
        String message = "{" +
                "\"providerName\": \"RDBMSBatchDataProvider\"," +
                "\"dataProviderConfiguration\": {" +
                "\"datasourceName\": \"DEMO_DB\"," +
                "\"query\": \"select * from Foo_Table\"," +
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
        Object[][] data = {{1.0, "Tacos"}, {2.0, "Tomato Soup"}, {3.0, "Grilled Cheese"}};
        DataModel expected = new DataModel(dataSetMetadata, data, -1, "test-topic");
        final String[] actualMessage = new String[1];
        responseCallBack = new ResponseCallBack() {
            int count = 0;

            @Override
            void responseMessage(String message) {
                count++;
                if (count == 1) {
                    log.info("Batch Data Provider Test Message: " + message);
                    actualMessage[0] = message;
                }
            }
        };
        responseCallBackListener = new ResponseCallBackListener(responseCallBack);
        dataProviderEndPoint.onMessage(message, session);
        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> actualMessage[0] != null);
        DataModel dataModel = new Gson().fromJson(actualMessage[0], DataModel.class);
        Assert.assertEquals(dataModel.getTopic(), expected.getTopic());
        Assert.assertEquals(dataModel.getData(), expected.getData());
        for (int i = 0; i < dataModel.getMetadata().getNames().length; i++) {
            Assert.assertEquals(dataModel.getMetadata().getNames()[i].equalsIgnoreCase(expected.getMetadata()
                    .getNames()[i]), true);
        }
        Assert.assertEquals(dataModel.getMetadata().getTypes(), expected.getMetadata().getTypes());
        Assert.assertEquals(dataModel.getLastRow(), expected.getLastRow());
        dataProviderEndPoint.onClose(session);
    }

    @Test(description = "RDBMS Stream Data provider table test.", dependsOnMethods = "batchDataProviderTest")
    public void streamDataProviderTest() throws InterruptedException, SQLException {
        DataProviderEndPoint.getSessionMap().put(session.getId(), session);
        DataProviderEndPoint dataProviderEndPoint = new DataProviderEndPoint();
        String message = "{" +
                "\"providerName\": \"RDBMSStreamingDataProvider\"," +
                "\"dataProviderConfiguration\": {" +
                "\"datasourceName\": \"DEMO_DB\"," +
                "\"query\": \"select * from Foo_Table\"," +
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
        Object[][] data = {{1.0, "Tacos"}, {2.0, "Tomato Soup"}, {3.0, "Grilled Cheese"}};
        DataModel expected = new DataModel(dataSetMetadata, data, -1, "test-topic");
        final String[] actualMessage = new String[1];
        responseCallBack = new ResponseCallBack() {
            int count = 0;

            @Override
            void responseMessage(String message) {
                count++;
                if (count == 1) {
                    log.info("Stream Data Provider Test Message: " + message);
                    actualMessage[0] = message;
                }
            }
        };
        responseCallBackListener = new ResponseCallBackListener(responseCallBack);
        dataProviderEndPoint.onMessage(message, session);
        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> actualMessage[0] != null);
        DataModel dataModel = new Gson().fromJson(actualMessage[0], DataModel.class);
        Assert.assertEquals(dataModel.getTopic(), expected.getTopic());
        Assert.assertEquals(dataModel.getData(), expected.getData());
        for (int i = 0; i < dataModel.getMetadata().getNames().length; i++) {
            Assert.assertEquals(dataModel.getMetadata().getNames()[i].equalsIgnoreCase(expected.getMetadata()
                    .getNames()[i]), true);
        }        Assert.assertEquals(dataModel.getMetadata().getTypes(), expected.getMetadata().getTypes());
        Assert.assertEquals(dataModel.getLastRow(), expected.getLastRow());
        dataProviderEndPoint.onClose(session);
    }

    @Test(description = "RDBMS Stream Data provider table test2.", dependsOnMethods = "streamDataProviderTest")
    public void streamDataProviderTest2() throws InterruptedException, SQLException {
        DataProviderEndPoint.getSessionMap().put(session.getId(), session);
        DataProviderEndPoint dataProviderEndPoint = new DataProviderEndPoint();
        String message = "{" +
                "\"providerName\": \"RDBMSStreamingDataProvider\"," +
                "\"dataProviderConfiguration\": {" +
                "\"datasourceName\": \"DEMO_DB\"," +
                "\"query\": \"select * from Foo_Table\"," +
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
        Object[][] data = {{4.0, "Pizza"}};
        DataModel expected = new DataModel(dataSetMetadata, data, -1, "test-topic");
        final String[] actualMessage1 = new String[1];
        final String[] actualMessage2 = new String[1];
        responseCallBack = new ResponseCallBack() {
            @Override
            void responseMessage(String message) {
                DataModel dataModel = new Gson().fromJson(message, DataModel.class);
                if (dataModel.getData().length == 3) {
                    log.info("Stream Data Provider Test Message1: " + message);
                    actualMessage1[0] = message;
                }
                if (dataModel.getData().length == 1) {
                    log.info("Stream Data Provider Test Message2: " + message);
                    actualMessage2[0] = message;
                }
            }
        };
        responseCallBackListener = new ResponseCallBackListener(responseCallBack);
        dataProviderEndPoint.onMessage(message, session);
        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> actualMessage1[0] != null);
        RDBMSTableTestUtils.insertRecords(4, "Pizza");
        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> actualMessage2[0] != null);
        DataModel dataModel = new Gson().fromJson(actualMessage2[0], DataModel.class);
        Assert.assertEquals(dataModel.getTopic(), expected.getTopic());
        Assert.assertEquals(dataModel.getData(), expected.getData());
        for (int i = 0; i < dataModel.getMetadata().getNames().length; i++) {
            Assert.assertEquals(dataModel.getMetadata().getNames()[i].equalsIgnoreCase(expected.getMetadata()
                    .getNames()[i]), true);
        }        Assert.assertEquals(dataModel.getMetadata().getTypes(), expected.getMetadata().getTypes());
        Assert.assertEquals(dataModel.getLastRow(), expected.getLastRow());
        dataProviderEndPoint.onClose(session);
    }

    @Test(description = "RDBMS Data provider data purging test.", dependsOnMethods = "streamDataProviderTest2")
    public void dataBatchProviderPurgingTest() throws InterruptedException, SQLException {
        DataProviderEndPoint.getSessionMap().put(session.getId(), session);
        DataProviderEndPoint dataProviderEndPoint = new DataProviderEndPoint();
        String message = "{" +
                "\"providerName\": \"RDBMSBatchDataProvider\"," +
                "\"dataProviderConfiguration\": {" +
                "\"datasourceName\": \"DEMO_DB\"," +
                "\"query\": \"select * from Foo_Table\"," +
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
        Object[][] data = {{2.0, "Tomato Soup"}, {3.0, "Grilled Cheese"}};
        DataModel expected = new DataModel(dataSetMetadata, data, -1, "test-topic");
        final String[] actualMessage = new String[1];
        responseCallBack = new ResponseCallBack() {
            @Override
            void responseMessage(String message) {
                DataModel dataModel = new Gson().fromJson(message, DataModel.class);
                if (dataModel.getData().length == 2) {
                    log.info("Purging Data Provider Test Message: " + message);
                    actualMessage[0] = message;
                }
            }
        };
        responseCallBackListener = new ResponseCallBackListener(responseCallBack);
        dataProviderEndPoint.onMessage(message, session);
        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> actualMessage[0] != null);
        DataModel dataModel = new Gson().fromJson(actualMessage[0], DataModel.class);
        Assert.assertEquals(dataModel.getTopic(), expected.getTopic());
        Assert.assertEquals(dataModel.getData(), expected.getData());
        for (int i = 0; i < dataModel.getMetadata().getNames().length; i++) {
            Assert.assertEquals(dataModel.getMetadata().getNames()[i].equalsIgnoreCase(expected.getMetadata()
                    .getNames()[i]), true);
        }        Assert.assertEquals(dataModel.getMetadata().getTypes(), expected.getMetadata().getTypes());
        Assert.assertEquals(dataModel.getLastRow(), expected.getLastRow());
        dataProviderEndPoint.onClose(session);
    }

    abstract class ResponseCallBack {
        abstract void responseMessage(String message);
    }

    static class ResponseCallBackListener {
        ResponseCallBack responseCallBack;

        ResponseCallBackListener(ResponseCallBack customResponseCallBack) {
            this.responseCallBack = customResponseCallBack;
        }

        void responseListener(String message) {
            if (responseCallBack != null) {
                responseCallBack.responseMessage(message);
            }
        }
    }
}
