/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.status.dashboard.core.internal;

import javax.xml.bind.annotation.XmlTransient;

/**
 * Auto generated class from Swagger to MSF4J.
 */
@javax.xml.bind.annotation.XmlRootElement
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
        date = "2017-03-15T08:56:59.657Z")
public class ApiResponseMessage {

    public static final String NOT_FOUND = "not-found";
    public static final String SUCCESS = "success";

    String type;
    String message;

    public ApiResponseMessage(String type, String message) {
        this.type = type;
        this.message = message;
    }

    @XmlTransient

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
