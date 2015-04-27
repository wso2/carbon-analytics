/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.wso2.carbon.event.receiver.core.config;

import org.wso2.carbon.databridge.commons.AttributeType;

/**
 * This class contains properties of inputs and their outputs
 */
public class InputMappingAttribute {

    private String fromElementKey;
    private String fromElementType;
    private String toElementKey;
    private AttributeType toElementType;
    private Integer toStreamPosition;
    private String defaultValue;

    public InputMappingAttribute(String fromElementKey, String toElementKey,
                                 AttributeType toElementType) {
        this(fromElementKey, toElementKey, toElementType, EventReceiverConstants.PAYLOAD_DATA_VAL);
    }

    public InputMappingAttribute(String fromElementKey, String toElementKey,
                                 AttributeType toElementType, String fromElementType) {
        this.fromElementKey = fromElementKey;
        this.toElementKey = toElementKey;
        this.toElementType = toElementType;
        this.fromElementType = fromElementType;
        this.toStreamPosition = -1;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getFromElementType() {
        return fromElementType;
    }

    public Integer getToStreamPosition() {
        return toStreamPosition;
    }

    public void setToStreamPosition(Integer toStreamPosition) {
        this.toStreamPosition = toStreamPosition;
    }

    public String getFromElementKey() {
        return fromElementKey;
    }

    public String getToElementKey() {
        return toElementKey;
    }

    public AttributeType getToElementType() {
        return toElementType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        InputMappingAttribute that = (InputMappingAttribute) o;

        if (!fromElementKey.equals(that.fromElementKey)) {
            return false;
        }
        if (fromElementType != null ? !fromElementType.equals(that.fromElementType) : that.fromElementType != null) {
            return false;
        }
        if (!toElementKey.equals(that.toElementKey)) {
            return false;
        }
        if (toElementType != that.toElementType) {
            return false;
        }
        if (toStreamPosition != null ? !toStreamPosition.equals(that.toStreamPosition) : that.toStreamPosition != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = fromElementKey.hashCode();
        result = 31 * result + toElementKey.hashCode();
        result = 31 * result + toElementType.hashCode();
        result = 31 * result + (fromElementType != null ? fromElementType.hashCode() : 0);
        result = 31 * result + (toStreamPosition != null ? toStreamPosition.hashCode() : 0);
        return result;
    }
}
