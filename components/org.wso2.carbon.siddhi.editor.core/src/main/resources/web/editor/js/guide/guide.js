/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['jquery', 'lodash', 'log', 'enjoyhint', 'designViewUtils', 'workspace', 'guideConstants'],
    function ($, _, log, EnjoyHintLib, DesignViewUtils, Workspace, Constants) {
        //Enjoy Hint instance to be used in the methods of this class
        var instance = null;
        var currentStep = null;
        /**
         * Arg: application instance
         */
        var Guide = function (application) {
            var self = this;
            this.app = application;
            this.tabList = this.app.tabController.getTabList();
            var browserStorage = this.app.browserStorage;
            var tempFile = 'SweetFactory__';
            var fileIncrement = 1;

            //URLs for AJAX requests
            self.workspaceServiceURL = this.app.config.services.workspace.endpoint;
            var checkFileURL = self.workspaceServiceURL + "/exists/workspace";
            var saveServiceURL = self.workspaceServiceURL + "/write";

            //Dialog box to choose tour options
            self.guideDialog = $('#guideDialog');

            //Script array for the complete guide
            this.completeGuide = [
                {
                    'click #newButton': '<b>Welcome to Siddhi Editor!</b> Click <b class="lime-text">New</b> to get started.',
                    'showNext' : false
                },
                {
                    'next .ace_line': 'First, We have defined an input stream for you!. <b class="lime-text">' +
                        'A stream</b> is a logical series of events ordered in time with a<br> uniquely identifiable ' +
                        'name, and set of defined attributes with specific data types defining its schema. ' +
                        '<br>Click <b class="lime-text">Next</b> to continue.',
                    'margin': 200,
                    'showSkip': false,
                    onBeforeStart: function () {
                        var activeTab = self.app.tabController.getActiveTab();
                        var editor = activeTab.getSiddhiFileEditor().getSourceView().getEditor();
                        var aceEditor = self.app.tabController.getActiveTab().getSiddhiFileEditor().getSourceView().getEditor();
                        editor.session.insert(aceEditor.getCursorPosition(), Constants.INSERT_STRING);
                        currentStep = null;
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
                    'keyCode': Constants.TAB_KEYCODE,
                    'selector': '#saveName',
                    'event': 'key',
                    'description': 'Your file name is this. Press <b class="lime-text">Tab </b>key to continue.',
                    'showNext': false,
                    'showSkip': false,
                    'timeout': 200,
                    onBeforeStart: function () {
                        fileIncrement = browserStorage.get('guideFileNameIncrement');
                        tempFile = "SweetFactory__" + fileIncrement;
                        var fileToBeChecked = "configName=" + self.app.utils.base64EncodeUnicode(tempFile + '.siddhi');
                        $.ajax({
                            url: checkFileURL,
                            type: "POST",
                            contentType: "text/plain; charset=utf-8",
                            data: fileToBeChecked,
                            async: false,
                            success: function (response) {
                                if(response.exists){
                                    tempFile = tempFile.slice(0, 14);
                                    fileIncrement++;
                                    tempFile = tempFile + fileIncrement;
                                }
                            },
                            error: function () {
                                DesignViewUtils.prototype.warnAlert("Server error has occurred");
                            }
                        });
                        setTimeout(function () {
                            $('#saveConfigModal').find('#configName').val(tempFile).focus();
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
                    'showNext': false,
                    onBeforeStart: function () {
                        tempFile = "SweetFactory__" + fileIncrement;
                        fileIncrement++;
                        browserStorage.put("guideFileNameIncrement", fileIncrement);
                    }
                },
                {
                    'next #stream': 'This is a <b class="lime-text">Stream </b>component. you need to use this to ' +
                        'generate the output. Click <b class="lime-text">Next</b>.',
                    'showSkip': false,
                    'showNext': true,
                    onBeforeStart: function () {
                        if(self.app.workspaceExplorer.isActive()){
                            self.app.commandManager.dispatch("toggle-file-explorer");
                        }
                        $('#stream').removeClass('stream-drag');
                        currentStep = instance.getCurrentStep();

                    }
                },
                {
                    'custom .design-view-container': 'Drag a <b class="lime-text">Stream </b>and drop it on the design view',
                    'showSkip': false,
                    'showNext': false,
                    'shape': 'rect',
                    'bottom': 300,
                    onBeforeStart: function () {
                        $('#tool-group-Collections').find('.tool-group-body').css('display', 'none');
                        $('#tool-group-Queries').find('.tool-group-body').css('display', 'none');
                        $('#tool-group-Functions').find('.tool-group-body').css('display', 'none');
                        $("#tool-group-I\\/O").find('.tool-group-body').css('display', 'none');
                        $('#trigger').removeClass('trigger-drag');
                        $('#partition').removeClass('partition-drag');
                        $('#stream').addClass('stream-drag');
                        currentStep = instance.getCurrentStep();
                        browserStorage.put("isWidgetFromTourGuide", true);
                        setTimeout(function () {
                            var interval = null;
                            interval = window.setInterval(function () {
                                var newElement = self.app.tabController.getActiveTab().getSiddhiFileEditor().getDesignView()
                                    .getConfigurationData().getSiddhiAppConfig();
                                if (newElement.streamList.length === 2) {
                                    instance.trigger('next');
                                    clearInterval(interval);
                                }
                            }, 3000);
                        }, 3000)
                    }
                },
                {
                    'click .incomplete-element': '<b class="lime-text">Move the cursor over the dropped component</b> and ' +
                        'click<b class="lime-text"> settings </b> icon to open the <b class="lime-text">Stream Configuration</b> form.',
                    'showSkip': false,
                    'showNext': false,
                    'shape': "rect",
                    'margin': 45,
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
                    'next #streamName': 'We have set the stream name as <b class="lime-text"> TotalProductionStream.</b> ' +
                        'Press<b class="lime-text"> Next</b> to continue.',
                    'showSkip': false,
                    onBeforeStart: function () {
                        $('.fw-delete').addClass('fw-delete');
                        $("#streamName").val("TotalProductionStream").focus();
                    }
                },
                {
                    'next .attr-name': 'We have set the attribute name as <b class="lime-text"> TotalProduction</b>. ' +
                        'Click<b class="lime-text"> Next</b> to continue',
                    'showSkip': false,
                    'showNext': true,
                    onBeforeStart: function () {
                        $(".attr-name").val("TotalProduction").focus();
                    }
                },
                {
                    'next .attr-type': 'We have set the attribute type as <b class="lime-text"> ' +
                        'long</b>. Click<b class="lime-text"> Next</b> to continue.',
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
                    'scrollAnimationSpeed': 500,
                    onBeforeStart: function () {
                        setTimeout(function () {
                            $('#btn-submit').focus();
                        }, 500);
                    }
                },
                {
                    'next #projection-query': 'This is a <b class="lime-text">Projection component</b> that allows' +
                        ' you to select some or all of the attributes from the input stream (Sweet Production Stream) <br>' +
                        'to be inserted into the output stream (Total Production Stream). Click <b class="lime-text"> Next</b> ',
                    'showSkip': false,
                    'showNext': true,
                    onBeforeStart: function () {
                        $('#projection-query').removeClass('projection-query-drag');
                        currentStep = instance.getCurrentStep();
                    }
                },
                {
                    'custom .design-view-container': 'Drag a <b class="lime-text">Projection component </b> and place' +
                        ' it between your input (Sweet Production Stream) and output stream (Total Production Stream) components.',
                    'showSkip': false,
                    'showNext': false,
                    'shape': 'rect',
                    'bottom': 300,
                    onBeforeStart: function () {
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
                        currentStep = instance.getCurrentStep();
                        setTimeout(function () {
                            var interval = null;
                            interval = window.setInterval(function () {
                                var newElement = self.app.tabController.getActiveTab().getSiddhiFileEditor().getDesignView()
                                    .getConfigurationData().getSiddhiAppConfig();
                                if (newElement.queryLists.WINDOW_FILTER_PROJECTION.length === 1) {
                                    instance.trigger('next');
                                    clearInterval(interval);
                                }
                            }, 1000);
                        }, 3000)
                    }
                },
                {
                    'custom .design-view-container': 'Connect the input stream (Sweet Production Stream) to the projection component, ' +
                        'Then connect the Projection component to the output stream (Total Production Stream) using nodes.<br> To do this you can ' +
                        '<b class="lime-text">draw a connecting arrow by dragging the cursor from one node to another</b>',
                    'showSkip': false,
                    'showNext': false,
                    'shape': 'rect',
                    'bottom': 300,
                    onBeforeStart: function () {
                        $('#filter-query').addClass('filter-query-drag');
                        $('#window-query').addClass('window-query-drag');
                        $('#pattern-query').addClass('pattern-query-drag');
                        $('#sequence-query').addClass('sequence-query-drag');
                        $("#tool-group-Queries").find('.tool-group-body').css('display', 'none');
                        setTimeout(function () {
                            var interval = null;
                            var newElement = self.app.tabController.getActiveTab().getSiddhiFileEditor().getDesignView()
                                .getConfigurationData().getSiddhiAppConfig().queryLists.WINDOW_FILTER_PROJECTION[0];
                            interval = window.setInterval(function () {
                                if(newElement.queryInput && newElement.queryOutput &&
                                    newElement.queryInput.from === "SweetProductionStream" &&
                                    newElement.queryOutput.target === "TotalProductionStream") {
                                    clearInterval(interval);
                                    instance.trigger('next');
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
                    'margin': 45,
                    onBeforeStart: function () {
                        $('#tool-group-Collections').find('.tool-group-body').css('display', 'block');
                        $("div[id='tool-group-Flow Constructs']").find('.tool-group-body').css('display', 'block');
                        $('#tool-group-Functions').find('.tool-group-body').css('display', 'block');
                        $("#tool-group-I\\/O").find('.tool-group-body').css('display', 'block');
                        $('.fw-delete').removeClass('fw-delete');
                    }
                },
                {
                    'next .query-name': 'We have changed the query name to <b class="lime-text">SweetTotalQuery</b> click Next',
                    'showSkip': false,
                    onBeforeStart: function () {
                        $('.query-name').val('SweetTotalQuery').focus();
                        $('.define-user-defined-attributes').show();
                        $('.attribute-selection-type').val('user_defined');
                    }
                },
                {
                    'next .attribute-expression': 'We have set the expression as <b class="lime-text">count()</b> click Next',
                    'showSkip': false,
                    'shape': 'rect',
                    onBeforeStart: function () {
                        $('.attribute-expression').val('count()');
                    }
                },
                {
                    'click #btn-submit': 'Click <b class="lime-text">Submit</b> to submit the query configuration',
                    'showSkip': false,
                    'showNext': false,
                    'scrollAnimationSpeed': 500,
                    onBeforeStart: function () {
                        setTimeout(function () {
                            $('#btn-submit').focus();
                        }, 500);
                    }
                },
                {
                    'next #sink': 'This is a <b class="lime-text">Sink component</b>.<br>Sink is a contract that takes ' +
                        'events arriving at a stream, maps them to a predefined data format,<br> and publishes them' +
                        ' to external endpoints. You are using this to <b class="lime-text">display the output result.</b>' +
                        ' Click <b class="lime-text">Next</b>',
                    'showSkip': false,
                    onBeforeStart: function () {
                        $('#sink').removeClass('sink-drag');
                        currentStep = instance.getCurrentStep();
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
                        $('#sink').addClass('sink-drag');
                        currentStep = instance.getCurrentStep();
                        setTimeout(function () {
                            var interval = null;
                            interval = window.setInterval(function () {
                                var newElement = self.app.tabController.getActiveTab().getSiddhiFileEditor().getDesignView()
                                    .getConfigurationData().getSiddhiAppConfig();
                                if (newElement.sinkList.length === 1) {
                                    instance.trigger('next');
                                    clearInterval(interval);
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
                        setTimeout(function () {
                            var interval = null;
                            var newElement = self.app.tabController.getActiveTab().getSiddhiFileEditor().getDesignView()
                                .getConfigurationData().getSiddhiAppConfig().sinkList[0];
                            interval = window.setInterval(function () {
                                if (newElement.connectedElementName === "TotalProductionStream") {
                                    clearInterval(interval);
                                    instance.trigger('next');
                                }
                            }, 1000);
                        }, 3000);
                    }
                },
                {
                    'click .sinkDrop': 'Move the mouse over the dropped component and click <b class="lime-text">settings</b> ' +
                        'icon to open the <b class="lime-text">Sink configuration</b>',
                    'showSkip': false,
                    'showNext': false,
                    'shape': "rect",
                    'margin': 45,
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
                        var interval = null;
                        interval = window.setInterval(function () {
                            if ($('#sink-type').val() === "log") {
                                instance.trigger('next');
                                clearInterval(interval);
                            }
                        }, 1000)
                    }
                },
                {
                    'click #btn-submit': 'Click <b class="lime-text">Submit</b> to submit the Sink configuration form',
                    'showSkip': false,
                    'showNext': false,
                    'scrollAnimationSpeed': 500,
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
                    'custom #siddhi-app-name': 'Select the latest <b class="lime-text">' + tempFile + '</b> Siddhi application',
                    'showSkip': false,
                    'showNext': false,
                    onBeforeStart: function () {
                        var interval = null;
                        interval = window.setInterval(function () {
                            if ($('#siddhi-app-name').val() === tempFile) {
                                instance.trigger('next');
                                clearInterval(interval);
                            }
                        }, 1000)
                        browserStorage.put("isWidgetFromTourGuide", false);
                    }
                },
                {
                    'custom #stream-name': 'Then select <b class="lime-text">SweetProductionStream</b>',
                    'showSkip': false,
                    'showNext': false,
                    onBeforeStart: function () {
                        var interval = null;
                        interval = window.setInterval(function () {
                            if ($('#stream-name').val() === "SweetProductionStream") {
                                instance.trigger('next');
                                clearInterval(interval);
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
                    'shape': 'rect'
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
                    'shape': 'rect'
                },
                {
                    'click #welcome-page': 'Click here to visit welcome page to view examples',
                    'showNext' : false,
                    'showSkip' : false
                },
                {
                    'click #sampleContent': 'Here are some samples for you to try out!',
                    'showSkip': false,
                    'showNext': true
                },
                {
                    'click .more-samples': 'Click here to view the full sample list',
                    'showSkip': false,
                    'showFalse': false,
                    'shape': 'rect',
                    'right': 500,
                    onBeforeStart: function () {
                        self.app.commandManager.dispatch("toggle-output-console");
                    }
                },
                {
                    'next #sampleDialog': 'You can try out more samples from here. click <b class="lime-text">Next</b>',
                    'showSkip': false,
                    'scrollAnimationSpeed': 500,
                    'bottom': 150
                },
                {
                    'click #DataPreprocessing' : '<b class="lime-text">Click</b> here to try a sample which ' +
                        '<b class="lime-text">collect data via TCP transport and preprocess.</b>',
                    'showNext' : false,
                    'showSkip' : false,
                    onBeforeStart: function () {
                        $('#DataPreprocessing').focus();
                    }
                }
            ];

            //Script array for the simulation guide
            this.simulateGuide = [
                {
                    'click .event-simulator-activate-btn': 'Now let us run our app using <b class="lime-text">Even Simulator.</b>' +
                        ' To open the Event Simulator, click this icon.',
                    'showNext': false,
                    'showSkip': false,
                    onBeforeStart: function () {
                        fileIncrement = browserStorage.get('guideFileNameIncrement');
                        tempFile = "SweetFactory__" + fileIncrement;
                        var encodedContent = self.app.utils.base64EncodeUnicode(Constants.CONTENT);
                        var payload = "configName=" + self.app.utils.base64EncodeUnicode(tempFile + '.siddhi') +
                            "&config=" + encodedContent;
                        var fileToBeChecked = "configName="+self.app.utils.base64EncodeUnicode(tempFile + '.siddhi');
                        $.ajax({
                            url: checkFileURL,
                            type: "POST",
                            contentType: "text/plain; charset=utf-8",
                            data: fileToBeChecked,
                            async: false,
                            success: function (response) {
                                if(response.exists){
                                    tempFile = tempFile.slice(0, 14);
                                    fileIncrement++;
                                    tempFile = tempFile + fileIncrement;
                                    payload = "configName=" + self.app.utils.base64EncodeUnicode(tempFile + '.siddhi')
                                        + "&config=" + encodedContent;
                                }
                            }
                        });
                        $.ajax({
                            url: saveServiceURL,
                            type: "POST",
                            data: payload,
                            contentType: "text/plain; charset=utf-8",
                            async: false,
                            success: function (data) {
                                self.app.commandManager.dispatch("open-folder", data.path);
                                self.app.workspaceManager.updateMenuItems();
                                self.app.commandManager.dispatch('remove-unwanted-streams-single-simulation', tempFile);
                                log.debug('Simulation file saved successfully');
                            },
                            error: function () {
                                DesignViewUtils.prototype.warnAlert("Server error has occurred");
                            }
                        });
                        fileIncrement++;
                        browserStorage.put('guideFileNameIncrement', fileIncrement)
                    }
                },
                {
                    'custom #siddhi-app-name': 'We have created a simulation siddhi application for you.' +
                        ' Select the latest<b class="lime-text"> ' + tempFile + '</b> siddhi application',
                    'showSkip': false,
                    'showNext': false,
                    onBeforeStart: function () {
                        var interval = null;
                        interval = window.setInterval(function () {
                            if ($('#siddhi-app-name').val() === tempFile) {
                                instance.trigger('next');
                                clearInterval(interval);
                            }
                        }, 1000)
                    }
                },
                {
                    'custom #stream-name': 'Then select <b class="lime-text">SweetProductionStream</b>',
                    'showSkip': false,
                    'showNext': false,
                    onBeforeStart: function () {
                        var interval = null;
                        interval = window.setInterval(function () {
                            if ($('#stream-name').val() === "SweetProductionStream") {
                                instance.trigger('next');
                                clearInterval(interval);
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
                    'shape': 'rect'
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
                    'shape': 'rect'
                },
                {
                    'click #welcome-page': 'Click here to visit welcome page to view examples',
                    'showSkip': false,
                    'showNext': false,
                    'shape': 'rect',
                    'top': 10
                },
                {
                    'click #sampleContent': 'Here are some samples for you to try out!',
                    'showSkip': false,
                    'showNext': true
                },
                {
                    'click .more-samples': 'Click here to view the full sample list',
                    'showSkip': false,
                    'showFalse': false,
                    'shape': 'rect',
                    'right': 500,
                    onBeforeStart: function () {
                        self.app.commandManager.dispatch("toggle-output-console");
                    }
                },
                {
                    'next #sampleDialog' : 'You can try out more samples from here. click <b class="lime-text">Next</b>',
                    'showSkip' : false,
                    'scrollAnimationSpeed' : 500,
                    'bottom' : 150
                },
                {
                    'click #DataPreprocessing' : '<b class="lime-text">Click</b> here to try a sample which ' +
                        '<b class="lime-text">collect data via TCP transport and preprocess.</b>',
                    'showNext' : false,
                    'showSkip' : false,
                    onBeforeStart: function () {
                        $('#DataPreprocessing').focus();
                    }
                }
            ];

            //Script array for the sample guide
            this.sampleGuide = [
                {
                    'click #sampleContent': 'Here are some samples for you to try out!',
                    'showSkip': false,
                    'showNext': true
                },
                {
                    'click .more-samples': 'Click here to view the full sample list',
                    'showSkip': false,
                    'showFalse': false,
                    'shape': 'rect',
                    'right': 500,
                    onBeforeStart: function () {
                        if(self.app.outputController.isActive()){
                            self.app.commandManager.dispatch("toggle-output-console");
                        }

                    }
                },
                {
                    'next #sampleDialog' : 'You can try out more samples from here. click <b class="lime-text">Next</b>',
                    'showSkip' : false,
                    'scrollAnimationSpeed' : 500,
                    'bottom' : 150
                },
                {
                    'click #DataPreprocessing' : '<b class="lime-text">Click</b> here to try a sample which ' +
                        '<b class="lime-text">collect data via TCP transport and preprocess.</b>',
                    'showNext' : false,
                    'showSkip' : false,
                    onBeforeStart: function () {
                        $('#DataPreprocessing').focus();
                    }
                }
            ];
        };

        //Constructor for the guide
        Guide.prototype.constructor = Guide;

        //function to view the dialog box to choose the guide option
        Guide.prototype.showModel =  function(){
            this.guideDialog.modal('show').on('shown.bs.modal', function(){
                $('#guideDialog').trigger('loaded');
            });
            this.guideDialog.on('hidden.bs.modal', function(){
                $('#guideDialog').trigger('unloaded');
            })
        };

        //start function for the complete guide
        Guide.prototype.startGuide = function () {
            var self = this;
            var guideModal = self.guideDialog.filter("#guideDialog");
            var callBack = function() { guideModal.modal('hide') };
            //check whether there are multiple tabs and if the current tab is "welcome-page"
            _.each(self.tabList, function (tab) {
                if (self.tabList.length === 1 && tab._title === "welcome-page") {
                    self.showModel();
                } else {
                    DesignViewUtils.prototype.errorAlert(Constants.ERROR_TEXT);
                }
            });
            //function for the complete tour
            guideModal.find("button").filter("#fullGuide").unbind().click(function() {
                //unbinding the click events from the previous click
                instance = new EnjoyHint({});
                $('#fullGuide').off('click');
                $('#simulationGuide').off('click');
                $('#sampleGuide').off('click');
                instance.setScript(self.completeGuide);
                instance.runScript();
                callBack();
                guideCloseCallback();
            });
            //function for the simulation tour
            guideModal.find("button").filter("#simulationGuide").unbind().click(function() {
                //unbinding the click events from the previous click
                instance = new EnjoyHint({});
                $('#fullGuide').off('click');
                $('#simulationGuide').off('click');
                $('#sampleGuide').off('click');
                instance.setScript(self.simulateGuide);
                instance.runScript();
                callBack();
                guideCloseCallback();
            });
            //function for the sample tour
            guideModal.find("button").filter("#sampleGuide").unbind().click(function() {
                //unbinding the click events from the previous click
                instance = new EnjoyHint({});
                $('#fullGuide').off('click');
                $('#simulationGuide').off('click');
                $('#sampleGuide').off('click');
                instance.setScript(self.sampleGuide);
                instance.runScript();
                callBack();
                guideCloseCallback();
            });

            var guideCloseCallback = function () {
                $('.enjoyhint_close_btn').click(function () {
                    switch (currentStep) {
                        case 7  :
                            $('#stream').addClass('stream-drag');
                            break;
                        case 8  :
                            $('#trigger').addClass('trigger-drag');
                            $('#partition').addClass('partition-drag');
                            break;
                        case 14 :
                            $('#projection-query').addClass('projection-query-drag');
                            break;
                        case 15 :
                            $('#filter-query').addClass('filter-query-drag');
                            $('#window-query').addClass('window-query-drag');
                            $('#pattern-query').addClass('pattern-query-drag');
                            $('#sequence-query').addClass('sequence-query-drag');
                            break;
                        case 21 :
                            $('#sink').addClass('sink-drag');
                            break;
                        case 22 :
                            $('#source').addClass('source-drag ');
                            break;
                        default :
                            break;
                    }
                    instance = null;
                });
            }
        };

        return Guide;
    });
