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

package org.wso2.carbon.event.builder.core.internal.type.json;

import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.event.builder.core.config.EventBuilderConfiguration;
import org.wso2.carbon.event.builder.core.config.InputMapper;
import org.wso2.carbon.event.builder.core.exception.EventBuilderConfigurationException;
import org.wso2.carbon.event.builder.core.internal.config.InputMappingAttribute;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderConstants;
import org.wso2.carbon.event.builder.core.internal.util.helper.EventBuilderConfigHelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class JsonInputMapper implements InputMapper {

    private static final Log log = LogFactory.getLog(JsonInputMapper.class);
    private EventBuilderConfiguration eventBuilderConfiguration = null;
    private List<JsonPathData> attributeJsonPathDataList = new ArrayList<JsonPathData>();

    public JsonInputMapper(EventBuilderConfiguration eventBuilderConfiguration) throws EventBuilderConfigurationException {
        this.eventBuilderConfiguration = eventBuilderConfiguration;
        if (eventBuilderConfiguration != null && eventBuilderConfiguration.getInputMapping() instanceof JsonInputMapping) {
            JsonInputMapping jsonInputMapping = (JsonInputMapping) eventBuilderConfiguration.getInputMapping();
            for (InputMappingAttribute inputMappingAttribute : jsonInputMapping.getInputMappingAttributes()) {
                String jsonPathExpr = inputMappingAttribute.getFromElementKey();
                String defaultValue = inputMappingAttribute.getDefaultValue();
                JsonPath compiledJsonPath = JsonPath.compile(jsonPathExpr);
                String type = EventBuilderConstants.ATTRIBUTE_TYPE_CLASS_TYPE_MAP.get(inputMappingAttribute.getToElementType());
                attributeJsonPathDataList.add(new JsonPathData(compiledJsonPath, type, defaultValue));
            }
        }
    }

    @Override
    public Object convertToMappedInputEvent(Object obj) throws EventBuilderConfigurationException {
        Object outObject = null;
        if (obj instanceof String) {
            String jsonString = (String) obj;
            if (jsonString.startsWith(EventBuilderConstants.JSON_ARRAY_START_CHAR)) {
                outObject = processMultipleEvents(obj);
            } else {
                outObject = processSingleEvent(obj);
            }
        }
        return outObject;
    }

    @Override
    public Object convertToTypedInputEvent(Object obj) throws EventBuilderConfigurationException {
        throw new UnsupportedOperationException("This feature is not yet supported for JSONInputMapping");
    }

    @Override
    public Attribute[] getOutputAttributes() {
        JsonInputMapping jsonInputMapping = (JsonInputMapping) eventBuilderConfiguration.getInputMapping();
        List<InputMappingAttribute> inputMappingAttributes = jsonInputMapping.getInputMappingAttributes();
        return EventBuilderConfigHelper.getAttributes(inputMappingAttributes);
    }

    private Object[][] processMultipleEvents(Object obj) throws EventBuilderConfigurationException {
        Object[][] objArray = null;
        if (obj instanceof String) {
            String events = (String) obj;
            JSONArray jsonArray;
            try {
                jsonArray = new JSONArray(events);
                objArray = new Object[jsonArray.length()][];
                for (int i = 0; i < jsonArray.length(); i++) {
                    objArray[i] = processSingleEvent(jsonArray.getJSONObject(i).toString());
                }
            } catch (JSONException e) {
                throw new EventBuilderConfigurationException("Error in parsing JSON: ", e);
            }
        }
        return objArray;
    }

    private Object[] processSingleEvent(Object obj) throws EventBuilderConfigurationException {
        Object[] outObjArray = null;
        if (obj instanceof String) {
            String jsonString = (String) obj;
            List<Object> objList = new ArrayList<Object>();
            for (JsonPathData jsonPathData : attributeJsonPathDataList) {
                JsonPath jsonPath = jsonPathData.getJsonPath();
                String type = jsonPathData.getType();
                try {
                    Object resultObject = jsonPath.read(jsonString);
                    Class<?> typeClass = Class.forName(type);
                    Object returnedObj = null;
                    if (resultObject != null) {
                        returnedObj = typeClass.cast(resultObject);
                    } else if (jsonPathData.getDefaultValue() != null && !jsonPathData.getDefaultValue().isEmpty()) {
                        if (!typeClass.equals(String.class)) {
                            Class<?> stringClass = String.class;
                            Method valueOfMethod = typeClass.getMethod("valueOf", stringClass);
                            returnedObj = valueOfMethod.invoke(null, jsonPathData.getDefaultValue());
                        } else {
                            returnedObj = jsonPathData.getDefaultValue();
                        }
                        log.warn("Unable to parse JSONPath to retrieve required attribute. Sending defaults.");
                    } else {
                        log.warn("Unable to parse JSONPath to retrieve required attribute. Skipping to next attribute.");
                    }
                    objList.add(returnedObj);
                } catch (ClassCastException e) {
                    log.warn("Unable to cast the input data to required type :" + type);
                } catch (InvalidPathException e) {
                    log.warn("Could not find any matches for the incoming event with JSONPath : " + jsonPath.toString());
                } catch (ClassNotFoundException e) {
                    throw new EventBuilderConfigurationException("Cannot find specified class for type " + type, e);
                } catch (NoSuchMethodException e) {
                    throw new EventBuilderConfigurationException("Error trying to convert default value to specified target type.", e);
                } catch (InvocationTargetException e) {
                    throw new EventBuilderConfigurationException("Error trying to convert default value to specified target type.", e);
                } catch (IllegalAccessException e) {
                    throw new EventBuilderConfigurationException("Error trying to convert default value to specified target type.", e);
                }
            }
            outObjArray = objList.toArray(new Object[objList.size()]);
        }
        return outObjArray;
    }

    private class JsonPathData {
        private String type;
        private JsonPath jsonPath;
        private String defaultValue;

        public JsonPathData(JsonPath jsonPath, String type, String defaultValue) {
            this.type = type;
            this.jsonPath = jsonPath;
            this.defaultValue = defaultValue;
        }

        private String getDefaultValue() {
            return defaultValue;
        }

        private JsonPath getJsonPath() {
            return jsonPath;
        }

        private String getType() {
            return type;
        }
    }

}
