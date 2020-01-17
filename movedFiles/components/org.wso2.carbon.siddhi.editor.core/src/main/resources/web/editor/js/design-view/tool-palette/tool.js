/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['require', 'backbone'], function (require, Backbone) {

    var tool = Backbone.Model.extend({
        initialize: function (attrs, options) {
        },

        modelName: "Tool",

        defaults: {
            id: "",
            className: "",
            title: "",
            icon: ""
        }
    });

    return tool;
});

