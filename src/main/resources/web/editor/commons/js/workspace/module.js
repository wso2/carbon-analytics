/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['./workspace', './browser-storage', './explorer'],
    function (Workspace, BrowserStorage, Explorer) {
        return {
            Explorer: Explorer,
            Manager: Workspace,
            BrowserStorage: BrowserStorage
            //File: File
        }
    });

