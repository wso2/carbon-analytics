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
import io.siddhi.core.SiddhiAppRuntime;
import io.siddhi.core.SiddhiManager;
import io.siddhi.core.event.Event;
import io.siddhi.query.api.definition.Attribute;
import io.siddhi.query.api.execution.query.StoreQuery;
import io.siddhi.query.compiler.SiddhiCompiler;
import io.siddhi.query.compiler.exception.SiddhiParserException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private static final Pattern LIMIT_FILTER_REGEX = Pattern.compile("(?<=limit )([0-9]*)");
    private static final Pattern OFFSET_AND_VALUE_REGEX = Pattern.compile("(offset )(?<=offset )([0-9]*)");
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

    public void publishWithPagination(JsonElement jsonElement, String topic, String sessionId) {
        this.siddhiDataProviderConfig = new Gson().fromJson(jsonElement, SiddhiDataProviderConfig.class);
        int currentPage = this.siddhiDataProviderConfig.getCurrentPage();
        int pageSize = this.siddhiDataProviderConfig.getPageSize();
        this.siddhiDataProviderConfig = (new Gson()).fromJson(jsonElement, SiddhiDataProviderConfig.class);
        String query = siddhiDataProviderConfig.getQueryData().getAsJsonObject().get(QUERY).getAsString();
        Matcher limitMatcher = LIMIT_FILTER_REGEX.matcher(query);
        Matcher offsetAndValueMatcher = OFFSET_AND_VALUE_REGEX.matcher(query);
        boolean limitMatcherFound = false;
        boolean offsetAndValueMatcherFound = false;
        int limit = 0;
        int offset = 0;
        if (limitMatcher.find()) {
            limitMatcherFound = true;
            limit = limitMatcher.group().isEmpty() ? 0 : Integer.parseInt(limitMatcher.group());
        }
        if (offsetAndValueMatcher.find()) {
            offsetAndValueMatcherFound = true;
            offset = offsetAndValueMatcher.group(2).isEmpty() ? 0 : Integer.parseInt(offsetAndValueMatcher.group(2));
        }
        int newOffset = offset + currentPage * pageSize;
        int newLimit = limitMatcherFound && limit < pageSize? limit : pageSize;
        if ((limitMatcherFound) && (newLimit + newOffset > limit + offset)) {
            int limitDiff = (limit + offset) - newOffset;
            if (limitDiff <= 0) {
                newLimit = 0;
            } else {
                newLimit = limitDiff;
            }
        }
        ArrayList<Object[]> data = new ArrayList<>();
        if (newLimit == 0) {
            publishToEndPoint(data, sessionId, topic);
        } else {
            if (!limitMatcherFound && offsetAndValueMatcherFound) {
                query = offsetAndValueMatcher.replaceFirst("limit " + newLimit + " offset " + newOffset);
            } else if (limitMatcherFound && offsetAndValueMatcherFound) {
                query = limitMatcher.replaceFirst(String.valueOf(newLimit));
                offsetAndValueMatcher = OFFSET_AND_VALUE_REGEX.matcher(query);
                query = offsetAndValueMatcher.replaceFirst("offset " + newOffset);
            } else if (limitMatcherFound) {
                query = limitMatcher.replaceFirst(newLimit + " offset " + newOffset);
            } else {
                query = query + " limit " + newLimit + " offset " + newOffset;
            }
            Event[] events = siddhiAppRuntime.query(query);
            if (events != null) {
                for (Event event : events) {
                    data.add(event.getData());
                }
            }
            publishToEndPoint(data, sessionId, topic);
        }
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

    public SiddhiDataProviderConfig getSiddhiDataProviderConfig() {
        return siddhiDataProviderConfig;
    }
}
