define(["jquery"], function (jQuery) {

    "use strict";   // JS strict mode

    var self = {};

    self.HTTP_GET = "GET";
    self.HTTP_POST = "POST";
    self.HTTP_PUT = "PUT";
    self.HTTP_DELETE = "DELETE";
    self.simulatorUrl = "http://localhost:9090/simulation";
    self.editorUrl = "http://localhost:9090/editor";


    self.retrieveSiddhiAppNames = function (successCallback, errorCallback) {
        jQuery.ajax({
            async: true,
            url: self.editorUrl + "/artifact/listSiddhiApps",
            type: self.HTTP_GET,
            success: function (data) {
                if (typeof successCallback === 'function')
                    successCallback(data)
            },
            error: function (msg) {
                if (typeof errorCallback === 'function')
                    errorCallback(msg)
            }
        });
    };

    self.retrieveStreamNames = function (siddhiAppName, successCallback, errorCallback) {
        if (siddhiAppName === null || siddhiAppName.length === 0) {
            console.error("Siddhi app name is required to retrieve stream names.")
        }
        if (siddhiAppName !== null && siddhiAppName.length > 0) {
            jQuery.ajax({
                async: true,
                url: self.editorUrl + "/artifact/listStreams/" + siddhiAppName,
                type: self.HTTP_GET,
                success: function (data) {
                    if (typeof successCallback === 'function')
                        successCallback(data)
                },
                error: function (msg) {
                    if (typeof errorCallback === 'function')
                        errorCallback(msg)
                }
            });
        }
    };

    self.retrieveStreamAttributes = function (siddhiAppName, streamName, successCallback, errorCallback) {
        if (siddhiAppName === null || siddhiAppName.length === 0) {
            console.error("Siddhi app name is required to retrieve stream attributes.")
        }
        if (streamName === null || streamName.length === 0) {
            console.error("Stream name is required to retrieve stream attributes.")
        }
        if (siddhiAppName !== null && siddhiAppName.length > 0
            && streamName !== null && streamName.length > 0) {
            jQuery.ajax({
                async: true,
                url: self.editorUrl + "/artifact/listAttributes/" + siddhiAppName + "/" + streamName,
                type: self.HTTP_GET,
                success: function (data) {
                    if (typeof successCallback === 'function')
                        successCallback(data)
                },
                error: function (msg) {
                    if (typeof errorCallback === 'function')
                        errorCallback(msg)
                }
            });
        }
    };

    self.singleEvent = function (singleEventConfig, successCallback, errorCallback) {
        if (singleEventConfig === null || singleEventConfig.length === 0) {
            console.error("Single event configuration is required.");
        }
        if (singleEventConfig !== null && singleEventConfig.length > 0) {
            jQuery.ajax({
                async: true,
                url: self.simulatorUrl + "/single",
                type: self.HTTP_POST,
                data: singleEventConfig,
                success: function (data) {
                    if (typeof successCallback === 'function')
                        successCallback(data)
                },
                error: function (msg) {
                    if (typeof errorCallback === 'function')
                        errorCallback(msg)
                }
            });
        }
    };

    self.retrieveCSVFileNames = function (successCallback, errorCallback) {
        jQuery.ajax({
            async: true,
            url: self.simulatorUrl + "/files",
            type: self.HTTP_GET,
            success: function (data) {
                if (typeof successCallback === 'function')
                    successCallback(data)
            },
            error: function (msg) {
                if (typeof  errorCallback === 'function')
                    errorCallback(msg)
            }
        })
    };

    self.uploadCSVFile = function (formData, successCallback, errorCallback) {
        jQuery.ajax({
            async: true,
            url: self.simulatorUrl + "/files",
            type: self.HTTP_POST,
            data: formData,
            contentType: false,
            processData: false,
            success: function (data) {
                if (typeof successCallback === 'function')
                    successCallback(data)
            },
            error: function (msg) {
                if (typeof errorCallback === 'function')
                    errorCallback(msg)

            }
        })
    };

    self.testDatabaseConnectivity = function (connectionDetails, successCallback, errorCallback) {
        if (connectionDetails !== null && connectionDetails.length > 0) {
            jQuery.ajax({
                async: true,
                url: self.simulatorUrl + "/connectToDatabase",
                type: self.HTTP_POST,
                dataType: "json",
                contentType: "application/json; charset=utf-8",
                data: connectionDetails,
                success: function (data) {
                    if (typeof successCallback === 'function')
                        successCallback(data)
                },
                error: function (msg) {
                    if (typeof errorCallback === 'function')
                        errorCallback(msg)
                }
            })
        }
    };

    self.retrieveTableNames = function (connectionDetails, successCallback, errorCallback) {
        if (connectionDetails !== null && connectionDetails.length > 0) {
            jQuery.ajax({
                async: true,
                url: self.simulatorUrl + "/connectToDatabase/retrieveTableNames",
                type: self.HTTP_POST,
                dataType: "json",
                contentType: "application/json; charset=utf-8",
                data: connectionDetails,
                success: function (data) {
                    if (typeof successCallback === 'function')
                        successCallback(data)
                },
                error: function (msg) {
                    if (typeof errorCallback === 'function')
                        errorCallback(msg)
                }
            })
        }
    };

    self.retrieveColumnNames = function (connectionDetails, tableName, successCallback, errorCallback) {
        if (connectionDetails !== null && connectionDetails.length > 0
            && tableName !== null && tableName.length > 0) {
            jQuery.ajax({
                async: true,
                url: self.simulatorUrl + "/connectToDatabase/" + tableName + "/retrieveColumnNames",
                type: self.HTTP_POST,
                dataType: "json",
                contentType: "application/json; charset=utf-8",
                data: connectionDetails,
                success: function (data) {
                    if (typeof successCallback === 'function')
                        successCallback(data)
                },
                error: function (msg) {
                    if (typeof errorCallback === 'function')
                        errorCallback(msg)
                }
            })
        }
    };

    self.uploadSimulation = function (simulationConfig, successCallback, errorCallback) {
        jQuery.ajax({
            async: true,
            url: self.simulatorUrl + "/feed",
            type: self.HTTP_POST,
            data: simulationConfig,
            dataType: "json",
            contentType: "text/plain",
            success: function (data) {
                if (typeof successCallback === 'function')
                    successCallback(data)
            },
            error: function (msg) {
                if (typeof errorCallback === 'function')
                    errorCallback(msg)
            }
        });
    };

    self.updateSimulation = function (simulationName, simulationConfig, successCallback, errorCallback) {
        jQuery.ajax({
            async: true,
            url: self.simulatorUrl + "/feed/"+simulationName+"",
            type: self.HTTP_PUT,
            data: simulationConfig,
            dataType: "json",
            contentType: "text/plain",
            success: function (data) {
                if (typeof successCallback === 'function')
                    successCallback(data)
            },
            error: function (msg) {
                if (typeof errorCallback === 'function')
                    errorCallback(msg)
            }
        });
    };

    self.deleteSimulation = function (simulationName, successCallback, errorCallback) {
        jQuery.ajax({
            async: true,
            url: self.simulatorUrl + "/feed/"+simulationName+"",
            type: self.HTTP_DELETE,
            dataType: "json",
            success: function (data) {
                if (typeof successCallback === 'function')
                    successCallback(data)
            },
            error: function (msg) {
                if (typeof errorCallback === 'function')
                    errorCallback(msg)
            }
        });
    };

    self.simulationAction = function (simulationName, action, successCallback, errorCallback) {
        if (simulationName !== null) {
            jQuery.ajax({
                async: true,
                url: self.simulatorUrl + "/feed/" + simulationName + "/?action=" + action,
                type: self.HTTP_POST,
                dataType: "json",
                contentType: "application/json; charset=utf-8",
                success: function (data) {
                    if (typeof successCallback === 'function')
                        successCallback(data)
                },
                error: function (msg) {
                    if (typeof errorCallback === 'function')
                        errorCallback(msg)
                }
            })
        }
    };

    self.getFeedSimulations = function (successCallback, errorCallback) {
        jQuery.ajax({
            async: true,
            url: self.simulatorUrl + "/feed",
            type: self.HTTP_GET,
            dataType: "json",
            success: function (data) {
                if (typeof successCallback === 'function')
                    successCallback(data)
            },
            error: function (msg) {
                if (typeof  errorCallback === 'function')
                    errorCallback(msg)
            }
        })
    };

    self.runSimulation = function (simulationName, successCallback, errorCallback) {
        if (simulationName !== null) {
            jQuery.ajax({
                async: true,
                url: self.simulatorUrl + "/feed/" + simulationName + "/?action=run",
                type: self.HTTP_POST,
                dataType: "json",
                contentType: "application/json; charset=utf-8",
                success: function (data) {
                    if (typeof successCallback === 'function')
                        successCallback(data)
                },
                error: function (msg) {
                    if (typeof errorCallback === 'function')
                        errorCallback(msg)
                }
            })
        }
    };

    self.getFeedSimulationStatus = function (simulationName, successCallback, errorCallback) {
        jQuery.ajax({
            async: true,
            url: self.simulatorUrl + "/feed/" + simulationName + "/status",
            type: self.HTTP_GET,
            dataType: "json",
            success: function (data) {
                if (typeof successCallback === 'function')
                    successCallback(data)
            },
            error: function (msg) {
                if (typeof  errorCallback === 'function')
                    errorCallback(msg)
            }
        })
    };

    return self;
});