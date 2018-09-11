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

package org.wso2.carbon.sp.jobmanager.core.util;

/**
 * This class contains constants needed for the Topology creation.
 */
public class SiddhiTopologyCreatorConstants {
    public static final Integer DEFAULT_PARALLEL = 1;

    public static final String INNERSTREAM_IDENTIFIER = "#";

    public static final String SINK_IDENTIFIER = "@sink";

    public static final String SOURCE_IDENTIFIER = "@source";

    public static final String PERSISTENCETABLE_IDENTIFIER = "store";

    public static final String DEFAULT_SIDDHIAPP_NAME = "SiddhiApp";

    public static final String DISTRIBUTED_IDENTIFIER = "dist";

    public static final String PARALLEL_IDENTIFIER = "parallel";

    public static final String EXECGROUP_IDENTIFIER = "execGroup";

    public static final String SIDDHIAPP_NAME_IDENTIFIER = "name";

    public static final String INPUTSTREAMID = "inputStreamID";

    public static final String OUTPUTSTREAMID = "outputStreamID";

    public static final String DEFAULT_PASSTROUGH_QUERY_TEMPLATE = "from ${" + INPUTSTREAMID + "} select * " + "insert"
            + " into " + "${" + OUTPUTSTREAMID + "}";
    public static final String TRANSPORT_CHANNEL_CREATION_IDENTIFIER = "transportChannelCreationEnabled";
    public static final String PASSTHROUGH = "passthrough";
}
