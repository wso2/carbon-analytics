package org.wso2.carbon.event.simulator.core.internal.util;

import org.json.JSONObject;
import org.wso2.carbon.event.simulator.core.exception.InvalidConfigException;
import org.wso2.carbon.event.simulator.core.internal.generator.random.RandomAttributeGenerator;
import org.wso2.siddhi.query.api.definition.Attribute;

/**
 * factory interface used for creating random attribute generators
 */
public interface RandomAttrGeneratorFactory {

    RandomAttributeGenerator createRandomAttrGenerator(JSONObject attributeConfig, Attribute.Type attrType) throws
            InvalidConfigException;

    void validateRandomAttrGenerator(JSONObject attributeConfig, Attribute.Type attrType) throws
            InvalidConfigException;

}
