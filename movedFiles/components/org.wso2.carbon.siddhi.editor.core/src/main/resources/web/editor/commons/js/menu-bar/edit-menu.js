/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define([], function () {
    var EditMenu = {
        id: "edit",
        label: "Edit",
        items: [
            {
                id: "undo",
                label: "Undo",
                command: {
                    id: "undo",
                    shortcuts: {
                        mac: {
                            key: "command+z",
                            label: "\u2318Z"
                        },
                        other: {
                            key: "ctrl+z",
                            label: "Ctrl+Z"
                        }
                    }
                },
                disabled: true
            },
            {
                id: "redo",
                label: "Redo",
                command: {
                    id: "redo",
                    shortcuts: {
                        mac: {
                            key: "command+shift+z",
                            label: "\u2318\u21E7Z"
                        },
                        other: {
                            key: "ctrl+shift+z",
                            label: "Ctrl+Shift+Z"
                        }
                    }
                },
                disabled: true
            },
            {
                id: "find",
                label: "Find",
                command: {
                    id: "find",
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
                disabled: true
            },
            {
                id: "findAndReplace",
                label: "Find and Replace",
                command: {
                    id: "findAndReplace",
                    labels: {
                        mac: {
                            //key: "command+option+f",
                            label: "\u2318\u2325F"
                        },
                        other: {
                            //key: "ctrl+h",
                            label: "Ctrl+H"
                        }
                    }
                },
                disabled: true
            },
            {
                id: "format",
                label: "Reformat Code",
                command: {
                    id: "format",
                    shortcuts: {
                        mac: {
                            key: "command+option+u",
                            label: "\u2318\u2325U"
                        },
                        other: {
                            key: "ctrl+alt+u",
                            label: "Ctrl+Alt+U"
                        }
                    }
                },
                disabled: true
            }

        ]

    };

    return EditMenu;
});