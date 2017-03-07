package org.wso2.carbon.analytics.indexerservice.impl;

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
import org.wso2.carbon.analytics.indexerservice.CarbonIndexDocument;
import org.wso2.carbon.analytics.indexerservice.CarbonIndexerService;
import org.wso2.carbon.analytics.indexerservice.IndexSchema;
import org.wso2.carbon.analytics.indexerservice.IndexSchemaField;
import org.wso2.carbon.analytics.indexerservice.config.IndexerConfiguration;
import org.wso2.carbon.analytics.indexerservice.exceptions.IndexSchemaNotFoundException;
import org.wso2.carbon.analytics.indexerservice.exceptions.IndexerException;
import org.wso2.carbon.analytics.indexerservice.utils.IndexerUtils;

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
 * This class represents a concrete implementation of {@link CarbonIndexerService}
 */
public class CarbonIndexerServiceImpl implements CarbonIndexerService {

    private static final String ATTR_ERRORS = "errors";
    private static final String ATTR_COLLECTIONS = "collections";
    private static Log log = LogFactory.getLog(CarbonIndexerServiceImpl.class);
    private volatile CarbonIndexerClient indexerClient = null;
    private static final String INDEXER_CONFIG_DIR = "analytics";
    private static final String INDEXER_CONFIG_FILE = "indexer-config.xml";
    private IndexerConfiguration indexConfig;
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
    public CarbonIndexerClient getIndexerClient() throws IndexerException {
        if (indexerClient == null) {
            synchronized (indexClientsLock) {
                if (indexerClient == null) {
                    if (indexConfig != null) {
                        SolrClient client = new CloudSolrClient.Builder().withZkHost(indexConfig.getSolrServerUrl()).build();
                        indexerClient = new CarbonIndexerClient(client);
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
            String tableNameWithTenant = IndexerUtils.getTableNameWithDomainName(table);
            if (!indexExists(table)) {
                if (!indexConfigsExists(table)) {
                    ConfigSetAdminResponse configSetResponse = createInitialIndexConfiguration(table);
                    Object errors = configSetResponse.getErrorMessages();
                    if (configSetResponse.getStatus() == 0 && errors == null) {
                        return createSolrCollection(table, tableNameWithTenant);
                    } else {
                        throw new IndexerException("Error in deploying initial index configurations for table: " + table + ", " +
                                ", Response code: " + configSetResponse.getStatus() + " , errors: " + errors.toString());
                    }
                } else {
                    return createSolrCollection(table, tableNameWithTenant);
                }
            }
            return false;
        } catch (SolrServerException | IOException e) {
            log.error("error while creating the index for table: " + table + ": " + e.getMessage(), e);
            throw new IndexerException("error while creating the index for table: " + table + ": " + e.getMessage(), e);
        }
    }

    /*
    This method is to create the initial index configurations for the index of a table. This will include a default
    indexSchema and other Solr configurations. Later by using updateIndexSchema we can edit the index schema
    */
    private ConfigSetAdminResponse createInitialIndexConfiguration(String table)
            throws SolrServerException, IOException,
                   IndexerException {
        String tableNameWithTenant = IndexerUtils.getTableNameWithDomainName(table);
        ConfigSetAdminRequest.Create configSetAdminRequest = new ConfigSetAdminRequest.Create();
        configSetAdminRequest.setBaseConfigSetName(indexConfig.getBaseConfigSet());
        configSetAdminRequest.setConfigSetName(tableNameWithTenant);
        return configSetAdminRequest.process(getIndexerClient());
    }

    private boolean createSolrCollection(String table,  String tableNameWithTenant) throws SolrServerException, IOException,
                   IndexerException {
        CollectionAdminRequest.Create createRequest =
                CollectionAdminRequest.createCollection(tableNameWithTenant, tableNameWithTenant,
                indexConfig.getNoOfShards(), indexConfig.getNoOfReplicas());
        createRequest.setMaxShardsPerNode(indexConfig.getNoOfShards());
        CollectionAdminResponse collectionAdminResponse = createRequest.process(getIndexerClient());
        Object errors = collectionAdminResponse.getErrorMessages();
        if (!collectionAdminResponse.isSuccess()) {
            throw new IndexerException("Error in deploying initial index configurations for table: " + table + ", " +
                                       ", Response code: " + collectionAdminResponse.getStatus() + " , errors: " + errors.toString());
        }
        return true;
    }

    @Override
    public boolean updateIndexSchema(String table, IndexSchema indexSchema, boolean merge)
            throws IndexerException {
        IndexSchema oldSchema;
        List<SchemaRequest.Update> updateFields = new ArrayList<>();
        SolrClient client = getIndexerClient();
        String tableNameWithTenantDomain = IndexerUtils.getTableNameWithDomainName(table);
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
            updateResponse = multiUpdateRequest.process(client, table);
            //UpdateResponse does not have a "getErrorMessages()" method, so we check if the errors attribute exists in the response
            Object errors = updateResponse.getResponse().get(ATTR_ERRORS);
            if (updateResponse.getStatus() == 0 && errors == null) {
                if (merge) {
                    IndexSchema mergedSchema = IndexerUtils.getMergedIndexSchema(oldSchema, indexSchema);
                    indexSchemaCache.put(tableNameWithTenantDomain, mergedSchema);
                } else {
                    indexSchemaCache.put(tableNameWithTenantDomain, indexSchema);
                }
                return true;
            } else {
                throw new IndexerException("Couldn't update index schema, Response code: " + updateResponse.getStatus() +
                        ", Errors: " + errors);
            }
        } catch (SolrServerException | IOException e) {
            log.error("error while updating the index schema for table: " + table + ": " + e.getMessage(), e);
            throw new IndexerException("error while updating the index schema for table: " + table + ": " + e.getMessage(), e);
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
        properties.put(IndexSchemaField.ATTR_INDEXED, field.getValue().isIndexed());
        properties.put(IndexSchemaField.ATTR_STORED, field.getValue().isStored());
        properties.put(IndexSchemaField.ATTR_FIELD_NAME, field.getValue().getFieldName());
        properties.put(IndexSchemaField.ATTR_TYPE, field.getValue().getType());
        properties.putAll(field.getValue().getOtherProperties());
        return properties;
    }

    private List<SchemaRequest.Update> createSolrDeleteFields(IndexSchema oldSchema) {
        List<SchemaRequest.Update> fields = new ArrayList<>();
        //TODO:add a config to define the default required field which should not be deleted. (e.g. id, there are other solr specific fields like _version_)
        oldSchema.getFields().entrySet().stream().filter(field -> !(field.getKey().equals(oldSchema.getUniqueKey()) ||
                field.getKey().equals(IndexSchemaField.FIELD_VERSION))).forEach(field -> {
            SchemaRequest.DeleteField deleteFieldRequest = new SchemaRequest.DeleteField(field.getKey());
            fields.add(deleteFieldRequest);
        });
        return fields;
    }

    @Override
    public IndexSchema getIndexSchema(String table)
            throws IndexerException, IndexSchemaNotFoundException {
        SolrClient client = getIndexerClient();
        String tableNameWithTenantDomain = IndexerUtils.getTableNameWithDomainName(table);
        IndexSchema indexSchema = indexSchemaCache.get(tableNameWithTenantDomain);
        if (indexSchema == null) {
            try {
                if (indexConfigsExists(table)) {
                    SchemaRequest.Fields fieldsRequest = new SchemaRequest.Fields();
                    SchemaRequest.UniqueKey uniqueKeyRequest = new SchemaRequest.UniqueKey();
                    SchemaResponse.FieldsResponse fieldsResponse = fieldsRequest.process(client, table);
                    SchemaResponse.UniqueKeyResponse uniqueKeyResponse = uniqueKeyRequest.process(client, table);
                    List<Map<String, Object>> fields = fieldsResponse.getFields();
                    String uniqueKey = uniqueKeyResponse.getUniqueKey();
                    indexSchema = createIndexSchemaFromSolrSchema(uniqueKey, fields);
                    indexSchemaCache.put(tableNameWithTenantDomain, indexSchema);
                } else {
                    throw new IndexSchemaNotFoundException("Index schema for table: " + table + "is not found");
                }
            } catch (SolrServerException | IOException | SolrException e) {
                log.error("error while retrieving the index schema for table: " + table + ": " + e.getMessage(), e);
                throw new IndexerException("error while retrieving the index schema for table: " + table + ": " + e.getMessage(), e);
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
        String type = IndexSchema.TYPE_STRING; //the default type in case if the type is not provided
        String fieldName;
        for (Map<String, Object> fieldProperties : fields) {
            if (fieldProperties != null && fieldProperties.containsKey(IndexSchemaField.ATTR_FIELD_NAME)) {
                fieldName = fieldProperties.remove(IndexSchemaField.ATTR_FIELD_NAME).toString();
                if (fieldProperties.containsKey(IndexSchemaField.ATTR_INDEXED)) {
                    isIndexed = (Boolean) fieldProperties.remove(IndexSchemaField.ATTR_INDEXED);
                }
                if (fieldProperties.containsKey(IndexSchemaField.ATTR_STORED)) {
                    isStored = (Boolean) fieldProperties.remove(IndexSchemaField.ATTR_STORED);
                }
                if (fieldProperties.containsKey(IndexSchemaField.ATTR_TYPE)) {
                    type = (String) fieldProperties.remove(IndexSchemaField.ATTR_TYPE);
                }
                indexFields.put(fieldName, new IndexSchemaField(fieldName, isStored, isIndexed, type, fieldProperties));
            } else {
                throw new IndexerException("Fields must have an attribute called " + IndexSchemaField.ATTR_FIELD_NAME);
            }
        }
        return indexFields;
    }

    @Override
    public boolean deleteIndexForTable(String table) throws IndexerException {
        try {
            if (indexExists(table)) {
                String tableNameWithTenant = IndexerUtils.getTableNameWithDomainName(table);
                CollectionAdminRequest.Delete deleteRequest = CollectionAdminRequest.deleteCollection(tableNameWithTenant);
                CollectionAdminResponse deleteRequestResponse =
                        deleteRequest.process(getIndexerClient(), tableNameWithTenant);
                if (deleteRequestResponse.isSuccess() && indexConfigsExists(table)) {
                    ConfigSetAdminRequest.Delete configSetAdminRequest = new ConfigSetAdminRequest.Delete();
                    configSetAdminRequest.setConfigSetName(tableNameWithTenant);
                    ConfigSetAdminResponse configSetResponse = configSetAdminRequest.process(getIndexerClient());
                    indexSchemaCache.remove(tableNameWithTenant);
                    Object errors = configSetResponse.getErrorMessages();
                    if (configSetResponse.getStatus() == 0 && errors == null) {
                        return true;
                    } else {
                        throw new IndexerException("Error in deleting index for table: " + table + ", " +
                                                   ", Response code: " + configSetResponse.getStatus() + " , errors: " + errors.toString());
                    }
                }
            }
        } catch (IOException | SolrServerException e) {
            log.error("error while deleting the index for table: " + table + ": " + e.getMessage(), e);
            throw new IndexerException("error while deleting the index for table: " + table + ": " + e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean indexExists(String table) throws IndexerException {
        CollectionAdminRequest.List listRequest = CollectionAdminRequest.listCollections();
        String tableWithTenant = IndexerUtils.getTableNameWithDomainName(table);
        try {
            CollectionAdminResponse listResponse = listRequest.process(getIndexerClient());
            Object errors = listResponse.getErrorMessages();
            if (listResponse.getStatus() == 0 && errors == null) {
                List collections = (List) listResponse.getResponse().get(ATTR_COLLECTIONS);
                return collections.contains(tableWithTenant);
            } else {
                throw new IndexerException("Error in checking index for table: " + table + ", " +
                        ", Response code: " + listResponse.getStatus() + " , errors: " + errors.toString());
            }
        } catch (IOException | SolrServerException e) {
            log.error("Error while checking the existence of index for table : " + table, e);
            throw new IndexerException("Error while checking the existence of index for table : " + table, e);
        }
    }

    @Override
    public boolean indexConfigsExists(String table) throws IndexerException {
        ConfigSetAdminResponse.List listRequestReponse;
        CarbonIndexerClient carbonIndexerClient = getIndexerClient();
        String tableNameWithTenantDomain = IndexerUtils.getTableNameWithDomainName(table);
        ConfigSetAdminRequest.List listRequest = new ConfigSetAdminRequest.List();
        try {
            listRequestReponse = listRequest.process(carbonIndexerClient);
            Object errors = listRequestReponse.getErrorMessages();
            if (listRequestReponse.getStatus()== 0 && errors == null) {
                return listRequestReponse.getConfigSets().contains(tableNameWithTenantDomain);
            } else {
                throw new IndexerException("Error in checking the existance of index configuration for table: " + table + ", " +
                        ", Response code: " + listRequestReponse.getStatus() + " , errors: " + errors.toString());
            }
        } catch (IOException | SolrServerException e) {
            log.error("Error while checking if index configurations exists for table: " + table, e);
            throw new IndexerException("Error while checking if index configurations exists for table: " + table, e);
        }
    }

    @Override
    public void indexDocuments(String table, List<CarbonIndexDocument> docs) throws IndexerException {
        try {
            CarbonIndexerClient client = getIndexerClient();
            client.add(table, IndexerUtils.getSolrInputDocuments(docs));
            //TODO:should find a better way to commit, there are different overloaded methods of commit
            client.commit(table);
        } catch (SolrServerException | IOException e) {
            log.error("Error while inserting the documents to index for table: " + table, e);
            throw new IndexerException("Error while inserting the documents to index for table: " + table, e);
        }
    }

    @Override
    public void deleteDocuments(String table, List<String> ids) throws IndexerException {
        if (ids != null && !ids.isEmpty()) {
            CarbonIndexerClient client = getIndexerClient();
            try {
                //TODO:use updateResponse
                client.deleteById(table, ids);
                client.commit(table);
            } catch (SolrServerException | IOException e) {
                log.error("Error while deleting index documents by ids, " + e.getMessage(), e);
                throw new IndexerException("Error while deleting index documents by ids, " + e.getMessage(), e);
            }
        }
    }

    @Override
    public void deleteDocuments(String table, String query) throws IndexerException {
        if (query != null && !query.isEmpty()) {
            CarbonIndexerClient client = getIndexerClient();
            try {
                //TODO:use updateResponse
                client.deleteByQuery(table, query);
                client.commit(table);
            } catch (SolrServerException | IOException e) {
                log.error("Error while deleting index documents by query, " + e.getMessage(), e);
                throw new IndexerException("Error while deleting index documents by query, " + e.getMessage(), e);
            }
        }
    }

    @Override
    public void destroy() throws IndexerException {
        try {
            if (indexerClient != null) {
                indexerClient.close();
            }
        } catch (IOException e) {
            log.error("Error while destroying the indexer service, " + e.getMessage(), e);
            throw new IndexerException("Error while destroying the indexer service, " + e.getMessage(), e);
        }
        indexerClient = null;
    }
}
