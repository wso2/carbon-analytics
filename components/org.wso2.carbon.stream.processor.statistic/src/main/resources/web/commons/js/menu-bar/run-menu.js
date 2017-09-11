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

define(([],function (){
    var RunMenu = {
        id: "run",
        label: "Run",
        items: [
            {
                id: "run",
                label: "Run",
                command: {
                    id: "run",
                    shortcuts: {
                        mac: {
                            key: "command+r",
                            label: "\u2318R"
                        },
                        other: {
                            key: "ctrl+r",
                            label: "Ctrl+R"
                        }
                    }
                },
                disabled: true
            },
             {
                 id: "debug",
                 label: "Debug",
                 command: {
                     id: "debug",
                     shortcuts: {
                         mac: {
                             key: "command+shift+d",
                             label: "\u2318\u21E7D"
                         },
                         other: {
                             key: "ctrl+shift+d",
                             label: "Ctrl+Shift+D"
                         }
                     }
                 },
                 disabled: true
             },
            {
               id: "stop",
               label: "Stop",
               command: {
                   id: "stop",
                   shortcuts: {
                       mac: {
                           key: "command+shift+p",
                           label: "\u2318\u21E7P"
                       },
                       other: {
                           key: "ctrl+shift+p",
                           label: "Ctrl+Shift+P"
                       }
                   }
               },
               disabled: true
           }

        ]

    };

    return RunMenu;
}));