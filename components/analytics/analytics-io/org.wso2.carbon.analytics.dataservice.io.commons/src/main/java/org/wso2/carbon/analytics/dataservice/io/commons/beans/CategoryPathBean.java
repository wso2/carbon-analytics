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

package org.wso2.carbon.analytics.dataservice.io.commons.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class represents a facet object bean. facet object defines the hierarchical category,
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
@XmlRootElement(name = "categoryPath")
@XmlAccessorType(XmlAccessType.FIELD)
public class CategoryPathBean {

    @XmlElement(name = "path")
    private String[] path;
    @XmlElement(name = "weight")
    private  float weight;

    /**
     * Create a hierarchical category with a given path and weight with 1.0.
     * Use this constructor when the categoryPaths need to be given for drill down search
     * in DrillDownRequest objects
     * @param path array of strings representing the category path.
     */
    public CategoryPathBean(String[] path) {
        this.path = path;
    }
    /**
     * Creates a hierarchical Category with the given weight and the array of Strings representing
     * the hierarchical category
     * @param weight Weight of the category, set it to 1.0 if weights are not necessary
     * @param path String array which represent the category path.
     */
    public CategoryPathBean(float weight, String[] path) {
        this.weight = weight;
        this.path = path;
    }

    public String[] getPath() {
        return path;
    }

    public void setPath(String[] path) {
        this.path = path;
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
