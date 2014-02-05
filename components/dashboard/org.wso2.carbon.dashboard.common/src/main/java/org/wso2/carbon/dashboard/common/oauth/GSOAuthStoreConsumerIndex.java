/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.dashboard.common.oauth;

import com.google.common.base.Objects;
import org.apache.shindig.gadgets.oauth.BasicOAuthStoreConsumerIndex;

public class GSOAuthStoreConsumerIndex extends BasicOAuthStoreConsumerIndex {
    @Override
    public int hashCode() {
        return Objects.hashCode(null, super.getServiceName());
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final BasicOAuthStoreConsumerIndex other = (BasicOAuthStoreConsumerIndex) obj;
        if (super.getServiceName() == null) {
            if (super.getServiceName() != null) return false;
        } else if (!super.getServiceName().equals(other.getServiceName())) return false;
        return true;
    }
}
