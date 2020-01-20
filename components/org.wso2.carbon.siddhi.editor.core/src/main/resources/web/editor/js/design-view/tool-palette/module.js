/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['require', 'tool_palette/tool', 'tool_palette/tool-view', 'tool_palette/tool-group',
        'tool_palette/tool-group-view', 'tool_palette/tool-palette'],
    function (require, tool, toolView, toolGroup, toolGroupView, toolPalette) {
        return {
            Models: {
                Tool: tool,
                ToolGroup: toolGroup
            },
            Views: {
                ToolView: toolView,
                ToolGroupView: toolGroupView,
                ToolPalette: toolPalette
            }
        }
    });

