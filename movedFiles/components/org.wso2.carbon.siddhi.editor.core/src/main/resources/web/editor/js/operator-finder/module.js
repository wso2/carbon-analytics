/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['./operator-finder'],
    function (OperatorFinder) {
        return  {
            OperatorFinder: OperatorFinder.OperatorFinder,
            Utils: {
                toggleAddToSource: OperatorFinder.toggleAddToSource,
            }
        };
    });

