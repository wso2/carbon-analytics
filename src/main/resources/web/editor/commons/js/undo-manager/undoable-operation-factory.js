/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['lodash', 'source_modify_operation'],
    function (_, SourceModifyOperation) {

        var UndoableOperationFactory = {};
        /**
         * A Factory method to create undoable operations
         * @param args
         */
        UndoableOperationFactory.getOperation = function (args) {
            switch (args.type) {
                case "child-added":
                    return new ASTNodeAddOperation(args);
                case "child-removed":
                    return new ASTNodeRemoveOperation(args);
                case "node-modified":
                    return new ASTNodeModifyOperation(args);
                case "source-modified":
                    return new SourceModifyOperation(args);
                case "custom":
                    return new CustomUndoableOperation(args);
            }
        };

        UndoableOperationFactory.isSourceModifiedOperation = function (undoableOperation) {
            return undoableOperation instanceof SourceModifyOperation;
        };

        return UndoableOperationFactory;
    });

