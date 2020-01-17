/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['log', 'lodash', 'jquery', 'event_channel', './file'],
    function(log, _, $, EventChannel, File) {

        /**
         * @class ServiceClient
         * @param {Object} args
         * @constructor
         */
        var ServiceClient = function (args) {
            this.application = _.get(args, 'application');
        };

        ServiceClient.prototype = Object.create(EventChannel.prototype);
        ServiceClient.prototype.constructor = EventChannel;

        ServiceClient.prototype.getErrorFromResponse = function(xhr, textStatus, errorThrown) {
            var msg = _.isString(errorThrown) ? errorThrown : xhr.statusText,
                responseObj;
            try {
                responseObj = JSON.parse(xhr.responseText);
            } catch (e) {
                // ignore
            }
            if(!_.isNil(responseObj)){
                if(_.has(responseObj, 'Error')){
                    msg = _.get(responseObj, 'Error');
                }
            }
            return {"error": true, "message": msg};
        };

        /**
         * parser source
         * @param ServiceClient
         */
        ServiceClient.prototype.parse = function (source) {
            var content = { "content": source };
            var data = {};
            $.ajax({
                type: "POST",
                context: this,
                url: _.get(this.application, 'config.services.parser.endpoint'),
                data: JSON.stringify(content),
                contentType: "application/json; charset=utf-8",
                async: false,
                dataType: "json",
                success: function (response) {
                    data = response;
                },
                error: function(xhr, textStatus, errorThrown){
                    data = getErrorFromResponse(xhr, textStatus, errorThrown);
                    log.error(data.message);
                }
            });
            return data;
        };

        /**
         * validate source
         * @param String source
         */
        ServiceClient.prototype.validate = function (source) {
            var content = { "content": source };
            var data = {};
            $.ajax({
                type: "POST",
                context: this,
                url: _.get(this.application, 'config.services.validator.endpoint'),
                data: JSON.stringify(content),
                contentType: "application/json; charset=utf-8",
                async: false,
                dataType: "json",
                success: function (response) {
                    data = response;
                },
                error: function(xhr, textStatus, errorThrown){
                    data = getErrorFromResponse(xhr, textStatus, errorThrown);
                    log.error(data.message);
                }
            });
            return data;
        };

        /**
         * read content of a file
         * @param {String} filePath file path
         */
        ServiceClient.prototype.readFileContent = function (filePath) {
            var data = {};
            $.ajax({
                type: "POST",
                context: this,
                url: _.get(this.application, 'config.services.workspace.endpoint') + "/read",
                data: filePath,
                contentType: "text/plain; charset=utf-8",
                async: false,
                success: function (response) {
                    data = response;
                },
                error: function(xhr, textStatus, errorThrown){
                    data = getErrorFromResponse(xhr, textStatus, errorThrown);
                    log.error(data.message);
                }
            });
            return data;
        };

        ServiceClient.prototype.readFile = function (filePath) {
            var fileData = this.readFileContent(filePath),
                pathArray = _.split(filePath, this.application.getPathSeperator()),
                fileName = _.last(pathArray),
                folderPath = _.join(_.take(pathArray, pathArray.length -1), this.application.getPathSeperator());

            return new File({
                name: fileName,
                path: folderPath,
                content: fileData.content,
                isPersisted: true,
                isDirty: false
            });
        };

        ServiceClient.prototype.exists = function (path) {
            var data = {};

            $.ajax({
                type: "GET",
                context: this,
                url: _.get(this.application, 'config.services.workspace.endpoint') + "/exists?" + "path=" +
                this.application.utils.base64EncodeUnicode(path),
                contentType: "text/plain; charset=utf-8",
                async: false,
                success: function (response) {
                    data = response;
                },
                error: function(xhr, textStatus, errorThrown){
                    data = getErrorFromResponse(xhr, textStatus, errorThrown);
                    log.error(data.message);
                }
            });
            return data;
        };

        ServiceClient.prototype.readPathSeparator = function () {
            var data = {};
            $.ajax({
                type: "GET",
                context: this,
                url: _.get(this.application, 'config.services.workspace.endpoint') + "/config",
                datatype : "application/json",
                async: false,
                success: function (response) {
                    data = response;
                },
                error: function(xhr, textStatus, errorThrown){
                    data = getErrorFromResponse(xhr, textStatus, errorThrown);
                    log.error(data.message);
                }
            });
            return data.fileSeparator;
        };

        ServiceClient.prototype.create = function (path, type) {
            var data = {};
            $.ajax({
                type: "GET",
                context: this,
                url: _.get(this.application, 'config.services.workspace.endpoint') + "/create?" + "path=" +
                this.application.utils.base64EncodeUnicode(path)
                + "&type=" + this.application.utils.base64EncodeUnicode(type),
                contentType: "text/plain; charset=utf-8",
                async: false,
                success: function (response) {
                    data = response;
                },
                error: function(xhr, textStatus, errorThrown){
                    data = getErrorFromResponse(xhr, textStatus, errorThrown);
                    log.error(data.message);
                }
            });
            return data;
        };

        ServiceClient.prototype.delete = function (path, type) {
            var data = {};
            $.ajax({
                type: "GET",
                context: this,
                url: _.get(this.application, 'config.services.workspace.endpoint') + "/delete?" + "path=" +
                this.application.utils.base64EncodeUnicode(path)
                + "&type=" + this.application.utils.base64EncodeUnicode(type),
                contentType: "text/plain; charset=utf-8",
                async: false,
                success: function (response) {
                    data = response;
                },
                error: function(xhr, textStatus, errorThrown){
                    data = getErrorFromResponse(xhr, textStatus, errorThrown);
                    log.error(data.message);
                }
            });
            return data;
        };

        ServiceClient.prototype.writeFile = function (file,content) {
            var self = this;
            var data = {};
            $.ajax({
                type: "POST",
                context: this,
                url: _.get(this.application, 'config.services.workspace.endpoint') + "/write",
                data: "location=" + this.application.utils.base64EncodeUnicode(file.getPath()) + "&configName=" +
                this.application.utils.base64EncodeUnicode(file.getName()) + "&config=" +
                this.application.utils.base64EncodeUnicode(content),
                contentType: "text/plain; charset=utf-8",
                async: false,
                success: function (response) {
                    data = response;
                    file.setDirty(false)
                        .setLastPersisted(_.now())
                        .save(true);
                    log.debug("File " + file.getName() + ' saved successfully at '+ file.getPath());
                },
                error: function(xhr, textStatus, errorThrown){
                    data = getErrorFromResponse(xhr, textStatus, errorThrown);
                    log.error(data.message);
                }
            });
            return data;
        };

        return ServiceClient;
    });