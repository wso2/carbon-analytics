/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['require', 'jquery', 'lodash', 'log'], function (require, $, _, log) {
    var ServiceView = function (config) {
        this._config = config;
    };
    ServiceView.prototype.constructor = ServiceView;
    ServiceView.prototype.render = function () {
        var config = this._config;
        var errMsg;
        if (!_.has(config, 'parentContainer')) {
            errMsg = 'unable to find configuration for parentContainer';
            log.error(errMsg);
            throw errMsg;
        }
        // get parent container which is innerSamples div
        var parentContainer = _.get(config, 'parentContainer');
        this._sampleName = config.sampleName;
        this._content = config.sampleDes;
        this._id = config.sampleName;

        //create the parent for drawn svg
        var sampleLi = $("<li class='col-md-6'></li>");
        var linkSample = $("<a href='#' style='height: 100px;display: block;'></a>");
        linkSample.text(this._sampleName);
        linkSample.attr('id', this._sampleName);
        linkSample.bind('click', config.clickEventCallback);

        var description = "<span class='description'>" + this._content + "</span>";
        linkSample.append(description);
        sampleLi.append(linkSample);
        parentContainer.append(sampleLi);
    };
    return ServiceView;
});
