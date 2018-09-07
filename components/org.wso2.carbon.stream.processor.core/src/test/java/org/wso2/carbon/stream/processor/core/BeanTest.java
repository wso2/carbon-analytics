/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.stream.processor.core;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.stream.processor.core.internal.beans.DeploymentConfig;
import org.wso2.carbon.stream.processor.core.internal.beans.LiveSyncConfig;

public class BeanTest {

    @Test
    public void testBeanFunctionality() {
        LiveSyncConfig liveSyncConfig = new LiveSyncConfig();
        liveSyncConfig.setEnabled(true);
        liveSyncConfig.setAdvertisedHost("localhost");
        liveSyncConfig.setAdvertisedPort(9090);
        liveSyncConfig.setUsername("admin");
        liveSyncConfig.setPassword("admin");
        DeploymentConfig deploymentConfig = new DeploymentConfig();
        deploymentConfig.setType("ha");
        deploymentConfig.setLiveSync(liveSyncConfig);
        deploymentConfig.setOutputSyncInterval(1000);
        deploymentConfig.setRetryAppSyncPeriod(1000);
        deploymentConfig.setSinkQueueCapacity(20000);
        deploymentConfig.setSourceQueueCapacity(20000);
        deploymentConfig.setStateSyncGracePeriod(1000);

        Assert.assertEquals(deploymentConfig.getType(), "ha");
        Assert.assertEquals(deploymentConfig.getOutputSyncInterval(), 1000);
        Assert.assertEquals(deploymentConfig.getRetryAppSyncPeriod(), 1000);
        Assert.assertEquals(deploymentConfig.getSinkQueueCapacity(), 20000);
        Assert.assertEquals(deploymentConfig.getSourceQueueCapacity(), 20000);
        Assert.assertEquals(deploymentConfig.getStateSyncGracePeriod(), 1000);
        Assert.assertEquals(deploymentConfig.getLiveSync().isEnabled(), true);
        Assert.assertEquals(deploymentConfig.getLiveSync().getAdvertisedHost(), "localhost");
        Assert.assertEquals(deploymentConfig.getLiveSync().getAdvertisedPort(), 9090);
        Assert.assertEquals(deploymentConfig.getLiveSync().getUsername(), "admin");
        Assert.assertEquals(deploymentConfig.getLiveSync().getPassword(), "admin");
    }
}
