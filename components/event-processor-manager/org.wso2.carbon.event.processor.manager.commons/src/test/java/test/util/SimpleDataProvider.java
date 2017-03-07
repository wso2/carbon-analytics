/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package test.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class SimpleDataProvider implements DataProvider {
    private Random random = new Random();
    private static final Map<String, String> ARBITRARY_ATTRIBUTE_MAP;

    static {
        Map<String, String> initialAttributeMap = new HashMap<>();
        initialAttributeMap.put("MSG_TYPE", "text");
        initialAttributeMap.put("maxCount", "1000");
        initialAttributeMap.put("testAttribute1", "testVal1");
        initialAttributeMap.put("testAttribute2", "testVal2");
        initialAttributeMap.put("testAttribute3", "testVal3");
        initialAttributeMap.put("testAttribute4", "testVal4");
        initialAttributeMap.put("testAttribute5", "testVal5");
        initialAttributeMap.put("testAttribute6", "testVal6");
        initialAttributeMap.put("testAttribute7", "testVal7");
        initialAttributeMap.put("testAttribute8", "testVal8");
        ARBITRARY_ATTRIBUTE_MAP = Collections.unmodifiableMap(initialAttributeMap);
    }

    @Override
    public Object[] getEvent() {
        return new Object[]{random.nextInt(), random.nextFloat(), "Abcdefghijklmnop" + random.nextLong(), random.nextInt()};
    }

    @Override
    public Map<String, String> getArbitraryAttributeMap() {
        Map<String, String> arbitraryMap = new HashMap<>();
        for (Map.Entry<String, String> entry : SimpleDataProvider.ARBITRARY_ATTRIBUTE_MAP.entrySet()) {
            if (random.nextInt() % 2 == 0) {
                arbitraryMap.put(entry.getKey(), entry.getValue());
            }
        }
        return arbitraryMap;
    }
}
