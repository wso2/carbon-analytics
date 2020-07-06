/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.siddhi.error.handler.core.execution;

import io.siddhi.core.util.error.handler.model.ErrorEntry;
import io.siddhi.core.util.error.handler.store.ErrorStore;
import org.wso2.carbon.siddhi.error.handler.core.exception.SiddhiErrorHandlerException;
import org.wso2.carbon.siddhi.error.handler.core.internal.SiddhiErrorHandlerDataHolder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Communicates with the Error Store for Siddhi Error Handler functionalities.
 */
public class ErrorStoreAccessor {
    private ErrorStoreAccessor() {
    }

    public static List<ErrorEntry> getErroneousEvents(String siddhiAppName, String limit, String offset)
        throws SiddhiErrorHandlerException {
        ErrorStore errorStore = SiddhiErrorHandlerDataHolder.getInstance().getErrorStore();
        if (errorStore != null) {
            Map<String, String> queryParams = new HashMap<>();
            if (limit != null) {
                queryParams.put("limit", limit);
            }
            if (offset != null) {
                queryParams.put("offset", offset);
            }
            return errorStore.loadErrorEntries(siddhiAppName, queryParams);
        }
        throw new SiddhiErrorHandlerException("Error store is unavailable.");
    }

    public static void discardErroneousEvent(int id) throws SiddhiErrorHandlerException {
        ErrorStore errorStore = SiddhiErrorHandlerDataHolder.getInstance().getErrorStore();
        if (errorStore != null) {
            errorStore.discardErroneousEvent(id);
        } else {
            throw new SiddhiErrorHandlerException("Error store is unavailable.");
        }
    }

    public static Map<String, String> getStatus() throws SiddhiErrorHandlerException {
        ErrorStore errorStore = SiddhiErrorHandlerDataHolder.getInstance().getErrorStore();
        if (errorStore != null) {
            return errorStore.getStatus();
        } else {
            throw new SiddhiErrorHandlerException("Error store is unavailable.");
        }
    }
}
