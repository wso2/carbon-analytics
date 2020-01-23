/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(function () {

    "use strict";   // JS strict mode

    /**
     * Constants used by the siddhi editor tour guide
     */
    var constants = {
        CONTENT: '@App:name(\'SweetFactory\')\n' +
            '@App:description(\'Factory Analysis App\')\n' +
            '\n' +
            'define stream SweetProductionStream (name string, amount long);\n' +
            '@sink(type = \'log\', \n' +
            '\t@map(type = \'passThrough\'))\n' +
            'define stream TotalProductionStream (TotalProduction long);\n' +
            '\n' +
            '@info(name = \'SweetTotalQuery\')\n' +
            'from SweetProductionStream \n' +
            'select count() as TotalProduction \n' +
            'insert into TotalProductionStream;\n',
        INSERT_STRING: 'define stream SweetProductionStream (name string, amount long);',
        TAB_KEYCODE : 9,
        ERROR_TEXT: "Please close all tabs except welcome-page to start the guide. You can use close all button on the" +
            " right corner of the screen",
        CURRENT_STEP: null
    };

    return constants;
});
