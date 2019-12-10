/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['log', 'lodash', 'event_channel', 'undoable_operation_factory'],
    function (log, _, EventChannel, UndoableOperationFactory) {

        /**
         * Class to represent undo/redo manager
         * @class UndoManager
         * @augments EventChannel
         * @param args
         * @constructor
         */
        var UndoManager = function (args) {
            this._limit = _.get(args, 'limit', 20);
            this._undoStack = [];
            this._redoStack = [];
        };

        UndoManager.prototype = Object.create(EventChannel.prototype);
        UndoManager.prototype.constructor = UndoManager;

        UndoManager.prototype.reset = function () {
            this._undoStack = [];
            this._redoStack = [];
            this.trigger('reset');
        };

        UndoManager.prototype._push = function (undoableOperation) {
            if (this._undoStack.length === this._limit) {
                // remove oldest undoable operation
                this._undoStack.splice(0, 1);
            }
            this._undoStack.push(undoableOperation);
            this.trigger('undoable-operation-added', undoableOperation);
        };

        UndoManager.prototype.hasUndo = function () {
            return !_.isEmpty(this._undoStack);
        };

        UndoManager.prototype.undoStackTop = function () {
            return _.last(this._undoStack);
        };

        UndoManager.prototype.redoStackTop = function () {
            return _.last(this._redoStack);
        };

        UndoManager.prototype.undo = function () {
            var taskToUndo = this._undoStack.pop();
            taskToUndo.undo();
            this._redoStack.push(taskToUndo);
        };

        UndoManager.prototype.hasRedo = function () {
            return !_.isEmpty(this._redoStack);
        };

        UndoManager.prototype.redo = function () {
            var taskToRedo = this._redoStack.pop();
            taskToRedo.redo();
            this._undoStack.push(taskToRedo);
        };

        UndoManager.prototype.getOperationFactory = function () {
            return UndoableOperationFactory;
        };

        UndoManager.prototype.onUndoableOperation = function (event) {
            var undoableOperation = UndoableOperationFactory.getOperation(event);
            this._push(undoableOperation);
        };

        return UndoManager;
    });

