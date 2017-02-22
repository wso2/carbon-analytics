package org.wso2.carbon.analytics.indexerservice;

import org.apache.solr.common.SolrInputField;

/**
 * This represents the IndexField which is input to solr index
 */
public class CarbonIndexDocumentField extends SolrInputField {

    public CarbonIndexDocumentField(String n) {
        super(n);
    }

}
