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
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;

public class RemoteRecordIterator<T> implements Iterator<T> {
    private static final Log log = LogFactory.getLog(RemoteRecordIterator.class);

    private InputStream inputStream;
    private T nextObject;
    private boolean completed;

    public RemoteRecordIterator(InputStream inputStream) throws IOException {
        this.inputStream = inputStream;
        this.completed = false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public synchronized boolean hasNext() {
        try {
            if (nextObject != null) {
                return true;
            } else {
                if (!completed) {
                    try {
                        this.nextObject = (T) GenericUtils.deserializeObject(this.inputStream);
                    } catch (EOFException ex) {
                        this.nextObject = null;
                        completed = true;
                        this.inputStream.close();
                        if (log.isDebugEnabled()) {
                            log.debug("Closing the HTTP connection created!");
                        }
                    }
                }
                return nextObject != null;
            }
        } catch (IOException e) {
            throw new RuntimeException("Error while streaming the results: " + e.getMessage(), e);
        }
    }

    @Override
    public synchronized T next() {
        if (hasNext()) {
            T currentObj = nextObject;
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

}
