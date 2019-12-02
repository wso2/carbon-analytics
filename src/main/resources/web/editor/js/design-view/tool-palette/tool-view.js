/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['require', 'jquery', 'backbone', 'lodash'], function (require, $, Backbone, _) {

    var toolView = Backbone.View.extend({

        toolTemplate: _.template("<div id=\"<%=id%>\" class=\"<%=className%> tool-container\"  " +
            "data-placement=\"bottom\" data-toggle=\"tooltip\" title='<%=title%>'> <img src=\"<%=icon%>\" " +
            "class=\"tool-image\"  /><p class=\"tool-title\"><%=title%></p></div>"),

        initialize: function (options) {
            _.extend(this, _.pick(options, ["toolPalette"]));
        },

        render: function (parent) {
            var element = this.toolTemplate(this.model.attributes);
            this.$el.replaceWith(element);
            this.setElement(element);
            this.$el.tooltip();
            $(this.$el).draggable({
                helper: 'clone',
                cursor: 'pointer',
                zIndex: 10001,
                tolerance: 'fit',
                revert: true
            });
            parent.append(this.$el);

            return this;
        }
    });

    return toolView;
});
