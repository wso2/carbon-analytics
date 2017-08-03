/*
 * Copyright (c)  2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.event.simulator.core.internal.util;

import org.apache.commons.fileupload.util.LimitedInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.event.simulator.core.exception.FileLimitExceededException;

import java.io.IOException;
import java.io.InputStream;

/**
 * ValidatedInputStream class validates whether the size of uploaded file is less than the maximum file size allowed.
 */
public class ValidatedInputStream extends LimitedInputStream {
    private static final Logger log = LoggerFactory.getLogger(ValidatedInputStream.class);

    public ValidatedInputStream(InputStream inputStream, long pSizeMax) {
        super(inputStream, pSizeMax);
    }

    /**
     * raiseError() alerts when the input stream exceeds the maximum size specified
     */
    @Override
    protected void raiseError(long sizeLimit, long actualSize) throws IOException {
        in.close();
        throw new FileLimitExceededException("File size exceeds the maximum size limit of " +
                (sizeLimit / 1024 / 1024) + " MB.");
    }
}
