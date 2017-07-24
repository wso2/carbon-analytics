/*
 *  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.analytics.dataservice.core;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.util.BytesRefBuilder;
import org.apache.lucene.util.NumericUtils;
import org.wso2.carbon.analytics.dataservice.core.indexing.AnalyticsDataIndexer;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.ColumnDefinition;

import java.text.SimpleDateFormat;
import java.util.Map;

/**
 * This represents a custom {@link QueryParser} implementation, with index data type awareness,
 * specifically, in handling numbers.
 */
public class AnalyticsQueryParser extends QueryParser {

    private Map<String, ColumnDefinition> indices;
    
    public AnalyticsQueryParser(Analyzer analyzer, Map<String, ColumnDefinition> indices) {
        super(null, analyzer);
        this.indices = indices;
    }
    
    @Override
    public Query getRangeQuery(String field, String part1, String part2, boolean si, boolean ei) throws ParseException {
        AnalyticsSchema.ColumnType type = null;
        ColumnDefinition column = this.indices.get(field);
        if (column != null) {
            type = column.getType();
        }
        if (type == null) {
            /* check for special fields */
            if (AnalyticsDataIndexer.INDEX_ID_INTERNAL_FIELD.equals(field)) {
                type = AnalyticsSchema.ColumnType.STRING;
            } else if (AnalyticsDataIndexer.INDEX_INTERNAL_TIMESTAMP_FIELD.equals(field)) {
                type = AnalyticsSchema.ColumnType.LONG;
            } 
        }
        if (type != null) {
            switch (type) {
            case STRING:
                return super.getRangeQuery(field, part1, part2, si, ei);
            case INTEGER:
                try {
                    return NumericRangeQuery.newIntRange(field, Integer.parseInt(part1), Integer.parseInt(part2), si, ei);
                } catch (NumberFormatException e) {
                    throw new ParseException("Invalid query, the field '" + field + "' must contain integers");
                }
            case LONG:
                try {
                    return NumericRangeQuery.newLongRange(field, this.parseTimestampOrDirectLong(part1), 
                            this.parseTimestampOrDirectLong(part2), si, ei);
                } catch (NumberFormatException e) {
                    throw new ParseException("Invalid query, the field '" + field + "' must contain long values");
                }
            case DOUBLE:
                try {
                    return NumericRangeQuery.newDoubleRange(field, Double.parseDouble(part1), Double.parseDouble(part2), si, ei);
                } catch (NumberFormatException e) {
                    throw new ParseException("Invalid query, the field '" + field + "' must contain double values");
                }
            case FLOAT:
                try {
                    return NumericRangeQuery.newFloatRange(field, Float.parseFloat(part1), Float.parseFloat(part2), si, ei);
                } catch (NumberFormatException e) {
                    throw new ParseException("Invalid query, the field '" + field + "' must contain float values");
                }
            case BOOLEAN:
                /* treated as a string (should have values "true", "false") */
                return super.getRangeQuery(field, part1, part2, si, ei);
            default:
                return super.getRangeQuery(field, part1, part2, si, ei);
            }
        } else {
            return super.getRangeQuery(field, part1, part2, si, ei);
        }
    }

    @Override
    public Query newTermQuery(org.apache.lucene.index.Term term) {
        String field = term.field();
        if (field == null) {
            throw new RuntimeException("Invalid query, a term must have a field");
        }
        AnalyticsSchema.ColumnType type = null;
        ColumnDefinition column = this.indices.get(field);
        if (column != null) {
            type = column.getType();
        }
        if (type == null) {
            /* check for special fields */
            if (AnalyticsDataIndexer.INDEX_ID_INTERNAL_FIELD.equals(field)) {
                type = AnalyticsSchema.ColumnType.STRING;
            } else if (AnalyticsDataIndexer.INDEX_INTERNAL_TIMESTAMP_FIELD.equals(field)) {
                type = AnalyticsSchema.ColumnType.LONG;
            } 
        }
        if (type != null) {
            switch (type) {
            case STRING:
                return super.newTermQuery(term);
            case INTEGER:
                try {
                    int value = Integer.parseInt(term.text());
                    BytesRefBuilder bytes = new BytesRefBuilder();
                    NumericUtils.intToPrefixCoded(value, 0, bytes);
                    return new TermQuery(new Term(term.field(), bytes.toBytesRef().utf8ToString()));
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Invalid query, the field '" + field + "' must contain integers");
                }
            case LONG:
                try {
                    long value = this.parseTimestampOrDirectLong(term.text());
                    BytesRefBuilder bytes = new BytesRefBuilder();
                    NumericUtils.longToPrefixCoded(value, 0, bytes);
                    return new TermQuery(new Term(term.field(), bytes.toBytesRef().utf8ToString()));
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Invalid query, the field '" + field + "' must contain long values");
                }
            case DOUBLE:
                try {
                    double value = Double.parseDouble(term.text());
                    BytesRefBuilder bytes = new BytesRefBuilder();
                    NumericUtils.longToPrefixCoded(NumericUtils.doubleToSortableLong(value), 0, bytes);
                    return new TermQuery(new Term(term.field(), bytes.toBytesRef().utf8ToString()));
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Invalid query, the field '" + field + "' must contain double values");
                }
            case FLOAT:
                try {
                    float value = Float.parseFloat(term.text());
                    BytesRefBuilder bytes = new BytesRefBuilder();
                    NumericUtils.intToPrefixCoded(NumericUtils.floatToSortableInt(value), 0, bytes);
                    return new TermQuery(new Term(term.field(), bytes.toBytesRef().utf8ToString()));
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Invalid query, the field '" + field + "' must contain float values");
                }
            case BOOLEAN:
                return super.newTermQuery(term);
            default:
                return super.newTermQuery(term);
            }
        } else {
            return super.newTermQuery(term);
        }
    }
    
    private long parseTimestampOrDirectLong(String textValue) throws NumberFormatException {
        try {
            return Long.parseLong(textValue);
        } catch (NumberFormatException ignore) {
            try {
                return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").parse(textValue).getTime();
            } catch (java.text.ParseException ignore2) {
                try {
                    return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(textValue).getTime();
                } catch (java.text.ParseException ignore3) {
                    try {
                        return new SimpleDateFormat("yyyy-MM-dd").parse(textValue).getTime();
                    } catch (java.text.ParseException e) {
                        throw new RuntimeException("Error in parsing long/timestamp field '" + 
                                textValue + "' : " + e.getMessage());
                    }
                }
            }
        }
    }
    
}
