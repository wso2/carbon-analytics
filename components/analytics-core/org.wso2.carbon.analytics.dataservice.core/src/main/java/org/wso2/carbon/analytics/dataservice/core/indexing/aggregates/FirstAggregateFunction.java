package org.wso2.carbon.analytics.dataservice.core.indexing.aggregates;

import org.wso2.carbon.analytics.dataservice.commons.Constants;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;

/**
 * This class represents the FIRST() aggregate which returns the specified field value of the first
 * matching record in a specific group.
 */
public class FirstAggregateFunction implements AggregateFunction {

    private Object firstValue;
    @Override
    public void process(RecordContext ctx, String[] aggregateFields)
            throws AnalyticsException {
        if (aggregateFields == null || aggregateFields.length == 0) {
            throw new AnalyticsException("Field to be aggregated, is missing");
        }
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
        return Constants.FIRST_AGGREGATE;
    }
}
