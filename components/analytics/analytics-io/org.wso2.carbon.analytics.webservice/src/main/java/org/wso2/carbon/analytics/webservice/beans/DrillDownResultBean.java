/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.analytics.webservice.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class represents the bean class for results of a drilldown request.
 */
@XmlRootElement(name = "drilldownInfo")
@XmlAccessorType(XmlAccessType.FIELD)
public class DrillDownResultBean {
    @XmlElement(name = "categories", required = true)
    private PerFieldDrillDownResultBean[] perFieldEntries;
    @XmlElement(name = "ranges", required = false)
    private DrillDownFieldRangeBean[] perFieldRanges;
    @XmlElement(name = "ll")
    private String language;
    private  String languageQuery;
    private String scoreFunction;
    private int categoryCount;

    private int categoryStart;
    private int recordCount;
    private  int recordStart;
    private boolean withIds;

    public PerFieldDrillDownResultBean[] getPerFieldEntries() {
        return perFieldEntries;
    }

    public void setPerFieldEntries(PerFieldDrillDownResultBean[] perFieldEntries) {
        this.perFieldEntries = perFieldEntries;

    }

    public DrillDownFieldRangeBean[] getPerFieldRanges() {
        return perFieldRanges;
    }

    public void setPerFieldRanges(DrillDownFieldRangeBean[] perFieldRanges) {
        this.perFieldRanges = perFieldRanges;
    }
}
