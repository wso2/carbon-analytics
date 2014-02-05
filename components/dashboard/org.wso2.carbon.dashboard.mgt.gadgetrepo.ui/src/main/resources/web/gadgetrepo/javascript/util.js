/*
 * Copyright 2005-2009 WSO2, Inc. http://wso2.com
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

Function.prototype.inherits = function(parentCtor) {
    function tempCtor() {
    }

    ;
    tempCtor.prototype = parentCtor.prototype;
    this.superClass_ = parentCtor.prototype;
    this.prototype = new tempCtor();
    this.prototype.constructor = this;
};

function redirectToHttpsUrl(relToUrl, httpPort) {
    var currentUrl = window.location.href;
    var pathName = location.pathname + '/' + '../' + relToUrl;
    window.location = 'https://' + location.host.split(':')[0] + ':' + httpPort + pathName;
}

function getHttpsUrl(relToUrl, httpsPort) {
    var currentUrl = window.location.href;
    var pathName = location.pathname + '/' + '../' + relToUrl;
    var retUrl = 'https://' + location.host.split(':')[0] + ':' + httpsPort + pathName;
    return retUrl;
}

function isSessionValid() {
    var response = jQuery.ajax({
        type: "POST",
        url: "GadgetRepo-ajaxprocessor.jsp",
        data: "func=sessionValid&nocache=" + new Date().getTime(),
        async:   false
    }).responseText;

    return (response.search('true'))
}
//This method is used for handling session timeouts in admin console view.
function checkSession() {
    if (isSessionValid() > 0) {
        return true;
    } else {
        
        window.location.href='../admin/login.jsp';
        return false;
    }
}

//This method is used for handling session timeouts in portal view.
function checkSessionInPortal() {
    if (isSessionValid() > 0) {
        return true;
    } else {
       CARBON.showErrorDialog(jsi18n["session.invalid"]);
         window.location.href='../gsusermgt/login.jsp';
        return false;
    }
}
