/**
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.analytics.hive;


public class RegistryAccessUtilConstants {

    public RegistryAccessUtilConstants() {
    }

    public static final char CLOSE_CURLY_BRACKET = '}';
    public static final char COLON = ':';

    public static final String REGISTRY_GOVERNANCE = "gov";
    public static final String REGISTRY_CONFIG = "conf";
    public static final String REGISTRY_LOCAL = "local";

    public static final String MEDIA_TYPE_TEXT_PLAIN = "text/plain";
    //\$\{(conf|gov|local)\w*:\/\w+(\/\w+)+\}
    public static final String REGISTRY_KEY_PATTERN = "\\$\\{(" + REGISTRY_GOVERNANCE + "|" + REGISTRY_CONFIG + "|" + REGISTRY_LOCAL + ")\\w*:\\/\\w+(\\/\\w+)+\\}";
}