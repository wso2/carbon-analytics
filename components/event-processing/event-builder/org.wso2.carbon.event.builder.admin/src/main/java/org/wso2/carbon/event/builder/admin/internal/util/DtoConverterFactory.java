/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.event.builder.admin.internal.util;

import org.wso2.carbon.event.builder.admin.internal.util.dto.converter.*;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderConstants;

public class DtoConverterFactory {
    private Wso2EventDtoConverter wso2EventDtoConverter;
    private XmlDtoConverter xmlDtoConverter;
    private MapDtoConverter mapDtoConverter;
    private JsonDtoConverter jsonDtoConverter;
    private TextDtoConverter textDtoConverter;
    private GenericDtoConverter genericDtoConverter;

    public DtoConverter getDtoConverter(String inputMappingType) {
        if (EventBuilderConstants.EB_WSO2EVENT_MAPPING_TYPE.equals(inputMappingType)) {
            if (this.wso2EventDtoConverter == null) {
                this.wso2EventDtoConverter = new Wso2EventDtoConverter();
            }
            return wso2EventDtoConverter;
        } else if (EventBuilderConstants.EB_XML_MAPPING_TYPE.equals(inputMappingType)) {
            if (this.xmlDtoConverter == null) {
                this.xmlDtoConverter = new XmlDtoConverter();
            }
            return xmlDtoConverter;
        } else if (EventBuilderConstants.EB_MAP_MAPPING_TYPE.equals(inputMappingType)) {
            if (this.mapDtoConverter == null) {
                this.mapDtoConverter = new MapDtoConverter();
            }
            return mapDtoConverter;
        } else if (EventBuilderConstants.EB_JSON_MAPPING_TYPE.equals(inputMappingType)) {
            if (this.jsonDtoConverter == null) {
                this.jsonDtoConverter = new JsonDtoConverter();
            }
            return jsonDtoConverter;
        } else if (EventBuilderConstants.EB_TEXT_MAPPING_TYPE.equals(inputMappingType)) {
            if (this.textDtoConverter == null) {
                this.textDtoConverter = new TextDtoConverter();
            }
            return textDtoConverter;
        } else {
            if (this.genericDtoConverter == null) {
                this.genericDtoConverter = new GenericDtoConverter();
            }
            return genericDtoConverter;
        }
    }
}
