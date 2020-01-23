/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['jquery', 'lodash', 'backbone', 'log'], function ($, _, Backbone, log) {

    var File = Backbone.Model.extend(
        {
            defaults: {
                path: 'temp',
                name: 'untitled',
                content: undefined,
                isPersisted: false,
                lastPersisted: _.now(),
                isDirty: true,
                debugStatus: false,
                runStatus: false,
                hashCode: undefined,
            },
            initialize: function (attrs, options) {
                var errMsg;
                if (!this.get('isPersisted')){
                    if(!_.has(options, 'storage')){
                        errMsg = 'unable to find storage' + _.toString(attrs);
                        log.error(errMsg);
                        throw errMsg;
                    }
                    this._storage = _.get(options, 'storage');
                    this._storage .create(this);
                } else {
                    this.setHash(this.generateHash(this.getContent().trim()));
                }
            },
            generateHash: function (hashCode) {
                var newHash = 0;
                var l = hashCode.length;
                var i = 0;
                if (l > 0) {
                    while (i < l) {
                        newHash = (newHash << 5) - newHash + hashCode.charCodeAt(i++) | 0;
                    }
                }
                return newHash;
            },

            save: function(isTriggeredBySaveFlow){
                if(!_.isNil(this._storage.get(this.id))){
                    if (isTriggeredBySaveFlow == true) {
                        this.setHash(this.generateHash(this.getContent().trim()));
                    }
                    this._storage.update(this);
                } else {
                    this._storage.create(this);
                }
                return this;
            },

            setHash: function (hashCode) {
                this.set('hashCode', hashCode);
                return this;
            },

            setPath: function(path){
                this.set('path', path);
                return this;
            },

            setStorage: function(storage){
                this._storage = storage;
                return this;
            },

            setPersisted: function(isPersisted){
                this.set('isPersisted', isPersisted);
                return this;
            },

            setLastPersisted: function(lsatPersisted){
                this.set('lastPersisted', lsatPersisted);
                return this;
            },

            setDirty: function(isDirty){
                this.set('isDirty', isDirty);
                this.trigger('dirty-state-change', isDirty);
                return this;
            },

            setName: function(name){
                this.set('name', name);
                return this;
            },

            setRunStatus: function(status){
                this.set('runStatus', status);
                return this;
            },

            setDebugStatus: function(status){
                this.set('debugStatus', status);
                return this;
            },

            setContent: function(name){
                this.set('content', name);
                return this;
            },

            getHash: function () {
                return this.get('hashCode');
            },

            getPath: function(){
                return this.get('path')
            },

            getName: function(){
                return this.get('name')
            },

            getRunStatus: function(){
                return this.get('runStatus')
            },

            getDebugStatus: function(){
                return this.get('debugStatus')
            },

            getContent: function(){
                return this.get('content')
            },

            getLastPersisted: function(){
                return this.get('lastPersisted');
            },


            isPersisted: function(){
                return this.get('isPersisted')
            },

            isDirty: function(){
                return this.get('isDirty')
            },

            isStopProcessRunning: function () {
                return this.get('stopProcessRunning');
            },

            setStopProcessRunning: function(stopTriggered){
                this.set('stopProcessRunning', stopTriggered);
                return this;
            },

        });

    return File;
});