/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.data.provider.siddhi;

import io.siddhi.core.SiddhiAppRuntime;
import io.siddhi.core.SiddhiManager;
import io.siddhi.query.compiler.exception.SiddhiParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.data.provider.exception.DataProviderException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class SiddhiAppRuntimeHolder {
    private static final Logger logger = LoggerFactory.getLogger(SiddhiAppRuntimeHolder.class);
    static Map<String, SiddhiAppRuntime> siddhiAppRuntimeMap = new HashMap<>();
    static SiddhiManager siddhiManager;

    public static SiddhiAppRuntime getSiddhiAppRuntime(String siddhiApp) throws DataProviderException {
        logger.debug("Siddhi App Runtime Map key set size :" + siddhiAppRuntimeMap.size());
        logger.debug("Siddhi App Runtime Map key set :" + Arrays.toString(siddhiAppRuntimeMap.keySet().toArray()));
        SiddhiAppRuntime siddhiAppRuntime;
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new DataProviderException("Invalid Encoding type SHA-256 ", e);
        }
        byte[] hash = digest.digest(siddhiApp.getBytes(StandardCharsets.UTF_8));
        String md5OfSiddhiApp = Base64.getEncoder().encodeToString(hash);
        if (siddhiAppRuntimeMap.containsKey(md5OfSiddhiApp)) {
            return siddhiAppRuntimeMap.get(md5OfSiddhiApp);
        } else {
            try {
                siddhiAppRuntime = getSiddhiManager().createSiddhiAppRuntime(siddhiApp);
                siddhiAppRuntime.setPurgingEnabled(false);
                siddhiAppRuntime.start();
                siddhiAppRuntimeMap.put(md5OfSiddhiApp, siddhiAppRuntime);
                return siddhiAppRuntime;
            } catch (SiddhiParserException e) {
                throw new DataProviderException("Invalid Siddhi App Context", e);
            }
        }
    }

    private static SiddhiManager getSiddhiManager() {
        if (siddhiManager == null) {
            siddhiManager = new SiddhiManager();
        }
        return siddhiManager;
    }
}
