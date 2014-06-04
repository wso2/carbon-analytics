/*
* Copyright 2004,2013 The Apache Software Foundation.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.carbon.bam.webapp.stat.publisher.conf;

import org.wso2.carbon.databridge.agent.thrift.Agent;
import org.wso2.carbon.databridge.agent.thrift.AsyncDataPublisher;
import org.wso2.carbon.databridge.agent.thrift.lb.LoadBalancingDataPublisher;

  /*
  * Purpose of this class is keep the configurations of BAM data publisher.
  */

public class EventPublisherConfig {

    private AsyncDataPublisher dataPublisher;
    private LoadBalancingDataPublisher loadBalancingDataPublisher;
    private static Agent agent = new Agent();

    public AsyncDataPublisher getDataPublisher() {
        return dataPublisher;
    }

    public void setDataPublisher(AsyncDataPublisher dataPublisher) {
        this.dataPublisher = dataPublisher;
    }

    public void setLoadBalancingPublisher(LoadBalancingDataPublisher loadBalancingPublisher){
        this.loadBalancingDataPublisher =  loadBalancingPublisher;
    }

    public LoadBalancingDataPublisher getLoadBalancingDataPublisher(){
        return loadBalancingDataPublisher;
    }

    public static Agent getAgent(){
        return agent;
    }

}
