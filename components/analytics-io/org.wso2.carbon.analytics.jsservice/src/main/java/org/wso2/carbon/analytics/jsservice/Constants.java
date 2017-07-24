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

/**
 * The Class Constants Contains the constants used by rest apis.
 */
public class Constants {

	public static final long MILLISECONDSPERSECOND = 1000;
    public static final String BASIC_AUTH_HEADER = "Basic";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String OPERATION_TYPE = "type";
    public static final String TABLE_NAME = "tableName";
    public static final String TIME_FROM = "timeFrom";
    public static final String TIME_TO = "timeTo";
    public static final String START = "start";
    public static final String COUNT = "count";
    public static final String WAIT_TIME = "waitTime";
    public static final String STRING_TYPE = "STRING";
    public static final String BOOLEAN_TYPE = "BOOLEAN";
    public static final String FLOAT_TYPE = "FLOAT";
    public static final String DOUBLE_TYPE = "DOUBLE";
    public static final String INTEGER_TYPE = "INTEGER";
    public static final String LONG_TYPE = "LONG";


    /**
	 * Instantiates a new constants.
	 */
	private Constants() {
	}

	/**
	 * The Class Status.
	 */
	public static final class Status {

		/** The Constant CREATED. */
		public static final String CREATED = "created";

		/** The Constant FAILED. */
		public static final String FAILED = "failed";

		/** The Constant SUCCESS. */
		public static final String SUCCESS = "success";

		/** The Constant NON_EXISTENT. */
		public static final String NON_EXISTENT = "non-existent";

        /** The Constant UNAUTHORIZED. */
        public static final String UNAUTHORIZED = "unauthorized";

        /** The Constant NON_EXISTENT. */
        public static final String UNAUTHENTICATED = "unauthenticated";

		/**
		 * Instantiates a new status.
		 */
		private Status() {
		}
	}
}
