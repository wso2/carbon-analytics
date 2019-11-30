/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['lodash', 'event_channel'],
    function (_, EventChannel){

        var MenuItem = function(args){
            _.assign(this, args);
            this._application = _.get(this, 'options.application');
        };

        MenuItem.prototype = Object.create(EventChannel.prototype);
        MenuItem.prototype.constructor = MenuItem;

        MenuItem.prototype.render = function(){
            var parent = _.get(this, 'options.parent');

            var item = $('<li></li>');

            //adding an id attr to the dropdown menu items
            item.attr('id', _.get(this, 'definition.id'));

            var title = $('<span class="pull-left"></span>');
            var link = $('<a></a>');
            parent.append(item);
            item.append(link);
            link.append(title);

            title.text(_.get(this, 'definition.label'));
            this._linkElement = link;
            this._title = title;
            this._listItemElement = item;

            var shortcuts = _.get(this, 'definition.command.shortcuts'),
                commandId = _.get(this, 'definition.command.id'),
                commandLabels = _.get(this, 'definition.command.labels');

            // Hide Debug menu until https://github.com/siddhi-io/distribution/issues/555 is solved
            if (_.get(this, 'definition.id') === "debug") {
                item.hide();
            } else {
                if (!_.isNil(shortcuts)) {
                    this._application.commandManager.registerCommand(commandId, {shortcuts: shortcuts});
                    this.renderShortcutLabel();
                } else {
                    if (!_.isNil(commandLabels)) {
                        this.renderCommandLabel();
                    }
                    this._application.commandManager.registerCommand(commandId, {});
                }
            }

            if (_.get(this, 'definition.disabled')) {
                this.disable();
            } else {
                this.enable();
            }

        };

        MenuItem.prototype.getID = function(){
            return _.get(this, 'definition.id');
        };

        MenuItem.prototype.renderShortcutLabel = function(){
            var shortcuts = _.get(this, 'definition.command.shortcuts'),
                shortcutLabel = $('<span></span>'),
                shortcut = this._application.isRunningOnMacOS() ? shortcuts.mac.label : shortcuts.other.label;
            shortcutLabel.addClass(_.get(this, 'options.cssClass.shortcut'));
            shortcutLabel.text(shortcut);
            this._linkElement.append(shortcutLabel);
        };

        MenuItem.prototype.renderCommandLabel = function(){
            var labels = _.get(this, 'definition.command.labels'),
                shortcutLabel = $('<span></span>'),
                shortcut = this._application.isRunningOnMacOS() ? labels.mac.label : labels.other.label;
            shortcutLabel.addClass(_.get(this, 'options.cssClass.shortcut'));
            shortcutLabel.text(shortcut);
            this._linkElement.append(shortcutLabel);
        };

        MenuItem.prototype.disable = function(){
            this._listItemElement.addClass(_.get(this, 'options.cssClass.inactive'));
            this._listItemElement.removeClass(_.get(this, 'options.cssClass.active'));
            this._linkElement.off("click");
        };

        MenuItem.prototype.enable = function(){
            this._listItemElement.addClass(_.get(this, 'options.cssClass.active'));
            this._listItemElement.removeClass(_.get(this, 'options.cssClass.inactive'));
            var self = this;
            this._linkElement.off("click");
            this._linkElement.click(function () {
                self._application.commandManager.dispatch(self.definition.command.id);
            });
        };

        MenuItem.prototype.addLabelSuffix = function(labelSuffix){
            if(!_.isNil(labelSuffix)){
                this._title.text(_.get(this, 'definition.label') + ' ' + labelSuffix);
            }
        };

        MenuItem.prototype.clearLabelSuffix = function () {
            this._title.text(_.get(this, 'definition.label'));
        };

        MenuItem.prototype.updateLabel = function(labelText) {
            if (!_.isNil(labelText)) {
                this._title.text(labelText);
            }
        };

        return MenuItem;

    });