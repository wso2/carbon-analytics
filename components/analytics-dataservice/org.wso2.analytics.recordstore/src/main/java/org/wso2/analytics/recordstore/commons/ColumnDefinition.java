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

package org.wso2.analytics.recordstore.commons;

import java.io.Serializable;

/**
 * This class represents a defintion of a column in an analytics schema.
 */
public class ColumnDefinition implements Serializable {

    private static final long serialVersionUID = 1806609078873096460L;

    private String name;

    private AnalyticsSchema.ColumnType type;

    public ColumnDefinition() { }
    
    public ColumnDefinition(String name, AnalyticsSchema.ColumnType type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AnalyticsSchema.ColumnType getType() {
        return type;
    }

    public void setType(AnalyticsSchema.ColumnType type) {
        this.type = type;
    }

    @Override
    public int hashCode() {
        return (this.getName() + ":" + this.getType().name()).hashCode();
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof ColumnDefinition)) {
            return false;
        }
        ColumnDefinition rhs = (ColumnDefinition) obj;
        return this.getName().equals(rhs.getName()) && this.getType().equals(rhs.getType());
    }

}
