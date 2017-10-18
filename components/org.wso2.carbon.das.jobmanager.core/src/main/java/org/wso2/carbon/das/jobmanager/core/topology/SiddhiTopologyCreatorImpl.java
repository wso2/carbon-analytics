/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.das.jobmanager.core.topology;

import org.wso2.carbon.das.jobmanager.core.SiddhiTopologyCreator;
import org.wso2.carbon.das.jobmanager.core.util.TransportStrategy;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.query.api.SiddhiApp;
import org.wso2.siddhi.query.api.exception.SiddhiAppValidationException;
import org.wso2.siddhi.query.api.execution.ExecutionElement;
import org.wso2.siddhi.query.api.execution.partition.Partition;
import org.wso2.siddhi.query.api.execution.partition.ValuePartitionType;
import org.wso2.siddhi.query.api.execution.query.Query;
import org.wso2.siddhi.query.api.execution.query.input.handler.StreamHandler;
import org.wso2.siddhi.query.api.execution.query.input.handler.Window;
import org.wso2.siddhi.query.api.execution.query.input.stream.InputStream;
import org.wso2.siddhi.query.api.execution.query.input.stream.JoinInputStream;
import org.wso2.siddhi.query.api.execution.query.input.stream.SingleInputStream;
import org.wso2.siddhi.query.api.execution.query.input.stream.StateInputStream;
import org.wso2.siddhi.query.api.expression.Variable;
import org.wso2.siddhi.query.api.util.ExceptionUtil;
import org.wso2.siddhi.query.compiler.SiddhiCompiler;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SiddhiTopologyCreatorImpl implements SiddhiTopologyCreator {

    private SiddhiTopologyDataHolder siddhiTopologyDataHolder;
    private SiddhiApp siddhiApp;
    private SiddhiAppRuntime siddhiAppRuntime;

    @Override
    public SiddhiTopology createTopology(String userDefinedSiddhiApp) {

        this.siddhiTopologyDataHolder = new SiddhiTopologyDataHolder();
        this.siddhiApp = SiddhiCompiler.parse(userDefinedSiddhiApp);
        this.siddhiAppRuntime = (new SiddhiManager()).createSiddhiAppRuntime(userDefinedSiddhiApp);
        SiddhiQueryGroup siddhiQueryExecutionGroup;
        int[] queryContextEndIndex;
        int[] queryContextStartIndex;
        String execGroupName;
        int parallel;
        boolean queryElement;


        siddhiTopologyDataHolder.setSiddhiAppName(getAppName());
        siddhiTopologyDataHolder.setUserDefinedSiddhiApp(userDefinedSiddhiApp);


        for (ExecutionElement executionElement : siddhiApp.getExecutionElementList()) {
            parallel = 1;
            execGroupName = null;

            for (int i = 0; i < executionElement.getAnnotations().size(); i++) {
                if (executionElement.getAnnotations().get(i).getElement("execGroup") != null) {
                    execGroupName = siddhiTopologyDataHolder.getSiddhiAppName() + "-" + executionElement.getAnnotations().get(i).getElement("execGroup");

                }

                if (executionElement.getAnnotations().get(i).getElement("parallel") != null) {
                    parallel = Integer.parseInt(executionElement.getAnnotations().get(i).getElement("parallel"));

                }
            }
            if (execGroupName != null && !siddhiTopologyDataHolder.getSiddhiQueryGroupMap().containsKey(execGroupName)){

                siddhiQueryExecutionGroup = new SiddhiQueryGroup();
                siddhiQueryExecutionGroup.setName(execGroupName);
                siddhiQueryExecutionGroup.setParallelism(parallel);

            } else if (siddhiTopologyDataHolder.getSiddhiQueryGroupMap().containsKey(execGroupName)) {
                siddhiQueryExecutionGroup = siddhiTopologyDataHolder.getSiddhiQueryGroupMap().get(execGroupName);

                //Same execution group given  with different parallel numbers
                if (siddhiQueryExecutionGroup.getParallelism() != parallel) {

                    throw new SiddhiAppValidationException("execGroup =" + "\'" + execGroupName + "\' not assigned a unique @dist(parallel)");
                }

            } else {
                //will work if execGroup is not mentioned-those will go to a single app
                siddhiQueryExecutionGroup = new SiddhiQueryGroup();
                siddhiQueryExecutionGroup.setName(siddhiTopologyDataHolder.getSiddhiAppName() + UUID.randomUUID());
                siddhiQueryExecutionGroup.setParallelism(parallel);
            }

            //if execution element is a query
            if (executionElement instanceof Query) {

                //set query
                queryContextStartIndex = ((Query) executionElement).getQueryContextStartIndex();
                queryContextEndIndex = ((Query) executionElement).getQueryContextEndIndex();
                siddhiQueryExecutionGroup.addQuery(removeMetainfoQuery((Query) executionElement, ExceptionUtil.getContext(queryContextStartIndex, queryContextEndIndex, userDefinedSiddhiApp)));

                queryElement = true;
                siddhiTopologyDataHolder.getSiddhiQueryGroupMap().put(execGroupName, assignStreamInfoSiddhiQueryGroup((Query) executionElement, execGroupName, siddhiQueryExecutionGroup, queryElement));


            } else if (executionElement instanceof Partition) {


                //set Partition
                queryContextStartIndex = ((Partition) executionElement).getQueryContextStartIndex();
                queryContextEndIndex = ((Partition) executionElement).getQueryContextEndIndex();
                siddhiQueryExecutionGroup.addQuery(removeMetainfoQuery((Partition) executionElement, ExceptionUtil.getContext(queryContextStartIndex, queryContextEndIndex, userDefinedSiddhiApp)));

                LinkedList<String> stringArrayList;

                //set PartitionMap
                for (Map.Entry<String, PartitionType> partitionTypeEntry : ((Partition) executionElement).getPartitionTypeMap().entrySet()) {

                    if (partitionTypeMap.containsKey(partitionTypeEntry.getKey())) {
                        stringArrayList = partitionTypeMap.get(partitionTypeEntry.getKey());
                    } else {
                        stringArrayList = new LinkedList<String>();
                    }

                    if (partitionTypeEntry.getValue() instanceof ValuePartitionType) {

                        stringArrayList.add(((Variable) ((ValuePartitionType) partitionTypeEntry.getValue()).getExpression()).getAttributeName());
                        partitionTypeMap.put(partitionTypeEntry.getKey(), stringArrayList);
                    } else {
                        //for now
                        throw new SiddhiAppValidationException("Range PartitionType not Supported in Distributed SetUp");
                    }

                }

                List<Query> partitionQueryList = ((Partition) executionElement).getQueryList();
                for (Query query : partitionQueryList) {
                    for (int k = 0; k < query.getAnnotations().size(); k++) {
                        if (query.getAnnotations().get(k).getElement("dist") != null) {
                            throw new SiddhiAppValidationException("Unsupported:@dist annotation inside partition queries");
                        }
                    }

                    queryElement = false;
                    siddhiTopologyDataHolder.getSiddhiQueryGroupMap().put(execGroupName, assignStreamInfoSiddhiQueryGroup(query, execGroupName, siddhiQueryExecutionGroup, queryElement));
                }
            }
        }
        return  new SiddhiTopology(siddhiTopologyDataHolder.getSiddhiAppName(),new ArrayList<>(siddhiTopologyDataHolder.getSiddhiQueryGroupMap().values()));
    }

    private String getAppName() {
        for (int i = 0; i < siddhiApp.getAnnotations().size(); i++) {
            if (siddhiApp.getAnnotations().get(i).getName().equals("name")) {
                return siddhiApp.getAnnotations().get(0).getElements().get(0).getValue();
            }
        }
        return "SiddhiApp-" + UUID.randomUUID();//defaultName
    }

    private String removeMetainfoQuery(ExecutionElement executionElement, String queryElement) {
        int[] queryContextStartIndex;
        int[] queryContextEndIndex;

        for (int i = 0; i < executionElement.getAnnotations().size(); i++) {
            if (executionElement.getAnnotations().get(i).getName().toLowerCase().equals("dist")) {
                queryContextStartIndex = executionElement.getAnnotations().get(i).getQueryContextStartIndex();
                queryContextEndIndex = executionElement.getAnnotations().get(i).getQueryContextEndIndex();
                queryElement = queryElement.replace(ExceptionUtil.getContext(queryContextStartIndex, queryContextEndIndex, siddhiTopologyDataHolder.getUserDefinedSiddhiApp()), "");
            }
        }

        return queryElement;
    }

    private SiddhiQueryGroup assignStreamInfoSiddhiQueryGroup(Query executionElement, String groupName, SiddhiQueryGroup siddhiQueryGroup, boolean queryElement) {
        String[] inputStreamDefinition;
        String[] outputStreamDefinition;

        int parallel = siddhiQueryGroup.getParallelism();

        InputStream inputStream = (executionElement).getInputStream();
        if (parallel > 1 && queryElement) {
            //send to check for validity of the query type eg:join , window, pattern , sequence
            checkQueryType(inputStream);
        }

        List<String> listInputStream = (executionElement).getInputStream().getAllStreamIds();
        for (int j = 0; j < listInputStream.size(); j++) {

            String inputStreamId = listInputStream.get(j);

            //not an inner Stream
            if (!inputStreamId.contains("#")) {

                inputStreamDefinition = returnStreamDefinition(inputStreamId, siddhiApp, userDefinedSiddhiApp, parallel, groupName);
                TransportStrategy transportStrategy = checkQueryStrategy(inputStream, queryElement, inputStreamId, parallel);
                siddhiQueryGroup.setInputStream(inputStreamId, inputStreamDefinition[0], inputStreamDefinition[1], inputStreamDefinition[2], transportStrategy);
            }


        }

        String outputStreamId = executionElement.getOutputStream().getId();
        //not an inner Stream
        if (!outputStreamId.contains("#")) {
            outputStreamDefinition = returnStreamDefinition(outputStreamId, siddhiApp, userDefinedSiddhiApp, parallel, groupName);
            siddhiQueryGroup.setOutputStream(outputStreamId, outputStreamDefinition[0], outputStreamDefinition[1], outputStreamDefinition[2]);

        }

        return siddhiQueryGroup;

    }

    private void checkQueryType(InputStream inputStream) {

        //need to alter partitions only work if its with a partitioned Stream
        if (inputStream instanceof JoinInputStream) {
            throw new SiddhiAppValidationException("Join queries can not have parallel greater than 1  ");

        } else if (inputStream instanceof StateInputStream) {

            String type = ((StateInputStream) inputStream).getStateType().name();
            throw new SiddhiAppValidationException(type + " queries can not have parallel greater than 1  ");

        } else if (inputStream instanceof SingleInputStream) {
            List<StreamHandler> streamHandlers = ((SingleInputStream) inputStream).getStreamHandlers();

            for (int i = 0; i < streamHandlers.size(); i++) {
                if (streamHandlers.get(i) instanceof Window) {
                    throw new SiddhiAppValidationException("Window queries can not have parallel greater than 1  ");
                }
            }
        }
    }

    private TransportStrategy checkQueryStrategy(InputStream inputStream, boolean queryElement, String streamId, int parallel) {

        if (parallel > 1) {
            if (!queryElement){
                if (partitionTypeMap.containsKey(streamId)) {
                    return TransportStrategy.FIELD_GROUPING;
                } else {
                    //inside a partition but not a partitioned stream
                    return TransportStrategy.ALL;
                }

            } else {
                return TransportStrategy.ROUND_ROBIN;
            }
        } else {
            return TransportStrategy.ALL;
        }


    }

}