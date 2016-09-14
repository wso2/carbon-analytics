/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.collections.IteratorUtils;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDataResponse;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDataResponse.Entry;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.ColumnDefinition;
import org.wso2.carbon.analytics.datasource.commons.ColumnDefinitionExt;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.ndatasource.core.DataSourceService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Analytics data service utilities.
 */
public class AnalyticsDataServiceUtils {
    
    public static final String OPTION_SCORE_PARAM = "-sp";
    public static final String OPTION_IS_FACET = "-f";
    public static final String OPTION_IS_INDEXED = "-i";

    @SuppressWarnings("unchecked")
    public static List<Record> listRecords(AnalyticsDataService ads,
                                           AnalyticsDataResponse response) throws AnalyticsException {
        List<Record> result = new ArrayList<Record>();
        for (Entry entry : response.getEntries()) {
            result.addAll(IteratorUtils.toList(ads.readRecords(entry.getRecordStoreName(), entry.getRecordGroup())));
        }
        return result;
    }
    
    private static Map<String, ColumnDefinitionExt> translate(Map<String, ColumnDefinition> colDefs) {
        Map<String, ColumnDefinitionExt> result = new LinkedHashMap<>(colDefs.size());
        for (Map.Entry<String, ColumnDefinition> colDef : colDefs.entrySet()) {
            result.put(colDef.getKey(), ColumnDefinitionExt.copy(colDef.getValue()));
        }
        return result;
    }
    
    public static AnalyticsSchema createMergedSchema(AnalyticsSchema existingSchema, 
            List<String> primaryKeys, List<ColumnDefinition> columns, List<String> indices) {
        Set<String> newPrimaryKeys;
        if (existingSchema.getPrimaryKeys() == null) {
            newPrimaryKeys = new HashSet<String>();
        } else {
            newPrimaryKeys = new HashSet<String>(existingSchema.getPrimaryKeys());
        }
        newPrimaryKeys.addAll(primaryKeys);
        Map<String, ColumnDefinitionExt> newColumns;
        if (existingSchema.getColumns() == null) {
            newColumns = new LinkedHashMap<>();
        } else {
            newColumns = translate(existingSchema.getColumns());
        }
        ColumnDefinitionExt targetColumn;
        for (ColumnDefinition column : columns) {
            targetColumn = newColumns.get(column.getName());
            if (targetColumn == null) {
                targetColumn = ColumnDefinitionExt.copy(column);
                newColumns.put(targetColumn.getName(), targetColumn);
            } else {
                if (column.isIndexed()) {
                    targetColumn.setIndexed(true);
                }
                if (column.isScoreParam()) {
                    targetColumn.setScoreParam(true);
                }
                if (column.isFacet()) {
                    targetColumn.setFacet(true);
                }
            }
        }
        for (String index : indices) {
            processIndex(newColumns, index);
        }
        return new AnalyticsSchema(new ArrayList<ColumnDefinition>(newColumns.values()), 
                new ArrayList<String>(newPrimaryKeys));
    }
    
    public static void processIndex(Map<String, ColumnDefinitionExt> indexedColumns, String index) {
        String[] tokens = index.split(" ");
        String name = tokens[0].trim();
        ColumnDefinitionExt column = indexedColumns.get(name);
        if (column != null) {
            column.setIndexed(true);
            Set<String> options = new HashSet<String>();
            for (int i = 1; i < tokens.length; i++) {
                options.add(tokens[i]);
            }
            if (options.contains(OPTION_SCORE_PARAM)) {
                column.setScoreParam(true);
            }
            if (options.contains(OPTION_IS_FACET)) {
                column.setFacet(true);
            }
        }
    }
    
    public static List<String> tokenizeAndTrimToList(String value, String delimiter) {
        if (value == null) {
            return new ArrayList<String>(0);
        }
        value = value.trim();
        String[] tokens = value.split(delimiter);
        String token;
        List<String> result = new ArrayList<String>(tokens.length);
        for (int i = 0; i < tokens.length; i++) {
            token = tokens[i].trim();
            if (token.length() > 0) {
                result.add(token.trim());
            }
        }
        return result;
    }
    
	public static Iterator<Record> responseToIterator(AnalyticsDataService service, AnalyticsDataResponse response)
			throws AnalyticsException {
		return new ResponseIterator(service, response);
	}
    
    /**
     * This class exposes an {@link AnalyticsDataResponse} as a record iterator.
     */
    public static class ResponseIterator implements Iterator<Record> {
        
        private Entry[] entries;

        private Iterator<Record> itr;
        
        private AnalyticsDataService service;

        private int index = -1;

        public ResponseIterator(AnalyticsDataService service, AnalyticsDataResponse response)
                throws AnalyticsException {
            this.service = service;
            this.entries = response.getEntries().toArray(new Entry[0]);
        }

        @Override
        public boolean hasNext() {
            boolean result;
            if (this.itr == null) {
                result = false;
            } else {
                result = this.itr.hasNext();
            }
            if (result) {
                return true;
            } else {
                if (this.entries.length > this.index + 1) {
                    try {
                        this.index++;
                        this.itr = this.service.readRecords(this.entries[index].getRecordStoreName(), 
                                this.entries[index].getRecordGroup());
                    } catch (AnalyticsException e) {
                        throw new IllegalStateException("Error in traversing record group: " + e.getMessage(), e);
                    }
                    return this.hasNext();
                } else {
                    return false;
                }
            }
        }

        @Override
        public Record next() {
            if (this.hasNext()) {
                return this.itr.next();
            } else {
                return null;
            }
        }

        @Override
        public void remove() {
            /* ignored */
        }
    }

    /**
     * This method is used to identify if the current JVM in which the AnalyticsDataService is instantiated,
     * is a carbon server or not.
     * @return true if its a carbon server
     */
    public static boolean isCarbonServer() {
        boolean isCarbonServer = true;
        DataSourceService dataSourceService = AnalyticsServiceHolder.getDataSourceService();
        if (dataSourceService == null) {
            isCarbonServer = false;
        }
        return isCarbonServer;
    }

}
