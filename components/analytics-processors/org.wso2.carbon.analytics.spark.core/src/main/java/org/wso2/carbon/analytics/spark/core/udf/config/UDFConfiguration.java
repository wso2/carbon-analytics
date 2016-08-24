/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.analytics.spark.core.udf.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * This class represents the bean class for Custom UDF classes
 */
@XmlRootElement(name = "udf-configuration")
public class UDFConfiguration {

    private List<String> customUDFClass;
    private List<CustomUDAF> customUDAFs;

    public UDFConfiguration() {
    }

    @XmlElementWrapper(name = "custom-udf-classes")
    @XmlElement(name = "class-name")
    public List<String> getCustomUDFClass() {
        return customUDFClass;
    }

    public void setCustomUDFClass(List<String> customUDFClass) {
        this.customUDFClass = customUDFClass;
    }

    @XmlElementWrapper(name = "custom-udaf-classes")
    @XmlElement(name = "custom-udaf")
    public List<CustomUDAF> getCustomUDAFs() {
        return customUDAFs;
    }

    public void setCustomUDAFs(List<CustomUDAF> customUDAFs) {
        this.customUDAFs = customUDAFs;
    }
}
