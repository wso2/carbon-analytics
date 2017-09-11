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
define(['lodash', 'undoable_operation'],
    function (_, UndoableOperation) {

        /**
         * Class to represent an undoable source modify operation
         * @class SourceModifyOperation
         * @augments UndoableOperation
         * @param args
         * @constructor
         */
        var SourceModifyOperation = function(args){
            UndoableOperation.call(this, args);
        };

        SourceModifyOperation.prototype = Object.create(UndoableOperation.prototype);
        SourceModifyOperation.prototype.constructor = SourceModifyOperation;


        SourceModifyOperation.prototype.undo = function(){
            if(this.canUndo()){
                this.getEditor().getSourceView().undo();
            }
        };

        SourceModifyOperation.prototype.redo = function(){
            if(this.canRedo()){
                this.getEditor().getSourceView().redo();
            }
        };

        SourceModifyOperation.prototype.canUndo = function(){
            return  this.getEditor().isInSourceView();
        };

        SourceModifyOperation.prototype.canUndo = function(){
            return this.getEditor().isInSourceView();
        };

        return SourceModifyOperation;
    });

