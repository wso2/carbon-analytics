package org.wso2.carbon.event.simulator.core.internal.generator.random;

import org.json.JSONObject;
import org.wso2.siddhi.query.api.definition.Attribute;

/**
 * RandomAttrGenAbstractImpl class overloads the createRandomAttributeDTO() of RandomAttributeGenerator
 */
public abstract class RandomAttrGenAbstractImpl implements RandomAttributeGenerator {

    public void createRandomAttributeDTO(Attribute.Type attributeType, JSONObject attributeConfig) {
        createRandomAttributeDTO(attributeConfig);
    }

    public abstract void createRandomAttributeDTO(JSONObject attributeConfig);

}
