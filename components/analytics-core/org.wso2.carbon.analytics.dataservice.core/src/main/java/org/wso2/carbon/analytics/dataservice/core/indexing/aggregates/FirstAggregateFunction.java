package org.wso2.carbon.analytics.dataservice.core.indexing.aggregates;

import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;

/**
 * This class represents the FIRST() aggregate
 */
public class FirstAggregateFunction implements AggregateFunction {

    private final static String AGGREGATE_NAME = "FIRST";
    private Object firstValue;
    @Override
    public void process(RecordValuesContext ctx, String[] aggregateFields)
            throws AnalyticsException {
        if (firstValue == null) {
            firstValue = ctx.getValue(aggregateFields[0]);
        }
    }

    @Override
    public Object finish() throws AnalyticsException {
        return firstValue;
    }

    @Override
    public String getAggregateName() {
        return AGGREGATE_NAME;
    }
}
