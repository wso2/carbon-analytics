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

package org.wso2.carbon.event.simulator.core.internal.generator.random.util;

import org.wso2.siddhi.query.api.definition.Attribute;

/**
 * DataParser class is used to validate whether a give data element can be parsed to the attribute type specified
 */
public class DataParser {

    /**
     * parse() parses the provided data element to the attribute type specified
     *
     * @param attributeType type to be parsed to
     * @param dataItem data element to be parsed
     * @throws NumberFormatException if the data element cannot be parsed to the attribute type*/
    public static void parse(Attribute.Type attributeType, Object dataItem) throws NumberFormatException {
        switch (attributeType) {
            case INT:
                Integer.parseInt(String.valueOf(dataItem));
                break;
            case LONG:
                Long.parseLong(String.valueOf(dataItem));
                break;
            case FLOAT:
                Float.parseFloat(String.valueOf(dataItem));
                break;
            case DOUBLE:
                Double.parseDouble(String.valueOf(dataItem));
                break;
            case STRING:
//                do nothing
                break;
            case BOOL:
                Boolean.parseBoolean(String.valueOf(dataItem));
                break;
            default:
//                this statement is never reached since attribute type is an enum
        }
    }
}
