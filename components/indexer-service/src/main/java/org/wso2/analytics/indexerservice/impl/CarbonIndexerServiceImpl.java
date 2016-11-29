package org.wso2.analytics.indexerservice.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.request.CollectionAdminRequest;
import org.apache.solr.client.solrj.request.ConfigSetAdminRequest;
import org.apache.solr.client.solrj.request.schema.SchemaRequest;
import org.apache.solr.client.solrj.response.CollectionAdminResponse;
import org.apache.solr.client.solrj.response.ConfigSetAdminResponse;
import org.apache.solr.client.solrj.response.schema.SchemaResponse;
import org.wso2.analytics.indexerservice.CarbonIndexerService;
import org.wso2.analytics.indexerservice.IndexField;
import org.wso2.analytics.indexerservice.IndexSchema;
import org.wso2.analytics.indexerservice.config.IndexerConfiguration;
import org.wso2.analytics.indexerservice.exceptions.IndexerException;
import org.wso2.analytics.indexerservice.utils.IndexerUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * This class represents a concrete implementation of {@link org.wso2.analytics.indexerservice.CarbonIndexerService}
 */
public class CarbonIndexerServiceImpl implements CarbonIndexerService {

    private static final String ATTR_NAME = "name";
    private static final String ATTR_INDEXED = "indexed";
    private static final String ATTR_STORED = "stored";
    private static final String ATTR_TYPE = "type";
    private static Log log = LogFactory.getLog(CarbonIndexerServiceImpl.class);
    private volatile Map<String, CarbonIndexerClient> indexerClients = new ConcurrentHashMap<>();
    private static final String INDEXER_CONFIG_DIR = "indexer";
    private static final String INDEXER_CONFIG_FILE = "indexer-config.xml";
    private IndexerConfiguration indexConfig;
    private final Object indexClientsLock = new Object();

    public CarbonIndexerServiceImpl() {
        /*try {
            indexConfig = loadIndexerConfigurations();
        } catch (IndexerException e) {
            log.error("Error while initializing Indexer service : " + e.getMessage(), e);
        }*/
    }

    private IndexerConfiguration loadIndexerConfigurations() throws IndexerException {
        try {
            File confFile = new File(IndexerUtils.getIndexerConfDirectory() + File.separator +
                                     INDEXER_CONFIG_DIR + File.separator +
                                     INDEXER_CONFIG_FILE);
            if (!confFile.exists()) {

                throw new IndexerException("the indexer service configuration file cannot be found at: " +
                                           confFile.getPath());
            }
            JAXBContext ctx = JAXBContext.newInstance(IndexerConfiguration.class);
            Unmarshaller unmarshaller = ctx.createUnmarshaller();
            return (IndexerConfiguration) unmarshaller.unmarshal(confFile);
        } catch (JAXBException e) {
            throw new IndexerException(
                    "Error in processing analytics indexer service configuration: " + e.getMessage(), e);
        }
    }

    @Override
    public CarbonIndexerClient getIndexerClient(String tableName, String tenantDomain) throws IndexerException {
        String tableNameWithTenant = getTableNameWithDomainName(tableName, tenantDomain);
        CarbonIndexerClient indexerClient = indexerClients.get(tableNameWithTenant);
        if (indexerClient == null) {
            synchronized (indexClientsLock) {
                if (indexerClient == null) {
                //    if (indexConfig != null && indexConfig.getSolrServerURLwithPort() != null) {
                        SolrClient client = new CloudSolrClient.Builder().withZkHost("localhost:9983").build();
                        indexerClient = new CarbonIndexerClient(client);
                        indexerClients.put(tableNameWithTenant, indexerClient);
                //    }
                }
            }
        }
        return indexerClient;
    }

    @Override
    public boolean createIndexForTable(String table, String tenantDomain) throws IndexerException {
        try {
            String tableNameWithTenant = getTableNameWithDomainName(table, tenantDomain);
            ConfigSetAdminRequest.List listRequest = new ConfigSetAdminRequest.List();
            ConfigSetAdminResponse.List listRequestReponse = listRequest.process(getIndexerClient(table, tenantDomain));
            if (!listRequestReponse.getConfigSets().contains(tableNameWithTenant)) {
                ConfigSetAdminRequest.Create configSetAdminRequest = new ConfigSetAdminRequest.Create();
                configSetAdminRequest.setBaseConfigSetName("gettingstarted");
                configSetAdminRequest.setConfigSetName(tableNameWithTenant);
                ConfigSetAdminResponse configSetResponse = configSetAdminRequest.process(getIndexerClient(table, tenantDomain));
                if (configSetResponse.getStatus() == 0) {
                    return createSolrCollection(table, tenantDomain, tableNameWithTenant);
                }
            } else {
                return createSolrCollection(table, tenantDomain, tableNameWithTenant);
            }
            return false;
        } catch (SolrServerException | IOException e) {
            log.error("error while creating the index for table: " + table + "in domain: " + tenantDomain + ": " + e.getMessage(), e);
            throw new IndexerException("error while creating the index for table: " + table + "in domain: " + tenantDomain + ": " + e.getMessage(), e);
        }
    }

