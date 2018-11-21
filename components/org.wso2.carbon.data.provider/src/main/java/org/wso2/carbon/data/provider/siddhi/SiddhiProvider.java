/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.data.provider.siddhi;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.data.provider.AbstractDataProvider;
import org.wso2.carbon.data.provider.DataProvider;
import org.wso2.carbon.data.provider.InputFieldTypes;
import org.wso2.carbon.data.provider.ProviderConfig;
import org.wso2.carbon.data.provider.bean.DataSetMetadata;
import org.wso2.carbon.data.provider.exception.DataProviderException;
import org.wso2.carbon.data.provider.siddhi.config.SiddhiDataProviderConfig;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.execution.query.StoreQuery;
import org.wso2.siddhi.query.compiler.SiddhiCompiler;
import org.wso2.siddhi.query.compiler.exception.SiddhiParserException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component(
        service = DataProvider.class,
        immediate = true
)
public class SiddhiProvider extends AbstractDataProvider {

    private static final String PROVIDER_NAME = "SiddhiStoreDataProvider";
    private static final String SIDDHI_APP = "siddhiApp";
    private static final String STORE_QUERY = "queryData";
    private static final String PULISHING_INTERVAL = "publishingInterval";
    private static final String TIME_COLUMNS = "timeColumns";
    private static final String QUERY = "query";
    private SiddhiDataProviderConfig siddhiDataProviderConfig;
    private DataSetMetadata metadata;
    private static SiddhiManager siddhiManager = null;
    private SiddhiAppRuntime siddhiAppRuntime;
    private String[] linearTypes = new String[]{"INT", "LONG", "FLOAT", "DOUBLE"};
    private String[] ordinalTypes = new String[]{"STRING", "BOOL"};
    private List<String> timeColumns;

    @Override
    public DataProvider init(String topic, String sessionId, JsonElement jsonElement) throws DataProviderException {
        this.siddhiDataProviderConfig = new Gson().fromJson(jsonElement, SiddhiDataProviderConfig.class);
        siddhiDataProviderConfig.setQueryData(((JsonObject) jsonElement).get(STORE_QUERY));
        siddhiDataProviderConfig.setSiddhiAppContext(((JsonObject) jsonElement).get(SIDDHI_APP).getAsString());
        this.timeColumns = Arrays.asList(this.siddhiDataProviderConfig.getTimeColumns().toUpperCase(Locale.ENGLISH)
                .split(","));
        super.init(topic, sessionId, siddhiDataProviderConfig);
        SiddhiAppRuntime siddhiAppRuntime = getSiddhiAppRuntime();
        siddhiAppRuntime.setPurgingEnabled(false);
        siddhiAppRuntime.start();
        StoreQuery storeQuery = SiddhiCompiler.parseStoreQuery(siddhiDataProviderConfig.getQueryData()
                .getAsJsonObject().get(QUERY).getAsString());
        Attribute[] outputAttributeList = siddhiAppRuntime.getStoreQueryOutputAttributes(storeQuery);
        metadata = new DataSetMetadata(outputAttributeList.length);
        Attribute outputAttribute;
        for (int i = 0; i < outputAttributeList.length; i++) {
            outputAttribute = outputAttributeList[i];
            metadata.put(i, outputAttribute.getName(),
                    getMetadataTypes(outputAttribute.getName(), outputAttribute.getType().toString()));
        }
        return this;
    }

    @Override
    public boolean configValidator(ProviderConfig providerConfig) throws DataProviderException {
        SiddhiDataProviderConfig siddhiDataProviderConfig = (SiddhiDataProviderConfig) providerConfig;
        return siddhiDataProviderConfig.getSiddhiAppContext() != null && siddhiDataProviderConfig.getQueryData()
                .getAsJsonObject().get(QUERY).getAsString() != null;
    }

    @Override
    public String providerName() {
        return PROVIDER_NAME;
    }

    @Override
    public DataSetMetadata dataSetMetadata() {
        return metadata;
    }

    @Override
    public String providerConfig() {
        Map<String, String> renderingTypes = new HashMap();
        Map<String, String> renderingHints = new HashMap();
        String providerDescription = "Siddhi app provider will allow the user to retrieve data from siddhi by " +
                "writing using a siddhi app";
        renderingTypes.put(SIDDHI_APP, InputFieldTypes.SIDDHI_CODE);
        renderingHints.put(SIDDHI_APP, "Siddhi app which is used to retrieve the data");
        renderingTypes.put(STORE_QUERY, InputFieldTypes.DYNAMIC_SIDDHI_CODE);
        renderingTypes.put(PULISHING_INTERVAL, InputFieldTypes.NUMBER);
        renderingHints.put(PULISHING_INTERVAL, "Rate at which data should be sent to the widget");
        renderingTypes.put(TIME_COLUMNS, InputFieldTypes.TEXT_FIELD);
        renderingHints.put(TIME_COLUMNS, "Comma separated columns of the table that contain timestamps");
        return new Gson().toJson(new Object[]{renderingTypes, new SiddhiDataProviderConfig(), renderingHints,
                providerDescription});
    }

    @Override
    public void publish(String topic, String sessionId) {
        Event[] events = siddhiAppRuntime.query(siddhiDataProviderConfig.getQueryData().getAsJsonObject().get
                (QUERY).getAsString());
        ArrayList<Object[]> data = new ArrayList<>();
        if (events != null) {
            for (Event event : events) {
                data.add(event.getData());
            }
        }
        publishToEndPoint(data, sessionId, topic);
    }

    @Override
    public void purging() {
        //In siddhi-store provider, we do not have a requirement to purge the data.
    }

    @Override
    public void setProviderConfig(ProviderConfig providerConfig) {
        this.siddhiDataProviderConfig = (SiddhiDataProviderConfig) providerConfig;

    }

    @Override
    public DataSetMetadata getMetadata() {
        return metadata;
    }

    @Override
    public void stop() {
        siddhiAppRuntime.shutdown();
        super.stop();
    }

    private static SiddhiManager getSiddhiManager() {
        if (siddhiManager == null) {
            siddhiManager = new SiddhiManager();
        }
        return siddhiManager;
    }

    private SiddhiAppRuntime getSiddhiAppRuntime() throws DataProviderException {
        if (this.siddhiAppRuntime == null) {
            try {
                this.siddhiAppRuntime = getSiddhiManager().createSiddhiAppRuntime(siddhiDataProviderConfig.getSiddhiAppContext());
            } catch (SiddhiParserException e) {
                throw new DataProviderException("Invalid Siddhi App Context", e);
            }
        }
        return this.siddhiAppRuntime;
    }

    /**
     * Get metadata type(linear,ordinal,time) for the given data type of the data base.
     *
     * @param dataType String data type name provided by the result set metadata
     * @return String metadata type
     */
    private DataSetMetadata.Types getMetadataTypes(String columnName, String dataType) {
        if (this.timeColumns.contains(columnName.toUpperCase(Locale.ENGLISH))) {
            return DataSetMetadata.Types.TIME;
        }
        if (Arrays.asList(linearTypes).contains(dataType.toUpperCase(Locale.ENGLISH))) {
            return DataSetMetadata.Types.LINEAR;
        } else if (Arrays.asList(ordinalTypes).contains(dataType.toUpperCase(Locale
                .ENGLISH))) {
            return DataSetMetadata.Types.ORDINAL;
        } else {
            return DataSetMetadata.Types.OBJECT;
        }
    }
}
