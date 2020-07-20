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

package org.wso2.carbon.siddhi.error.handler.core.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import io.siddhi.core.util.error.handler.model.ErrorEntry;
import io.siddhi.core.util.error.handler.util.ErrorHandlerUtils;
import org.wso2.carbon.siddhi.error.handler.core.exception.SiddhiErrorHandlerException;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains utility methods related to Siddhi Error Handler.
 */
public class SiddhiErrorHandlerUtils {

    private SiddhiErrorHandlerUtils() {}

    public static List<ErrorEntry> convertToList(JsonArray errorEntryWrappersBody) throws SiddhiErrorHandlerException {
        List<ErrorEntry> errorEntries = new ArrayList<>();
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        Type mapType = new TypeToken<List<ErrorEntryWrapper>>() {}.getType();
        List<ErrorEntryWrapper> errorEntryWrappers = gson.fromJson(errorEntryWrappersBody, mapType);
        for (ErrorEntryWrapper errorEntryWrapper : errorEntryWrappers) {
            if (errorEntryWrapper.isPayloadModifiable()) {
                ErrorEntry errorEntry = errorEntryWrapper.getErrorEntry();
                String payloadString = errorEntryWrapper.getModifiablePayloadString();
                try {
                    errorEntries.add(new ErrorEntry(errorEntry.getId(), errorEntry.getTimestamp(),
                        errorEntry.getSiddhiAppName(), errorEntry.getStreamName(),
                        ErrorHandlerUtils.getAsBytes(payloadString),
                        errorEntry.getCause(), errorEntry.getStackTrace(), errorEntry.getOriginalPayload(),
                        errorEntry.getErrorOccurrence(), errorEntry.getEventType(), errorEntry.getErrorType()));
                } catch (IOException e) {
                    throw new SiddhiErrorHandlerException(String.format(
                        "Failed to get modifiable payload as bytes for error entry with id: %s.", errorEntry.getId()));
                }
            } else {
                errorEntries.add(errorEntryWrapper.getErrorEntry());
            }
        }
        return errorEntries;
    }

    public static long getRetentionStartTimestamp(long currentTimestamp, int retentionDays) {
        return currentTimestamp - (1000L * 60 * 60 * 24 * retentionDays);
    }
}
