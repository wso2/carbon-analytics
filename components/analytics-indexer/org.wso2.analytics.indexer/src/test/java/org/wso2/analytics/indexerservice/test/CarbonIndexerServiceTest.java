package org.wso2.analytics.indexerservice.test;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.analytics.indexerservice.CarbonIndexDocument;
import org.wso2.analytics.indexerservice.CarbonIndexerService;
import org.wso2.analytics.indexerservice.IndexSchema;
import org.wso2.analytics.indexerservice.IndexSchemaField;
import org.wso2.analytics.indexerservice.exceptions.IndexSchemaNotFoundException;
import org.wso2.analytics.indexerservice.exceptions.IndexerException;
import org.wso2.analytics.indexerservice.impl.CarbonIndexerClient;
import org.wso2.analytics.indexerservice.utils.IndexerUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * This class contains the unit tests for indexer service;
 */
public class CarbonIndexerServiceTest {

    private CarbonIndexerService indexerService;


    @BeforeClass
    public void init() throws IndexerException {
        System.setProperty(IndexerUtils.WSO2_ANALYTICS_INDEX_CONF_DIRECTORY_SYS_PROP, "src/test/resources/conf");
        ServiceLoader<CarbonIndexerService> analyticsIndexServiceServiceLoader = ServiceLoader.load(CarbonIndexerService.class);
        if (this.indexerService == null) {
            this.indexerService = analyticsIndexServiceServiceLoader.iterator().next();
            if (this.indexerService == null) {
                throw new IndexerException("Indexer Service cannot be loaded!");
            }
        }
    }
    @Test
    public void testCreateIndexForTable() throws IndexerException {
        indexerService.createIndexForTable("T1");
        Assert.assertTrue(indexerService.indexExists("T1"));
    }

    @Test(dependsOnMethods = "testCreateIndexForTable")
    public void testCreateExistingIndex() throws IndexerException {
        Assert.assertTrue(indexerService.indexExists("T1"));
        Assert.assertFalse(indexerService.createIndexForTable("T1"));
    }

    @Test(dependsOnMethods = "testCreateExistingIndex")
    public void testIfInitialConfigsAreCopied() throws IndexerException {
        Assert.assertTrue(indexerService.indexConfigsExists("T1"));
    }

    @Test(dependsOnMethods = "testIfInitialConfigsAreCopied", expectedExceptions = IndexSchemaNotFoundException.class)
    public void testNonExistingIndexSchema() throws IndexerException, IndexSchemaNotFoundException {
        indexerService.getIndexSchema("T2");

    }

    @Test(dependsOnMethods = "testNonExistingIndexSchema")
    public void testInitialIndexSchema() throws IndexerException, IndexSchemaNotFoundException {
        IndexSchema indexSchema = indexerService.getIndexSchema("T1");
        Assert.assertEquals(indexSchema.getDefaultSearchField(), null, "Found a different default search field: " +
                indexSchema.getDefaultSearchField());
        Assert.assertEquals(indexSchema.getUniqueKey(), "id");
    }

