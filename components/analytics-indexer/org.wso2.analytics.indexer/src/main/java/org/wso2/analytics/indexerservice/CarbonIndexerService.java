package org.wso2.analytics.indexerservice;

import org.wso2.analytics.indexerservice.exceptions.IndexSchemaNotFoundException;
import org.wso2.analytics.indexerservice.exceptions.IndexerException;
import org.wso2.analytics.indexerservice.impl.CarbonIndexerClient;

import java.io.IOException;
import java.util.List;

/**
 * This class represents the indexer interface. This can be used to implement the indexing implementation based on Solr
 */
public interface CarbonIndexerService {
    /**
     * Returns the indexingClient for the specific tenant's table. Can be used to add, delete, update query/perform searches the tables' index
     * @return {@link org.wso2.analytics.indexerservice.impl.CarbonIndexerClient} A wrapper for {@link org.apache.solr.client.solrj.SolrClient}
     * @throws IndexerException Exception thrown if something goes wrong while creating or retrieving the client.
     */
    public CarbonIndexerClient getIndexerClient() throws IndexerException;

    /**
     * Create the Index/core/collection for the given table
     * @param table The table for which the index is created.
     * @return Returns true if successful, otherwise false
     * @throws IndexerException Exception thrown if something goes wrong while creating the index.
     */
    public boolean createIndexForTable(String table)
            throws IndexerException;

    /**
     * Update or create the schema for the index of a specific table.
     * @param table Tablename of which the schema of the index being created
     * @param indexSchema The indexing Schema which represents the solr schema for the solr index/collection
     * @return returns true if successful, otherwise false
     * @throws IndexerException Exception thrown if something goes wrong while updating the index schema.
     */
    public boolean updateIndexSchema(String table, IndexSchema indexSchema, boolean merge) throws IndexerException;

    /**
     * Returns the indexSchema of a table of a tenant domain
     * @param table Name of the table
     * @return {@link org.wso2.analytics.indexerservice.IndexSchema}
     * @throws IndexerException Exception thrown if something goes wrong while retrieving the indexSchema
     */
    public IndexSchema getIndexSchema(String table)
            throws IndexerException, IndexSchemaNotFoundException;

    /**
     * Delete the index for a specific table in a tenant domain. The schema also will be deleted.
     * @param table Name of the table of which the index should be deleted
     * @return return true if successful, otherwise false
     * @throws IndexerException Exception thrown if something goes wrong while deleting the index.
     */
    public boolean deleteIndexForTable(String table) throws IndexerException;

    /**
     * Checks if the index for a specific table exists or not.
     *
     * @param table Name of the table for the index being checked
     * @return True if there is an index for the given table, otherwise false
     * @throws IndexerException Exception is thrown if something goes wrong.
     */
    public boolean indexExists(String table) throws IndexerException;

    /**
     * Checks if the index configuration exists or not.
     *
     * @param table The name of the table for the index being checked
     * @return True of the configurations exists otherwise false
     * @throws IndexerException Exceptions is thrown if something goes wrong.
     */
    public boolean indexConfigsExists(String table) throws IndexerException;

    /**
     * Inserts records as Solr documents to Solr index.
     * @param table The name of the table from which the documents/records are indexed
     * @param docs Documents which represents the records
     * @throws IndexerException Exceptions is thrown if something goes wrong.
     */
    public void indexDocuments(String table, List<CarbonIndexDocument> docs) throws IndexerException;

    /**
     * Delete index documents by given document/record ids
     * @param table the name of the table to which the records belong
     * @param ids list of ids of records to be deleted
     * @throws IndexerException
     */
    public void deleteDocuments(String table, List<String> ids) throws IndexerException;

    /**
     * Delete index documents which match the given solr query
     * @param table the name of the table to which the records belong
     * @param query the solr query to filter out the records to be deleted
     * @throws IndexerException
     */
    public void deleteDocuments(String table, String query) throws IndexerException;

    /**
     * Closes the internally maintained Solr clients
     * @throws IOException
     */
    public void destroy() throws IndexerException;

}
