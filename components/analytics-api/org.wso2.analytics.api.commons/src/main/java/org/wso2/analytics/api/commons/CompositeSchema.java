package org.wso2.analytics.api.commons;

import org.wso2.analytics.data.commons.service.AnalyticsSchema;
import org.wso2.analytics.indexerservice.IndexSchema;

import java.io.Serializable;

/**
 * This class represents a composition of Index Schema and the table schema
 */
public class CompositeSchema implements Serializable {

    private static final long serialVersionUID = 5502344378308773308L;
    private AnalyticsSchema analyticsSchema;
    private IndexSchema indexSchema;

    public CompositeSchema() {
        analyticsSchema = new AnalyticsSchema();
        indexSchema = new IndexSchema();
    }

    public CompositeSchema(AnalyticsSchema analyticsSchema, IndexSchema indexSchema) {
        if (analyticsSchema == null) {
            this.analyticsSchema = new AnalyticsSchema();
        } else {
            this.analyticsSchema = analyticsSchema;
        }
        if (indexSchema == null) {
            this.indexSchema = new IndexSchema();
        } else {
            this.indexSchema = indexSchema;
        }
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CompositeSchema)) {
            return false;
        }

        CompositeSchema that = (CompositeSchema) o;

        if (analyticsSchema != null ? !analyticsSchema.equals(that.analyticsSchema) : that.analyticsSchema != null) {
            return false;
        }
        if (indexSchema != null ? !indexSchema.equals(that.indexSchema) : that.indexSchema != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = analyticsSchema != null ? analyticsSchema.hashCode() : 0;
        result = 31 * result + (indexSchema != null ? indexSchema.hashCode() : 0);
        return result;
    }
}
