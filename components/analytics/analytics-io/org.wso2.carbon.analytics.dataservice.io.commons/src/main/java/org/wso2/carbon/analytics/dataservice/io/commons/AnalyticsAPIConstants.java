/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.analytics.dataservice.io.commons;

public class AnalyticsAPIConstants {
    public static final String DATASERVICE_API_URI = "/analytics-api/";
    public static final String AUTHENTICATION_SERVICE_URI = DATASERVICE_API_URI+"AnalyticsAPIAuthenticationProcessor";
    public static final String TABLE_PROCESSOR_SERVICE_URI = DATASERVICE_API_URI+"AnalyticsTableProcessor";
    public static final String SCHEMA_PROCESSOR_SERVICE_URI = DATASERVICE_API_URI+"AnalyticsTableSchemaProcessor";
    public static final String RECORD_PROCESSOR_SERVICE_URI = DATASERVICE_API_URI+"AnalyticsRecordProcessor";
    public static final String INDEX_PROCESSOR_SERVICE_URI = DATASERVICE_API_URI+"AnalyticsIndexProcessor";
    public static final String SEARCH_PROCESSOR_SERVICE_URI = DATASERVICE_API_URI+"AnalyticsSearchProcessor";
    public static final String ANALYTICS_SERVICE_PROCESSOR_URI = DATASERVICE_API_URI+"AnalyticsServiceProcessor";
    public static final String ANALYTIC_RECORD_READ_PROCESSOR_SERVICE_URI = DATASERVICE_API_URI+"AnalyticsRecordReadProcessor";

    public static final String TENANT_ID_PARAM = "tenant_id";
    public static final String TABLE_NAME_PARAM = "table_name";
    public static final String SCHEMA_PARAM = "table_schema";
    public static final String USERNAME_PARAM ="username";
    public static final String PASSWORD_PARAM = "password";
    public static final String TIME_FROM_PARAM = "timeFrom";
    public static final String TIME_TO_PARAM = "timeTo";
    public static final String RECORDS_PARAM = "records";
    public static final String RECORD_IDS_PARAM = "recordIds";
    public static final String INDEX_PARAM = "index";
    public static final String LANGUAGE_PARAM = "language";
    public static final String QUERY = "query";
    public static final String START_PARAM = "start";
    public static final String COUNT_PARAM = "count";
    public static final String MAX_WAIT_PARAM = "maxWait";
    public static final String RECORD_GROUP_IMPL_CLASS_PARAM = "recordGroupClass";
    public static final String RECORD_GROUP = "recordGroup";
    public static final String PARTITIONER_NO_PARAM = "partitionerNo";
    public static final String COLUMNS_PARAM = "columns";
    public static final String RECORD_FROM_PARAM = "recordFrom";

    public static final String OPERATION = "__operation";
    public static final String SESSION_ID = "__sessionId";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String SEPARATOR = ":";
    public static final String RECORD_COUNT = "__recordCount";
    public static final String SEARCH_COUNT = "__searchCount";
    public static final String LIST_TABLES_OPERATION = "__list_tables_opr";
    public static final String CREATE_TABLE_OPERATION = "__create_table_opr";
    public static final String DELETE_TABLE_OPERATION = "__delete_table_opr";
    public static final String PUT_RECORD_OPERATION = "__put_records_opr";
    public static final String SET_SCHEMA_OPERATION = "__set_schema_opr";
    public static final String GET_SCHEMA_OPERATION = "__get_schema_opr";
    public static final String GET_RECORD_COUNT_OPERATION = "__get_record_count_opr";
    public static final String TABLE_EXISTS_OPERATION = "__table_exists_opr";
    public static final String TABLE_EXISTS = "__tableExists";
    public static final String LOGIN_OPERATION = "__login_opr";
    public static final String DELETE_RECORDS_RANGE_OPERATION = "__delete_records_range_opr";
    public static final String DELETE_RECORDS_IDS_OPERATION = "__delete_records_ids_opr";
    public static final String SET_INDICES_OPERATION = "__set_indices_opr";
    public static final String GET_INDICES_OPERATION ="__get_indices_opr";
    public static final String DELETE_INDICES_OPERATION = "__delete_indices_opr";
    public static final String SEARCH_OPERATION = "__search_opr";
    public static final String SEARCH_COUNT_OPERATION = "__search_count_opr";
    public static final String WAIT_FOR_INDEXING_OPERATION = "__wait_for_index_opr";
    public static final String DESTROY_OPERATION = "__destroy_opr";
    public static final String READ_RECORD_OPERATION = "__readRecord_opr";
    public static final String GET_RANGE_RECORD_GROUP_OPERATION = "__get_range_record_group_opr";
    public static final String GET_IDS_RECORD_GROUP_OPERATION = "__get_ids_record_group_opr";
    public static final String ANALYTICS_REMOTE_API_INVOCATION_PERMISSION = "/permission/admin/manage/analytics/api";
}
