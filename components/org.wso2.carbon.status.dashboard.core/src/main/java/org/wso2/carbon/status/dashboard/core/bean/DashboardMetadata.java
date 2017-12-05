/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.status.dashboard.core.bean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.status.dashboard.core.exception.DashboardException;

/**
 * Class to represent a DashboardMetadata(Dashboard JSON).
 */
public class DashboardMetadata {

    private static final Logger log = LoggerFactory.getLogger(DashboardMetadata.class);

    protected String id;
    protected boolean hasViewerPermission;
    protected boolean hasManagerPermission;
    protected boolean hasMetricManagerPermission;

    /**
     * This method is to get whether user has owner permission or not
     * @return boolean hasViewerPermission
     */
    public boolean isHasViewerPermission() {
        return hasViewerPermission;
    }

    /**
     * This method is to set whether user has owner permission or not
     * @param hasViewerPermission
     */
    public void setHasViewerPermission(boolean hasViewerPermission) {
        this.hasViewerPermission = hasViewerPermission;
    }

    /**
     * This method is to get whether user has designer/editor permission or not
     * @return boolean hasViewerPermission
     */
    public boolean isHasManagerPermission() {
        return hasManagerPermission;
    }

    /**
     * This method is to set whether user has designer/editor permission or not
     * @param hasManagerPermission
     */
    public void setHasManagerPermission(boolean hasManagerPermission) {
        this.hasManagerPermission = hasManagerPermission;
    }

    /**
     * This method is to get whether user has viewer permission or not
     * @return boolean hasViewerPermission
     */
    public boolean isHasMetricManagerPermission() {
        return hasMetricManagerPermission;
    }

    /**
     * This method is to set whether user has viewer permission or not
     * @param hasMetricManagerPermission
     */
    public void setHasMetricManagerPermission(boolean hasMetricManagerPermission) {
        this.hasMetricManagerPermission = hasMetricManagerPermission;
    }

    /**
     * This method is used to get the DB level dashboard id
     *
     * @return String returns the id of dashboard
     */
    public String getId() {
        return id;
    }

    /**
     * This method is used to set the DB level dashboard id
     *
     * @param id id of core/dashboard
     */
    public void setId(String id) {
        this.id = id;
    }


}
