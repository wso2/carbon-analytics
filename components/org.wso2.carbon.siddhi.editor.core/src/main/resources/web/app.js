/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
requirejs.config({
    baseUrl: 'editor',
    paths: {
        lib: "commons/lib",
        app: "js",
        commons_app: "commons",
        /////////////////////////
        // third party modules //
        ////////////////////////
        remarkable:"commons/lib/remarkable/remarkable.min",
        handlebar: "commons/lib/handlebar/handlebars-v4.0.11.min",
        jquery: "commons/lib/jquery_v1.9.1/jquery-1.9.1.min",
        jquery_ui: "commons/lib/jquery-ui_v1.12.1/jquery-ui.min",
        bootstrap: "commons/lib/theme-wso2-2.0.0/js/bootstrap.min",
        dagre: "commons/lib/dagre-0.7.4/dagre.min",
        dragSelect: "commons/lib/dragSelect/ds.min",
        jquery_validate: "commons/lib/jquery_validate/jquery.validate.min",
        jquery_timepicker: "commons/lib/jquery_timepicker/jquery-ui-timepicker-addon.min",
        log4javascript: "commons/lib/log4javascript-1.4.13/log4javascript",
        lodash: "commons/lib/lodash_v4.13.1/lodash.min",
        backbone: "commons/lib/backbone_v1.3.3/backbone.min",
        file_saver: "commons/lib/file_saver/FileSaver",
        mousetrap: "commons/lib/mousetrap_v1.6.0/mousetrap.min",
        jquery_mousewheel: "commons/lib/jquery-mousewheel_v3.1.13/jquery.mousewheel.min",
        jquery_context_menu: "commons/lib/context-menu_v2.4.2/jquery.contextMenu.min",
        js_tree: "commons/lib/js-tree-v3.3.8/jstree.min",
        render_json: "commons/lib/renderjson/renderjson",
        nano_scroller: "commons/lib/nanoscroller_0.8.7/jquery.nanoscroller.min",
        perfect_scrollbar: "commons/lib/perfect-scrollbar-1.4.0/perfect-scrollbar.min",
        theme_wso2: "commons/lib/theme-wso2-2.0.0/js/theme-wso2.min",
        ace: "commons/lib/ace-editor",
        overlay_scroller: "commons/lib/overlay_scroll/js/jquery.overlayScrollbars.min",
        datatables: "commons/lib/data-table/jquery.dataTables.min",
        datatables_bootstrap: "commons/lib/data-table/dataTables.bootstrap.min",
        datatables_wso2: "commons/lib/theme-wso2-2.0.0/extensions/datatables/js/dataTables.wso2",
        enjoyhint: "commons/lib/enjoyhint/enjoyhint.min",
        smart_wizard: "commons/lib/smartWizard/js/jquery.smartWizard.min",
        //beautify: "commons/lib/beautify",
        ///////////////////////
        // custom modules ////
        //////////////////////
        log: "commons/js/log/log",
        version: "js/version/version",
        menu_bar: "commons/js/menu-bar/menu-bar",
        tool_bar: "commons/js/tool-bar/tool-bar",
        context_menu: "commons/js/context-menu/context-menu",
        tool_palette: "js/design-view/tool-palette",
        tab: "js/tab/",
        toolEditor: "js/tool-editor/module",
        command: "commons/js/command/command",
        alerts: "commons/js/utils/alerts",
        workspace: "commons/js/workspace/workspace",
        event_channel: "commons/js/event/channel",
        menu_group: "commons/js/menu-bar/menu-group",
        menu_definitions: "commons/js/menu-bar/menu-definitions",
        file_menu: "commons/js/menu-bar/file-menu",
        edit_menu: "commons/js/menu-bar/edit-menu",
        run_menu: "commons/js/menu-bar/run-menu",
        tools_menu: "commons/js/menu-bar/tools-menu",
        deploy_menu: "commons/js/menu-bar/deploy-menu",
        export_menu: "commons/js/menu-bar/export-menu",
        menu_item: "commons/js/menu-bar/menu-item",
        file_browser: "commons/js/file-browser/file-browser",
        undo_manager: "commons/js/undo-manager/undo-manager",
        launcher: "js/launcher/launcher",
        launch_manager: "js/launcher/launch-manager",
        undoable_operation_factory: "commons/js/undo-manager/undoable-operation-factory",
        source_modify_operation: "commons/js/undo-manager/source-modify-operation",
        undoable_operation: "commons/js/undo-manager/undoable-operation",
        sample_preview: "js/sample-preview/sample-preview-view",
        sample_view: "js/sample-view/sample-view",
        output_console_list: "js/output-console/console-list",
        console: "js/output-console/console",
        service_console: "js/output-console/service-console-manager",
        debugger_console: "js/debugger/debugger",
        design_view: "js/design-view/design-view",
        initialiseData: "js/design-view/initialise-data",
        jsonValidator: "js/design-view/JSON-validator",
        designViewGrid: "js/design-view/design-grid",
        designViewUtils: "js/design-view/util/design-view-utils",
        dropElements: "js/design-view/drop-elements",
        configurationData: "js/design-view/configuration-data/configuration-data",
        appData: "js/design-view/configuration-data/app-data",
        elementUtils: "js/design-view/elements/element-utils",
        formBuilder: "js/design-view/form-builder/form-builder",
        formUtils: "js/design-view/form-builder/form-utils",
        sourceForm: "js/design-view/form-builder/forms/source-form",
        sinkForm: "js/design-view/form-builder/forms/sink-form",
        streamForm: "js/design-view/form-builder/forms/stream-form",
        tableForm: "js/design-view/form-builder/forms/table-form",
        windowForm: "js/design-view/form-builder/forms/window-form",
        aggregationForm: "js/design-view/form-builder/forms/aggregation-form",
        triggerForm: "js/design-view/form-builder/forms/trigger-form",
        windowFilterProjectionQueryForm: "js/design-view/form-builder/forms/window-filter-projection-query-form",
        patternQueryForm: "js/design-view/form-builder/forms/pattern-query-form",
        sequenceQueryForm: "js/design-view/form-builder/forms/sequence-query-form",
        joinQueryForm: "js/design-view/form-builder/forms/join-query-form",
        partitionForm: "js/design-view/form-builder/forms/partition-form",
        functionForm: "js/design-view/form-builder/forms/function-form",
        appAnnotationForm: "js/design-view/form-builder/forms/app-annotation-form",
        guide: "js/guide/guide",
        utils: "commons/js/utils/utils",
        // element data holding objects starts here
        // annotations
        sourceOrSinkAnnotation: "js/design-view/elements/annotations/source-or-sink-annotation",
        mapAnnotation: "js/design-view/elements/annotations/map",
        storeAnnotation: "js/design-view/elements/annotations/store-annotation",
        payloadOrAttribute: "js/design-view/elements/annotations/source-or-sink-annotation-sub-elements/payload-or-attribute",
        annotationObject: "js/design-view/elements/annotation-object",
        annotationElement: "js/design-view/elements/annotation-element",
        // definitions
        stream: "js/design-view/elements/definitions/stream",
        table: "js/design-view/elements/definitions/table",
        window: "js/design-view/elements/definitions/window",
        trigger: "js/design-view/elements/definitions/trigger",
        aggregation: "js/design-view/elements/definitions/aggregation",
        functionDefinition: "js/design-view/elements/definitions/function",
        aggregateByTimePeriod: "js/design-view/elements/definitions/aggregation-definition-sub-elements/aggregate-by-time-period",
        // query sub elements
        querySelect: "js/design-view/elements/queries/query-sub-elements/query-select/query-select",
        queryOrderByValue: "js/design-view/elements/queries/query-sub-elements/query-orderBy/query-orderBy-value",
        queryOutput: "js/design-view/elements/queries/query-sub-elements/query-output/query-output",
        queryOutputInsert: "js/design-view/elements/queries/query-sub-elements/query-output/query-output-types/query-output-insert",
        queryOutputDelete: "js/design-view/elements/queries/query-sub-elements/query-output/query-output-types/query-output-delete",
        queryOutputUpdate: "js/design-view/elements/queries/query-sub-elements/query-output/query-output-types/query-output-update",
        queryOutputUpdateOrInsertInto: "js/design-view/elements/queries/query-sub-elements/query-output/query-output-types/query-output-update-or-insert-into",
        // queries
        joinQuerySource: "js/design-view/elements/queries/query-sub-elements/query-input/join-query-input/sub-elements/join-query-source",
        joinQueryInput: "js/design-view/elements/queries/query-sub-elements/query-input/join-query-input/join-query-input",
        partition: "js/design-view/elements/partitions/partition",
        partitionWith: "js/design-view/elements/partitions/partition-sub-elements/partition-with",
        patternOrSequenceQueryInput: "js/design-view/elements/queries/query-sub-elements/query-input/pattern-or-sequence-query-input/pattern-or-sequence-query-input",
        patternOrSequenceQueryCondition: "js/design-view/elements/queries/query-sub-elements/query-input/pattern-or-sequence-query-input/sub-elements/pattern-or-sequence-query-condition",
        windowFilterProjectionQueryInput: "js/design-view/elements/queries/query-sub-elements/query-input/window-filter-projection-query/window-filter-projection-query-input",
        queryWindowOrFunction: "js/design-view/elements/queries/query-sub-elements/query-input/query-window-or-function",
        streamHandler: "js/design-view/elements/queries/query-sub-elements/query-input/stream-handler",
        query: "js/design-view/elements/queries/query",
        edge: "js/design-view/configuration-data/edge",
        annotation: "js/design-view/elements/annotation",
        attribute: "js/design-view/elements/attribute",
        // element data holding objects ends here
        //constants
        constants: "js/design-view/constants",
        guideConstants: "js/guide/constants",
        templateAppDialog: "js/export-deployment-artifacts/template-app-dialog",
        jarsSelectorDialog: "js/export-deployment-artifacts/jars-selector-dialog",
        siddhiAppSelectorDialog: "js/export-deployment-artifacts/siddhiapp-selector-dialog",
        templateConfigDialog: "js/export-deployment-artifacts/template-config-dialog",
        templateConfigBlocks: "js/export-deployment-artifacts/template-config-blocks",
        dockerConfigDialog: "js/export-deployment-artifacts/docker-config-dialog",
        dockerImageTypeDialog: "js/export-deployment-artifacts/docker-image-type-dialog",
        fillTemplateValueDialog: "js/export-deployment-artifacts/fill-template-value-dialog",
        kubernetesConfigDialog: "js/export-deployment-artifacts/kubernetes-config-dialog"
    },
    map: {
        "*": {
            // use lodash instead of underscore
            underscore: "lodash",
            jQuery: "jquery",
            'datatables.net': "datatables"
        }
    },
    packages: [
        {
            name: 'welcome-page',
            location: 'commons/js/welcome-page',
            main: 'module'
        },
        {
            name: 'tab',
            location: 'js/tab',
            main: 'module'
        },
        {
            name: 'workspace',
            location: 'commons/js/workspace',
            main: 'module'
        },
        {
            name: 'dialogs',
            location: 'commons/js/dialog',
            main: 'module'
        },
        {
            name: 'event_simulator',
            location: 'js/event-simulator',
            main: 'module'
        },
        {
            name: 'operator_finder',
            location: 'js/operator-finder',
            main: 'module'
        },
        {
            name: 'template_deploy',
            location: 'js/template-deploy',
            main: 'module'
        }
    ]
});
