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


import org.apache.commons.httpclient.HttpMethod;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Iterator;

public class RemoteRecordIterator<T> implements Iterator {
    private ObjectInputStream objectInputStream;
    private T nextObject;
    private boolean completed;

    public RemoteRecordIterator(InputStream inputStream) throws IOException {
        objectInputStream = new ObjectInputStream(inputStream);
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
                        this.nextObject = (T) objectInputStream.readObject();
                    } catch (EOFException ex) {
                        this.nextObject = null;
                        completed = true;
                        this.objectInputStream.close();
                    }
                }
                return nextObject != null;
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Error while streaming the results ." + e.getMessage(), e);
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
