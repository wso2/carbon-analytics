/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['file_menu', 'edit_menu', 'run_menu', 'tools_menu', 'deploy_menu', 'export_menu'],
    function (FileMenu, EditMenu, RunMenu, ToolsMenu, DeployMenu, ExportMenu) {
    var menuBar = {};
    menuBar[FileMenu.id] = FileMenu;
    menuBar[EditMenu.id] = EditMenu;
    menuBar[RunMenu.id] = RunMenu;
    menuBar[ToolsMenu.id] = ToolsMenu;
    menuBar[DeployMenu.id] = DeployMenu;
    menuBar[ExportMenu.id] = ExportMenu;
    return menuBar;
});
