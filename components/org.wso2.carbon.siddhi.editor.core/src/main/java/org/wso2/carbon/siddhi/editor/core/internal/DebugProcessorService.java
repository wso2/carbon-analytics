/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


package org.wso2.carbon.siddhi.editor.core.internal;


import org.wso2.carbon.siddhi.editor.core.exception.NoSuchSiddhiAppException;

/**
 * Class which manage execution plans and their runtime
 */
public class DebugProcessorService {

    public DebugRuntime getSiddhiAppRuntimeHolder(String siddhiAppName) {
        return EditorDataHolder.getSiddhiAppMap().get(siddhiAppName);
    }

    public synchronized void start(String siddhiAppName) {
        if (EditorDataHolder.getSiddhiAppMap().containsKey(siddhiAppName)) {
            DebugRuntime runtimeHolder = EditorDataHolder.getSiddhiAppMap().get(siddhiAppName);
            runtimeHolder.start();
        } else {
            throw new NoSuchSiddhiAppException(
                    String.format("Siddhi App %s does not exists", siddhiAppName)
            );
        }
    }

    public synchronized void debug(String siddhiAppName) {
        if (EditorDataHolder.getSiddhiAppMap().containsKey(siddhiAppName)) {
            DebugRuntime runtimeHolder = EditorDataHolder.getSiddhiAppMap().get(siddhiAppName);
            runtimeHolder.debug();
        } else {
            throw new NoSuchSiddhiAppException(
                    String.format("Siddhi App %s does not exists", siddhiAppName)
            );
        }
    }

    public synchronized void stop(String siddhiAppName) {
        if (EditorDataHolder.getSiddhiAppMap().containsKey(siddhiAppName)) {
            DebugRuntime runtimeHolder = EditorDataHolder.getSiddhiAppMap().get(siddhiAppName);
            runtimeHolder.stop();
        } else {
            throw new NoSuchSiddhiAppException(
                    String.format("Siddhi App %s does not exists", siddhiAppName)
            );
        }
    }

    public synchronized void deploy(String siddhiAppName, String siddhiApp) {
        if (!EditorDataHolder.getSiddhiAppMap().containsKey(siddhiAppName)) {
            DebugRuntime runtimeHolder = new DebugRuntime(siddhiAppName, siddhiApp);
            EditorDataHolder.getSiddhiAppMap().put(siddhiAppName, runtimeHolder);
        } else {
            EditorDataHolder.getSiddhiAppMap().get(siddhiAppName).reload(siddhiApp);
        }
    }

    public synchronized void undeploy(String siddhiAppName) {
        if (EditorDataHolder.getSiddhiAppMap().containsKey(siddhiAppName)) {
            DebugRuntime runtimeHolder = EditorDataHolder.getSiddhiAppMap().get(siddhiAppName);
            runtimeHolder.stop();
            EditorDataHolder.getSiddhiAppMap().remove(siddhiAppName);
        } else {
            throw new NoSuchSiddhiAppException(
                    String.format("Siddhi App %s does not exists", siddhiAppName)
            );
        }
    }


}
