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

package org.wso2.carbon.analytics.webservice.beans;

import java.io.Serializable;

/**
 * This class Contains Sub-categories of a given facet field/column. "path" represents the hierarchical
 * path to the categories and "categories" contains the list of categories with their scores.
 */
public class SubCategoriesBean implements Serializable {

    private static final long serialVersionUID = -7343594197193560975L;
    private String[] path;
    private CategorySearchResultEntryBean[] categories;
    private int categoryCount;

    public SubCategoriesBean() {
    }

    public String[] getPath() {
        return path;
    }

    public void setPath(String[] path) {
        this.path = path;
    }

    public CategorySearchResultEntryBean[] getCategories() {
        return categories;
    }

    public void setCategories(CategorySearchResultEntryBean[] categories) {
        this.categories = categories;
    }

    public int getCategoryCount() {
        return categoryCount;
    }

    public void setCategoryCount(int categoryCount) {
        this.categoryCount = categoryCount;
    }
}
