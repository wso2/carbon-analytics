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

package org.wso2.carbon.das.jobmanager.core.appCreator;

import org.wso2.carbon.das.jobmanager.core.topology.SiddhiQueryGroup;

import java.util.ArrayList;
import java.util.List;

public class SPSiddhiAppCreator extends AbstractSiddhiAppCreator {

    @Override protected List<String> createApps(SiddhiQueryGroup queryGroup) {
        String queryTemplate = queryGroup.getSiddhiApp();
        List<String> queryList = generateQueryList(queryTemplate, queryGroup.getName(), queryGroup.getParallelism());
//        queryTemplate = processInputStreams(queryTemplate, queryGroup.getInputStreams());
        return null;
    }

    private List<String> generateQueryList(String queryTemplate, String name, int parallelism) {
        List<String> queries = new ArrayList<>(parallelism);
        for (int i = 0; i < parallelism; i++) {
            //use apache string substitute
        }
        return null;
    }


}
