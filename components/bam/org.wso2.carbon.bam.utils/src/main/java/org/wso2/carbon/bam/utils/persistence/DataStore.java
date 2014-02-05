/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.bam.utils.persistence;

import java.nio.ByteBuffer;
import java.util.Map;

public interface DataStore {

    public void initializeStore(Map<String, String> credentials, boolean force) 
            throws InitializationException;

    public boolean isInitialized();

    public boolean persistData(String table, String key, Map<String, String> data);

    public boolean persistBinaryData(String table, String key, Map<String, ByteBuffer> data);

    public void startBatchCommit();

    public void endBatchCommit();

}