    private boolean createSolrCollection(String table, String tenantDomain,
                                         String tableNameWithTenant)
            throws SolrServerException, IOException,
                   IndexerException {
        CollectionAdminRequest.Create createRequest = CollectionAdminRequest.createCollection(tableNameWithTenant, tableNameWithTenant, 2, 2);
        createRequest.setMaxShardsPerNode(2);
        CollectionAdminResponse CollectionAdminResponse = createRequest.process(getIndexerClient(table, tenantDomain));
        return CollectionAdminResponse.getStatus() == 0;
    }

    @Override
    public boolean updateIndexSchema(String table, String tenantDomain, IndexSchema indexSchema, boolean merge)
            throws IndexerException {
        IndexSchema oldSchema = getIndexSchema(table, tenantDomain);
        List<SchemaRequest.Update> updateFields = new ArrayList<>();
        SolrClient client = getIndexerClient(table, tenantDomain);
        String tableNameWithTenantDomain = getTableNameWithDomainName(table, tenantDomain);
        SchemaResponse.UpdateResponse updateResponse;
        //TODO: do we let users to change the uniqueKey "id"?
        if (!merge) {
            List<SchemaRequest.Update> oldFields = createSolrDeleteFields(oldSchema);
            List<SchemaRequest.Update> newFields = createSolrAddFields(indexSchema);
            updateFields.addAll(oldFields);
            updateFields.addAll(newFields);
        } else {
            updateFields = indexSchema.getFields().entrySet().stream()
                    .map(field -> oldSchema.getField(field.getKey()) != null ? updateSchemaAndGetReplaceFields(oldSchema, field) :
                     updateSchemaAndGetAddFields(oldSchema, field)).collect(Collectors.toList());
        }
        SchemaRequest.MultiUpdate multiUpdateRequest = new SchemaRequest.MultiUpdate(updateFields);
        try {
            updateResponse = multiUpdateRequest.process(client, tableNameWithTenantDomain);
            return updateResponse.getStatus() == 0;
        } catch (SolrServerException | IOException e) {
            log.error("error while updating the index schema for table: " + table + "in domain: " + tenantDomain + ": " + e.getMessage(), e);
            throw new IndexerException("error while updating the index schema for table: " + table + "in domain: " + tenantDomain + ": " + e.getMessage(), e);
        }
    }

    private SchemaRequest.Update updateSchemaAndGetReplaceFields(IndexSchema oldSchema,
                                                                 Map.Entry<String, IndexField> field) {
        oldSchema.setField(field.getKey(), new IndexField(field.getValue()));
        return new SchemaRequest.ReplaceField(getSolrIndexProperties(field));
    }

    private SchemaRequest.Update updateSchemaAndGetAddFields(IndexSchema oldSchema,
                                                             Map.Entry<String, IndexField> field) {
        oldSchema.setField(field.getKey(), new IndexField(field.getValue()));
        return new SchemaRequest.AddField(getSolrIndexProperties(field));
    }

    private List<SchemaRequest.Update> createSolrAddFields(IndexSchema indexSchema) {
        List<SchemaRequest.Update> fields = new ArrayList<>();
        indexSchema.getFields().entrySet().stream().forEach(field -> {
            Map<String, Object> properties = getSolrIndexProperties(field);
            SchemaRequest.AddField addFieldRequest = new SchemaRequest.AddField(properties);
            fields.add(addFieldRequest);
        });
        return fields;
    }

