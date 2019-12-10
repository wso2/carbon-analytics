/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['require', 'lodash', 'jquery', 'log', 'ace/ace', 'app/source-editor/editor'],
    function (require, _, $, log, ace, SiddhiEditor) {

        var KubernetesConfigDialog = function (options) {
            this.app = options.app;
            this.templateContainer = options.templateHeader;
            this.siddhiProcessName = options.siddhiProcessName;
            this.natsConfigsGiven = false;
            this.needDefaultNats = false;
            this.pvConfigsGiven = false;
            this.statePersistenceConfigGiven = false;
            this.editorObjectArrayList = [];
        };

        KubernetesConfigDialog.prototype.constructor = KubernetesConfigDialog;

        KubernetesConfigDialog.prototype.render = function () {
            var self = this;
            var distributionSelectionInput = self.templateContainer.find("#distribution-selection");
            var persistenceSelectionInput = self.templateContainer.find("#persistent-select");
            distributionSelectionInput.find("#non-distributed").prop('checked', true);
            persistenceSelectionInput.find("#stateless").prop('checked', true);

            var messagingEdtdivId = "kubernetes-messaging-editor-id";
            var messagingTemplateEntry = "<div class='kubernetes-config-editor' style='display:none' id='".concat(messagingEdtdivId).concat("'></div>");
            self.templateContainer.find('#distribution-selection').append(messagingTemplateEntry);
            this._mainEditor = new SiddhiEditor({
                divID: messagingEdtdivId,
                realTimeValidation: false,
                autoCompletion: false
            });

            var messagingSampleConfig = 'messagingSystem:\n' +
                '  type: nats\n' +
                '  # config: \n' +
                '  #   bootstrapServers: \n' +
                '  #     - "nats://siddhi-nats:4222"\n' +
                '  #   streamingClusterId: siddhi-stan';
            this._editor1 = ace.edit(messagingEdtdivId);
            this._editor1.getSession().setValue(messagingSampleConfig);
            this._editor1.resize(true);
            var obj1 = {
                name: 'messaging',
                content: this._editor1
            };
            self.editorObjectArrayList.push(obj1);

            self.templateContainer.find('#distributed-with-ext-nats').change(function(){
                self.natsConfigsGiven = true;
                self.needDefaultNats = false;
                self.templateContainer.find('#kubernetes-messaging-editor-id').show();
            });

            self.templateContainer.find('#non-distributed').change(function(){
                self.natsConfigsGiven = false;
                self.needDefaultNats = false;
                self.templateContainer.find('#kubernetes-messaging-editor-id').hide();
            });

            self.templateContainer.find('#distributed-with-nats').change(function(){
                self.natsConfigsGiven = false;
                self.needDefaultNats = true;
                self.templateContainer.find('#kubernetes-messaging-editor-id').hide();
            });

            this.divId = "kubernetes-pv-editor-id";
            this.divParentId = "kubernetes-pv-editor-parent-id";
            var pvcTemplateEntry = "<br /><br /><div id='".concat(this.divParentId);
            pvcTemplateEntry = pvcTemplateEntry.concat("' class='clearfix'>Persistence volume claim configuration for Kubernetes<div class='kubernetes-config-editor' style='display:none' id='");
            pvcTemplateEntry = pvcTemplateEntry.concat(this.divId).concat("'></div></div>");
            if (self.templateContainer.find("#" + this.divParentId).length == 0) {
                self.templateContainer.find('#persistent-select').append(pvcTemplateEntry);
            }
            this._mainEditor = new SiddhiEditor({
                divID: this.divId,
                realTimeValidation: false,
                autoCompletion: false
            });
            var pvSampleConfig = 'persistentVolumeClaim: \n' +
                '  accessModes: \n' +
                '    - ReadWriteOnce\n' +
                '  resources: \n' +
                '    requests: \n' +
                '      storage: 1Gi\n' +
                '  storageClassName: standard\n' +
                '  volumeMode: Filesystem';
            this._editor2 = ace.edit(this.divId);
            this._editor2.getSession().setValue(pvSampleConfig);
            this._editor2.resize(true);
            var obj2 = {
                name: 'persistence',
                content: this._editor2
            };
            self.editorObjectArrayList.push(obj2);

            this.persistenceConfDivId = "kubernetes-persistence-type-editor-id";
            this.persistenceConfDivParentId = "kubernetes-persistence-type-editor-parent-id";
            var statePersistenceTemplateEntry = "<br /><div id='".concat(this.persistenceConfDivParentId);
            statePersistenceTemplateEntry = statePersistenceTemplateEntry.concat("' class='clearfix'>State persistence configuration for Siddhi runtime<div class='kubernetes-config-editor' style='display:none' id='");
            statePersistenceTemplateEntry = statePersistenceTemplateEntry.concat(this.persistenceConfDivId).concat("'></div></div>");
            if (self.templateContainer.find("#" + this.persistenceConfDivParentId).length == 0) {
                self.templateContainer.find('#persistent-select').append(statePersistenceTemplateEntry);
            }
            this._mainEditor = new SiddhiEditor({
                divID: this.persistenceConfDivId,
                realTimeValidation: false,
                autoCompletion: false
            });
            this.databasePersistenceConfig = 'statePersistence: \n' +
                 '  enabled: true \n' +
                 '  intervalInMin: 1 \n' +
                 '  revisionsToKeep: 3 \n' +
                 '  persistenceStore: io.siddhi.distribution.core.persistence.DBPersistenceStore \n' +
                 '  config: \n' +
                 '    datasource: DATASOURCE_NAME   # A datasource with this name should be defined in datasources namespace \n' +
                 '    table: TABLE_NAME \n';
            this.amazons3PersistenceConfig = 'statePersistence: \n' +
                 '  enabled: true \n' +
                 '  intervalInMin: 1 \n' +
                 '  revisionsToKeep: 2 \n' +
                 '  persistenceStore: io.siddhi.distribution.core.persistence.S3PersistenceStore \n' +
                 '  config: \n' +
                 '    accessKey: ACEESS_KEY \n' +
                 '    secretKey: SECRET_KEY \n' +
                 '    region: us-west-2 \n' +
                 '    bucketName: siddhi-app-persistence \n';
            this.filePersistenceConfig = 'statePersistence: \n' +
                 '  enabled: true \n' +
                 '  intervalInMin: 1 \n' +
                 '  revisionsToKeep: 2 \n' +
                 '  persistenceStore: io.siddhi.distribution.core.persistence.FileSystemPersistenceStore \n' +
                 '  config: \n' +
                 '    location: siddhi-app-persistence \n';
            this._editor3 = ace.edit(this.persistenceConfDivId);
            this._editor3.getSession().setValue("");
            this._editor3.resize(true);
            this._editor3.setOption("minLines", 10);
            var obj3 = {
                name: 'state-persistence',
                content: this._editor3
            };
            self.editorObjectArrayList.push(obj3);
            self.templateContainer.find("#" + self.persistenceConfDivParentId).hide();
            self.templateContainer.find("#" + self.divParentId).hide();

            self.templateContainer.find('#kube-persistence-dropdown-id').change(function(){
                var kubePersistenceOptions = self.templateContainer.find('#kube-persistence-dropdown-id')[0].options;
                if (kubePersistenceOptions[kubePersistenceOptions.selectedIndex].value == "disable-persistence") {
                    self.pvConfigsGiven = false;
                    self.statePersistenceConfigGiven = false;
                    self._editor3.getSession().setValue("");
                    self.templateContainer.find("#" + self.persistenceConfDivParentId).hide();
                    self.templateContainer.find("#" + self.persistenceConfDivId).hide();
                    self.templateContainer.find("#" + self.divParentId).hide();
                    self.templateContainer.find("#" + self.divId).hide();
                } else if (kubePersistenceOptions[kubePersistenceOptions.selectedIndex].value == "file-system-persistence") {
                    self.pvConfigsGiven = true;
                    self.statePersistenceConfigGiven = true;
                    self._editor3.getSession().setValue(self.filePersistenceConfig);
                    self.templateContainer.find("#" + self.persistenceConfDivParentId).show();
                    self.templateContainer.find("#" + self.persistenceConfDivId).show();
                    self.templateContainer.find("#" + self.divParentId).show();
                    self.templateContainer.find("#" + self.divId).show();
                } else if (kubePersistenceOptions[kubePersistenceOptions.selectedIndex].value == "database-persistence") {
                    self.pvConfigsGiven = false;
                    self.statePersistenceConfigGiven = true;
                    self._editor3.getSession().setValue(self.databasePersistenceConfig);
                    self.templateContainer.find("#" + self.persistenceConfDivParentId).show();
                    self.templateContainer.find("#" + self.persistenceConfDivId).show();
                    self.templateContainer.find("#" + self.divParentId).hide();
                    self.templateContainer.find("#" + self.divId).hide();
                } else if (kubePersistenceOptions[kubePersistenceOptions.selectedIndex].value == "amazon-s3-persistence") {
                    self.pvConfigsGiven = false;
                    self.statePersistenceConfigGiven = true;
                    self._editor3.getSession().setValue(self.amazons3PersistenceConfig);
                    self.templateContainer.find("#" + self.persistenceConfDivParentId).show();
                    self.templateContainer.find("#" + self.persistenceConfDivId).show();
                    self.templateContainer.find("#" + self.divId).hide();
                    self.templateContainer.find("#" + self.divParentId).hide();
                }
            });
        };

        KubernetesConfigDialog.prototype.getConfigs = function () {
            var self = this;
            var messagingConfig ='';
            var pvConfig = '';
            var statePersistenceConfig = '';
            var siddhiProcessName = self.siddhiProcessName || 'sample-siddhi-process';
            var siddhiProcessNameConfig = "siddhiProcessName: ".concat(siddhiProcessName.toString());
            var messagingDefaultConfig = 'messagingSystem:\n' +
                            '  type: nats\n';
            self.editorObjectArrayList.forEach(function(editorObj) {
                if (self.natsConfigsGiven && !self.needDefaultNats && editorObj.name == 'messaging') {
                    messagingConfig = "\n" + editorObj.content.session.getValue().toString();
                }
                if (self.needDefaultNats && !self.natsConfigsGiven && editorObj.name == 'messaging') {
                    messagingConfig = "\n" + messagingDefaultConfig;
                }
                if(self.pvConfigsGiven && editorObj.name == 'persistence') {
                    pvConfig = "\n" + editorObj.content.session.getValue().toString().trim();
                }
                if(self.statePersistenceConfigGiven && editorObj.name == 'state-persistence') {
                    statePersistenceConfig = "\n" + editorObj.content.session.getValue().toString().trim();
                }
            });

            return {
                        "kubernetesConfig": siddhiProcessNameConfig + messagingConfig + pvConfig,
                        "statePersistenceConfig": statePersistenceConfig
            };
        };


        return KubernetesConfigDialog;
    });


