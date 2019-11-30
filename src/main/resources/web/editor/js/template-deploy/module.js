/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['./template-deploy'],
    function (TemplateDeploy) {
        return  {
            TemplateDeploy: TemplateDeploy.TemplateDeploy,
            Utils: {
                toggleAddToSource: TemplateDeploy.toggleAddToSource,
            }
        };
    });
