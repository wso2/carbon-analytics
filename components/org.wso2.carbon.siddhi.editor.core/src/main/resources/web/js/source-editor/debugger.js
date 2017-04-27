/*
 * Copyright (c)  2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

/**
 * This Javascript module exposes Siddhi debug API as Javascript methods.
 */

function DebugClient() {
    var HTTP_GET = "GET";
    var HTTP_POST = "POST";
    var DATA_TYPE_JzSON = "json";
    var CONTENT_TYPE_JSON = "application/json";
    var RESPONSE_ELEMENT = "responseJSON";
    this.serverUrl = "";

    this.startDebug = function (executionPlan, callback, error) {
        jQuery.ajax({
            async: true,
            url: this.serverUrl + "/debug",
            type: HTTP_POST,
            data: executionPlan,
            success: function (data) {
                if (typeof callback === 'function')
                    callback(data)
            },
            error: function (msg) {
                if (typeof error === 'function')
                    error(msg)
            }
        });
    };

    this.stopDebug = function (runtimeId, callback, error) {
        jQuery.ajax({
            async: true,
            url: this.serverUrl + "/" + runtimeId + "/stop",
            type: HTTP_GET,
            success: function (data) {
                if (typeof callback === 'function')
                    callback(data)
            },
            error: function (msg) {
                if (typeof error === 'function')
                    error(msg)
            }
        });
    };

    this.acquireBreakPoint = function (runtimeId, queryName, queryTerminal, callback, error) {
        jQuery.ajax({
            async: true,
            url: this.serverUrl + "/" + runtimeId + "/acquire",
            type: HTTP_GET,
            data: {queryName: queryName, queryTerminal: queryTerminal},
            success: function (data) {
                if (typeof callback === 'function')
                    callback(data)
            },
            error: function (msg) {
                if (typeof error === 'function')
                    error(msg)
            }
        });
    };

    this.releaseBreakPoint = function (runtimeId, queryName, queryTerminal, callback, error) {
        jQuery.ajax({
            async: true,
            url: this.serverUrl + "/" + runtimeId + "/release",
            type: HTTP_GET,
            data: {queryName: queryName, queryTerminal: queryTerminal},
            success: function (data) {
                if (typeof callback === 'function')
                    callback(data)
            },
            error: function (msg) {
                if (typeof error === 'function')
                    error(msg)
            }
        });
    };

    this.next = function (runtimeId, callback, error) {
        jQuery.ajax({
            async: true,
            url: this.serverUrl + "/" + runtimeId + "/next",
            type: HTTP_GET,
            success: function (data) {
                if (typeof callback === 'function')
                    callback(data)
            },
            error: function (msg) {
                if (typeof error === 'function')
                    error(msg)
            }
        });
    };

    this.play = function (runtimeId, callback, error) {
        jQuery.ajax({
            async: true,
            url: this.serverUrl + "/" + runtimeId + "/play",
            type: HTTP_GET,
            success: function (data) {
                if (typeof callback === 'function')
                    callback(data)
            },
            error: function (msg) {
                if (typeof error === 'function')
                    error(msg)
            }
        });
    };

    this.state = function (runtimeId, callback, error) {
        jQuery.ajax({
            async: true,
            url: this.serverUrl + "/" + runtimeId + "/state",
            type: HTTP_GET,
            success: function (data) {
                if (typeof callback === 'function')
                    callback(data)
            },
            error: function (msg) {
                if (typeof error === 'function')
                    error(msg)
            }
        });
    };

    this.sendEvent = function (runtimeId, streamName, eventData, callback, error) {
        jQuery.ajax({
            async: true,
            url: this.serverUrl + "/" + runtimeId + "/" + streamName + "/send",
            type: HTTP_POST,
            data: JSON.stringify(eventData),
            success: function (data) {
                if (typeof callback === 'function')
                    callback(data)
            },
            error: function (msg) {
                if (typeof error === 'function')
                    error(msg)
            }
        });
    };
}

/**
 * Construct an DebugClient object by given the serverUrl.
 * @param svrUrl the server url
 * @returns {DebugClient} DebugClient object
 */
DebugClient.prototype.init = function (svrUrl) {
    if (svrUrl) {
        this.serverUrl = svrUrl;
    } else {
        this.serverUrl = "http://localhost:9090/editor/";
    }
    return this;
};

