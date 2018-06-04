/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
define(['require', 'log', 'jquery', 'lodash', 'console','workspace','toolEditor'],
    function (require, log, jquery, _, Console, Workspace,ToolEditor) {
        var  ServiceConsole;

        ServiceConsole = Console.extend({
            initialize: function (options) {
                Console.prototype.initialize.call(this, options);
                this.app = options.application;
            },
            getFile: function(){
                return this._file;
            },

            getTitle: function(){
                return this._title;
            },

            getContentContainer: function(){
                return this.$el;
            },

            updateHeader: function(){
            },

            showInitialStartingMessage: function(message){
                this.$el.append('<span class="INFO">' + message + '<span>');
                this.$el.append("<br />");
                this.$el.scrollTop(100000);
            },

            println: function(message){
                this.$el.append('<span class="' + message.type + '">' + message.message + '<span>');
                this.$el.append("<br />");
                this.$el.scrollTop(100000);
                $(".nano").nanoScroller();
                var parentDiv = this.$el.parent()[0];
                parentDiv.scrollTop = parentDiv.scrollHeight;
                var childLength = this.$el.children().size();
                //console.log("Count="+);
                if(childLength > 2500){
                    $(this.$el).children().first().remove();
                }
            },
            
            addRunningPlan: function(executionPlan){
                this.addRunningPlanToList(executionPlan);
            },

            clear: function(){
                this.$el.empty();
            }

        });

        return ServiceConsole;
    });