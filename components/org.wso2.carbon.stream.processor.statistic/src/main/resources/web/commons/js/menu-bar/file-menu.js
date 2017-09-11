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
    var FileMenu = {
        id: "file",
        label: "File",
        items: [
            {
                id: "new",
                label: "New",
                command: {
                    id: "create-new-tab",
                    shortcuts: {
                        mac: {
                            key: "command+ctrl+n",
                            label: "\u2318\u2303N"
                        },
                        other: {
                            key: "ctrl+alt+n",
                            label: "Ctrl+Alt+N"
                        }
                    }
                },
                disabled: false
            },
            {
                id: "open",
                label: "Open File",
                command: {
                    id: "open-file-open-dialog",
                    shortcuts: {
                        mac: {
                            key: "command+o",
                            label: "\u2318O"
                        },
                        other: {
                            key: "ctrl+o",
                            label: "Ctrl+O"
                        }
                    }
                },
                disabled: false
            },
            {
                id: "save",
                label: "Save",
                command: {
                    id: "save",
                    shortcuts: {
                        mac: {
                            key: "command+s",
                            label: "\u2318S"
                        },
                        other: {
                            key: "ctrl+s",
                            label: "Ctrl+S"
                        }
                    }
                },
                disabled: false
            },
            {
                id: "saveAs",
                label: "Save As",
                command: {
                    id: "open-file-save-dialog",
                    shortcuts: {
                        mac: {
                            key: "command+shift+s",
                            label: "\u2318\u21E7S"
                        },
                        other: {
                            key: "ctrl+shift+s",
                            label: "Ctrl+Shift+S"
                        }
                    }
                },
                disabled: false
            },
            {
                id: "import",
                label: "Import File",
                command: {
                    id: "import-file-import-dialog",
                    shortcuts: {
                        mac: {
                            key: "command+i",
                            label: "\u2318I"
                        },
                        other: {
                            key: "ctrl+i",
                            label: "Ctrl+I"
                        }
                    }
                },
                disabled: false
            },
            {
                id: "export",
                label: "Export File",
                command: {
                    id: "export",
                    shortcuts: {
                        mac: {
                            key: "command+e",
                            label: "\u2318E"
                        },
                        other: {
                            key: "ctrl+e",
                            label: "Ctrl+E"
                        }
                    }
                },
                disabled: false
            }

            ]

    };

    return FileMenu;
}));
