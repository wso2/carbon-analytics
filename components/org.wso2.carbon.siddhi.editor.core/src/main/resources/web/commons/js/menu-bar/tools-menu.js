/**
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
    var ToolsMenu = {
        id: "tools",
        label: "Tools",
        items: [
            {
                id: "toggleEventSimulator",
                label: "Event Simulator",
                command: {
                    id: "toggle-event-simulator",
                    shortcuts: {
                        mac: {
                            key: "command+shift+i",
                            label: "\u2318\u21E7I"
                        },
                        other: {
                            key: "ctrl+shift+i",
                            label: "Ctrl+Shift+I"
                        }
                    }
                },
                disabled: false
            },
            // {
            //     id: "deploy",
            //     label: "Deploy",
            //     command: {
            //         id: "deploy",
            //         shortcuts: {
            //             mac: {
            //                 key: "command+shift+z",
            //                 label: "\u2318\u21E7Z"
            //             },
            //             other: {
            //                 key: "ctrl+shift+z",
            //                 label: "Ctrl+Shift+Z"
            //             }
            //         }
            //     },
            //     disabled: true
            // },
            {
                id: "sampleEvent",
                label: "Sample Event",
                command: {
                    id: "sample-event",
                    labels: {
                        mac: {
                            //key: "command+f",
                            label: "\u2318F"
                        },
                        other: {
                            //key: "ctrl+f",
                            label: "Ctrl+F"
                        }
                    }
                },
                disabled: false
            },
            {
                id: "queryStore",
                label: "Siddhi Store Query",
                command: {
                    id: "query-store",
                    labels:{
                        mac: {
                            //key: "command+option+f",
                            label: "\u2318\u2325f"
                        },
                        other: {
                            //key: "ctrl+h",
                            label: "Ctrl+H"
                        }
                    }
                },
                disabled: false
            }
        ]

    };

    return ToolsMenu;
}));
