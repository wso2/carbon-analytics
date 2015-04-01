/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.analytics.dataservice.restapi.adapters;

import org.wso2.carbon.analytics.dataservice.restapi.Constants;
import org.wso2.carbon.analytics.dataservice.restapi.beans.RecordValueTypeBean;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsCategoryPath;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents the adapter class for converting json java map to a list.
 */
public final class RecordValuesAdapter extends XmlAdapter<List<RecordValueTypeBean>, LinkedHashMap<String, Object>> {
    /**
     * NOTE : Cannot use generic MAP interface, since jaxb cannot resolve the type.
     */
    @Override
    public List<RecordValueTypeBean> marshal(LinkedHashMap<String, Object> valuesMap) throws Exception {
        List<RecordValueTypeBean> recordValues = new ArrayList<RecordValueTypeBean>();
        for(Map.Entry<String, Object> entry : valuesMap.entrySet()) {
            RecordValueTypeBean recordEntry =
                    new RecordValueTypeBean();
            recordEntry.setField(entry.getKey());
            recordEntry.setValue(entry.getValue());
            recordValues.add(recordEntry);
        }
        return recordValues;
    }

    @Override
    @SuppressWarnings("unchecked")
    public LinkedHashMap<String, Object> unmarshal(List<RecordValueTypeBean> recordValues) throws Exception {
        LinkedHashMap<String, Object> valueMap = new LinkedHashMap<String, Object>();
        for(RecordValueTypeBean recordEntry: recordValues) {
            if (recordEntry.getValue() instanceof LinkedHashMap) {
                Map<String, Object> keyValPairMap = (LinkedHashMap<String, Object>) recordEntry.getValue();
                String[] path = (String[]) keyValPairMap.get(Constants.FacetAttributes.PATH);
                Float weight = (Float) keyValPairMap.get(Constants.FacetAttributes.WEIGHT);
                if (path != null && keyValPairMap.keySet().size() <= 2) {
                    AnalyticsCategoryPath categoryPath = new
                            AnalyticsCategoryPath(path);
                    if (weight != null) {
                        categoryPath.setWeight(weight);
                    }
                    valueMap.put(recordEntry.getField(), categoryPath);
                }
            } else {
                valueMap.put(recordEntry.getField(), recordEntry.getValue());
            }
        }
        return valueMap;
    }

}