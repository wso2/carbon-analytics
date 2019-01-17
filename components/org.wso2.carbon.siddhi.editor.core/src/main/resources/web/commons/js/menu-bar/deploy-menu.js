/**
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

define(([], function () {
    var DeployMenu = {
        id: "deploy",
        label: "Deploy",
        items: [
            {
                id: "deploy-to-server",
                label: "Deploy To Server",
                command: {
                    id: "deploy-to-server",
                    shortcuts: {
                        mac: {
                            key: "shift+d",
                            label: "\u21E7D"
                        },
                        other: {
                            key: "shift+d",
                            label: "Shift+D"
                        }
                    }
                },
                disabled: false
            }

        ]

    };

    return DeployMenu;
}));