/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['app/main', 'jquery'],
    function (Application, $) {

    var app, config;
    config = {
        baseUrl: window.location.protocol + "//" + window.location.host + '/editor',
        baseUrlHost: window.location.host,
        container: "#page-content",
        welcome: {
            container: "#welcome-container",
            cssClass: {
                parent: "initial-background-container",
                outer: "initial-welcome-container",
                leftPane: "left-pane",
                buttonWrap: "btn-wrap",
                productNameWrap: "productname-wrap",
                contentPane: "content-pane",
                recentFilesPane: "recent-file-pane",
                samplesPane: "samples-pane",
                quickLinksPane: "quick-links-pane",
                buttonNew: " btn btn-block new-welcome-button",
                buttonOpen: " btn btn-block open-welcome-button"
            },
            samples: [
                "artifacts/EventCollection/ReceiveThroughHttp.siddhi",
                "artifacts/EventCleansing/SimpleFilter.siddhi",
                "artifacts/DataTransformation/StringTransformation.siddhi",
                "artifacts/DataSummarization/TimeWindow.siddhi",
                "artifacts/EventStoreIntegration/RDBMSIntegration.siddhi",
                "artifacts/EventCollection/ReceiveKafkaInAvroFormat.siddhi",
                "artifacts/MachineLearning/StreamingRegressor/StreamingRegressor.siddhi",
                "artifacts/Pattern&Trends/SimpleSequence.siddhi",
                "artifacts/DataSummarization/NamedAggregation/NamedAggregation.siddhi"]
        },
        stream_predefined_annotations: [
            {
                predefined: true,
                name: 'Async',
                class: 'optional-annotation',
                isMandatory: false,
                elements: [
                    {
                        name: 'buffer.size',
                        defaultValue: '256',
                        class: 'mandatory-key',
                        isMandatory: true
                    },
                    {
                        name: 'workers',
                        defaultValue: '2',
                        class: 'mandatory-key',
                        isMandatory: true
                    },
                    {
                        name: 'batch.size.max',
                        defaultValue: '5',
                        class: 'mandatory-key',
                        isMandatory: true
                    },
                ]
            }
        ],
        type_table_predefined_annotations: [
            {
                name: "primaryKey",
                optional: true
            },
            {
                name: "index",
                optional: true
            }
        ],
        type_aggregation_predefined_annotations: [
            {
                name: "purge",
                optional: true,
                parameters: [
                    {
                        name: "enable",
                        defaultValue: "true",
                        description: "Data purging can be enabled or disabled using this option.",
                        optional: true,
                        type: ["BOOL"]
                    },
                    {
                        name: "interval",
                        defaultValue: "10 sec",
                        description: "The interval to purge data can be mentioned using this option.",
                        optional: true,
                        type: ["INT", "LONG", "TIME"]
                    }
                ],
                annotations: [{
                    name: "retentionPeriod",
                    optional: true,
                    parameters: [
                        {
                            name: "sec",
                            defaultValue: "120 sec",
                            description: "The amount of data to be kept in the database in seconds.",
                            optional: true,
                            type: ["INT", "LONG", "TIME"]
                        },
                        {
                            name: "min",
                            defaultValue: "24 hours",
                            description: "The amount of data to be kept in the database in minutes.",
                            optional: true,
                            type: ["INT", "LONG", "TIME"]
                        },
                        {
                            name: "hours",
                            defaultValue: "30 days",
                            description: "The amount of data to be kept in the database in hours.",
                            optional: true,
                            type: ["INT", "LONG", "TIME"]
                        },
                        {
                            name: "days",
                            defaultValue: "1 year",
                            description: "The amount of data to be kept in the database in days.",
                            optional: true,
                            type: ["INT", "LONG", "TIME"]
                        },
                        {
                            name: "months",
                            defaultValue: "all",
                            description: "The amount of data to be kept in the database in months.",
                            optional: true,
                            type: ["INT", "LONG", "TIME"]
                        },
                        {
                            name: "years",
                            defaultValue: "all",
                            description: "The amount of data to be kept in the database in years.",
                            optional: true,
                            type: ["INT", "LONG", "TIME"]
                        }
                    ]
                }
                ]
            },
            {
                name: "partitionById",
                optional: true,
                parameters: [
                    {
                        name: "enable",
                        defaultValue: "true",
                        description: "The distributed aggregation can be enabled or disabled using this option.",
                        optional: true,
                        type: ["BOOL"]
                    }
                ]
            },
            {
                name: "index",
                optional: true
            }
        ],
        type_query_predefined_annotations: [
            {
                name: "dist",
                optional: true,
                parameters: [
                    {
                        name: "execGroup",
                        defaultValue: "group-1",
                        description: "The name of the execution group. Elements with the same execution group are " +
                        "executed in the same siddhi application. Elements with different execution group are executed " +
                        "in separate siddhi Application per execution group",
                        optional: true,
                        type: ["STRING"]
                    },
                    {
                        name: "parallel",
                        defaultValue: "1",
                        description: "The number of instances in which the execution element must be executed in parallel." +
                        "If there is a mismatch in the parallel instances specified for an execution group, an exception occurs.",
                        optional: true,
                        type: ["INT"]

                    }
                ]

            }
        ],
        query_output_options: [
            {
                name: "limit",
                description: "When events are emitted as a batch, limit allows to limit the number\n" +
                " of events in the batch from the defined offset."
            },
            {
                name: "offset",
                description: "When events are emitted as a batch, offset allows to offset beginning\n" +
                " of the output event batch."
            },
            {
                name: "rate-limiting",
                description: "It allows queries to output events periodically based on a specified\n" +
                "condition."
            }
        ],
        query_operators: ["==", ">=", ">", "<=", "<", "AND", "OR", "NOT", "IN", "IS NULL"],
        output_rate_limit_keywords: ["snapshot", "every", "within", "events", "ALL", "LAST", "FIRST"],
        logic_statement_keywords: ["for", "every", "within"],
        join_types: ["join", "left_outer_join", "right_outer_join", "full_outer_join"],
        stream_handler_types: ["filter", "function", "window"],
        query_output_operations: ["insert", "delete", "update", "update_or_insert_into"],
        stream_handler_types_without_window: ["filter", "function"],
        trigger: [
            {
                name: "start",
                description: "An event is triggered when Siddhi is started.",
                defaultValue: ""
            },
            {
                name: "cron-expression",
                description: "An event is triggered periodically based on the given cron expression",
                defaultValue: "0 15 10 ? * MON-FRI"
            },
            {
                name: "every",
                description: "An event is triggered periodically at the given time interval",
                defaultValue: "5 min"
            }
        ],
        rdbms_types: [
            {
                name: "inline-config",
                parameters: [
                    {
                        name: "jdbc.url",
                        optional: false,
                        description: "The JDBC URL via which the RDBMS data store is accessed.",
                        defaultValue: ""
                    },
                    {
                        name: "username",
                        optional: false,
                        description: "The username to be used to access the RDBMS data store.",
                        defaultValue: ""
                    },
                    {
                        name: "password",
                        optional: false,
                        description: "The password to be used to access the RDBMS data store.",
                        defaultValue: ""
                    },
                    {
                        name: "jdbc.driver.name",
                        optional: false,
                        description: "The driver class name for connecting the RDBMS data store.",
                        defaultValue: ""
                    },
                    {
                        name: "pool.properties",
                        optional: true,
                        description: "Any pool parameters for the database connection must be specified as " +
                        "key-value pairs.",
                        defaultValue: "null"
                    },
                    {
                        name: "table.name",
                        optional: true,
                        description: "The name with which the event table should be persisted in the store." +
                        " If no name is specified via this parameter, the event table is persisted with " +
                        "the same name as the Siddhi table.",
                        defaultValue: "The table name defined in the Siddhi App query."
                    },
                    {
                        name: "field.length",
                        optional: true,
                        description: "The number of characters that the values for fields of the 'STRING' type in" +
                        " the table definition must contain. Each required field must be provided as a " +
                        "comma-separated list of key-value pairs in the [field.name:length] format." +
                        "If this is not specified, the default number of characters specific to the database type is considered.",
                        defaultValue: "The table name defined in the Siddhi App query."
                    }

                ]

            },
            {
                name: "datasource",
                parameters: [
                    {
                        name: "datasource",
                        optional: false,
                        description: "The name of the Carbon datasource that should be used for creating the " +
                        "connection with the database. If this is found, neither the pool properties nor the " +
                        "JNDI resource name described above are taken into account and the connection is " +
                        "attempted via Carbon datasources instead. ",
                        defaultValue: ""
                    },
                    {
                        name: "pool.properties",
                        optional: true,
                        description: "Any pool parameters for the database connection must be specified as " +
                        "key-value pairs.",
                        defaultValue: "null"
                    },
                    {
                        name: "table.name",
                        optional: true,
                        description: "The name with which the event table should be persisted in the store." +
                        " If no name is specified via this parameter, the event table is persisted with " +
                        "the same name as the Siddhi table.",
                        defaultValue: "The table name defined in the Siddhi App query."
                    },
                    {
                        name: "field.length",
                        optional: true,
                        description: "The number of characters that the values for fields of the 'STRING' type in" +
                        " the table definition must contain. Each required field must be provided as a " +
                        "comma-separated list of key-value pairs in the [field.name:length] format." +
                        "If this is not specified, the default number of characters specific to the database type is considered.",
                        defaultValue: "The table name defined in the Siddhi App query."
                    }
                ]
            },
            {
                name: "jndi-resource",
                parameters: [
                    {
                        name: "jndi.resource",
                        optional: false,
                        description: "The name of the JNDI resource through which the connection is attempted. " +
                        "If this is found, the pool properties described above are not taken into account and " +
                        "the connection is attempted via JNDI lookup instead.",
                        defaultValue: ""
                    },
                    {
                        name: "pool.properties",
                        optional: true,
                        description: "Any pool parameters for the database connection must be specified as " +
                        "key-value pairs.",
                        defaultValue: "null"
                    },
                    {
                        name: "table.name",
                        optional: true,
                        description: "The name with which the event table should be persisted in the store." +
                        " If no name is specified via this parameter, the event table is persisted with " +
                        "the same name as the Siddhi table.",
                        defaultValue: "The table name defined in the Siddhi App query."
                    },
                    {
                        name: "field.length",
                        optional: true,
                        description: "The number of characters that the values for fields of the 'STRING' type in" +
                        " the table definition must contain. Each required field must be provided as a " +
                        "comma-separated list of key-value pairs in the [field.name:length] format." +
                        "If this is not specified, the default number of characters specific to the database type is considered.",
                        defaultValue: "The table name defined in the Siddhi App query."
                    }

                ]

            }
        ],
        services: {
            workspace: {
                endpoint: window.location.protocol + "//" + window.location.host + '/editor/workspace'
            },
            deploymentConfigs: {
                endpoint: window.location.protocol + "//" + window.location.host + '/editor/deploymentConfigs'
            }
        },
        alerts: {
            container: "#alerts-container",
            cssClass: {}
        },
        menu_bar: {
            container: "#menu-bar-container",
            menu_group: {
                menu_item: {
                    cssClass: {
                        label: "menu-label pull-left",
                        shortcut: "shortcut-label pull-right",
                        active: "menu-item-enabled",
                        inactive: "menu-item-disabled"
                    }
                },
                cssClass: {
                    group: "menu-group file-menu-group",
                    menu: "dropdown-menu file-dropdown-menu",
                    toggle: "dropdown-toggle"
                }
            },
            cssClass: {
                menu_bar: "dropdown-menu file-dropdown-menu",
                active: "active",
                menu: "dropdown-menu",
                item: "dropdown-toggle",
                menu_group: "menu-group"
            }
        },
        tool_bar: {
            container: "#tool-bar-container",
            runIconBtn: '.run_btn',
            debugIconBtn: '.debug_btn',
            stopIconBtn: '.stop_btn',
            revertIconBtn: '.revert_btn',
            commandRun: {
                id: "run_icon-siddhi-app"
            },
            commandDebug: {
                id: "debug_icon-siddhi-app"
            },
            commandStop: {
                id: "stop_icon-siddhi-app"
            },
            commandRevert: {
                id: "revert_icon-siddhi-app"
            }
        },
        event_simulator: {
            container: ".sidebar-left",
            activateBtn: '.event-simulator-activate-btn',
            separator: '.sidebar-left-separator',
            containerToAdjust: "#right-container",
            leftOffset: 40,
            separatorOffset: 5,
            defaultWidth: 380,
            resizeLimits: {
                minX: 200,
                maxX: 800
            },
            containerId: "event-simulator",
            cssClass: {
                container: 'event-simulator-container ws-tab-pane'
            },
            command: {
                id: "toggle-event-simulator",
                shortcuts: {
                    mac: {
                        key: "command+shift+i",
                        label: "\u2318\u21E7I"
                    },
                    other: {
                        key: "ctrl+shift+i",
                        label: "Ctrl+Shift+I"
                    }
                }
            },
            commandAddSingleSimulatorForm: {
                id: "add-single-simulator"
            }
        },
        operator_finder: {
            container: '.sidebar-left',
            activateBtn: '.operator-finder-activate-btn',
            separator: '.sidebar-left-separator',
            containerToAdjust: '#right-container',
            leftOffset: 40,
            separatorOffset: 5,
            defaultWidth: 380,
            resizeLimits: {
                minX: 200,
                maxX: 800
            },
            containerId: 'operator-finder',
            command: {
                id: 'toggle-operator-finder',
                shortcuts: {
                    mac: {
                        key: 'command+shift+x',
                        label: '\u2318\u21E7X'
                    },
                    other: {
                        key: 'ctrl+shift+x',
                        label: 'Ctrl+Shift+X'
                    }
                }
            }
        },
        template_deploy: {
            container: '.sidebar-left',
            activateBtn: '.template-deploy-activate-btn',
            separator: '.sidebar-left-separator',
            containerToAdjust: '#right-container',
            leftOffset: 40,
            separatorOffset: 5,
            defaultWidth: 380,
            resizeLimits: {
                minX: 200,
                maxX: 800
            },
            containerId: 'template-deploy',
            command: {
                id: 'toggle-template-deploy',
                shortcuts: {
                    mac: {
                        key: 'command+shift+t',
                        label: '\u2318\u21E7T'
                    },
                    other: {
                        key: 'ctrl+shift+t',
                        label: 'Ctrl+Shift+T'
                    }
                }
            }
        },
        workspace_explorer: {
            container: ".sidebar-left",
            activateBtn: '.workspace-explorer-activate-btn',
            separator: '.sidebar-left-separator',
            containerToAdjust: "#right-container",
            command: {
                id: "toggle-file-explorer",
                shortcuts: {
                    mac: {
                        key: "command+shift+e",
                        label: "\u2318\u21E7E"
                    },
                    other: {
                        key: "ctrl+shift+e",
                        label: "Ctrl+Shift+E"
                    }
                }
            },
            leftOffset: 40,
            separatorOffset: 5,
            defaultWidth: 290,
            resizeLimits: {
                minX: 200,
                maxX: 800
            },
            containerId: 'workspace-explorer',
            cssClass: {
                container: 'workspace-explorer-container ws-tab-pane',
                openFolderButton: 'btn  btn-default open-folder-button'
            }
        },
        settings_dialog: {
            selector: "#modalSettings",
            submit_button: "#saveSettingsButton"
        },
        sample_event_dialog: {
            selector: "#modalSampleEvent",
            submit_button: "#generateSampleEventButton"
        },
        query_store_api: {
            selector: "#modalStoreQuery",
            submit_button: "#executeQuery"
        },
        output_controller: {
            container: "#console-container",
            activateBtn: '.output-console-activate-btn',
            openConsoleBtn: '.open-console-btn',
            closeConsoleBtn: '.close-console-btn',
            cleanConsoleBtn: '.clear-console-btn',
            reloadConsoleBtn: '.reload-console-btn',
            headers: {
                // relative selector within container for tab controller
                container: ".output-headers",
                cssClass: {
                    list: 'output output-consoles',
                    item: 'console-header',
                    active: 'active'
                }
            },
            consoles: {
                container: ".output-console-content-wrapper",
                console: {
                    template: "#console-template",
                    cssClass: {
                        console: 'output-pane',
                        console_active: 'active',
                        console_close_btn: 'close closeConsole pull-right'
                    }
                }
            },
            command: {
                id: "toggle-output-console",
                shortcuts: {
                    mac: {
                        key: "command+shift+k",
                        label: "\u2318\u21E7K"
                    },
                    other: {
                        key: "ctrl+shift+k",
                        label: "Ctrl+Shift+K"
                    }
                }
            },
            commandClearConsole: {
                id: "clear-output-console",
                shortcuts: {
                    mac: {
                        key: "command+shift+l",
                        label: "\u2318\u21E7L"
                    },
                    other: {
                        key: "ctrl+shift+l",
                        label: "Ctrl+Shift+L"
                    }
                }
            }
        },
        debugger_instance: {
            container: '.debug-container',
            resumeBtn: '.debugger-resume-btn',
            stepoverBtn: '.debugger-stepover-btn',
            stopBtn: '.debugger-stop-btn',
            commandResume: {
                id: "debugger-resume",
                shortcuts: {
                    mac: {
                        key: "shift+r",
                        label: "\u21E7R"
                    },
                    other: {
                        key: "shift+r",
                        label: "Shift+R"
                    }
                }
            },
            commandStepOver: {
                id: "debugger-stepover",
                shortcuts: {
                    mac: {
                        key: "shift+r",
                        label: "\u21E7R"
                    },
                    other: {
                        key: "shift+r",
                        label: "Shift+R"
                    }
                }
            }
        },

        tab_controller: {
            container: "#tabs-container",
            headers: {
                // relative selector within container for tab controller
                container: ".tab-headers",
                cssClass: {
                    list: 'nav nav-tabs',
                    item: 'nav-tab-header',
                    active: 'active'
                }
            },
            tabs: {
                // relative selector within container for tab controller
                container: ".tab-content",
                tab: {
                    template: "#tab-template",
                    cssClass: {
                        tab: 'ws-tab-pane',
                        tab_active: 'active',
                        run_state: 'run-state',
                        debug_state: 'debug-state',
                        tab_close_btn: 'close closeTab pull-right'
                    },
                    das_editor: {
                        canvas: {
                            // relative selector within container for a tab
                            container: '.canvas-container'
                        },
                        loading_screen: {
                            // relative selector within container for a tab
                            container: '.loading-screen'
                        },
                        source: {
                            // relative selector within container for a tab
                            container: '.source-container'
                        },
                        debug: {
                            // relative selector within container for a tab
                            container: '.debug-container'
                        },
                        toggle_controls: {
                            // relative selector within container for a tab
                            container: '.toggle-controls-container',
                            toggle_view: '.toggle-view-button'
                        },
                        notifications: {
                            container: '#notification-container'
                        },
                        design_view: {
                            // relative selector within container for a tab
                            container: '.design-view-container',
                            grid_container: '.grid-container',
                            new_drop_timeout: 3000,
                            tool_palette: {
                                // relative selector within container for design view
                                container: ".tool-palette-container",
                                toolGroup: {
                                    tool: {
                                        containment_element: '#tabs-container',
                                        cssClass: {
                                            dragContainer: 'tool-drag-container',
                                            disabledIconContainer: 'disabled-icon-container',
                                            disabledIcon: 'fw fw-lg fw-block tool-disabled-icon'
                                        }
                                    }
                                }
                            }
                        },
                        source_view: {
                            // relative selector within container for a tab
                            container: '.source-view-container',
                            theme: 'ace/theme/twilight',
                            font_size: '14pt',
                            scroll_margin: '20',
                            mode: 'ace/mode/siddhi'
                        },
                        cssClass: {
                            text_editor_class: 'text-editor',
                            outer_box: 'outer-box',
                            svg_container: 'svg-container',
                            outer_div: 'panel panel-default container-outer-div',
                            panel_title: 'panel-title',
                            panel_icon: 'panel-icon',
                            service_icon: 'fw fw-service fw-inverse',
                            struct_icon: 'fw fw-struct fw-inverse',
                            connector_icon: 'fw fw-connector fw-inverse',
                            function_icon: 'fw fw-function fw-inverse',
                            main_function_icon: 'fw fw-main-function fw-inverse',
                            title_link: 'collapsed canvas-title',
                            panel_right_icon: 'fw fw-up pull-right right-icon-clickable collapser hoverable',
                            head_div: 'canvas-heading',
                            body_div: 'panel-collapse collapse',
                            canvas: 'panel-body collapse in',
                            design_view_drop: 'design-view-hover',
                            canvas_container: 'canvas-container',
                            canvas_top_controls_container: 'canvas-top-controls-container',
                            canvas_top_control_package_define: 'package-definition-wrapper',
                            canvas_top_control_packages_import: 'package-imports-wrapper',
                            canvas_top_control_constants_define: 'constants-definition-wrapper',
                            panel_delete_icon: 'fw fw-delete pull-right right-icon-clickable delete-icon hoverable',
                            panel_annotation_icon: 'fw fw-annotation pull-right right-icon-clickable hoverable',
                            panel_args_icon: 'fw fw-import pull-right right-icon-clickable hoverable',
                            type_mapper_icon: 'fw fw-type-converter fw-inverse',
                            type_struct_icon: 'fw fw-dgm-service fw-inverse',
                            canvas_heading_new: 'canvas-heading-new'
                        },
                        dialog_boxes: {
                            parser_error: "#parserErrorModel"
                        }
                    }
                }
            }
        },
        export_dialog: {
            selector: "#openExportModal"
        },
        export_k8s_path_step_5: {
            selector: "#k8s-path-step-5"
        },
        export_k8s_path_step_6: {
            selector: "#k8s-path-step-6"
        },
        export_k8s_path_step_8: {
            selector: "#k8s-path-step-8"
        },
        export_docker_path_step_5: {
            selector: "#docker-path-step-5"
        },
        export_docker_config_common_step: {
            selector: "#docker-config-common-step"
        }
    };
    app = new Application(config);
    app.render();
    app.displayInitialView();
    app.runInitialGuide();
    $("#tabs-container > div.init-loading-screen").css({"display": "none"});
    $("#tabs-container > div.tab-headers.tab-headers-bar").css({"display": "block"});
    $("#tab-content-wrapper").css({"display": "block"});
});

