/*
* Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.extension.siddhi.io.mgwfile;

/**
 *  MGWFile EventSource specific constatnts class
 */
public class MGWFileSourceConstants {

    public static final String API_USAGE_FILE_CONTENT = "FILE_CONTENT";

    public static final String API_USAGE_OUTPUT_FILE_NAME = "api-usage-data.dat";

    public static final String UPLOADED_USAGE_PUBLISH_FREQUENCY_PROPERTY = "usage.publishing.frequency";

    public static final String UPLOADED_USAGE_CLEANUP_FREQUENCY_PROPERTY = "usage.cleanup.frequency";

    public static final String FILE_RETENTION_DAYS_PROPERTY = "file.retention.days";

    public static final String DEFAULT_FILE_RETENTION_DAYS = "5";

    public static final String WORKER_THREAD_COUNT_PROPERTY = "usage.publishing.thread.count";

    public static final String DEFAULT_UPLOADED_USAGE_PUBLISH_FREQUENCY = "300000";

    public static final String DEFAULT_UPLOADED_USAGE_CLEANUP_FREQUENCY = "1800000";

    public static final String INSERT_UPLOADED_FILE_INFO_QUERY = "INSERT INTO AM_USAGE_UPLOADED_FILES "
            + "(FILE_NAME,FILE_TIMESTAMP,FILE_CONTENT) VALUES(?,?,?)";

    public static final String GET_NEXT_FILES_TO_PROCESS_QUERY_DEFAULT =
            "SELECT FILE_NAME,FILE_TIMESTAMP FROM AM_USAGE_UPLOADED_FILES "
                    + "WHERE FILE_PROCESSED=0 ORDER BY FILE_TIMESTAMP LIMIT ? FOR UPDATE;";

    public static final String GET_NEXT_FILES_TO_PROCESS_QUERY_ORACLE = "SELECT FILE_NAME,FILE_TIMESTAMP "
            + "FROM AM_USAGE_UPLOADED_FILES WHERE rownum<=? AND "
            + "FILE_PROCESSED=0 ORDER BY FILE_TIMESTAMP FOR UPDATE";

    public static final String GET_NEXT_FILES_TO_PROCESS_QUERY_MSSQL = "SELECT TOP (?) FILE_NAME,"
            + "FILE_TIMESTAMP FROM AM_USAGE_UPLOADED_FILES WITH (UPDLOCK) "
            + "WHERE FILE_PROCESSED=0 ORDER BY FILE_TIMESTAMP";

    public static final String GET_NEXT_FILES_TO_PROCESS_QUERY_DB2 =
            "SELECT FILE_NAME,FILE_TIMESTAMP FROM AM_USAGE_UPLOADED_FILES "
                    + "WHERE FILE_PROCESSED=0 ORDER BY FILE_TIMESTAMP LIMIT ? FOR UPDATE";

    public static final String UPDATE_FILE_PROCESSING_STARTED_STATUS = "UPDATE AM_USAGE_UPLOADED_FILES "
            + "SET FILE_PROCESSED=1 WHERE FILE_PROCESSED=0 AND FILE_NAME = ?";

    public static final String DELETE_OLD_UPLOAD_COMPLETED_FILES = "DELETE FROM AM_USAGE_UPLOADED_FILES "
            + "WHERE FILE_PROCESSED=2 AND FILE_TIMESTAMP < ?";

    public static final String GET_UPLOADED_FILE_CONTENT_QUERY = "SELECT FILE_CONTENT "
            + "FROM AM_USAGE_UPLOADED_FILES WHERE FILE_NAME=?";

    public static final String UPDATE_COMPETITION_QUERY = "UPDATE AM_USAGE_UPLOADED_FILES "
            + "SET FILE_PROCESSED=2 WHERE FILE_NAME=?";

    public static final String DEFAULT_WORKER_THREAD_COUNT = "3";

    // Separators used for persisting events
    public static final String EVENT_SEPARATOR = "-ES-";

    public static final String KEY_VALUE_SEPARATOR = "-KS-";

    public static final String OBJECT_SEPARATOR = "-OS-";

}
