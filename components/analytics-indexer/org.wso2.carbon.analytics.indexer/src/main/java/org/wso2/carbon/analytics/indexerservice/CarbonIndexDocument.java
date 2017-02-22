package org.wso2.carbon.analytics.indexerservice;

import org.apache.solr.common.SolrInputDocument;
import org.wso2.carbon.analytics.indexerservice.utils.IndexerUtils;

import java.util.Map;

/**
 * This represents a Solr document which is created from a DAL record.
 */
public class CarbonIndexDocument extends SolrInputDocument {

    public CarbonIndexDocument(String... fields) {
        super(fields);
    }

    public CarbonIndexDocument(Map<String, CarbonIndexDocumentField> fields) {
        super(IndexerUtils.getSolrFields(fields));
    }
}
