/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.analytics.spark.core.udf.facets;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * This util class is used for facet UDFs.
 */
public class FacetUtils {
    /**
     * Converts the objects list to String value arrays.
     *
     * @param objects array of objects to be concatenated in to the String array.
     * @return An array of String values.
     */
    public static String getFacetString(Object... objects) {
        List<String> valueList = new ArrayList<>();
        for (Object object : objects) {
            valueList.add(String.valueOf(object));
        }
        return StringUtils.join(valueList, ',');
    }
}
