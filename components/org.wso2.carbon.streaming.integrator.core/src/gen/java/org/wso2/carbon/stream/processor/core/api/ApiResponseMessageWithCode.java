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

package org.wso2.carbon.streaming.integrator.core.api;

import javax.xml.bind.annotation.XmlTransient;

/**
 * Auto generated class from Swagger to MSF4J.
 */
@javax.xml.bind.annotation.XmlRootElement
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
        date = "2017-03-15T08:56:59.657Z")
public class ApiResponseMessageWithCode {

    public static final int VALIDATION_ERROR = 800101;
    public static final int FILE_PROCESSING_ERROR = 800102;

    int code;
    String type;
    String message;

    public ApiResponseMessageWithCode() {
    }

    public ApiResponseMessageWithCode(int code, String message) {
        this.code = code;
        switch (code) {
            case VALIDATION_ERROR:
                setType("validation error");
                break;
            case FILE_PROCESSING_ERROR:
                setType("file processing error");
                break;
            default:
                setType("unknown");
                break;
        }
        this.message = message;
    }

    @XmlTransient
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

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
