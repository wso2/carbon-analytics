/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['jquery'], function ($) {

    "use strict";   // JS strict mode

    var mavenVersion = $("#mavenVersion").text().toUpperCase();
    var dockerVersion;
    var latestPackVersion;
    var packVersion = mavenVersion.match(/[^.]+\.[^.]+\.[^.]+/g);
    var minorVersion = mavenVersion.match(/[^.]+\.[^.]+/g);
    if (mavenVersion.includes("SNAPSHOT")||mavenVersion.includes("ALPHA")||mavenVersion.includes("BETA")) {
        dockerVersion = "latest-dev";
        latestPackVersion = "latest";
    } else {
        dockerVersion = packVersion;
        latestPackVersion = packVersion;
    }

    /**
     * versions used by the tool - editor
     */
    var version = {
        DOCKER_VERSION: dockerVersion,
        LATEST_PACK_VERSION: latestPackVersion,
        PACK_VERSION: packVersion,
        MINOR_VERSION: minorVersion
    };

    return version;
});
