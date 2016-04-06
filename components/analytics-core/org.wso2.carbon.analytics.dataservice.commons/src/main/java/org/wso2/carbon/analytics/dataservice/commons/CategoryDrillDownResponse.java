/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.analytics.dataservice.commons;

import java.io.Serializable;
import java.util.List;

/**
 * This class represents the class which contains the list of CategorySearchResultEntry and the total matching category count
 */
public class CategoryDrillDownResponse implements Serializable {

    private static final long serialVersionUID = 7400389388825590624L;
    private List<CategorySearchResultEntry> categories;

    public CategoryDrillDownResponse() {

    }

    public CategoryDrillDownResponse(
            List<CategorySearchResultEntry> categories) {
        this.categories = categories;
    }

    public List<CategorySearchResultEntry> getCategories() {
        return categories;
    }

    public void setCategories(List<CategorySearchResultEntry> categories) {
        this.categories = categories;
    }
}
