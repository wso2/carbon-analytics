define(['jquery', 'backbone', 'log','lodash'], function ($, Backbone,log, _) {
    var DebugManager = Backbone.View.extend({
        initialize: function(config) {
            var errMsg;
            var self = this;
            this._template = $("#debugger-template");

            if (!_.has(config, 'parent-container')) {
                errMsg = 'unable to find configuration for parent-container';
                log.error(errMsg);
                throw errMsg;
            }
            var parentContainer = $(_.get(config, 'parent-container'));

            this._$parent_el = parentContainer;

            if (!_.has(config, 'application')) {
                log.error('Cannot init debugger. config: application not found.')
            }

            var clonedDebugger = this._template.children('div').clone();
            this._console = clonedDebugger;

            this.application = _.get(config, 'application');
            this._options = config;
            //this.debuggerServiceUrl = _.get(this._options, 'application.config.services.debugger.endpoint');
            this._lastWidth = undefined;

            // register command
//            this.application.commandManager.registerCommand(config.command.id, {shortcuts: config.command.shortcuts});
//            this.application.commandManager.registerHandler(config.command.id, this.toggleDebugger, this);

        },

        isActive: function(){
            return this._activateBtn.parent('li').hasClass('active');
        },

        getConsole: function(){
            return this._console;
        },

        render: function() {
//            var self = this;
//            var activateBtn = $(_.get(this._options, 'activateBtn'));
//            this._activateBtn = activateBtn;
//
//            this.renderContent();
//            activateBtn.on('show.bs.tab', function (e) {
//                self._isActive = true;
//                var width = self._lastWidth || _.get(self._options, 'defaultWidth');
//                self._$parent_el.parent().width(width);
//                self._containerToAdjust.css('padding-left', width + _.get(self._options, 'leftOffset'));
//                self._verticalSeparator.css('left',  width + _.get(self._options, 'leftOffset') - _.get(self._options, 'separatorOffset'));
//            });
//
//            activateBtn.on('hide.bs.tab', function (e) {
//                self._isActive = false;
//            });
//
//            activateBtn.on('click', function(e){
//                $(this).tooltip('hide');
//                e.preventDefault();
//                e.stopPropagation();
//                self.application.commandManager.dispatch(_.get(self._options, 'command.id'));
//            });
//
//            activateBtn.attr("data-placement", "bottom").attr("data-container", "body");
//
//            if (this.application.isRunningOnMacOS()) {
//                activateBtn.attr("title", "Debugger (" + _.get(self._options, 'command.shortcuts.mac.label') + ") ").tooltip();
//            } else {
//                activateBtn.attr("title", "Debugger  (" + _.get(self._options, 'command.shortcuts.other.label') + ") ").tooltip();
//            }
//
//            this._verticalSeparator.on('drag', function(event){
//                if( event.originalEvent.clientX >= _.get(self._options, 'resizeLimits.minX')
//                    && event.originalEvent.clientX <= _.get(self._options, 'resizeLimits.maxX')){
//                    self._verticalSeparator.css('left', event.originalEvent.clientX);
//                    self._verticalSeparator.css('cursor', 'ew-resize');
//                    var newWidth = event.originalEvent.clientX;
//                    self._$parent_el.parent().width(newWidth);
//                    self._containerToAdjust.css('padding-left', event.originalEvent.clientX);
//                    self._lastWidth = newWidth;
//                    self._isActive = true;
//                }
//                event.preventDefault();
//                event.stopPropagation();
//            });

            return this;

        },

        renderContent: function () {
//            var debuggerContainer = $('<div>'
//                                    + '<div class="debug-tools-container"></div>'
//                                    + '<div class="debug-frams-container"></div>'
//                                    + '<div class="debug-variables-container"></div>'
//                                    + '</div>');
//            debuggerContainer.addClass(_.get(this._options, 'cssClass.container'));
//            debuggerContainer.attr('id', _.get(this._options, ('containerId')));
//            this._$parent_el.append(debuggerContainer);
//
//            Tools.setArgs({ container : debuggerContainer.find('.debug-tools-container') ,
//                            launchManager: this.launchManager,
//                            application: this.application });
//            Tools.render();
//
//            Frames.setContainer(debuggerContainer.find('.debug-frams-container'));
//
//            this._debuggerContainer = debuggerContainer;
//            debuggerContainer.mCustomScrollbar({
//                theme: "minimal",
//                scrollInertia: 0
//            });
        }


    });

    return DebugManager;
});