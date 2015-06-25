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

package org.wso2.carbon.analytics.spark.master


import akka.serialization.Serialization
import org.apache.spark.SparkConf
import org.apache.spark.deploy.master.{LeaderElectable, StandaloneRecoveryModeFactory}
import org.wso2.carbon.analytics.spark.core.util.master.{AnalyticsLeaderElectionAgent, AnalyticsPersistenceEngine}

/**
 * Created by niranda on 6/25/15.
 */
class AnalyticsStandaloneRecoveryModeFactoryScala(conf: SparkConf, serializer: Serialization)
  extends StandaloneRecoveryModeFactory(conf, serializer) {

  override def createPersistenceEngine() = new AnalyticsPersistenceEngine(conf, serializer)

  override def createLeaderElectionAgent(master: LeaderElectable) = new
      AnalyticsLeaderElectionAgent(master)
}
