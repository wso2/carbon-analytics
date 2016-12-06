package org.wso2.analytics.indexerservice;

import org.wso2.analytics.indexerservice.exceptions.IndexerException;
import org.wso2.analytics.indexerservice.impl.CarbonIndexerClient;

/**
 * This class represents the indexer interface. This can be used to implement the indexing implementation based on Solr
 */
public interface CarbonIndexerService {
    /**
     * Returns the indexingClient for the specific tenant's table. Can be used to add, delete, update query/perform searches the tables' index
     * @param table The tableName
     * @param tenantDomain The domain name representing the tenant
     * @return {@link org.wso2.analytics.indexerservice.impl.CarbonIndexerClient} A wrapper for {@link org.apache.solr.client.solrj.SolrClient}
     * @throws IndexerException Exception thrown if something goes wrong while creating or retrieving the client.
     */
    public CarbonIndexerClient getIndexerClient(String table, String tenantDomain) throws IndexerException;

    /**
     * Create the Index/core/collection for the given table
     * @param table The table for which the index is created.
     * @param tenantDomain Tenant domain to which the table belongs.
     * @return Returns true if successful, otherwise false
     * @throws IndexerException Exception thrown if something goes wrong while creating the index.
     */
    public boolean createIndexForTable(String table, String tenantDomain) throws IndexerException;

    /**
     * Update or create the schema for the index of a specific table.
     * @param table Tablename of which the schema of the index being created
     * @param tenantDomain the tenant domain name to which the table belongs
     * @param indexSchema The indexing Schema which represents the solr schema for the solr index/collection
     * @return returns true if successful, otherwise false
     * @throws IndexerException Exception thrown if something goes wrong while updating the index schema.
     */
    public boolean updateIndexSchema(String table, String tenantDomain, IndexSchema indexSchema, boolean merge) throws IndexerException;

    /**
     * Returns the indexSchema of a table of a tenant domain
     * @param table Name of the table
     * @param tenantDomain Name of the tenant domain to which the table belongs
     * @return {@link org.wso2.analytics.indexerservice.IndexSchema}
     * @throws IndexerException Exception thrown if something goes wrong while retrieving the indexSchema
     */
    public IndexSchema getIndexSchema(String table, String tenantDomain) throws IndexerException;

    /**
     * Delete the index for a specific table in a tenant domain. The schema also will be deleted.
     * @param table Name of the table of which the index should be deleted
     * @param tenantDomain the tenant domain to which the table belongs
     * @return return true if successful, otherwise false
     * @throws IndexerException Exception thrown if something goes wrong while deleting the index.
     */
    public boolean deleteIndexForTable(String table, String tenantDomain) throws IndexerException;
}
