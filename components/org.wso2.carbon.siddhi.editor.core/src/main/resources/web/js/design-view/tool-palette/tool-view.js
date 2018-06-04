/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
            //this.$el =
            this.$el.tooltip();
            parent.append(this.$el);
            var className = '.' + this.model.get("className");
            $(className).draggable({
                helper: 'clone',
                cursor: 'pointer',
                zIndex: 10001,
                tolerance: 'fit',
                revert: true
            });

            return this;
        }
    });

    return toolView;
});
