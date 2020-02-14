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

package org.wso2.carbon.siddhi.extensions.installer.core.config.mapping.models;

/**
 * Denotes the type of the jar (A bundle itself is a jar too, but with some added headers).
 * The correct folder inside the base folder (decided based on {@link UsedByType}) - where the jar should be put to,
 * will be decided based on this.
 * Necessary jar to bundle conversion will be done during pack start-up.
 */
public enum UsageType {
    JAR, BUNDLE
}
