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

import io.siddhi.core.util.error.handler.model.ErrorEntry;
import io.siddhi.core.util.error.handler.util.ErrorHandlerUtils;

import java.io.IOException;

/**
 * Wraps an {@link ErrorEntry} in order to deal with editable payloads in
 * {@link io.siddhi.core.util.error.handler.util.ErroneousEventType#PAYLOAD_STRING}.
 */
public class ErrorEntryWrapper {
    private ErrorEntry errorEntry;
    private String modifiablePayloadString;
    private boolean isPayloadModifiable;

    public ErrorEntryWrapper(ErrorEntry errorEntry, boolean isPayloadModifiable)
        throws IOException, ClassNotFoundException {
        this.errorEntry = errorEntry;
        this.isPayloadModifiable = isPayloadModifiable;
        if (isPayloadModifiable) {
            this.modifiablePayloadString = (String) ErrorHandlerUtils.getAsObject(errorEntry.getEventAsBytes());
        }
    }

    public ErrorEntry getErrorEntry() {
        return errorEntry;
    }

    public String getModifiablePayloadString() {
        return modifiablePayloadString;
    }

    public boolean isPayloadModifiable() {
        return isPayloadModifiable;
    }
}
