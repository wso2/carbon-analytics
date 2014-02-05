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
        url: "dashboardServiceClient-ajaxprocessor.jsp",
        data: "func=sessionValid&nocache=" + new Date().getTime(),
        async:   false
    }).responseText;

    return (response.search('true'))
}

function checkSession() {
    if (isSessionValid() > 0) {
        return true;
    } else {
        CARBON.showErrorDialog(jsi18n["session.invalid"]);
        redirectToHttpsUrl('../gsusermgt/login.jsp', backendHttpsPort);
        return false;
    }

}

// sessionAwareFunction used with dashboard
// This supports dashboard inside other products(i.e. greg) and also inside gadget server
function sessionAwareFunction(success, message, failure) {
    var random = Math.floor(Math.random() * 2000);
    var errorMessage = "Session timed out. Please login again.";
    if (message && typeof message != "function") {
        errorMessage = message;
    } else if (success && typeof success != "function") {
        errorMessage = success;
    }
    if (!failure && typeof message == "function") {
        failure = message;
    }
    if(typeof(Ajax) != "undefined"){
	    new Ajax.Request('../dashboard/includes/session-validate.jsp',
	    {
	        method:'post',
	        asynchronous:false,
	        onSuccess: function(transport) {
	            var returnValue = transport.responseText;
	            if(returnValue.search(/----valid----/) == -1){
	                if (failure && typeof failure == "function") {
	                    failure();
	                } else {
	                    CARBON.showErrorDialog(errorMessage,function(){
	                        location.href="../admin/logout_action.jsp";
	                    }, function(){
	                        location.href="../admin/logout_action.jsp";
	                    });
	                }
	            } else {
	                if (success && typeof success == "function") {
	                    success();
	                }
	            }
	        },
	        onFailure: function() {

	        }
	    });
    }else{
        jQuery.ajax({
        type:"POST",
        url:'../dashboard/includes/session-validate.jsp',
        data: 'random='+random,
        success:
                function(data, status)
                {
                    var returnValue = data;
	            if(returnValue.search(/----valid----/) == -1){
	                if (failure && typeof failure == "function") {
	                    failure();
	                } else {
	                    CARBON.showErrorDialog(errorMessage,function(){
	                        location.href="../admin/logout_action.jsp";
	                    }, function(){
	                        location.href="../admin/logout_action.jsp";
	                    });
	                }
	            } else {
	                if (success && typeof success == "function") {
	                    success();
	                }
	            }

                }
    	});
    }
}
