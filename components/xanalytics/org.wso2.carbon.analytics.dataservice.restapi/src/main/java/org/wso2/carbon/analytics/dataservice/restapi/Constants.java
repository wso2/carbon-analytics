/**
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
 * The Class Constants.
 */
public class Constants {

	public static final String TIMESTAMP_PATTERN = "yyyy-MM-dd hh:mm:ss";

	/**
	 * Instantiates a new constants.
	 */
	private Constants() {
	}

	/**
	 * The Class Status.
	 */
	public static final class Status {

		/** The Constant FAILED. */
		public static final String FAILED = "failed";

		/** The Constant SUCCESS. */
		public static final String SUCCESS = "success";

		/** The Constant NON_EXISTENT. */
		public static final String NON_EXISTENT = "non existent";

		/**
		 * Instantiates a new status.
		 */
		private Status() {
		}
	}
}
