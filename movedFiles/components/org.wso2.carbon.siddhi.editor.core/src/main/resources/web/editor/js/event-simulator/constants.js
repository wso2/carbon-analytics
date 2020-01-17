/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(function () {
    "use strict"; // JS strict mode

    /**
     * Constants used by the tool - editor
     */
    var constants = {
        ALLOWED_KEYS: ["Delete", "ArrowRight", "ArrowLeft", "Backspace"],
        INT_LONG_REGEX_MATCH: /^-?[\d]*$/,
        DOUBLE_FLOAT_REGEX_MATCH: /^-?\d*[.]?\d*$/,
        INT_LONG: ["INT", "LONG"],
        DOUBLE_FLOAT: ["DOUBLE", "FLOAT"]
    };

    return constants;
});
