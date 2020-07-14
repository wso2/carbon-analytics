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
    private static final String ERROR_STORE_IS_UNAVAILABLE_MESSAGE = "Error store is unavailable.";

    private ErrorStoreAccessor() {
    }

    public static int getTotalErrorEntriesCount() throws SiddhiErrorHandlerException {
        ErrorStore errorStore = SiddhiErrorHandlerDataHolder.getInstance().getErrorStore();
        if (errorStore != null) {
            int count = errorStore.getTotalErrorEntriesCount();
            if (count > -1) {
                return count;
            } else {
                throw new SiddhiErrorHandlerException("Failed to get total error entries count.");
            }
        }
        throw new SiddhiErrorHandlerException(ERROR_STORE_IS_UNAVAILABLE_MESSAGE);
    }

    public static int getErrorEntriesCount(String siddhiAppName) throws SiddhiErrorHandlerException {
        ErrorStore errorStore = SiddhiErrorHandlerDataHolder.getInstance().getErrorStore();
        if (errorStore != null) {
            int count = errorStore.getErrorEntriesCount(siddhiAppName);
            if (count > -1) {
                return count;
            } else {
                throw new SiddhiErrorHandlerException(
                    String.format("Failed to get error entries count for Siddhi app: %s.", siddhiAppName));
            }
        }
        throw new SiddhiErrorHandlerException(ERROR_STORE_IS_UNAVAILABLE_MESSAGE);
    }

    public static List<ErrorEntry> getErrorEntries(String siddhiAppName, String isDescriptive,
                                                   String limit, String offset)
        throws SiddhiErrorHandlerException {
        ErrorStore errorStore = SiddhiErrorHandlerDataHolder.getInstance().getErrorStore();
        if (errorStore != null) {
            Map<String, String> queryParams = new HashMap<>();
            if (isDescriptive != null) {
                queryParams.put("descriptive", isDescriptive);
            }
            if (limit != null) {
                queryParams.put("limit", limit);
            }
            if (offset != null) {
                queryParams.put("offset", offset);
            }
            return errorStore.loadErrorEntries(siddhiAppName, queryParams);
        }
        throw new SiddhiErrorHandlerException(ERROR_STORE_IS_UNAVAILABLE_MESSAGE);
    }

    public static ErrorEntry getErrorEntry(int id) throws SiddhiErrorHandlerException {
        ErrorStore errorStore = SiddhiErrorHandlerDataHolder.getInstance().getErrorStore();
        if (errorStore != null) {
            return errorStore.loadErrorEntry(id);
        }
        throw new SiddhiErrorHandlerException(ERROR_STORE_IS_UNAVAILABLE_MESSAGE);
    }

    public static void purgeErrorStore(Map retentionPolicyParams) throws SiddhiErrorHandlerException {
        ErrorStore errorStore = SiddhiErrorHandlerDataHolder.getInstance().getErrorStore();
        if (errorStore != null) {
            errorStore.purge(retentionPolicyParams);
        } else {
            throw new SiddhiErrorHandlerException(ERROR_STORE_IS_UNAVAILABLE_MESSAGE);
        }
    }

    public static void discardErrorEntry(int id) throws SiddhiErrorHandlerException {
        ErrorStore errorStore = SiddhiErrorHandlerDataHolder.getInstance().getErrorStore();
        if (errorStore != null) {
            errorStore.discardErrorEntry(id);
        } else {
            throw new SiddhiErrorHandlerException(ERROR_STORE_IS_UNAVAILABLE_MESSAGE);
        }
    }

    public static void discardErrorEntries(String siddhiAppName) throws SiddhiErrorHandlerException {
        ErrorStore errorStore = SiddhiErrorHandlerDataHolder.getInstance().getErrorStore();
        if (errorStore != null) {
            errorStore.discardErrorEntries(siddhiAppName);
        } else {
            throw new SiddhiErrorHandlerException(ERROR_STORE_IS_UNAVAILABLE_MESSAGE);
        }
    }
}
