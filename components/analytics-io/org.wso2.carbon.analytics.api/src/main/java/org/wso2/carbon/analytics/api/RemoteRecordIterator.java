/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.analytics.api;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsIterator;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.io.commons.GenericUtils;

/**
 * This is the iterator which is provided back to the caller of the analytics API to
 * iterate through the result set,
 */

public class RemoteRecordIterator implements AnalyticsIterator<Record> {
    private static final Log log = LogFactory.getLog(RemoteRecordIterator.class);

    private InputStream inputStream;
    private Record nextObject;
    private boolean completed;

    public RemoteRecordIterator(InputStream inputStream) throws IOException {
        this.inputStream = inputStream;
        this.completed = false;
    }

    @Override
    public synchronized boolean hasNext() {
        try {
            if (nextObject != null) {
                return true;
            } else {
                if (!completed) {
                    try {
                        this.nextObject = (Record) GenericUtils.deserializeObject(this.inputStream);
                    } catch (EOFException ex) {
                        cleanup();
                    }
                }
                return nextObject != null;
            }
        } catch (IOException e) {
            throw new RuntimeException("Error while streaming the results: " + e.getMessage(), e);
        }
    }

    @Override
    public synchronized Record next() {
        if (hasNext()) {
            Record currentObj = nextObject;
            nextObject = null;
            return currentObj;
        } else {
            return null;
        }
    }

    @Override
    public void remove() {
        throw new RuntimeException("Cannot remove the records from this iterator");
    }

    private void cleanup() throws IOException {
        this.nextObject = null;
        completed = true;
        this.inputStream.close();
        if (log.isDebugEnabled()) {
            log.debug("Closing the HTTP connection created!");
        }
    }

    @Override
    public void close() throws IOException {
        if (!completed) {
            cleanup();
        }
    }
}
