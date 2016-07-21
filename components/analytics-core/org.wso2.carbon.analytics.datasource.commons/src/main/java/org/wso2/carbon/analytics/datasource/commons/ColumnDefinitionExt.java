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
package org.wso2.carbon.analytics.datasource.commons;

import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema.ColumnType;

/**
 * This class represents a definition of a column in an analytics schema with any field having
 * the ability to become a facet.
 */
public class ColumnDefinitionExt extends ColumnDefinition {

    private static final long serialVersionUID = 4825753125950062005L;

    private boolean isFacet;
    
    public ColumnDefinitionExt() { }
    
    public ColumnDefinitionExt(String name, ColumnType type) {
        this(name, type, false, false);
    }
    
    public ColumnDefinitionExt(String name, ColumnType type, boolean indexed, boolean scoreParam) {
        this(name, type, indexed, scoreParam, false);
    }

    public ColumnDefinitionExt(String name, ColumnType type, boolean indexed, boolean scoreParam, boolean isFacet) {
        super(name, type, indexed, scoreParam);
        this.isFacet = isFacet;
        if (!this.isFacet) {
            this.isFacet = super.isFacet();
        }
    }
    
    public static ColumnDefinitionExt copy(ColumnDefinition colDef) {
        return new ColumnDefinitionExt(colDef.getName(), colDef.getType(), colDef.isIndexed(), colDef.isScoreParam(), colDef.isFacet());
    }

    @Override
    public boolean isFacet() {
        return isFacet;
    }

    public void setFacet(boolean isFacet) {
        this.isFacet = isFacet;
    }

}
