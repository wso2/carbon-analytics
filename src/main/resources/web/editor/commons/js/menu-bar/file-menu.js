/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define([], function () {
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
                            key: "command+option+n",
                            label: "\u2318\u2325N"
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
                id: "openSample",
                label: "Import Sample",
                command: {
                    id: "open-sample-file-open-dialog",
                    shortcuts: {
                        mac: {
                            key: "command+shift+o",
                            label: "\u2318\u21E7O"
                        },
                        other: {
                            key: "ctrl+shift+o",
                            label: "Ctrl+Shift+O"
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
            },
            {
                id: "close",
                label: "Close File",
                command: {
                    id: "close",
                    shortcuts: {
                        mac: {
                            key: "command+shift+c",
                            label: "\u2318\u21E7C"
                        },
                        other: {
                            key: "ctrl+shift+c",
                            label: "Ctrl+Shift+C"
                        }
                    }
                },
                disabled: false
            },
            {
                id: "closeAll",
                label: "Close All Files",
                command: {
                    id: "close-all",
                    shortcuts: {
                        mac: {
                            key: "command+alt+x",
                            label: "\u2318\u2303X"
                        },
                        other: {
                            key: "ctrl+alt+x",
                            label: "Ctrl+Alt+X"
                        }
                    }
                },
                disabled: false
            },
            {
                id: "delete",
                label: "Delete File",
                command: {
                    id: "delete-file-delete-dialog",
                    shortcuts: {
                        mac: {
                            key: "command+d",
                            label: "\u2318D"
                        },
                        other: {
                            key: "ctrl+d",
                            label: "Ctrl+D"
                        }
                    }
                },
                disabled: false
            },
            {
                id: "settings",
                label: "Settings",
                command: {
                    id: "open-settings-dialog",
                    shortcuts: {
                        mac: {
                            key: "command+option+e",
                            label: "\u2318\u2325E"
                        },
                        other: {
                            key: "ctrl+alt+e",
                            label: "Ctrl+Alt+E"
                        }
                    }
                },
                disabled: false
            }

        ]

    };

    return FileMenu;
});
