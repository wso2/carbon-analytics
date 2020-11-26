/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['jquery', 'log', '../dist/bundle.js', 'lodash', 'workspace',
        /* void libs*/ 'bootstrap', 'theme_wso2', 'jquery_ui', 'jquery_validate', 'jquery_timepicker', './templates'],
    function ($, log, ApiDefGenerator, _) {

        "use strict";   // JS strict mode

        var self = {};

        self.init = function (config) {
            self.feedConfigs = [];

            self.pollingSimulation();

            self.addAvailableFeedSimulations();

            self.$eventFeedConfigTabContent.on('click', 'a i.fw-assign', function () {
                var $panel = $(this).closest('.input-group');
                var simulationName = $panel.attr('data-name');
                Simulator.simulationAction(
                    simulationName,
                    "pause",
                    function (data) {
                        var message = {
                            "type": "INFO",
                            "message": data.message
                        };
                        self.console.println(message);
                        self.activeSimulationList[simulationName].status = "PAUSE";
                        $panel.find('i.fw-start').closest('a').addClass("hidden");
                        $panel.find('i.fw-assign').closest('a').addClass("hidden");
                        $panel.find('i.fw-resume').closest('a').removeClass("hidden");
                        $panel.find('i.fw-stop').closest('a').removeClass("hidden");
                    },
                    function (msg) {
                        var message = {
                            "type": "ERROR",
                            "message": msg
                        };
                        self.console.println(message);
                    }
                );
            });
        };

        function getWarningNotification(warningMessage) {
            return $(
                "<div style='z-index: 9999;' style='line-height: 20%;' class='alert alert-warning' id='error-alert'>" +
                "<span class='notification'>" +
                warningMessage +
                "</span>" +
                "</div>");
        };
        return self;
    });
