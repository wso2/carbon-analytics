/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.event.stream.core.internal.util.helper;

import org.wso2.carbon.event.stream.core.internal.util.TenantDefaultArtifactDeployer;
import org.wso2.carbon.stratos.common.beans.TenantInfoBean;
import org.wso2.carbon.stratos.common.exception.StratosException;
import org.wso2.carbon.stratos.common.listeners.TenantMgtListener;

public class TenantMgtListenerImpl implements TenantMgtListener{
    @Override
    public void onTenantCreate(TenantInfoBean tenantInfoBean) throws StratosException {
        TenantDefaultArtifactDeployer.deployDefaultArtifactsForTenant(tenantInfoBean.getTenantId());
    }

    @Override
    public void onTenantUpdate(TenantInfoBean tenantInfoBean) throws StratosException {
    }

    @Override
    public void onTenantDelete(int i) {

    }

    @Override
    public void onTenantRename(int tenantId, String s, String s2) throws StratosException {
    }

    @Override
    public void onTenantInitialActivation(int tenantId) throws StratosException {
    }

    @Override
    public void onTenantActivation(int tenantId) throws StratosException {
    }

    @Override
    public void onTenantDeactivation(int tenantId) throws StratosException {
    }

    @Override
    public void onSubscriptionPlanChange(int tenantId, String s, String s2) throws StratosException {

    }

    @Override
    public int getListenerOrder() {
        return 0;
    }

    @Override
    public void onPreDelete(int i) throws StratosException {

    }
}
