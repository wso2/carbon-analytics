package org.wso2.analytics.indexerservice.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents the Indexing Server details to connect.
 */
@XmlRootElement(name = "indexer-config")
public class IndexerConfiguration {
    private final static String DEFAULT_SOLR_URL = "localhost:9983";
    private final static String BASE_CONFIG_SET = "gettingstarted";
    private final static String DEFAULT_NO_OF_SHARDS = "2";
    private final static String DEFAULT_NO_OF_REPLICA = "1";
    private String solrServerUrl;
    private int noOfShards;
    private int noOfReplicas;
    private String baseConfigSet;

    @XmlElement(name = "solr-cloud-url", defaultValue = DEFAULT_SOLR_URL )
    public String getSolrServerUrl() {
        return solrServerUrl;
    }

    public void setSolrServerUrl(String solrServerUrl) {
        this.solrServerUrl = solrServerUrl;
    }

    @XmlElement(name = "no-of-shards", defaultValue = DEFAULT_NO_OF_SHARDS)
    public int getNoOfShards() {
        return noOfShards;
    }

    public void setNoOfShards(int noOfShards) {
        this.noOfShards = noOfShards;
    }

    @XmlElement(name = "no-of-replica", defaultValue = DEFAULT_NO_OF_REPLICA)
    public int getNoOfReplicas() {
        return noOfReplicas;
    }

    public void setNoOfReplicas(int noOfReplicas) {
        this.noOfReplicas = noOfReplicas;
    }

    @XmlElement(name = "base-config-set", defaultValue = BASE_CONFIG_SET)
    public String getBaseConfigSet() {
        return baseConfigSet;
    }

    public void setBaseConfigSet(String baseConfigSet) {
        this.baseConfigSet = baseConfigSet;
    }
}
