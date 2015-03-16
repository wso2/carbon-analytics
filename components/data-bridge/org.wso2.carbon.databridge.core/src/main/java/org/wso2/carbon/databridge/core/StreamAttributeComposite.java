/*
*  Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.databridge.core;

import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.utils.EventDefinitionConverterUtils;

/**
 * Class to hold attribute type order array and size of attributes.
 */

public class StreamAttributeComposite {
    private AttributeType[][] attributeTypes;
    private StreamDefinition streamDefinition;
    private int attributeSize;

    public StreamAttributeComposite(StreamDefinition streamDefinition) {
        this.streamDefinition = streamDefinition;
        this.attributeTypes = new AttributeType[][]{EventDefinitionConverterUtils.generateAttributeTypeArray(streamDefinition.getMetaData()),
                                                    EventDefinitionConverterUtils.generateAttributeTypeArray(streamDefinition.getCorrelationData()),
                                                    EventDefinitionConverterUtils.generateAttributeTypeArray(streamDefinition.getPayloadData())};
        this.attributeSize = getSize(attributeTypes);

    }

    private int getSize(AttributeType[][] attributeTypes) {
        int size = 0;
        if (attributeTypes[0] != null) {
            size += attributeTypes[0].length;
        }
        if (attributeTypes[1] != null) {
            size += attributeTypes[1].length;
        }
        if (attributeTypes[2] != null) {
            size += attributeTypes[2].length;
        }
        return size;
    }

    public int getAttributeSize() {
        return attributeSize;
    }

    public void setAttributeSize(int attributeSize) {
        this.attributeSize = attributeSize;
    }

    public AttributeType[][] getAttributeTypes() {
        return attributeTypes;
    }

    public void setAttributeTypes(AttributeType[][] attributeTypes) {
        this.attributeTypes = attributeTypes;
    }

    public StreamDefinition getStreamDefinition() {
        return streamDefinition;
    }
}