    @Test(dependsOnMethods = "testInitialIndexSchema")
    public void testUpdateIndexSchemaWithoutMerge()
            throws IndexerException, IndexSchemaNotFoundException {
        Map<String, IndexSchemaField> fieldMap = new HashMap<>();
        IndexSchemaField intField = new IndexSchemaField("IntField", true, true, "int", null);
        IndexSchemaField longField = new IndexSchemaField("LongField", true, true, "long", null);
        IndexSchemaField floatField = new IndexSchemaField("FloatField", true, true, "float", null);
        IndexSchemaField doubleField = new IndexSchemaField("DoubleField", false, true, "double", null);
        IndexSchemaField boolField = new IndexSchemaField("BoolField", true, true, "boolean", null);
        IndexSchemaField timestamp = new IndexSchemaField("_timestamp", true, true, "long", null);
        fieldMap.put(intField.getFieldName(), intField);
        fieldMap.put(longField.getFieldName(), longField);
        fieldMap.put(doubleField.getFieldName(), doubleField);
        fieldMap.put(floatField.getFieldName(), floatField);
        fieldMap.put(boolField.getFieldName(), boolField);
        fieldMap.put(timestamp.getFieldName(), timestamp);
        IndexSchema indexSchema = new IndexSchema("id", "text", fieldMap);
        Assert.assertTrue(indexerService.updateIndexSchema("T1", indexSchema, false));
        IndexSchema indexSchema1 = indexerService.getIndexSchema("T1");
        Assert.assertEquals(indexSchema1.getField("IntField"), indexSchema.getField("IntField"));
        Assert.assertEquals(indexSchema1.getField("LongField"), indexSchema.getField("LongField"));
        Assert.assertEquals(indexSchema1.getField("DoubleField"), indexSchema.getField("DoubleField"));
        Assert.assertEquals(indexSchema1.getField("FloatField"), indexSchema.getField("FloatField"));
        Assert.assertEquals(indexSchema1.getField("BoolField"), indexSchema.getField("BoolField"));
        Assert.assertEquals(indexSchema1.getField("_timestamp"), indexSchema.getField("_timestamp"));
    }

    @Test(dependsOnMethods = "testUpdateIndexSchemaWithoutMerge")
    public void testUpdateIndexSchemaWithMerge()
            throws IndexerException, IndexSchemaNotFoundException {
        IndexSchema oldIndexSchema = indexerService.getIndexSchema("T1");
        Map<String, IndexSchemaField> fieldMap = new HashMap<>();
        IndexSchemaField intField = new IndexSchemaField("IntField1", true, true, "int", null);
        IndexSchemaField longField = new IndexSchemaField("LongField1", true, true, "long", null);
        IndexSchemaField floatField = new IndexSchemaField("FloatField1", true, true, "float", null);
        IndexSchemaField doubleField = new IndexSchemaField("DoubleField1", false, true, "double", null);
        IndexSchemaField boolField = new IndexSchemaField("BoolField1", true, true, "boolean", null);
        fieldMap.put(intField.getFieldName(), intField);
        fieldMap.put(longField.getFieldName(), longField);
        fieldMap.put(doubleField.getFieldName(), doubleField);
        fieldMap.put(floatField.getFieldName(), floatField);
        fieldMap.put(boolField.getFieldName(), boolField);
        IndexSchema indexSchema = new IndexSchema("id", "text", fieldMap);
        Assert.assertTrue(indexerService.updateIndexSchema("T1", indexSchema, true));
        IndexSchema newIndexSchema = indexerService.getIndexSchema("T1");
        Assert.assertEquals(newIndexSchema.getField("IntField1"), indexSchema.getField("IntField1"));
        Assert.assertEquals(newIndexSchema.getField("LongField1"), indexSchema.getField("LongField1"));
        Assert.assertEquals(newIndexSchema.getField("DoubleField1"), indexSchema.getField("DoubleField1"));
        Assert.assertEquals(newIndexSchema.getField("FloatField1"), indexSchema.getField("FloatField1"));
        Assert.assertEquals(newIndexSchema.getField("BoolField1"), indexSchema.getField("BoolField1"));

        Assert.assertEquals(newIndexSchema.getField("IntField"), oldIndexSchema.getField("IntField"));
        Assert.assertEquals(newIndexSchema.getField("LongField"), oldIndexSchema.getField("LongField"));
        Assert.assertEquals(newIndexSchema.getField("DoubleField"), oldIndexSchema.getField("DoubleField"));
        Assert.assertEquals(newIndexSchema.getField("FloatField"), oldIndexSchema.getField("FloatField"));
        Assert.assertEquals(newIndexSchema.getField("BoolField"), oldIndexSchema.getField("BoolField"));
        Assert.assertEquals(newIndexSchema.getField("_timestamp"), oldIndexSchema.getField("_timestamp"));
    }

