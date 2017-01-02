package org.wso2.analytics.indexerservice.config;

/**
 * Represents the Indexing Server details to connect.
 */
public class IndexerConfiguration {
    private String solrServerUrl;
    private String solrServerPort;
    private int noOfShards;
    private int noOfReplicas;

    public String getSolrServerPort() {
        return solrServerPort;
    }

    public void setSolrServerPort(String solrServerPort) {
        this.solrServerPort = solrServerPort;
    }

    public String getSolrServerUrl() {
        return solrServerUrl;
    }

    public void setSolrServerUrl(String solrServerUrl) {
        this.solrServerUrl = solrServerUrl;
    }

    public String getSolrServerURLwithPort() {
        return solrServerUrl + ":" + solrServerPort;
    }

    public int getNoOfShards() {
        return noOfShards;
    }

    public void setNoOfShards(int noOfShards) {
        this.noOfShards = noOfShards;
    }

    public int getNoOfReplicas() {
        return noOfReplicas;
    }

    public void setNoOfReplicas(int noOfReplicas) {
        this.noOfReplicas = noOfReplicas;
    }
}
