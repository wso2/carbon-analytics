package org.wso2.carbon.bam.jmx.agent;

import org.wso2.carbon.databridge.agent.thrift.DataPublisher;

import java.util.HashMap;
import java.util.Map;

/**
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class TenantPublisherConfigData {

    private static Map<String, DataPublisher> dataPublisherHashMap;

    public static Map<String, DataPublisher> getDataPublisherMap() {

        if (dataPublisherHashMap == null) {
            synchronized (TenantPublisherConfigData.class) {
                if (dataPublisherHashMap == null) {
                    dataPublisherHashMap = new HashMap<String, DataPublisher>();
                }
            }
        }
        return dataPublisherHashMap;
    }
}