    @Test(dependsOnMethods = "testUpdateIndexSchemaWithMerge")
    public void testIndexDocuments() throws IndexerException, IOException, SolrServerException {
        CarbonIndexDocument doc1 = new CarbonIndexDocument();
        doc1.addField("id", "1");
        doc1.addField("_timestamp", System.currentTimeMillis());
        doc1.addField("IntField", 100);
        doc1.addField("LongField", 100l);
        doc1.addField("FloatField", 100f);
        doc1.addField("DoubleField", 100d);
        doc1.addField("BoolField", true);
        CarbonIndexDocument doc2 = new CarbonIndexDocument();
        doc2.addField("id", "2");
        doc2.addField("IntField1", 1000);
        doc2.addField("LongField1", 1000l);
        doc2.addField("FloatField1", 1000f);
        doc2.addField("DoubleField1", 1000d);
        doc2.addField("BoolField1", true);
        List<CarbonIndexDocument> docs = new ArrayList<>();
        docs.add(doc1);
        docs.add(doc2);
        indexerService.indexDocuments("T1", docs);

        CarbonIndexerClient client = indexerService.getIndexerClient();
        SolrQuery query = new SolrQuery();
        query.setQuery("id:1");

        QueryResponse response = client.query("T1", query);
        SolrDocumentList list = response.getResults();
        Assert.assertEquals(list.size(), 1);
        Assert.assertEquals(doc1.getFieldValue("id"), list.get(0).getFieldValue("id"));
        Assert.assertEquals(doc1.getFieldValue("DoubleField"), list.get(0).getFieldValue("DoubleField"));

        query = new SolrQuery();
        query.setQuery("id:2");
        QueryResponse response1 = client.query("T1", query);
        SolrDocumentList list1 = response1.getResults();
        Assert.assertEquals(list1.size(), 1);
        Assert.assertEquals(doc2.getFieldValue("id"), list1.get(0).getFieldValue("id"));
    }

    @Test(dependsOnMethods = "testIndexDocuments")
    public void testDeleteDocumentsByTimeRange()
            throws IndexerException, IOException, SolrServerException {
        String strQuery = "_timestamp:[0 TO " + System.currentTimeMillis() + "]";
        indexerService.deleteDocuments("T1", strQuery);
        CarbonIndexerClient client = indexerService.getIndexerClient();
        SolrQuery query = new SolrQuery();
        query.setQuery(strQuery);
        QueryResponse response = client.query("T1", query);
        SolrDocumentList list = response.getResults();
        Assert.assertTrue(list.isEmpty());
    }

    @Test(dependsOnMethods = "testDeleteDocumentsByTimeRange")
    public void testDeleteDocumentsByIds() throws IndexerException, IOException,
                                                  SolrServerException {
        List<String> ids = new ArrayList<>();
        ids.add("2");
        indexerService.deleteDocuments("T1",ids);
        CarbonIndexerClient client = indexerService.getIndexerClient();
        SolrQuery query = new SolrQuery();
        query.setQuery("id:2");
        QueryResponse response = client.query("T1", query);
        SolrDocumentList list = response.getResults();
        Assert.assertTrue(list.isEmpty());
    }

    @Test(dependsOnMethods = "testDeleteDocumentsByIds")
    public void testDeleteIndexForTable() throws IndexerException {
        Assert.assertTrue(indexerService.deleteIndexForTable("T1"));
    }

    @Test(dependsOnMethods = "testDeleteIndexForTable")
    public void testDeleteNonExistingIndex() throws IndexerException {
        Assert.assertFalse(indexerService.deleteIndexForTable("T1"));
    }

    @Test(dependsOnMethods = "testDeleteNonExistingIndex")
    public void testDestroy() throws IndexerException {
        indexerService.destroy();
    }
}
