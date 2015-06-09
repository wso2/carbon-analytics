/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.analytics.spark.core.util.master;

import org.wso2.carbon.analytics.spark.master.AbstractPersistenceEngine;
import scala.collection.Seq;
import scala.reflect.ClassTag;

/**
 * Created by niranda on 6/9/15.
 */
public class AnalyticsPersistanceEngine extends AbstractPersistenceEngine {
    /**
     * Defines how the object is serialized and persisted. Implementation will
     * depend on the store used.
     *
     * @param name
     * @param obj
     */
    @Override
    public void persist(String name, Object obj) {

    }

    /**
     * Defines how the object referred by its name is removed from the store.
     *
     * @param name
     */
    @Override
    public void unpersist(String name) {

    }

    /**
     * Gives all objects, matching a prefix. This defines how objects are
     * read/deserialized back.
     *
     * @param prefix
     * @param evidence$1
     */
    @Override
    public <T> Seq<T> read(String prefix, ClassTag<T> evidence$1) {
        return null;
    }
}
