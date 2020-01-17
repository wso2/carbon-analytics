/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['require', 'log4javascript'], function (require, log4javascript) {


    var logger = log4javascript.getLogger("client-logger");
    var consoleAppender = new log4javascript.BrowserConsoleAppender();
    logger.addAppender(consoleAppender);

    logger.initAjaxAppender = function (workspaceServiceEP) {
        var ajaxAppender = new log4javascript.AjaxAppender(workspaceServiceEP + "/log");
        logger.addAppender(ajaxAppender);
    };

    return logger;
});