/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.analytics.jsservice;

import org.apache.axiom.om.util.Base64;
import org.wso2.carbon.analytics.jsservice.beans.AnalyticsSchemaBean;
import org.wso2.carbon.analytics.jsservice.beans.ColumnTypeBean;
import org.wso2.carbon.analytics.jsservice.beans.IndexConfigurationBean;
import org.wso2.carbon.analytics.jsservice.beans.IndexTypeBean;
import org.wso2.carbon.analytics.jsservice.beans.Record;
import org.wso2.carbon.analytics.webservice.stub.beans.AnalyticsCategoryPathBean;
import org.wso2.carbon.analytics.webservice.stub.beans.IndexEntryBean;
import org.wso2.carbon.analytics.webservice.stub.beans.RecordBean;
import org.wso2.carbon.analytics.webservice.stub.beans.RecordValueEntryBean;
import org.wso2.carbon.analytics.webservice.stub.beans.SchemaColumnBean;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents a set of utility functionalities for the analytics REST API.
 */
public class Utils {

    private static final String DEFAUL_CHARSETT = "UTF-8";
    public static final int CATEGORYPATH_FIELD_COUNT = 2;
    public static final float DEFAUL_CATEGORYPATH_WEIGHT = 1.0f;

    public static List<Record> getRecordBeans(RecordBean[] recordBeans) {
        List<Record> records = new ArrayList<>();
        for (RecordBean recordBean : recordBeans) {
            Record record = new Record();
            record.setId(recordBean.getId());
            record.setTableName(recordBean.getTableName());
            record.setValues(validateAndReturn(recordBean.getValues()));
            records.add(record);
        }
        return records;
    }

    private static Map<String, Object> validateAndReturn(RecordValueEntryBean[] values) {
        Map<String, Object> columnValues = new LinkedHashMap<>();
        for (RecordValueEntryBean valueEntryBean : values) {
            switch (valueEntryBean.getType()) {
                case "INTEGER":
                    columnValues.put(valueEntryBean.getFieldName(), valueEntryBean.getIntValue());
                    break;
                case "DOUBLE":
                    columnValues.put(valueEntryBean.getFieldName(), valueEntryBean.getDoubleValue());
                    break;
                case "LONG":
                    columnValues.put(valueEntryBean.getFieldName(), valueEntryBean.getLongValue());
                    break;
                case "FLOAT":
                    columnValues.put(valueEntryBean.getFieldName(), valueEntryBean.getFloatValue());
                    break;
                case "STRING":
                    columnValues.put(valueEntryBean.getFieldName(), valueEntryBean.getStringValue());
                    break;
                case "BOOLEAN":
                    columnValues.put(valueEntryBean.getFieldName(), valueEntryBean.getBooleanValue());
                    break;
                case "FACET":
                    AnalyticsCategoryPathBean categoryPathBean = valueEntryBean.getAnalyticsCategoryPathBeanValue();
                    Map<String, Object> facet = new LinkedHashMap<>();
                    facet.put("path", categoryPathBean.getPath());
                    columnValues.put(valueEntryBean.getFieldName(), facet);
                    break;
                default:
                    columnValues.put(valueEntryBean.getFieldName(), valueEntryBean.getStringValue());
            }
        }
        return columnValues;
    }

    public static RecordBean[] getRecords(List<Record> recordBeans) {
        List<RecordBean> records = new ArrayList<>();
        for (Record recordBean : recordBeans) {
            RecordBean record = new RecordBean();
            record.setId(recordBean.getId());
            record.setTableName(recordBean.getTableName());
            record.setValues(getRecordValueEntryBeans(recordBean.getValues()));
        }
        return records.toArray(new  RecordBean[records.size()]);
    }

    private static AnalyticsCategoryPathBean validateAndReturn(Object value) {
        List<String> pathList = (ArrayList<String>) value;
        AnalyticsCategoryPathBean categoryPathBean = new AnalyticsCategoryPathBean();
        if (pathList != null && pathList.size() > 0) {
            String[] path = pathList.toArray(new String[pathList.size()]);
            categoryPathBean.setPath(path);
        }
        return categoryPathBean;
    }

