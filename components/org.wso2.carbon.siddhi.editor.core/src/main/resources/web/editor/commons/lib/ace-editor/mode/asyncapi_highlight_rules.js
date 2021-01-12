/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

define("ace/mode/asyncapi_highlight_rules", ["require", "exports", "module", "ace/lib/oop",
    "ace/mode/text_highlight_rules"], function (require, exports, module) {
    "use strict";   // JS strict mode
    var oop = require("../lib/oop");
    var TextHighlightRules = require("./text_highlight_rules").TextHighlightRules;
    var AsyncAPIHighlightRules = function () {
        var keywords = "WEBSOCKET|SSE|WEBHOOK";
        var builtInConstants = "TRUE|FALSE|NULL";
        var builtInBooleanConstants = "TRUE|FALSE";
        var builtInTypes = "STRING|INT|LONG|FLOAT|DOUBLE|BOOL|OBJECT";
        var keywordMapper = this.createKeywordMapper({
            "keyword": keywords,
            "constant.language": builtInConstants,
            "constant.language.boolean": builtInBooleanConstants,
            "support.type": builtInTypes
        }, "identifier", true);
        this.$rules = {
            "start": [
                {
                    token: ["annotation.plan.start", "keyword.other", "annotation.plan", "keyword.other"],
                    regex: "(@\\s*[Aa][Pp][Pp]\\s*)(:)(\\s*[a-zA-Z_][a-zA-Z_0-9]*\\s*)(\\(\\s*)",
                    next: "annotation"
                }
            ],
            "annotation": [
                {
                    token: ["annotation.common.start", "keyword.other"],
                    regex: "(@\\s*[a-zA-Z_][a-zA-Z_0-9]*\\s*)(\\(\\s*)"
                }
            ]
        };
        this.normalizeRules();
    };
    oop.inherits(AsyncAPIHighlightRules, TextHighlightRules);
    exports.AsyncAPIHighlightRules = AsyncAPIHighlightRules;
});
