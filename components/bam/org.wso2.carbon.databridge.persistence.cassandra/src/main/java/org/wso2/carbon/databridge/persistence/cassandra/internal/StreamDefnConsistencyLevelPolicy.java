package org.wso2.carbon.databridge.persistence.cassandra.internal;

import me.prettyprint.cassandra.service.OperationType;
import me.prettyprint.hector.api.ConsistencyLevelPolicy;
import me.prettyprint.hector.api.HConsistencyLevel;

/**
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class StreamDefnConsistencyLevelPolicy implements ConsistencyLevelPolicy {

    private final HConsistencyLevel readConsistency;
    private final HConsistencyLevel writeConsistency;


    public StreamDefnConsistencyLevelPolicy(String readConsistency, String writeConsistency) {
        this.readConsistency = HConsistencyLevel.valueOf(readConsistency);
        this.writeConsistency = HConsistencyLevel.valueOf(writeConsistency);
    }

    @Override
    public HConsistencyLevel get(OperationType op) {

        if (op.equals(OperationType.META_WRITE) || op.equals(OperationType.WRITE)) {
            return writeConsistency;
        } else if (op.equals(OperationType.META_READ) || op.equals(OperationType.READ))  {
            return readConsistency;
        }
        return HConsistencyLevel.QUORUM;
    }

    @Override
    public HConsistencyLevel get(OperationType op, String cfName) {
        if (op.equals(OperationType.META_WRITE) || op.equals(OperationType.WRITE)) {
            return writeConsistency;
        } else if (op.equals(OperationType.META_READ) || op.equals(OperationType.READ))  {
            return readConsistency;
        }
        return HConsistencyLevel.QUORUM;
    }
}
