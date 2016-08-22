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

package org.wso2.carbon.analytics.spark.core.deploy;

import org.apache.spark.SparkConf;
import org.apache.spark.deploy.master.LeaderElectable;
import org.apache.spark.deploy.master.LeaderElectionAgent;
import org.apache.spark.deploy.master.PersistenceEngine;
import org.apache.spark.deploy.master.StandaloneRecoveryModeFactory;
import org.apache.spark.serializer.Serializer;

/**
 * Created by niranda on 6/9/15.
 */
public class AnalyticsRecoveryModeFactory extends StandaloneRecoveryModeFactory {

    private Serializer serializer;

    public AnalyticsRecoveryModeFactory(SparkConf conf, Serializer serializer) {
        super(conf, serializer);
        this.serializer = serializer;
    }

    /**
     * PersistenceEngine defines how the persistent data(Information about worker, driver etc..)
     * is handled for recovery.
     */
    @Override
    public PersistenceEngine createPersistenceEngine() {
        return new AnalyticsPersistenceEngine(serializer);
    }

    /**
     * Create an instance of LeaderAgent that decides who gets elected as master.
     *
     * @param master
     */
    @Override
    public LeaderElectionAgent createLeaderElectionAgent(LeaderElectable master) {
        return new AnalyticsLeaderElectionAgent(master);
    }
}