    private static RecordValueEntryBean[] getRecordValueEntryBeans(Map<String, Object> values) {
        List<RecordValueEntryBean> recordValueEntryBeans = new ArrayList<>();
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            RecordValueEntryBean recordValueEntryBean = new RecordValueEntryBean();
            recordValueEntryBean.setFieldName(entry.getKey());
            Object value = entry.getValue();
            if (value instanceof Integer) {
                recordValueEntryBean.setType("INTEGER");
                recordValueEntryBean.setIntValue(((Integer) value).intValue());
            } else if (value instanceof Long) {
                recordValueEntryBean.setType("LONG");
                recordValueEntryBean.setLongValue(((Long) value).longValue());
            } else if (value instanceof Double) {
                recordValueEntryBean.setType("DOUBLE");
                recordValueEntryBean.setDoubleValue(((Double) value).doubleValue());
            } else if (value instanceof Float) {
                recordValueEntryBean.setType("FLOAT");
                recordValueEntryBean.setFloatValue(((Float) value).floatValue());
            } else if (value instanceof Boolean) {
                recordValueEntryBean.setType("BOOLEAN");
                recordValueEntryBean.setBooleanValue(((Boolean) value).booleanValue());
            } else if (value instanceof String) {
                recordValueEntryBean.setType("STRING");
                recordValueEntryBean.setStringValue(value.toString());
            } else if (value instanceof LinkedHashMap) {
                recordValueEntryBean.setType("FACET");
                recordValueEntryBean.setAnalyticsCategoryPathBeanValue(validateAndReturn(value));
            }
            recordValueEntryBeans.add(recordValueEntryBean);
        }
        return recordValueEntryBeans.toArray(new RecordValueEntryBean[recordValueEntryBeans.size()]);
    }

    public static org.wso2.carbon.analytics.webservice
            .stub.beans.IndexConfigurationBean createIndexConfiguration(IndexConfigurationBean indexInfoBean) {
        org.wso2.carbon.analytics.webservice.stub.beans.IndexConfigurationBean
                indexInfo = new org.wso2.carbon.analytics.webservice.stub.beans.IndexConfigurationBean();
        indexInfo.setIndices(getIndices(indexInfoBean.getIndices()));
        indexInfo.setScoreParams(getScoreParams(indexInfoBean.getScoreParams()));
        return indexInfo;
    }

    private static String[] getScoreParams(List<String> scoreParams) {
        return scoreParams.toArray(new String[scoreParams.size()]);
    }

    private static IndexEntryBean[] getIndices(Map<String, IndexTypeBean> indices) {
        List<IndexEntryBean> indexEntryBeans = new ArrayList<>();
        for (Map.Entry<String, IndexTypeBean> entry : indices.entrySet()) {
            IndexEntryBean bean = new IndexEntryBean();
            bean.setFieldName(entry.getKey());
            bean.setIndexType(entry.getValue().toString());
            indexEntryBeans.add(bean);
        }
        return indexEntryBeans.toArray(new IndexEntryBean[indexEntryBeans.size()]);
    }

    /**
     * Creates the index type bean from index type.
     *
     * @param indexType the index type
     * @return the index type bean
     */
    public static IndexTypeBean createIndexTypeBean(String indexType) {
        switch (indexType) {
            case "BOOLEAN":
                return IndexTypeBean.BOOLEAN;
            case "FLOAT":
                return IndexTypeBean.FLOAT;
            case "DOUBLE":
                return IndexTypeBean.DOUBLE;
            case "INTEGER":
                return IndexTypeBean.INTEGER;
            case "LONG":
                return IndexTypeBean.LONG;
            case "STRING":
                return IndexTypeBean.STRING;
            case "FACET":
                return IndexTypeBean.FACET;
            default:
                return IndexTypeBean.STRING;
        }
    }

    /**
     * Creates the index type bean map from index type map.
     *
     * @param indexEntries the index type map
     * @return the map
     */
    public static Map<String, IndexTypeBean> createIndexTypeBeanMap(
            IndexEntryBean[] indexEntries) {
        Map<String, IndexTypeBean> indexTypeBeans = new LinkedHashMap<>();
        for (IndexEntryBean indexEntryBean : indexEntries) {
            indexTypeBeans.put(indexEntryBean.getFieldName(),
                               createIndexTypeBean(indexEntryBean.getIndexType()));
        }
        return indexTypeBeans;
    }

    /**
     * Create a Analytics schema from a bean class
     *
     * @param analyticsSchemaBean bean table schema to be converted to Analytics Schema.
     * @return Analytics schema
     */
    public static org.wso2.carbon.analytics.
            webservice.stub.beans.AnalyticsSchemaBean createAnalyticsSchema(AnalyticsSchemaBean analyticsSchemaBean) {
        org.wso2.carbon.analytics.webservice.stub.beans.AnalyticsSchemaBean
                schemaBean = new org.wso2.carbon.analytics.webservice.stub.beans.AnalyticsSchemaBean();
        schemaBean.setColumns(createSchemaColumns(analyticsSchemaBean.getColumns()));
        schemaBean.setPrimaryKeys(createPrimaryKeys(analyticsSchemaBean.getPrimaryKeys()));
        return schemaBean;
    }

    private static String[] createPrimaryKeys(List<String> primaryKeys) {
        return primaryKeys.toArray(new String[primaryKeys.size()]);
    }

    private static SchemaColumnBean[] createSchemaColumns(Map<String, ColumnTypeBean> columns) {
        List<SchemaColumnBean> columnBeans = new ArrayList<>();
        for (Map.Entry<String, ColumnTypeBean> entry : columns.entrySet()) {
            SchemaColumnBean bean = new SchemaColumnBean();
            bean.setColumnName(entry.getKey());
            bean.setColumnType(entry.getValue().toString());
            columnBeans.add(bean);
        }
        return columnBeans.toArray(new SchemaColumnBean[columnBeans.size()]);
    }

    /**
     * Create table schema bean from a analytics schema
     *
     * @param analyticsSchema Analytics schema to be converted to table schema bean
     * @return Table schema bean
     */
    public static AnalyticsSchemaBean createTableSchemaBean(
            org.wso2.carbon.analytics.webservice.stub.beans.AnalyticsSchemaBean analyticsSchema) {
        Map<String, ColumnTypeBean> columnTypeBeanTypes = new HashMap<String, ColumnTypeBean>();
        List<String> primaryKeys = new ArrayList<String>();
        if (analyticsSchema.getColumns() != null) {
            for (SchemaColumnBean columnBean :
                    analyticsSchema.getColumns()) {
                columnTypeBeanTypes.put(columnBean.getColumnName(), getColumnTypeBean(columnBean.getColumnType()));
            }
        }
        if (analyticsSchema.getPrimaryKeys() != null) {
            primaryKeys = Arrays.asList(analyticsSchema.getPrimaryKeys());
        }
        return new AnalyticsSchemaBean(columnTypeBeanTypes, primaryKeys);
    }

    /**
     * convert a column type to bean type
     *
     * @param columnType the ColumnType to be converted to bean type
     * @return ColumnTypeBean instance
     */
    private static ColumnTypeBean getColumnTypeBean(String columnType) {
        switch (columnType) {
            case "STRING":
                return ColumnTypeBean.STRING;
            case "INTEGER":
                return ColumnTypeBean.INT;
            case "LONG":
                return ColumnTypeBean.LONG;
            case "FLOAT":
                return ColumnTypeBean.FLOAT;
            case "DOUBLE":
                return ColumnTypeBean.DOUBLE;
            case "BOOLEAN":
                return ColumnTypeBean.BOOLEAN;
            case "BINARY":
                return ColumnTypeBean.BINARY;
            default:
                return ColumnTypeBean.STRING;
        }
    }

    public static String[] authenticate(String authHeader) throws UnauthenticatedUserException{

        String credentials[];
        if (authHeader != null && authHeader.startsWith(Constants.BASIC_AUTH_HEADER)) {
            // Authorization: Basic base64credentials
            String base64Credentials = authHeader.substring(Constants.BASIC_AUTH_HEADER.length()).trim();
            String tempCredentials = new String(Base64.decode(base64Credentials),
                                                Charset.forName(DEFAUL_CHARSETT));
            // credentials = username:password
            final String[] values = tempCredentials.split(":", 2);
            String username = values[0];
            String password = values[1];
            credentials = values;
            if ("".equals(username) || username == null || "".equals(password) || password == null) {
                throw new UnauthenticatedUserException("Username and password cannot be empty");
            }
            String tenantDomain = MultitenantUtils.getTenantDomain(username);
            String tenantLessUserName = MultitenantUtils.getTenantAwareUsername(username);
            try {
                // get super tenant context and get realm service which is an osgi service
                RealmService realmService = (RealmService) PrivilegedCarbonContext
                        .getThreadLocalCarbonContext().getOSGiService(RealmService.class, null);
                if (realmService != null) {
                    int tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
                    if (tenantId == MultitenantConstants.INVALID_TENANT_ID) {
                        throw new UnauthenticatedUserException("Authentication failed - Invalid domain");
                    }
                    // get tenant's user realm
                    UserRealm userRealm = realmService.getTenantUserRealm(tenantId);
                    boolean isAuthenticated = userRealm.getUserStoreManager()
                            .authenticate(tenantLessUserName, password);
                    if (!isAuthenticated) {
                        throw  new UnauthenticatedUserException("User is not authenticated!");
                    } else {
                        return credentials;
                    }
                }
            } catch (UserStoreException e) {
                throw new UnauthenticatedUserException("Error while accessing the user realm of user :"
                                             + username, e);
            }
        } else {
            throw new UnauthenticatedUserException("Invalid authentication header");
        }
        return credentials;
    }
}
