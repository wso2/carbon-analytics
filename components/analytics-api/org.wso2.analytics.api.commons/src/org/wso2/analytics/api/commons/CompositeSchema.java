package org.wso2.analytics.api.commons;

import org.wso2.analytics.data.commons.service.AnalyticsSchema;
import org.wso2.analytics.indexerservice.IndexSchema;

/**
 * This class represents a composition of Index Schema and the table schema
 */
public class CompositeSchema {
    private AnalyticsSchema analyticsSchema;
    private IndexSchema indexSchema;

    public CompositeSchema() {
        analyticsSchema = new AnalyticsSchema();
        indexSchema = new IndexSchema();
    }

    public CompositeSchema(AnalyticsSchema analyticsSchema, IndexSchema indexSchema) {
        this.analyticsSchema = analyticsSchema;
        this.indexSchema = indexSchema;
    }

    public AnalyticsSchema getAnalyticsSchema() {
        return analyticsSchema;
    }

    public void setAnalyticsSchema(AnalyticsSchema analyticsSchema) {
        this.analyticsSchema = analyticsSchema;
    }

    public IndexSchema getIndexSchema() {
        return indexSchema;
    }

    public void setIndexSchema(IndexSchema indexSchema) {
        this.indexSchema = indexSchema;
    }
}
