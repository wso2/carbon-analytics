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
import org.apache.solr.common.SolrException;
import org.wso2.analytics.indexerservice.CarbonIndexDocument;
import org.wso2.analytics.indexerservice.CarbonIndexerService;
import org.wso2.analytics.indexerservice.IndexSchema;
import org.wso2.analytics.indexerservice.IndexSchemaField;
import org.wso2.analytics.indexerservice.config.IndexerConfiguration;
import org.wso2.analytics.indexerservice.exceptions.IndexSchemaNotFoundException;
import org.wso2.analytics.indexerservice.exceptions.IndexerException;
import org.wso2.analytics.indexerservice.utils.IndexerUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
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
    private static final String INDEXER_CONFIG_DIR = "analytics";
    private static final String INDEXER_CONFIG_FILE = "indexer-config.xml";
    private IndexerConfiguration indexConfig;
    private String tenantDomain = "DEFAULT";
    private final Object indexClientsLock = new Object();
    private Map<String, IndexSchema> indexSchemaCache = new ConcurrentHashMap<>();

    public CarbonIndexerServiceImpl() {
        try {
            indexConfig = loadIndexerConfigurations();
        } catch (IndexerException e) {
            log.error("Failed to initialize Indexer service : " + e.getMessage(), e);
        }
    }

    private IndexerConfiguration loadIndexerConfigurations() throws IndexerException {
        File confFile = new File(IndexerUtils.getIndexerConfDirectory() + File.separator +
                                 INDEXER_CONFIG_DIR + File.separator +
                                 INDEXER_CONFIG_FILE);
        try {
            if (!confFile.exists()) {
                confFile = IndexerUtils.getFileFromSystemResources(INDEXER_CONFIG_FILE);
                throw new IndexerException("the indexer service configuration file cannot be found at: " +
                                           confFile.getPath());
            }
            JAXBContext ctx = JAXBContext.newInstance(IndexerConfiguration.class);
            Unmarshaller unmarshaller = ctx.createUnmarshaller();
            return (IndexerConfiguration) unmarshaller.unmarshal(confFile);
        } catch (JAXBException e) {
            throw new IndexerException(
                    "Error in processing analytics indexer service configuration: " + e.getMessage(), e);
        } catch (URISyntaxException e) {
            throw new IndexerException("Error while loading configurations: cannot find the indexer configuration files in " +
                confFile.getPath() + " or from classpath.", e);
        }
    }

    @Override
    public CarbonIndexerClient getIndexerClient(String tableName) throws IndexerException {
        String tableNameWithTenant = getTableNameWithDomainName(tableName);
        CarbonIndexerClient indexerClient = indexerClients.get(tableNameWithTenant);
        if (indexerClient == null) {
            synchronized (indexClientsLock) {
                if (indexerClient == null) {
                    if (indexConfig != null) {
                        SolrClient client = new CloudSolrClient.Builder().withZkHost(indexConfig.getSolrServerUrl()).build();
                        indexerClient = new CarbonIndexerClient(client);
                        indexerClients.put(tableNameWithTenant, indexerClient);
                    }
                }
            }
        }
        return indexerClient;
    }

    @Override
    public boolean createIndexForTable(String table)
            throws IndexerException {
        try {
            String tableNameWithTenant = getTableNameWithDomainName(table);
            if (!indexExists(table)) {
                if (!indexConfigsExists(table)) {
                    ConfigSetAdminResponse configSetResponse = createInitialIndexConfiguration(table);
                    if (configSetResponse.getStatus() == 0) {
                        return createSolrCollection(table, tableNameWithTenant);
                    }
                } else {
                    return createSolrCollection(table, tableNameWithTenant);
                }
            }
            return false;
        } catch (SolrServerException | IOException e) {
            log.error("error while creating the index for table: " + table + "in domain: " +
                      tenantDomain + ": " + e.getMessage(), e);
            throw new IndexerException("error while creating the index for table: " + table + "in domain: " +
                                       tenantDomain + ": " + e.getMessage(), e);
        }
    }

    /*
    This method is to create the initial index configurations for the index of a table. This will include a default
    indexSchema and other Solr configurations. Later by using updateIndexSchema we can edit the index schema
    */
    private ConfigSetAdminResponse createInitialIndexConfiguration(String table)
            throws SolrServerException, IOException,
                   IndexerException {
        String tableNameWithTenant = getTableNameWithDomainName(table);
        ConfigSetAdminRequest.Create configSetAdminRequest = new ConfigSetAdminRequest.Create();
        configSetAdminRequest.setBaseConfigSetName(indexConfig.getBaseConfigSet());
        configSetAdminRequest.setConfigSetName(tableNameWithTenant);
        return configSetAdminRequest.process(getIndexerClient(table));
    }

    private boolean createSolrCollection(String table,  String tableNameWithTenant) throws SolrServerException, IOException,
                   IndexerException {
        CollectionAdminRequest.Create createRequest =
                CollectionAdminRequest.createCollection(tableNameWithTenant, tableNameWithTenant,
                indexConfig.getNoOfShards(), indexConfig.getNoOfReplicas());
        createRequest.setMaxShardsPerNode(indexConfig.getNoOfShards());
        CollectionAdminResponse collectionAdminResponse = createRequest.process(getIndexerClient(table));
        return collectionAdminResponse.isSuccess();
    }

    @Override
    public boolean updateIndexSchema(String table, IndexSchema indexSchema, boolean merge)
            throws IndexerException {
        IndexSchema oldSchema;
        List<SchemaRequest.Update> updateFields = new ArrayList<>();
        SolrClient client = getIndexerClient(table);
        String tableNameWithTenantDomain = getTableNameWithDomainName(table);
        SchemaResponse.UpdateResponse updateResponse;
        try {
            oldSchema = getIndexSchema(table);
        } catch (IndexSchemaNotFoundException e) {
            try {
                createInitialIndexConfiguration(table);
                oldSchema = getIndexSchema(table);
            } catch (IOException | IndexSchemaNotFoundException | SolrServerException e1) {
                throw new IndexerException("Error while retrieving  the index schema for table: " + table, e1);
            }
        }
        updateFields = createUpdateFields(indexSchema, merge, oldSchema, updateFields);
        SchemaRequest.MultiUpdate multiUpdateRequest = new SchemaRequest.MultiUpdate(updateFields);
        try {
            updateResponse = multiUpdateRequest.process(client, tableNameWithTenantDomain);
            if (updateResponse.getStatus() == 0) {
                if (merge) {
                    IndexSchema mergedSchema = IndexerUtils.getMergedIndexSchema(oldSchema, indexSchema);
                    indexSchemaCache.put(tableNameWithTenantDomain, mergedSchema);
                } else {
                    indexSchemaCache.put(tableNameWithTenantDomain, indexSchema);
                }
            }
            return updateResponse.getStatus() == 0;
        } catch (SolrServerException | IOException e) {
            log.error("error while updating the index schema for table: " + table + "in domain: " +
                      tenantDomain + ": " + e.getMessage(), e);
            throw new IndexerException("error while updating the index schema for table: " + table +
                                       "in domain: " + tenantDomain + ": " + e.getMessage(), e);
        }
    }

    private List<SchemaRequest.Update> createUpdateFields(IndexSchema indexSchema, boolean merge,
                                                          IndexSchema finalOldSchema,
                                                          List<SchemaRequest.Update> updateFields) {
        //TODO: do we let users to change the uniqueKey "id"?
        if (!merge) {
            List<SchemaRequest.Update> oldFields = createSolrDeleteFields(finalOldSchema);
            List<SchemaRequest.Update> newFields = createSolrAddFields(indexSchema);
            updateFields.addAll(oldFields);
            updateFields.addAll(newFields);
        } else {
            updateFields = indexSchema.getFields().entrySet().stream()
                    .map(field -> finalOldSchema.getField(field.getKey()) != null ? updateSchemaAndGetReplaceFields(finalOldSchema, field) :
                     updateSchemaAndGetAddFields(finalOldSchema, field)).collect(Collectors.toList());
        }
        return updateFields;
    }

    private SchemaRequest.Update updateSchemaAndGetReplaceFields(IndexSchema oldSchema,
                                                                 Map.Entry<String, IndexSchemaField> field) {
        oldSchema.addField(field.getKey(), new IndexSchemaField(field.getValue()));
        return new SchemaRequest.ReplaceField(getSolrIndexProperties(field));
    }

    private SchemaRequest.Update updateSchemaAndGetAddFields(IndexSchema oldSchema,
                                                             Map.Entry<String, IndexSchemaField> field) {
        oldSchema.addField(field.getKey(), new IndexSchemaField(field.getValue()));
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

    private Map<String, Object> getSolrIndexProperties(Map.Entry<String, IndexSchemaField> field) {
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
        oldSchema.getFields().entrySet().stream().filter(field -> !(field.getKey().equals(oldSchema.getUniqueKey()) ||
                field.getKey().equals("_version_"))).forEach(field -> {
            SchemaRequest.DeleteField deleteFieldRequest = new SchemaRequest.DeleteField(field.getKey());
            fields.add(deleteFieldRequest);
        });
        return fields;
    }

    @Override
    public IndexSchema getIndexSchema(String table)
            throws IndexerException, IndexSchemaNotFoundException {
        SolrClient client = getIndexerClient(table);
        String tableNameWithTenantDomain = getTableNameWithDomainName(table);
        IndexSchema indexSchema = indexSchemaCache.get(tableNameWithTenantDomain);
        if (indexSchema == null) {
            try {
                if (indexConfigsExists(table)) {
                    SchemaRequest.Fields fieldsRequest = new SchemaRequest.Fields();
                    SchemaRequest.UniqueKey uniqueKeyRequest = new SchemaRequest.UniqueKey();
                    SchemaResponse.FieldsResponse fieldsResponse = fieldsRequest.process(client, tableNameWithTenantDomain);
                    SchemaResponse.UniqueKeyResponse uniqueKeyResponse = uniqueKeyRequest.process(client, tableNameWithTenantDomain);
                    List<Map<String, Object>> fields = fieldsResponse.getFields();
                    String uniqueKey = uniqueKeyResponse.getUniqueKey();
                    indexSchema = createIndexSchemaFromSolrSchema(uniqueKey, fields);
                    indexSchemaCache.put(tableNameWithTenantDomain, indexSchema);
                } else {
                    throw new IndexSchemaNotFoundException("Index schema for table: " + table + "is not found");
                }
            } catch (SolrServerException | IOException | SolrException e) {
                log.error("error while retrieving the index schema for table: " + table + "in domain: " +
                          tenantDomain + ": " + e.getMessage(), e);
                throw new IndexerException("error while retrieving the index schema for table: " + table +
                                           "in domain: " + tenantDomain + ": " + e.getMessage(), e);
            }
        }
        return indexSchema;
    }

    private static IndexSchema createIndexSchemaFromSolrSchema(String uniqueKey, List<Map<String, Object>> fields) throws IndexerException {
        IndexSchema indexSchema = new IndexSchema();
        indexSchema.setUniqueKey(uniqueKey);
        indexSchema.setFields(createIndexFields(fields));
        return indexSchema;
    }

    private static Map<String, IndexSchemaField> createIndexFields(List<Map<String, Object>> fields) throws IndexerException {
        Map<String, IndexSchemaField> indexFields = new LinkedHashMap<>();
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
                indexFields.put(fieldName, new IndexSchemaField(fieldName, isStored, isIndexed, type, fieldProperties));
            } else {
                throw new IndexerException("Fields must have an attribute called " + ATTR_NAME);
            }
        }
        return indexFields;
    }

    @Override
    public boolean deleteIndexForTable(String table) throws IndexerException {
        try {
            if (indexExists(table)) {
                String tableNameWithTenant = getTableNameWithDomainName(table);
                CollectionAdminRequest.Delete deleteRequest = CollectionAdminRequest.deleteCollection(tableNameWithTenant);
                CollectionAdminResponse deleteRequestResponse =
                        deleteRequest.process(getIndexerClient(table), tableNameWithTenant);
                if (deleteRequestResponse.isSuccess() && indexConfigsExists(table)) {
                    ConfigSetAdminRequest.Delete configSetAdminRequest = new ConfigSetAdminRequest.Delete();
                    configSetAdminRequest.setConfigSetName(tableNameWithTenant);
                    ConfigSetAdminResponse configSetResponse = configSetAdminRequest.process(getIndexerClient(table));
                    indexSchemaCache.remove(tableNameWithTenant);
                    return configSetResponse.getStatus() == 0;
                }
            }
        } catch (IOException | SolrServerException e) {
            log.error("error while deleting the index for table: " + table + "in domain: " +
                    tenantDomain + ": " + e.getMessage(), e);
            throw new IndexerException("error while deleting the index for table: " + table +
                    "in domain: " + tenantDomain + ": " + e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean indexExists(String table) throws IndexerException {
        CollectionAdminRequest.List listRequest = CollectionAdminRequest.listCollections();
        String tableWithTenant = getTableNameWithDomainName(table);
        try {
            CollectionAdminResponse listResponse = listRequest.process(getIndexerClient(table));
            List collections = (List) listResponse.getResponse().get("collections");
            return collections.contains(tableWithTenant);
        } catch (IOException | SolrServerException e) {
            log.error("Error while checking the existence of index for table : " + table, e);
            throw new IndexerException("Error while checking the existence of index for table : " + table, e);
        }
    }

    @Override
    public boolean indexConfigsExists(String table) throws IndexerException {
        ConfigSetAdminResponse.List listRequestReponse;
        CarbonIndexerClient carbonIndexerClient = getIndexerClient(table);
        String tableNameWithTenantDomain = getTableNameWithDomainName(table);
        ConfigSetAdminRequest.List listRequest = new ConfigSetAdminRequest.List();
        try {
            listRequestReponse = listRequest.process(carbonIndexerClient);
        } catch (IOException | SolrServerException e) {
            log.error("Error while checking if index configurations exists for table: " + table, e);
            throw new IndexerException("Error while checking if index configurations exists for table: " + table, e);
        }
        return listRequestReponse.getConfigSets().contains(tableNameWithTenantDomain);
    }

    @Override
    public void indexDocuments(String table, List<CarbonIndexDocument> docs) throws IndexerException {
        try {
            String tableWithTenantDomain = getTableNameWithDomainName(table);
            CarbonIndexerClient client = getIndexerClient(table);
            client.add(tableWithTenantDomain, IndexerUtils.getSolrInputDocuments(docs));
            //TODO:should find a better way to commit, there are different overloaded methods of commit
            client.commit(tableWithTenantDomain);
        } catch (SolrServerException | IOException e) {
            log.error("Error while inserting the documents to index for table: " + table, e);
            throw new IndexerException("Error while inserting the documents to index for table: " + table, e);
        }
    }

    @Override
    public void deleteDocuments(String table, List<String> ids) throws IndexerException {
        if (ids != null && !ids.isEmpty()) {
            CarbonIndexerClient client = getIndexerClient(table);
            String tableWithTenantDomain = getTableNameWithDomainName(table);
            try {
                client.deleteById(tableWithTenantDomain, ids);
                client.commit(tableWithTenantDomain);
            } catch (SolrServerException | IOException e) {
                log.error("Error while deleting index documents by ids, " + e.getMessage(), e);
                throw new IndexerException("Error while deleting index documents by ids, " + e.getMessage(), e);
            }
        }
    }

    @Override
    public void deleteDocuments(String table, String query) throws IndexerException {
        if (query != null && !query.isEmpty()) {
            CarbonIndexerClient client = getIndexerClient(table);
            String tableWithTenantDomain = getTableNameWithDomainName(table);
            try {
                client.deleteByQuery(tableWithTenantDomain, query);
                client.commit(tableWithTenantDomain);
            } catch (SolrServerException | IOException e) {
                log.error("Error while deleting index documents by query, " + e.getMessage(), e);
                throw new IndexerException("Error while deleting index documents by query, " + e.getMessage(), e);
            }
        }

    }

    @Override
    public void destroy() throws IndexerException {
        try {
            for (SolrClient solrClient : indexerClients.values()) {
                solrClient.close();
            }
        } catch (IOException e) {
            log.error("Error while destroying the indexer service, " + e.getMessage(), e);
            throw new IndexerException("Error while destroying the indexer service, " + e.getMessage(), e);
        }
        indexerClients = null;
    }

    private String getTableNameWithDomainName(String tableName) {
        return tenantDomain.toUpperCase() + "_" + tableName.toUpperCase();
    }

    public static void main(String[] args) throws IOException, SolrServerException,
                                                  IndexerException, IndexSchemaNotFoundException {
        CarbonIndexerService carbonIndexerService = new CarbonIndexerServiceImpl();
    //    carbonIndexerService.createIndexForTable("81");
        IndexSchema schema = new IndexSchema();
        schema.addField("testField", new IndexSchemaField("testField", true, true, "string", new HashMap<>()));
        schema.addField("ssss", new IndexSchemaField("ssss", true, true, "double", new HashMap<>()));
      //  Object carbonIndexerClient = carbonIndexerService.createIndexForTable("gettingstarted");
     //   carbonIndexerService.updateIndexSchema("81", schema, false);
        CarbonIndexDocument doc = new CarbonIndexDocument();
        doc.addField("testField", "testttttt");
        doc.addField("ssss", 43);
        doc.addField("id", 23);
        List<CarbonIndexDocument> l = new ArrayList<>();
        l.add(doc);
        carbonIndexerService.indexDocuments("81", l);
        List<String> ids = new ArrayList<>();
        ids.add("23");
        carbonIndexerService.deleteDocuments("81", ids);
      //  carbonIndexerService.deleteIndexForTable("81");

    }

}
