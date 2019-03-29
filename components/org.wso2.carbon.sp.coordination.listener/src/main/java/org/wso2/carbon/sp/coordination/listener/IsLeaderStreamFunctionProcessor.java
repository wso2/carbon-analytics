/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.sp.coordination.listener;

import org.wso2.carbon.sp.coordination.listener.internal.CoordinationListenerDataHolder;
import org.wso2.siddhi.annotation.Example;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.annotation.ReturnAttribute;
import org.wso2.siddhi.annotation.util.DataType;
import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.function.FunctionExecutor;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.util.Map;

/**
 * This Siddhi extension returns true if this node is the leader node in the cluster.
 * False otherwise.
 * In case the leader in the cluster could not be determined (e.g. due to cluster-DB connection issue)
 * false is returned.
 *
 */
@Extension(
        name = "isLeader",
        namespace = "coordination",
        description = "This extension returns true if this node is the leader node in the cluster. False otherwise.",
        returnAttributes = @ReturnAttribute(
                description = "This returns true if this node is the leader node in the cluster. False otherwise.",
                type = {DataType.BOOL}),
        examples = @Example(
                syntax = "from inputStream \n" +
                        "select attribute1, attribute2, coordination:isLeader() as isLeader\n" +
                        "insert into leaderAwareStream;",
                description = "In this query, the third attribute of the leaderAwareStream 'isLeader' will be a " +
                        "boolean, which will have the values either 'true' or 'false'."
        )
)
public class IsLeaderStreamFunctionProcessor extends FunctionExecutor {

    Attribute.Type returnType = Attribute.Type.BOOL;

    @Override
    protected void init(ExpressionExecutor[] expressionExecutors, ConfigReader configReader,
                        SiddhiAppContext siddhiAppContext) {
        //Nothing to be done.
    }

    @Override
    protected Object execute(Object[] data) {
        //Since this function takes in no parameters, this method does not get called. Hence, not implemented.
        return null;
    }

    @Override
    protected Object execute(Object data) {
        return CoordinationListenerDataHolder.isLeader();
    }

    @Override
    public Attribute.Type getReturnType() {
        return returnType;
    }

    @Override
    public Map<String, Object> currentState() {
        return null;  //No need to maintain a state.
    }

    @Override
    public void restoreState(Map<String, Object> map) {
        //Since there's no need to maintain a state, nothing needs to be done here.
    }
}
