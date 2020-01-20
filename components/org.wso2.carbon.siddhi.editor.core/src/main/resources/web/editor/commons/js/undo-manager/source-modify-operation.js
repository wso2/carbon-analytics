/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['lodash', 'undoable_operation'],
    function (_, UndoableOperation) {

        /**
         * Class to represent an undoable source modify operation
         * @class SourceModifyOperation
         * @augments UndoableOperation
         * @param args
         * @constructor
         */
        var SourceModifyOperation = function (args) {
            UndoableOperation.call(this, args);
        };

        SourceModifyOperation.prototype = Object.create(UndoableOperation.prototype);
        SourceModifyOperation.prototype.constructor = SourceModifyOperation;


        SourceModifyOperation.prototype.undo = function () {
            if (this.canUndo()) {
                this.getEditor().getSourceView().undo();
            }
        };

        SourceModifyOperation.prototype.redo = function () {
            if (this.canRedo()) {
                this.getEditor().getSourceView().redo();
            }
        };

        SourceModifyOperation.prototype.canUndo = function () {
            return this.getEditor().isInSourceView();
        };

        SourceModifyOperation.prototype.canUndo = function () {
            return this.getEditor().isInSourceView();
        };

        return SourceModifyOperation;
    });

