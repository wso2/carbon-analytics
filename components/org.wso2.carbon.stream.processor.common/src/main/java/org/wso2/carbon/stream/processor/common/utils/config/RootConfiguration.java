/*
 * Copyright (c)  2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.stream.processor.common.utils.config;

import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code RootConfiguration} is a bean class for root level configuration.
 */
@Configuration(namespace = "siddhi", description = "Siddhi extension and siddhi reference parent level configuration")
public class RootConfiguration {

    @Element(description = "Extension list", required = true)
    private List<Extension> extensions;

    @Element(description = "References list", required = false)
    private List<Reference> refs;

    public RootConfiguration() {
        extensions = new ArrayList<>();
        refs = new ArrayList<>();
    }

    public List<Extension> getExtensions() {
        return extensions;
    }

    public List<Reference> getRefs() {
        return refs;
    }
}