    private Map<String, Object> getSolrIndexProperties(Map.Entry<String, IndexField> field) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(ATTR_INDEXED, field.getValue().isIndexed());
        properties.put(ATTR_STORED, field.getValue().isStored());
        properties.put(ATTR_NAME, field.getValue().getFieldName());
        properties.put(ATTR_TYPE, field.getValue().getType());
        properties.putAll(field.getValue().getOtherProperties());
        return properties;
    }

    private List<SchemaRequest.Update> createSolrDeleteFields(IndexSchema oldSchema) {
        List<SchemaRequest.Update> fields = new ArrayList<>();
        //TODO:add a config to define the default required field which should not be deleted. (e.g. id, there are other solr specific fields like _version_)
        oldSchema.getFields().entrySet().stream().filter(field -> !(field.getKey().equals(oldSchema.getUniqueKey()) || field.getKey().equals("_version_"))).forEach(field -> {
            SchemaRequest.DeleteField deleteFieldRequest = new SchemaRequest.DeleteField(field.getKey());
            fields.add(deleteFieldRequest);
        });
        return fields;
    }

    @Override
    public IndexSchema getIndexSchema(String table, String tenantDomain) throws IndexerException {
        SchemaRequest.Fields fieldsRequest = new SchemaRequest.Fields();
        SchemaRequest.UniqueKey uniqueKeyRequest = new SchemaRequest.UniqueKey();
        SolrClient client = getIndexerClient(table, tenantDomain);
        String tableNameWithTenantDomain = getTableNameWithDomainName(table, tenantDomain);
        try {
            SchemaResponse.FieldsResponse fieldsResponse = fieldsRequest.process(client, tableNameWithTenantDomain);
            SchemaResponse.UniqueKeyResponse uniqueKeyResponse = uniqueKeyRequest.process(client, tableNameWithTenantDomain);
            List<Map<String, Object>> fields = fieldsResponse.getFields();
            String uniqueKey = uniqueKeyResponse.getUniqueKey();
            return createIndexSchemaFromSolrSchema(uniqueKey, fields);
        } catch (SolrServerException | IOException e) {
            log.error("error while retrieving the index schema for table: " + table + "in domain: " + tenantDomain + ": " + e.getMessage(), e);
            throw new IndexerException("error while retrieving the index schema for table: " + table + "in domain: " + tenantDomain + ": " + e.getMessage(), e);
        }
    }

    private static IndexSchema createIndexSchemaFromSolrSchema(String uniqueKey, List<Map<String, Object>> fields) throws IndexerException {
        IndexSchema indexSchema = new IndexSchema();
        indexSchema.setUniqueKey(uniqueKey);
        indexSchema.setFields(createIndexFields(fields));
        return indexSchema;
    }

    private static Map<String, IndexField> createIndexFields(List<Map<String, Object>> fields) throws IndexerException {
        Map<String, IndexField> indexFields = new LinkedHashMap<>();
        boolean isIndexed = false;
        boolean isStored = false;
        String type = "string"; //the default type in case if the type is not provided
        String fieldName;
        for (Map<String, Object> fieldProperties : fields) {
            if (fieldProperties != null && fieldProperties.containsKey(ATTR_NAME)) {
                fieldName = fieldProperties.remove(ATTR_NAME).toString();
                if (fieldProperties.containsKey(ATTR_INDEXED)) {
                    isIndexed = (Boolean) fieldProperties.remove(ATTR_INDEXED);
                }
                if (fieldProperties.containsKey(ATTR_STORED)) {
                    isStored = (Boolean) fieldProperties.remove(ATTR_STORED);
                }
                if (fieldProperties.containsKey(ATTR_TYPE)) {
                    type = (String) fieldProperties.remove(ATTR_TYPE);
                }
                indexFields.put(fieldName, new IndexField(fieldName, isStored, isIndexed, type, fieldProperties));
            } else {
                throw new IndexerException("Fields must have an attribute called " + ATTR_NAME);
            }
        }
        return indexFields;
    }

    @Override
    public boolean deleteIndexForTable(String table, String tenantDomain) throws IndexerException {
        String tableNameWithTenant = getTableNameWithDomainName(table, tenantDomain);
        CollectionAdminRequest.Delete deleteRequest = CollectionAdminRequest.deleteCollection(tableNameWithTenant);
        try {
            CollectionAdminResponse deleteRequestResponse = deleteRequest.process(getIndexerClient(table, tenantDomain), tableNameWithTenant);
            if (deleteRequestResponse.getStatus() == 0) {
                ConfigSetAdminRequest.Delete configSetAdminRequest = new ConfigSetAdminRequest.Delete();
                configSetAdminRequest.setConfigSetName(tableNameWithTenant);
                ConfigSetAdminResponse configSetResponse = configSetAdminRequest.process(getIndexerClient(table, tenantDomain));
                return configSetResponse.getStatus() == 0;
            }
        } catch (IOException | SolrServerException e) {
            log.error("error while deleting the index for table: " + table + "in domain: " + tenantDomain + ": " + e.getMessage(), e);
            throw new IndexerException("error while deleting the index for table: " + table + "in domain: " + tenantDomain + ": " + e.getMessage(), e);
        }
        return false;
    }

    private String getTableNameWithDomainName(String tableName, String tenantDomain) {
        return tenantDomain + "_" + tableName;
    }

    public static void main(String[] args) throws IOException, SolrServerException,
                                                  IndexerException {
        CarbonIndexerService carbonIndexerService = new CarbonIndexerServiceImpl();
        carbonIndexerService.createIndexForTable("fd", "77");
        IndexSchema schema = new IndexSchema();
        schema.setField("testField", new IndexField("testField", true, true, "string", new HashMap<>()));
        schema.setField("ssss", new IndexField("ssss", true, true, "double", new HashMap<>()));
        Object carbonIndexerClient = carbonIndexerService.getIndexSchema("fd", "77");
        carbonIndexerService.updateIndexSchema("fd", "77", schema, false);
        carbonIndexerService.deleteIndexForTable("fd", "77");

    }

}
