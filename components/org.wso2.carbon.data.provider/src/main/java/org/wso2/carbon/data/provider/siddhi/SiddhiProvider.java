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
import org.wso2.siddhi.core.util.parser.SiddhiAppParser;
import org.wso2.siddhi.query.api.SiddhiApp;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.execution.query.StoreQuery;
import org.wso2.siddhi.query.compiler.SiddhiCompiler;
import org.wso2.siddhi.query.compiler.exception.SiddhiParserException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Component(
        name = "siddhi-data-provider",
        service = DataProvider.class,
        immediate = true
)
public class SiddhiProvider extends AbstractDataProvider {

    private static final String PROVIDER_NAME = "SiddhiStoreDataProvider";
    private static final String SIDDHI_APP = "siddhiApp";
    private static final String STORE_QUERY = "query";
    private static final String PURGING_INTERVAL = "purgingInterval";
    private SiddhiDataProviderConfig siddhiDataProviderConfig;
    private DataSetMetadata metadata;
    private static SiddhiManager siddhiManager = null;
    private SiddhiAppRuntime siddhiAppRuntime;
    private String[] linearTypes = new String[]{"INT", "LONG", "FLOAT", "DOUBLE"};
    private String[] ordinalTypes = new String[]{"STRING", "BOOL"};

    @Override
    public DataProvider init(String topic, String sessionId, JsonElement jsonElement) throws DataProviderException {
        this.siddhiDataProviderConfig = new Gson().fromJson(jsonElement, SiddhiDataProviderConfig.class);
        siddhiDataProviderConfig.setQuery(((JsonObject) jsonElement).get(STORE_QUERY).getAsString());
        siddhiDataProviderConfig.setSiddhiAppContext(((JsonObject) jsonElement).get(SIDDHI_APP).getAsString());
        super.init(topic, sessionId, siddhiDataProviderConfig);
        SiddhiAppRuntime siddhiAppRuntime = getSiddhiAppRuntime(topic);
        siddhiAppRuntime.start();
        StoreQuery storeQuery = SiddhiCompiler.parseStoreQuery(siddhiDataProviderConfig.getQuery());
        Attribute[] outputAttributeList = siddhiAppRuntime.getStoreQueryOutputAttributes(storeQuery);
        metadata = new DataSetMetadata(outputAttributeList.length);
        Attribute outputAttribute;
        for (int i = 0; i < outputAttributeList.length; i++) {
            outputAttribute = outputAttributeList[i];
            metadata.put(i, outputAttribute.getName(), getMetadataTypes(outputAttribute.getType().toString()));
        }
        return this;
    }

    @Override
    public boolean configValidator(ProviderConfig providerConfig) throws DataProviderException {
        return true;
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
        renderingTypes.put(SIDDHI_APP, InputFieldTypes.SIDDHI_CODE);
        renderingTypes.put(STORE_QUERY, InputFieldTypes.SIDDHI_CODE);
        renderingTypes.put(PURGING_INTERVAL, InputFieldTypes.NUMBER);
        return new Gson().toJson(new Object[]{renderingTypes, new SiddhiDataProviderConfig()});
    }

    @Override
    public void publish(String topic, String sessionId) {
        Event[] events = siddhiAppRuntime.query(siddhiDataProviderConfig.getQuery());
        ArrayList<Object[]> data = new ArrayList<>();
        for (int i = 0; i < events.length; i++) {
            data.add(events[i].getData());
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
            return new SiddhiManager();
        }
        return siddhiManager;
    }

    private SiddhiAppRuntime getSiddhiAppRuntime(String topic) throws DataProviderException {
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
    private DataSetMetadata.Types getMetadataTypes(String dataType) {
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
