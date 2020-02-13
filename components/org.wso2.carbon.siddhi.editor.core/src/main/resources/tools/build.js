/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

{

    // appDir: '../web',
    // mainConfigFile: '../web/app.js',
    // dir: '../../../../target/classes/web',
    // generateSourceMaps: true,
    // preserveLicenseComments: false,
    // optimizeCss: "standard",
    // "modules": [
    //     {
    //     // module names are relative to baseUrl
    //     name: "../editor/js/app-main",
    //         exclude: [
    //             'commons/lib/enjoyhint/enjoyhint.min',
    //             'commons/lib/ace-editor/mode/xquery/xqlint',
    //             "commons/lib/ace-editor/mode/coffee/coffee",
    //             "commons/lib/mCustomScrollbar_v3.1.5/js/jquery.mCustomScrollbar.concat.min",
    //             "commons/lib/jquery-mousewheel_v3.1.13/test/browserify/bundle"
    //         ]
    //     }
    // ]

    mainConfigFile: '../web/app.js',
    name: "app/app-main",
    out: '../../../../target/classes/web/editor/js/app-main.js',
    generateSourceMaps: true,
    preserveLicenseComments: false,
    optimizeCss: "standard",
    exclude: ['commons/lib/enjoyhint/enjoyhint.min']
}