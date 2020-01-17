/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['require', 'alerts'],
    function (require, alerts) {

        /**
         * @class DesignViewUtils Contains utility methods related to design view
         * @constructor
         */
        var DesignViewUtils = function () {
        };

        /**
         * Display's a warning using the AlertsManager.
         *
         * @param message The content to be displayed in the alert
         */
        DesignViewUtils.prototype.warnAlert = function (message) {
            alerts.warn(message);
        };

        /**
         * Display's a error using the AlertsManager.
         *
         * @param message The content to be displayed in the alert
         */
        DesignViewUtils.prototype.errorAlert = function (message) {
            alerts.error(message);
        };

        return DesignViewUtils;
    });
