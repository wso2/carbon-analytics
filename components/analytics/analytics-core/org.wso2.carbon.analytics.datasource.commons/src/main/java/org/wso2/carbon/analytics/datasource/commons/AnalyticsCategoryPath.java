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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.analytics.datasource.commons;

/**
 * This class represents a facet object. facet object defines the hierarchical category,
 * which can be drilled down. This can be used as a value in a record.
 * Example :
 *   Assume a record represents a book.
 *      Then the record field : value pairs will be, e.g.
 *          Price : $50.00
 *          Author : firstName LastName
 *          ISBN : 234325435445435436
 *          Published Date : "1987" , "March", "21"
 *
 * Here Publish Date will be a facet/categoryPath, since it can be drilled down to Year, then month and date
 * and categorizes by each level.
 *
 */
public class AnalyticsCategoryPath {

    private String[] path;
    private  float weight;
    private static final String SEPARATOR = "/";

    /**
     * Create a hierarchical category with a given path and weight with 1.0.
     * Use this constructor when the categoryPaths need to be given for drill down search
     * in DrillDownRequest objects
     * @param path array of strings representing the category path.
     */
    public AnalyticsCategoryPath(String[] path) {
        this.weight = 1.0f;
        this.path = path;
    }
    /**
     * Creates a hierarchical Category with the given weight and the array of Strings representing
     * the hierarchical category
     * @param weight Weight of the category, set it to 1.0 if weights are not necessary
     * @param path String array which represent the category path.
     */
    public AnalyticsCategoryPath(float weight, String[] path) {
        this.weight = weight;
        this.path = path;
    }

    public String[] getPath() {
        return path;
    }


    /**
     * Combine the category path array to a single string separated by "/"
     * @param path String array path to be combined
     * @return Combined String
     */
    public static String getCombinedPath(String[] path) {
        StringBuilder fulPath = new StringBuilder(path[0]);
        for(int i = 1;i < path.length; i++) {
            fulPath.append(SEPARATOR);
            fulPath.append(path[i]);
        }
        return fulPath.toString();
    }

    public void setPath(String[] path) {
        this.path = path;
    }

    public static String[] getPathAsArray(String path) {
        return path.split(SEPARATOR);
    }

    public float getWeight() {
        return weight;
    }

    /**
     * Set a weight for the facet category
     * @param weight weight in float ( set is as 1.0 if weights are not needed)
     */
    public void setWeight(float weight) {
        this.weight = weight;
    }
}
