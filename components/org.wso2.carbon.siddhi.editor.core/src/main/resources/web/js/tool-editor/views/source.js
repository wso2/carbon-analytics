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
define(['require', 'log', 'lodash', 'jquery', 'event_channel', 'app/source-editor/editor','ace/ace'
    ],
    function(require , log, _, $, EventChannel, SiddhiEditor,ace) {

        /**
         * @class SourceView
         * @constructor
         * @class SourceView  Wraps the Ace editor for source view
         * @param {Object} args - Rendering args for the view
         * @param {String} args.container - selector for div element to render ace editor
         * @param {String} [args.content] - initial content for the editor
         */
        var SourceView = function (args) {
            this._options = args;
            if(!_.has(args, 'sourceContainer')){
                log.error('container is not specified for rendering source view.')
            }
            this._container = _.get(args, 'sourceContainer');
            this._mainEditor = new SiddhiEditor({
                divID: args.sourceContainer,
                realTimeValidation: true,
                autoCompletion: true
            });
            this._editor = ace.edit(this._container);
            this._mainEditor.setContent(args.source);
        };

        SourceView.prototype = Object.create(EventChannel.prototype);
        SourceView.prototype.constructor = SourceView;

        SourceView.prototype.render = function (options) {
            var self = this;
            $(this._container).show();

            this._editor.on("change", function(event) {
                if(!self._inSilentMode){
                    var changeEvent = {
                        type: "source-modified",
                        title: "Modify source",
                        data: {
                            type: event.action,
                            lines: event.lines
                        }
                    };
                    self.trigger('modified', changeEvent);
                }
            });

        };

        SourceView.prototype.editorResize = function () {
            var self = this;
            self._editor.resize(true);
        };


        /**
         * Set the content of text editor.
         * @param {String} content - content for the editor.
         *
         */
        SourceView.prototype.setContent = function(content){
            this._inSilentMode = true;
            this._editor.session.setValue(content);
            this._inSilentMode = false;
            this.markClean();
        };

        SourceView.prototype.getContent = function(){
            return this._editor.session.getValue();
        };

        SourceView.prototype.getEditor = function(){
            return this._editor;
        };

        SourceView.prototype.getDebugger = function(){
            return this._mainEditor.getDebugger();
        };

        /**
         * Binds a shortcut to ace editor so that it will trigger the command on source view upon key press.
         * All the commands registered app's command manager will be bound to source view upon render.
         *
         * @param command {Object}
         * @param command.id {String} Id of the command to dispatch
         * @param command.shortcuts {Object}
         * @param command.shortcuts.mac {Object}
         * @param command.shortcuts.mac.key {String} key combination for mac platform eg. 'Command+N'
         * @param command.shortcuts.other {Object}
         * @param command.shortcuts.other.key {String} key combination for other platforms eg. 'Ctrl+N'
         */
        SourceView.prototype.bindCommand = function(command){
            var id = command.id,
                hasShortcut = _.has(command, 'shortcuts'),
                self = this;
            if(hasShortcut){
                var macShortcut = _.replace(command.shortcuts.mac.key, '+', "-"),
                    winShortcut = _.replace(command.shortcuts.other.key, '+', "-");
                this.getEditor().commands.addCommand({
                    name: id,
                    bindKey: {win: winShortcut, mac: macShortcut},
                    exec: function(editor) {
                        self.trigger('dispatch-command', id);
                    }
                });
            }
        };

        SourceView.prototype.show = function(){
            $(this._container).show();
        };

        SourceView.prototype.hide = function(){
            $(this._container).hide();
        };

        SourceView.prototype.isVisible = function(){
            return  $(this._container).is(':visible')
        };

        SourceView.prototype.format = function(doSilently){
//            var selectedRange = this._editor.selection.getRange();//TODO format selection
//            if(doSilently){
//                this._inSilentMode = true;
//            }
//            this._fomatter.beautify(this._editor.getSession());
//            if(doSilently){
//                this._inSilentMode = false;
//                this.markClean();
//            }
        };

        SourceView.prototype.isClean = function(){
            return this._editor.getSession().getUndoManager().isClean();
        };

        SourceView.prototype.undo = function(){
            return this._editor.getSession().getUndoManager().undo();
        };

        SourceView.prototype.redo = function(){
            return this._editor.getSession().getUndoManager().redo();
        };

        SourceView.prototype.markClean = function(){
            this._editor.getSession().getUndoManager().markClean();
        };

        return SourceView;
    });