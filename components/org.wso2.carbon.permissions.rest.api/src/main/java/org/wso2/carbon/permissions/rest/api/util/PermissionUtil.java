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
package org.wso2.carbon.permissions.rest.api.util;

import org.wso2.carbon.analytics.permissions.bean.Permission;

import java.nio.charset.Charset;
import java.util.UUID;

/**
 * Permission Util Class.
 */
public class PermissionUtil {
    private static final Charset charset = Charset.forName("UTF-8");

    /**
     * @param permission
     * @return
     */
    public static String createPermissionID(Permission permission) {
        String uuid = UUID.nameUUIDFromBytes(permission.toString().getBytes(charset)).toString();
        return uuid;
    }

    public static Permission mapPermissionModel(org.wso2.carbon.permissions.rest.api.model.Permission model) {
        return new Permission(model.getAppName(), model.getPermissionString());
    }

    public static String removeCRLFCharacters(String str) {
        if (str != null) {
            str = str.replace('\n', '_').replace('\r', '_');
        }
        return str;
    }
}