function Debugger(executionPlan) {
    var self = this;
    self.__pollingInterval = 1000;
    self.__pollingLock = false;
    self.__pollingJob = null;
    self.__callback = null;
    self.executionPlan = executionPlan;
    self.debugger = new DebugClient().init();
    self.runtimeId = null;
    self.streams = null;
    self.queries = null;

    this.start = function () {
        this.debugger.startDebug(
            this.executionPlan,
            function (data) {
                self.runtimeId = data['runtimeId'];
                self.streams = data['streams'];
                self.queries = data['queries'];
                if (self.streams == null || self.streams.length == 0) {
                    console.warn("Streams cannot be empty.");
                }
                if (self.queries == null || self.queries.length == 0) {
                    console.warn("Queries cannot be empty.");
                }
                if (self.streams != null && self.streams.length > 0 &&
                    self.queries != null && self.queries.length > 0) {
                    self.__pollingJob = setInterval(function () {
                        if (!self.__pollingLock) {
                            self.state();
                        }
                    }, self.__pollingInterval);
                }
                console.info(JSON.stringify(data));
            },
            function (error) {
                console.error(JSON.stringify(error));
            }
        );
    };

    this.stop = function () {
        if (this.__pollingJob != null) {
            clearInterval(this.__pollingJob);
        }
        if (this.runtimeId != null) {
            this.debugger.stopDebug(
                this.runtimeId,
                function (data) {
                    console.info(JSON.stringify(data));
                },
                function (error) {
                    console.error(JSON.stringify(error));
                }
            );
        } else {
            console.log("Debugger has not been started yet.")
        }
    };

    this.acquire = function (queryName, pointer) {
        if (this.runtimeId != null && queryName != null && pointer != null) {
            if (this.queries.indexOf(queryName) !== -1) {
                this.debugger.acquireBreakPoint(
                    this.runtimeId,
                    queryName,
                    pointer,
                    function (data) {
                        console.info(JSON.stringify(data));
                    },
                    function (error) {
                        console.error(JSON.stringify(error));
                    }
                );
            } else {
                console.warn("No such query : " + queryName);
            }
        } else {
            console.log("Debugger has not been started yet.")
        }
    };

    this.release = function (queryName, pointer) {
        if (this.runtimeId != null && queryName != null && pointer != null) {
            if (this.queries.indexOf(queryName) !== -1) {
                this.debugger.releaseBreakPoint(
                    this.runtimeId,
                    queryName,
                    pointer,
                    function (data) {
                        console.info(JSON.stringify(data));
                    },
                    function (error) {
                        console.error(JSON.stringify(error));
                    }
                );
            } else {
                console.warn("No such query : " + queryName);
            }
        } else {
            console.log("Debugger has not been started yet.")
        }
    };

    this.next = function () {
        if (this.runtimeId != null) {
            this.debugger.next(
                this.runtimeId,
                function (data) {
                    console.info(JSON.stringify(data));
                },
                function (error) {
                    console.error(JSON.stringify(error));
                }
            );
        } else {
            console.log("Debugger has not been started yet.")
        }
    };

    this.play = function () {
        if (this.runtimeId != null) {
            this.debugger.play(
                this.runtimeId,
                function (data) {
                    console.info(JSON.stringify(data));
                },
                function (error) {
                    console.error(JSON.stringify(error));
                }
            );
        } else {
            console.log("Debugger has not been started yet.")
        }
    };

    this.state = function () {
        self.__pollingLock = true;
        if (this.runtimeId != null) {
            this.debugger.state(
                this.runtimeId,
                function (data) {
                    if (data.hasOwnProperty('eventState')) {
                        if (typeof self.__callback === 'function') {
                            self.__callback(data);
                        }
                    }
                    self.__pollingLock = false;
                },
                function (error) {
                    console.error(JSON.stringify(error));
                    self.__pollingLock = false;
                }
            );
        } else {
            console.log("Debugger has not been started yet.")
        }
    };

    this.sendEvent = function (queryName, streamId, event) {
        if (this.runtimeId != null && queryName != null) {
            this.debugger.sendEvent(
                this.runtimeId,
                streamId,
                event,
                function (data) {
                    console.info(JSON.stringify(data));
                },
                function (error) {
                    console.error(JSON.stringify(error));
                }
            );
        } else {
            console.log("Debugger has not been started yet.")
        }
    };

    this.setOnUpdateCallback = function (onUpdateCallback) {
        this.__callback = onUpdateCallback;
    };
}