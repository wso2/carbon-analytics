/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define([],function (){
    var ToolsMenu = {
        id: "tools",
        label: "Tools",
        items: [
            {
                id: "toggleFileExplorer",
                label: "File Explorer",
                command: {
                    id: "toggle-file-explorer",
                    shortcuts: {
                        mac: {
                            key: "command+shift+e",
                            label: "\u2318\u21E7E"
                        },
                        other: {
                            key: "ctrl+shift+e",
                            label: "Ctrl+Shift+E"
                        }
                    }
                },
                disabled: false
            },
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
            {
                id: "toggleConsole",
                label: "Console",
                command: {
                    id: "toggle-output-console",
                    shortcuts: {
                        mac: {
                            key: "command+shift+k",
                            label: "\u2318\u21E7K"
                        },
                        other: {
                            key: "ctrl+shift+k",
                            label: "Ctrl+Shift+K"
                        }
                    }
                },
                disabled: false
            },
            {
                id: "sampleEvent",
                label: "Sample Event Generator",
                command: {
                    id: "sample-event",
                    labels: {
                        mac: {
                            key: "command+shift+g",
                            label: "\u2318\u21E7G"
                        },
                        other: {
                            key: "ctrl+shift+g",
                            label: "Ctrl+Shift+G"
                        }
                    }
                },
                disabled: false
            },
            {
                id: "queryStore",
                label: "On-Demand Query",
                command: {
                    id: "query-store",
                    labels:{
                        mac: {
                            key: "command+shift+q",
                            label: "\u2318\u21E7Q"
                        },
                        other: {
                            key: "ctrl+shift+q",
                            label: "Ctrl+Shift+Q"
                        }
                    }
                },
                disabled: false
            },
            {
                id: 'tour-guide',
                label: 'Tour guide',
                command: {
                    id: 'tour-guide',
                    shortcuts: {
                        mac: {
                            key: "command+shift+h",
                            label: "\u2318\u21E7H"
                        },
                        other: {
                            key: "ctrl+shift+h",
                            label: "Ctrl+Shift+H"
                        }
                    }
                },
                disabled: false
            }
        ]

    };

    return ToolsMenu;
});
