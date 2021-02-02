/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.streaming.integrator.core.internal.asyncapi.util;

import java.io.File;

public class Constants {
    public static final String ZIP_FILE_EXTENSION = "zip";
    public static final String SERVICE_DEF_DIRECTORY = System.getProperty("user.dir") +
            File.separator + "deployment"+File.separator + ".service-definitions" + File.separator;
    public static final String ASYNC_API_INFO = "info";
    public static final String ASYNC_API_TITLE = "title";
    public static final String ASYNC_API_VERSION = "version";
}
