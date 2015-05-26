/**
 * Copyright (c) 2005 - 2013, WSO2 Inc. (http://www.wso2.com) All Rights Reserved.
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
package org.wso2.carbon.analytics.dashboard;

import org.apache.axis2.deployment.DeploymentException;

/**
 * This class represents the exception to be thrown when there are some problems encountered
 * when deploying the analytics dashboard artifacts.
 */
public class DashboardDeploymentException extends DeploymentException {

    private static final long serialVersionUID = -6919598081706988827L;

    public DashboardDeploymentException(String message, Throwable cause) {
        super(message, cause);
    }
}


