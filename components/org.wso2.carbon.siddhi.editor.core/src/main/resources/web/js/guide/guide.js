/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

//Todo
//session check workspace
/*
check the flag for the first user
Make a new option as help
css button change
highlighted area concern
three options from the popup
script changes
don't delete the file
 */
define(['ace/ace', 'jquery', 'lodash', 'log', 'enjoyhint', 'designViewUtils'],
    function (ace, $, _, log, EnjoyHintLib, DesignViewUtils) {

        /**
         * Arg: application instance
         */
        var Guide = function (application) {

            var app = application;
            this.tabList = app.tabController.getTabList();
            this.instanceVal;
            this.tabLength;
            var self = this;
            var workspaceServiceURL = app.config.services.workspace.endpoint;
            var saveServiceURL = workspaceServiceURL + "/write";
            var content = '@App:name(\'SweetFactory\')\n' +
                '@App:description(\'Description of the plan\')\n' +
                '\n' +
                '-- Please refer to https://docs.wso2.com/display/SP400/Quick+Start+Guide on getting started with SP editor. \n' +
                '\n' +
                'define stream SweetProductionStream (name string, amount long);\n' +
                '@sink(type = \'log\', \n' +
                '\t@map(type = \'passThrough\'))\n' +
                'define stream TotalProductionStream (TotalProduction long);\n' +
                '\n' +
                '@info(name = \'SweetTotalQuery\')\n' +
                'from SweetProductionStream \n' +
                'select count() as TotalProduction \n' +
                'insert into TotalProductionStream;\n';

            var payload = "configName=" + btoa('SweetFactory.siddhi') + "&config=" + (btoa(content));

            //Script array for the complete guide
            this.createNewProject = [
                {
                    'click #newButton': '<b>Welcome to WSO2 SP!</b> Click <b class="lime-text">New</b> to get started.',
                    'showSkip': true,
                    onBeforeStart: function () {
                        $('#hintTour').removeClass('menu-item-enabled').addClass('menu-item-disabled').css('pointer-events', 'none');
                    }
                },
                {
                    'next .ace_line_group': 'First, We have defined an input stream for you!. <b class="lime-text">' +
                        'A stream</b> is a logical series of events ordered in time with a<br> uniquely identifiable ' +
                        'name, and set of defined attributes with specific data types defining its schema. ' +
                        '<br>Click <b class="lime-text">Next</b> to continue.',
                    'margin': 200,
                    'showSkip': false,
                    onBeforeStart: function () {
                        var activeTab = app.tabController.getActiveTab();
                        var editor = activeTab.getSiddhiFileEditor().getSourceView().getEditor();
                        var aceEditor = app.tabController.getActiveTab().getSiddhiFileEditor().getSourceView().getEditor();
                        editor.session.insert(aceEditor.getCursorPosition(), 'define stream SweetProductionStream (name string, amount long);');
                    }
                },
                {
                    'click #File': 'To save the file, click <b class="lime-text">File</b>.',
                    'showSkip': false,
                    'showNext': false,
                    'shape': 'rect',
                    'left': 3,
                    'right': 5,
                    'bottom': 5
                },
                {
                    'click #saveAs': 'Then click <b class="lime-text">Save As</b>.',
                    'showSkip': false,
                    'showNext': false,
                    'left': 3,
                    'right': 3,
                    'bottom': 7,
                    'top': 7
                },
                {
                    'keyCode': 9,
                    selector: '#saveName',
                    event: 'key',
                    description: 'Your file name is <b class="lime-text">SweetFactory.</b> Press <b class="lime-text"> ' +
                        'Tab </b>key to continue.',
                    'showNext': false,
                    'showSkip': false,
                    'timeout': 200,
                    onBeforeStart: function () {
                        setTimeout(function () {
                            $('#saveConfigModal').find('#configName').val('SweetFactory');
                            $('input[name=siddhiAppName]').focus();
                        }, 800);
                    }
                },
                {
                    'click #saveButton': 'Click <b class="lime-text">Save</b> to save the file.',
                    'showSkip': false
                },
                {
                    'click .toggle-controls-container': 'Now let us implement the rest of your application from ' +
                        '<b class="lime-text">Design View.<br></b>Click <b class="lime-text">Design View</b> ' +
                        'to open the Design View.',
                    'showSkip': false,
                    'showNext': false
                },
                {
                    'next #stream': 'This is a <b class="lime-text">Stream </b>component. you need to use this to ' +
                        'generate the output. Click <b class="lime-text">Next</b>.',
                    'showSkip': false,
                    'showNext': true,
                    onBeforeStart: function () {
                        $('#stream').removeClass('stream-drag');
                    }
                },
                {
                    'custom .design-view-container': 'Drag a <b class="lime-text">Stream </b>component and place' +
                        ' it to the right of SweetProduction component.',
                    'showSkip': false,
                    'showNext': false,
                    'shape': 'rect',
                    'bottom': 300,
                    onBeforeStart: function () {
                        var flag = true;
                        $('#tool-group-Collections').find('.tool-group-body').css('display', 'none');
                        $('#tool-group-Queries').find('.tool-group-body').css('display', 'none');
                        $('#tool-group-Functions').find('.tool-group-body').css('display', 'none');
                        $("#tool-group-I\\/O").find('.tool-group-body').css('display', 'none');
                        $('#trigger').removeClass('trigger-drag');
                        $('#partition').removeClass('partition-drag');
                        $('#stream').addClass('stream-drag');
                        setTimeout(function () {
                            var Interval = null;
                            Interval = window.setInterval(function () {
                                var newElement = app.tabController.getActiveTab().getSiddhiFileEditor().getDesignView()
                                    .getConfigurationData().getSiddhiAppConfig();
                                if (newElement.streamList.length === 2) {
                                    self.instanceVal.trigger('next');
                                    clearInterval(Interval);
                                } else if (flag) {
                                    flag = false;
                                    DesignViewUtils.prototype.warnAlert("Please drag and drop a Stream Component");
                                }
                            }, 1000);
                        }, 3000)
                    }
                },
                {
                    'click .incomplete-element': '<b class="lime-text">Move the cursor over the dropped component</b> and ' +
                        'click<b class="lime-text"> settings </b> icon to open the <b class="lime-text">Stream Configuration</b> form.',
                    'showSkip': false,
                    'showNext': false,
                    'shape': "rect",
                    'margin': 35,
                    onBeforeStart: function () {
                        $('#tool-group-Collections').find('.tool-group-body').css('display', 'block');
                        $('#tool-group-Queries').find('.tool-group-body').css('display', 'block');
                        $('#tool-group-Functions').find('.tool-group-body').css('display', 'block');
                        $("#tool-group-I\\/O").find('.tool-group-body').css('display', 'block');
                        $('.fw-delete').removeClass('fw-delete');
                        $('#trigger').addClass('trigger-drag');
                        $('#partition').addClass('partition-drag');
                    }
                },
                {
                    'keyCode': 9,
                    selector: '#streamName',
                    event: 'key',
                    description: 'We have set the stream name as <b class="lime-text"> TotalProductionStream.</b> ' +
                        'Press<b class="lime-text"> Tab</b> to continue.',
                    'showNext': false,
                    'showSkip': false,
                    onBeforeStart: function () {
                        $('.fw-delete').addClass('fw-delete');
                        $("#streamName").val("TotalProductionStream").focus();
                    }
                },
                {
                    'next .attr-name': 'We have set the attribute name as <b class="lime-text"> ' +
                        'TotalProduction. Click</b><b class="lime-text"> Next</b> to continue',
                    'showSkip': false,
                    'showNext': true,
                    onBeforeStart: function () {
                        $(".attr-name").val("TotalProduction").focus();
                    }
                },
                {
                    'next .attr-type': 'We have set the attribute type as <b class="lime-text"> ' +
                        'long. Click</b><b class="lime-text"> Next</b> to continue.',
                    'showSkip': false,
                    'showNext': true,
                    onBeforeStart: function () {
                        $(".attr-type").val("long");
                    }
                },

                {
                    'click #btn-submit': 'Click <b class="lime-text">Submit</b> to submit the stream configuration.',
                    'showSkip': false,
                    'showNext': false,
                    scrollAnimationSpeed: 500,
                    onBeforeStart: function () {
                        setTimeout(function () {
                            $('#btn-submit').focus();
                        }, 500);
                    }
                },
                {
                    'next #projection-query': 'This is a <b class="lime-text">Projection component</b> that allows' +
                        ' you to select some or all of the attributes from the input stream to be inserted into the ' +
                        'output stream. Click <b class="lime-text"> Next</b> ',
                    'showSkip': false,
                    'showNext': true,
                    onBeforeStart: function () {
                        $('#projection-query').removeClass('projection-query-drag');
                    }
                },
                {
                    'custom .design-view-container': 'Drag a <b class="lime-text">Projection component </b> and place' +
                        ' it between your input and output stream components.',
                    'showSkip': false,
                    'showNext': false,
                    'shape': 'rect',
                    'bottom': 300,
                    onBeforeStart: function () {
                        var flag = true;
                        $('#tool-group-Collections').find('.tool-group-body').css('display', 'none');
                        $("div[id='tool-group-Flow Constructs']").find('.tool-group-body').css('display', 'none');
                        $('#tool-group-Functions').find('.tool-group-body').css('display', 'none');
                        $("#tool-group-I\\/O").find('.tool-group-body').css('display', 'none');
                        $('#projection-query').addClass('projection-query-drag');
                        $('#filter-query').removeClass('filter-query-drag');
                        $('#window-query').removeClass('window-query-drag');
                        $('#pattern-query').removeClass('pattern-query-drag');
                        $('#sequence-query').removeClass('sequence-query-drag');
                        $('#join-query').removeClass('join-query-drag');
                        $('#function-query').removeClass('function-query-drag');
                        setTimeout(function () {
                            var Interval = null;
                            Interval = window.setInterval(function () {
                                var newElement = app.tabController.getActiveTab().getSiddhiFileEditor().getDesignView()
                                    .getConfigurationData().getSiddhiAppConfig();
                                if (newElement.queryLists.WINDOW_FILTER_PROJECTION.length === 1) {
                                    self.instanceVal.trigger('next');
                                    clearInterval(Interval);
                                } else if (flag) {
                                    flag = false;
                                    DesignViewUtils.prototype.warnAlert("Please drag and drop a Projection Component");
                                }
                            }, 1000);
                        }, 3000)
                    }
                },
                {
                    'custom .design-view-container': 'Connect the input stream to the projection component, ' +
                        'Then connect the Projection component to the output stream using nodes.<br> To do this you can ' +
                        '<b class="lime-text">draw a connecting arrow by dragging the cursor from one node to another</b>',
                    'showSkip': false,
                    'showNext': false,
                    'shape': 'rect',
                    'bottom': 300,
                    onBeforeStart: function () {
                        var flag = true;
                        $('#filter-query').addClass('filter-query-drag');
                        $('#window-query').addClass('window-query-drag');
                        $('#pattern-query').addClass('pattern-query-drag');
                        $('#sequence-query').addClass('sequence-query-drag');
                        $("#tool-group-Queries").find('.tool-group-body').css('display', 'none');
                        setTimeout(function () {
                            var Interval = null;
                            Interval = window.setInterval(function () {
                                if ($('.projectionQueryDrop')[0].title === "Output section of Query form is not filled") {
                                    clearInterval(Interval);
                                    self.instanceVal.trigger('next');
                                } else if (flag) {
                                    flag = false;
                                    DesignViewUtils.prototype.warnAlert("Please connect the elements to continue");
                                }
                            }, 1000);
                        }, 3000);
                    }
                },
                {
                    'click .projectionQueryDrop': 'Move the cursor over the dropped component and click on the ' +
                        '<b class="lime-text">settings</b> icon to open the <b class="lime-text">' +
                        'Query configuration</b> form',
                    'showSkip': false,
                    'showNext': false,
                    'shape': "rect",
                    'margin': 35,
                    onBeforeStart: function () {
                        $('#tool-group-Collections').find('.tool-group-body').css('display', 'block');
                        $("div[id='tool-group-Flow Constructs']").find('.tool-group-body').css('display', 'block');
                        $('#tool-group-Functions').find('.tool-group-body').css('display', 'block');
                        $("#tool-group-I\\/O").find('.tool-group-body').css('display', 'block');
                        $('.fw-delete').removeClass('fw-delete');
                    }
                },
                {
                    selector: '#form-query-name',
                    event: 'custom',
                    description: 'Change the query name to <b class="lime-text">SweetTotalQuery</b>',
                    'showNext': false,
                    'showSkip': false,
                    onBeforeStart: function () {
                        var flag = true;
                        setTimeout(function () {
                            var Interval = null;
                            Interval = window.setInterval(function () {
                                if ($('#form-query-name').find('.form-control').val() == 'SweetTotalQuery') {
                                    self.instanceVal.trigger('next');
                                    clearInterval(Interval);
                                } else if (flag) {
                                    flag = false;
                                    DesignViewUtils.prototype.warnAlert("Please insert the valid query name");
                                }
                            }, 1000);
                        }, 3000)
                    }
                },
                {
                    'custom .has-error': 'Enter <b class="lime-text">count()</b> as the expression',
                    'showSkip': false,
                    'showNext': false,
                    'keyCode': 13,
                    'shape': 'rect',
                    'bottom': 20,
                    onBeforeStart: function () {
                        var flag = true;
                        setTimeout(function () {
                            var Interval = null;
                            Interval = window.setInterval(function () {
                                if ($('.has-error').find('.form-control').val() == 'count()') {
                                    self.instanceVal.trigger('next');
                                    clearInterval(Interval);
                                } else if (flag) {
                                    flag = false;
                                    DesignViewUtils.prototype.warnAlert("Please insert the valid expression");
                                }
                            }, 1000);
                        }, 3000)
                    }
                },
                {
                    'click #btn-submit': 'Click <b class="lime-text">Submit</b> to submit the query configuration',
                    'showSkip': false,
                    'showNext': false,
                    scrollAnimationSpeed: 500,
                    onBeforeStart: function () {
                        setTimeout(function () {
                            $('#btn-submit').focus();
                        }, 500);
                    }
                },
                {
                    'next #sink': 'This is a <b class="lime-text">Sink component</b>.<br>Sink is a contract that takes ' +
                        'events arriving at a stream, maps them to a predefined data format,<br> and publishes them' +
                        ' to external endpoints. You are using this to <b class="lime-text">display the output result.</b>',
                    'showSkip': false,
                    onBeforeStart: function () {
                        $('#sink').removeClass('sink-drag');
                    }
                },
                {
                    'custom .design-view-container': 'Drag a <b class="lime-text">Sink</b> component and place it to ' +
                        'the right of the <b class="lime-text">TotalProduction</b> component',
                    'showSkip': false,
                    'showNext': false,
                    'shape': 'rect',
                    'bottom': 300,
                    onBeforeStart: function () {
                        $('#tool-group-Collections').find('.tool-group-body').css('display', 'none');
                        $("div[id='tool-group-Flow Constructs']").find('.tool-group-body').css('display', 'none');
                        $('#tool-group-Functions').find('.tool-group-body').css('display', 'none');
                        $("#tool-group-Queries").find('.tool-group-body').css('display', 'none');
                        $('#source').removeClass('source-drag ');
                        var flag = true;
                        $('#sink').addClass('sink-drag');
                        setTimeout(function () {
                            var Interval = null;
                            Interval = window.setInterval(function () {
                                var newElement = app.tabController.getActiveTab().getSiddhiFileEditor().getDesignView()
                                    .getConfigurationData().getSiddhiAppConfig();
                                if (newElement.sinkList.length === 1) {
                                    self.instanceVal.trigger('next');
                                    clearInterval(Interval);
                                } else if (flag) {
                                    flag = false;
                                    DesignViewUtils.prototype.warnAlert("Please drag and drop a Sink Component");
                                }
                            }, 1000);
                        }, 3000)
                    }
                },
                {
                    'custom .design-view-container': 'Connect <b class="lime-text">TotalProduction</b> component to it',
                    'showSkip': false,
                    'showNext': false,
                    'shape': 'rect',
                    'bottom': 300,
                    onBeforeStart: function () {
                        var flag = true;
                        setTimeout(function () {
                            var Interval = null;
                            Interval = window.setInterval(function () {
                                if ($('.sinkDrop')[0].title === "Sink annotation form is incomplete") {
                                    clearInterval(Interval);
                                    self.instanceVal.trigger('next');
                                } else if (flag) {
                                    flag = false;
                                    DesignViewUtils.prototype.warnAlert("Please connect the elements to continue");
                                }
                            }, 1000);
                        }, 3000);
                    }
                },
                {
                    'click .sinkDrop': 'Move the mouse over the dropped component and click' +
                        ' <b class="lime-text">settings</b> icon to open the <b class="lime-text">Sink configuration</b>',
                    'showSkip': false,
                    'showNext': false,
                    'shape': "rect",
                    'margin': 35,
                    onBeforeStart: function () {
                        $('.fw-delete').removeClass('fw-delete');
                    }
                },
                {
                    'custom #sink-type': 'Then select <b class="lime-text">Log</b> from the list in the <b class="lime-text">' +
                        'Sink type</b> field',
                    'showSkip': false,
                    'showNext': false,
                    onBeforeStart: function () {
                        var Interval = null;
                        Interval = window.setInterval(function () {
                            if ($('#sink-type').val() === "log") {
                                self.instanceVal.trigger('next');
                                clearInterval(Interval);
                            }
                        }, 1000)
                    }
                },
                {
                    'click #btn-submit': 'Click <b class="lime-text">Submit</b> to submit the Sink configuration form',
                    'showSkip': false,
                    'showNext': false,
                    scrollAnimationSpeed: 500,
                    onBeforeStart: function () {
                        setTimeout(function () {
                            $('#btn-submit').focus();
                        }, 500);
                    }
                },
                {
                    'click .toggle-view-button': 'Let us get back to the Source view. Click <b class="lime-text">' +
                        'Source view</b>',
                    'showSkip': false,
                    'showNext': false
                },
                {
                    'click #File': 'To save the file, click <b class="lime-text">File</b>',
                    'showSkip': false,
                    'showNext': false,
                    'shape': 'rect',
                    'left': 3,
                    'right': 5,
                    'bottom': 7
                },
                {
                    'click #save': 'Then click <b class="lime-text">Save</b>',
                    'showSkip': false,
                    'showNext': false,
                    'left': 3,
                    'right': 3,
                    'bottom': 5,
                    'top': 5
                },
                {
                    'click .event-simulator-activate-btn': 'Now let us run our app using <b class="lime-text">Even Simulator.</b>' +
                        ' To open the Event Simulator, click this icon. ',
                    'showNext': false,
                    'showSkip': false
                },
                {
                    'custom #siddhi-app-name': 'Select the <b class="lime-text">Sweet Factory</b> Siddhi application',
                    'showSkip': false,
                    'showNext': false,
                    onBeforeStart: function () {
                        var Interval = null;
                        Interval = window.setInterval(function () {
                            if ($('#siddhi-app-name').val() === "SweetFactory") {
                                self.instanceVal.trigger('next');
                                clearInterval(Interval);
                            }
                        }, 1000)
                    }
                },
                {
                    'custom #stream-name': 'Then select <b class="lime-text">SweetProductionStream</b>',
                    'showSkip': false,
                    'showNext': false,
                    onBeforeStart: function () {
                        var Interval = null;
                        Interval = window.setInterval(function () {
                            if ($('#stream-name').val() === "SweetProductionStream") {
                                self.instanceVal.trigger('next');
                                clearInterval(Interval);
                            }
                        }, 1000)
                    }
                },
                {
                    'next #attribute-table': 'Enter attribute values as follows.<br>Name :  <b style="color:lime;">Cake</b><br>' +
                        'Amount : <b style="color:lime;">120</b><br>Then click <b class="lime-text">Next.</b>',
                    'showSkip': false,
                    'showNext': true
                },
                {
                    'click #start-and-send': 'To start the Siddhi application and send the test event,' +
                        'click <b class="lime-text">Start and Send</b>',
                    'showSkip': false,
                    'showNext': false
                },
                {
                    'next #console-container': 'You can see the results logged in this console',
                    'showSkip': false,
                    'showNext': true,
                    'shape': 'rect',
                    'top': 100
                },
                {
                    'next #attribute-table': 'Simulate another event by entering attribute values as follows' +
                        '<br>Name :  <b class="lime-text">Toffee</b><br>' +
                        'Amount : <b class="lime-text">350</b><br>Then click <b class="lime-text">Next</b>.',
                    'showSkip': false,
                    'showNext': true
                },
                {
                    'click #start-and-send': 'Click <b class="lime-text">Send</b> to send this event',
                    'showSkip': false,
                    'showNext': false
                },
                {
                    'next #console-container': 'Now the second event is also logged in this console',
                    'showSkip': false,
                    'showNext': true,
                    'shape': 'rect',
                    'top': 100
                },
                {
                    'click #sampleContent': 'Here are some samples for you to try out!',
                    'showSkip': false,
                    'showNext': true,
                    onBeforeStart: function () {
                        var $singleEventConfigList = $("#single-event-configs")
                            .find("div[id^='event-content-parent-']");
                        var $singleEventConfigTabs = $("#single-event-config-tab");
                        var activetingUuid = -1;
                        $singleEventConfigList.each(function () {
                            var $singleEventConfig = $(this);
                            var siddhiAppName =
                                $singleEventConfig.find("select[name='single-event-siddhi-app-name']").val();
                            var simulationConfigId = $singleEventConfig.attr("id").replace("event-content-parent-", "");
                            var eventConfigTab = $singleEventConfigTabs
                                .find("li[data-uuid=\"" + simulationConfigId + "\"]");
                            if ('SweetFactory' == siddhiAppName) {
                                eventConfigTab.remove();
                                $(this).remove();
                            } else {
                                if (!eventConfigTab.hasClass("active")) {
                                    activetingUuid = eventConfigTab.attr("data-uuid");
                                }
                            }
                        });
                        if (activetingUuid != -1) {
                            var eventConfigTab = $singleEventConfigTabs.find("li[data-uuid=\"" + activetingUuid + "\"]");
                            eventConfigTab.addClass("active");
                            var $singleEventConfig = $("#single-event-config-tab-content")
                                .find("div[id='event-content-parent-" + activetingUuid + "']");
                            $singleEventConfig.addClass("active");
                        }

                        var $singleEventConfigListTemp = $("#single-event-configs")
                            .find("div[id^='event-content-parent-']");
                        if ($singleEventConfigListTemp.size() == 0) {
                            app.commandManager.dispatch("add-single-simulator");
                        }
                        var workspaceServiceURL = app.config.services.workspace.endpoint;
                        var activeTab = app.tabController.activeTab;
                        $.ajax({
                            url: workspaceServiceURL + "/delete?siddhiAppName=SweetFactory.siddhi",
                            type: "DELETE",
                            contentType: "text/plain; charset=utf-8",
                            async: false,
                            success: function (data, textStatus, xhr) {
                                app.tabController.removeTab(activeTab, undefined, true);
                                log.debug('file deleted successfully');
                                app.commandManager.dispatch("open-folder", data.path);
                                app.eventSimulator.getFeedSimulator().updateFeedCreationButtonAndNotification();
                                app.commandManager.dispatch("remove-siddhi-apps-on-delete", 'SweetFactory');
                            }
                        });
                    }
                },
                {
                    'click .more-samples': 'Click here to view the full sample list',
                    'showSkip': false,
                    'showFalse': false,
                    'shape': 'rect',
                    'right': 500
                },
                {
                    'next #sampleDialog' : 'You can try out more samples from here',
                    'showSkip' : false,
                    'nextButton' : {className: "myNext", text: "Finish"},
                    scrollAnimationSpeed : 900,
                    'bottom' : 150
                }
            ];

            //Script array for the simulation guide
            this.simulateProject = [
                {
                    'click .event-simulator-activate-btn': 'Now let us run our app using <b class="lime-text">Even Simulator.</b>' +
                        ' To open the Event Simulator, click this icon.',
                    'showNext': false,
                    'showSkip': false,
                    onBeforeStart: function () {
                        $.ajax({
                            url: saveServiceURL,
                            type: "POST",
                            data: payload,
                            contentType: "text/plain; charset=utf-8",
                            async: false,
                            success: function (data, textStatus, xhr) {
                                    app.commandManager.dispatch("open-folder", data.path);
                                    if (!app.workspaceExplorer.isActive()) {
                                        app.commandManager.dispatch("toggle-file-explorer");
                                        app.commandManager.dispatch('remove-unwanted-streams-single-simulation',
                                            'SweetFactory');
                                    }
                                    app.workspaceManager.updateMenuItems();
                                    log.debug('Simulation file saved successfully');

                            },
                            error: function () {
                                alert('Error saving the file');
                            }
                        });
                    }
                },
                {
                    'custom #siddhi-app-name': 'Select the <b class="lime-text">Sweet Factory</b> Siddhi application',
                    'showSkip': false,
                    'showNext': false,
                    onBeforeStart: function () {
                        var Interval = null;
                        Interval = window.setInterval(function () {
                            if ($('#siddhi-app-name').val() === "SweetFactory") {
                                self.instanceVal.trigger('next');
                                clearInterval(Interval);
                            }
                        }, 1000)
                    }
                },
                {
                    'custom #stream-name': 'Then select <b class="lime-text">SweetProductionStream</b>',
                    'showSkip': false,
                    'showNext': false,
                    onBeforeStart: function () {
                        //scriptStep =  self.instanceVal.getCurrentStep();
                        var Interval = null;
                        Interval = window.setInterval(function () {
                            if ($('#stream-name').val() === "SweetProductionStream") {
                                self.instanceVal.trigger('next');
                                clearInterval(Interval);
                            }
                        }, 1000)
                    }
                },
                {
                    'next #attribute-table': 'Enter attribute values as follows.<br>Name :  <b style="color:lime;">Cake</b><br>' +
                        'Amount : <b style="color:lime;">120</b><br>Then click <b class="lime-text">Next.</b>',
                    'showSkip': false,
                    'showNext': true,
                    onBeforeStart: function () {
                        //scriptStep =  self.instanceVal.getCurrentStep();
                    }
                },
                {
                    'click #start-and-send': 'To start the Siddhi application and send the test event,' +
                        'click <b class="lime-text">Start and Send</b>',
                    'showSkip': false,
                    'showNext': false,
                    onBeforeStart: function () {
                        //scriptStep =  self.instanceVal.getCurrentStep();
                    }
                },
                {
                    'next #console-container': 'You can see the results logged in this console',
                    'showSkip': false,
                    'showNext': true,
                    'shape': 'rect',
                    'top': 100,
                    onBeforeStart: function () {
                        //scriptStep =  self.instanceVal.getCurrentStep();
                    }
                },
                {
                    'next #attribute-table': 'Simulate another event by entering attribute values as follows' +
                        '<br>Name :  <b class="lime-text">Toffee</b><br>' +
                        'Amount : <b class="lime-text">350</b><br>Then click <b class="lime-text">Next</b>.',
                    'showSkip': false,
                    'showNext': true,
                    onBeforeStart: function () {
                        // scriptStep =  self.instanceVal.getCurrentStep();
                    }
                },
                {
                    'click #start-and-send': 'Click <b class="lime-text">Send</b> to send this event',
                    'showSkip': false,
                    'showNext': false,
                    onBeforeStart: function () {
                        //scriptStep =  self.instanceVal.getCurrentStep();
                    }
                },
                {
                    'next #console-container': 'Now the second event is also logged in this console',
                    'showSkip': false,
                    'showNext': true,
                    'shape': 'rect',
                    'top': 100
                },
                {
                    'next #sampleContent': 'Here are some samples for you to try out!',
                    'showSkip': false,
                    'showNext': false,
                    'shape': 'rect',
                    'top': 10,
                    onBeforeStart: function () {
                        var $singleEventConfigList = $("#single-event-configs")
                            .find("div[id^='event-content-parent-']");
                        var $singleEventConfigTabs = $("#single-event-config-tab");
                        var activetingUuid = -1;
                        $singleEventConfigList.each(function () {
                            var $singleEventConfig = $(this);
                            var siddhiAppName =
                                $singleEventConfig.find("select[name='single-event-siddhi-app-name']").val();
                            var simulationConfigId = $singleEventConfig.attr("id").replace("event-content-parent-", "");
                            var eventConfigTab = $singleEventConfigTabs
                                .find("li[data-uuid=\"" + simulationConfigId + "\"]");
                            if ('SweetFactory' == siddhiAppName) {
                                eventConfigTab.remove();
                                $(this).remove();
                            } else {
                                if (!eventConfigTab.hasClass("active")) {
                                    activetingUuid = eventConfigTab.attr("data-uuid");
                                }
                            }
                        });
                        if (activetingUuid != -1) {
                            var eventConfigTab = $singleEventConfigTabs.find("li[data-uuid=\"" + activetingUuid + "\"]");
                            eventConfigTab.addClass("active");
                            var $singleEventConfig = $("#single-event-config-tab-content")
                                .find("div[id='event-content-parent-" + activetingUuid + "']");
                            $singleEventConfig.addClass("active");
                        }

                        var $singleEventConfigListTemp = $("#single-event-configs")
                            .find("div[id^='event-content-parent-']");
                        if ($singleEventConfigListTemp.size() == 0) {
                            app.commandManager.dispatch("add-single-simulator");
                        }
                        var workspaceServiceURL = app.config.services.workspace.endpoint;
                        var activeTab = app.tabController.activeTab;
                        $.ajax({
                            url: workspaceServiceURL + "/delete?siddhiAppName=SweetFactory.siddhi",
                            type: "DELETE",
                            contentType: "text/plain; charset=utf-8",
                            async: false,
                            success: function (data, textStatus, xhr) {
                                    app.tabController.removeTab(activeTab, undefined, true);
                                    log.debug('file deleted successfully');
                                    app.commandManager.dispatch("open-folder", data.path);
                                    app.eventSimulator.getFeedSimulator().updateFeedCreationButtonAndNotification();
                                    app.commandManager.dispatch("remove-siddhi-apps-on-delete", 'SweetFactory');
                            }
                        });
                    }
                }
            ];
        };

        //Constructor for the guide
        Guide.prototype.constructor = Guide;

        //render function for the complete guide
        Guide.prototype.renderFull = function () {

            var _tabList = this.tabList;
            var _createNewProjectScript = this.createNewProject;
            var _hintInstance = new EnjoyHint({});
            _hintInstance.setScript(_createNewProjectScript);

            _.each(_tabList, function (tab) {
                if (_tabList.length === 1 && tab._title === "welcome-page") {
                    _hintInstance.runScript();
                } else {
                    DesignViewUtils.prototype.errorAlert("Please close all tabs except welcome-page to start the guide");
                }
            });

            this.instanceVal = _hintInstance;
            this.tabLength = _tabList.length;

            return _hintInstance;
        };

        //render function for the simulate guide
        Guide.prototype.renderSimulate = function () {

            var _tabList = this.tabList;
            var _simulateProject = this.simulateProject;

            var _hintInstance = new EnjoyHint({});
            _hintInstance.setScript(_simulateProject);
            _hintInstance.runScript();

            this.instanceVal = _hintInstance;
            this.tabLength = _tabList.length;

            return _hintInstance;
        };

        return Guide;
    });