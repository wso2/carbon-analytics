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

package org.wso2.carbon.analytics.dataservice.restapi;

/**
 * The Class Constants Contains the constants used by rest apis.
 */
public class Constants {

	public static final long MILLISECONDSPERSECOND = 1000;
    public static final String BASIC_AUTH_HEADER = "Basic";

	/**
	 * Instantiates a new constants.
	 */
	private Constants() {
	}

	/**
	 * The Class Status.
	 */
	public static final class Status {

		public static final String CREATED = "created";
		public static final String FAILED = "failed";
		public static final String SUCCESS = "success";
		public static final String NON_EXISTENT = "non-existent";
        public static final String UNAUTHORIZED = "unauthorized";
        public static final String UNAUTHENTICATED = "unauthenticated";
		private Status() {
		}
	}

    public final static class FacetAttributes {

        public static final String WEIGHT = "weight";
        public static final String PATH = "path";
    }

    /**
     * The Class ResourcePath.
     */
    public static final class ResourcePath {

        public static final String GENERATE_TOKEN = "generate_token";
        public static final String ROOT_CONTEXT = "/";
		public static final String TABLES = "tables";
		public static final String TABLE_EXISTS = "table_exists";
		public static final String RECORDS = "records";
		public static final String SEARCH = "search";
		public static final String SEARCH_COUNT = "search_count";
		public static final String INDEXING_DONE = "indexing_done";
		public static final String DRILLDOWN = "drilldown";
		public static final String DRILLDOWNCOUNT = "drilldowncount";
    }

    public static final class JsonElements {

        public static final String INDICES = "indices";
        public static final String SCORE_PARAMS = "scoreParams";
        public static final String ID = "id";
        public static final String TABLENAME = "tableName";
        public static final String TIMESTAMP = "timestamp";
        public static final String PATH = "path";
        public static final String WEIGHT = "weight";
        public static final String VALUES = "values";
    }
}
