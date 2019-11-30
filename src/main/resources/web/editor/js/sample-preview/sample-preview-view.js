/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['require', 'jquery', 'lodash', 'log'], function (require, $, _, log) {

    var ServicePreviewView = function (config) {
        this._config = config;
    };

    ServicePreviewView.prototype.constructor = ServicePreviewView;

    ServicePreviewView.prototype.render = function () {
        var config = this._config;
        var errMsg;
        if (!_.has(config, 'parentContainer')) {
            errMsg = 'unable to find configuration for parentContainer';
            log.error(errMsg);
            throw errMsg;
        }
        // get parent container which is innerSamples div
        var parentContainer = $(_.get(config, 'parentContainer'));

        this._sampleName = config.sampleName;
        this._content = config.content;
        var regexToExtractAppDescription = new RegExp("@[Aa][Pp][Pp]:[Dd][Ee][Ss][Cc][Rr][Ii][Pp][Tt][Ii][Oo]" +
            "[Nn]\\(['|\"](.*?)['|\"]\\)", "ig");

        //create the parent for drawn svg
        var previewLi = $("<li class='col-md-6 col-lg-4'></li>");

        var linkSample = $("<a href='' style='height: 100px;'></a>");
        linkSample.text(this._sampleName);
        linkSample.bind('click', config.clickEventCallback);
        var description = "<span class='description'>" + (regexToExtractAppDescription.exec(this._content))[1] +
            "</span>";

        linkSample.append(description);
        previewLi.append(linkSample);
        parentContainer.append(previewLi);
    };

    return ServicePreviewView;

});
