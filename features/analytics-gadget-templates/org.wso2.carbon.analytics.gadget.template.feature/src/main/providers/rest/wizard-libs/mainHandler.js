var NO_AUTH = "No Auth";
var BASIC_AUTH = "Basic Auth";
var OAUTH2_PASSWORD_GRANT_TYPE = "OAuth2 with Password Grant Type";
var OAUTH2_CLIENT_CREDENTIALS_GRANT_TYPE = "OAuth2 with Client Credentials Grant Type";
var APPLICATION_JSON = "application/json";
var HTTP_POST = "POST";
var authorizationFields = ["username", "password", "authURL", "clientId", "clientSecret", "scope"];

hideElements(authorizationFields);

$("#requestAccessToken").hide();
$('select').on('change', function () {
    clearErrorMessage();
    clearSuccessMessage();
    if ($(this).attr("name") === "authorizationMethod") {
        var authorizationType = $(this).find(":selected").val();
        switch (authorizationType) {
            case NO_AUTH:
                hideElements(authorizationFields);
                $("#requestAccessToken").hide();
                break;
            case BASIC_AUTH:
                hideElements(authorizationFields);
                $("#requestAccessToken").show();
                showElements(["username", "password"]);
                break;
            case OAUTH2_PASSWORD_GRANT_TYPE:
                $("#requestAccessToken").show();
                showElements(authorizationFields);
                break;
            case OAUTH2_CLIENT_CREDENTIALS_GRANT_TYPE:
                hideElements(authorizationFields);
                $("#requestAccessToken").show();
                showElements(["authURL", "clientId", "clientSecret", "scope"]);
                break;
        }
    }
});

$("#requestAccessToken").click(function () {
    var isExecute = false;
    var url = "/portal/extensions/providers/rest/restClientInvoker.jag";
    var payload = {};
    var authorizationType = $("#provider-config-form select").find(":selected").val();
    clearErrorMessage();
    clearSuccessMessage();
    switch (authorizationType) {
        case NO_AUTH:
            break;
        case BASIC_AUTH:
            payload["password"] = getElementsValue("password");
            payload["username"] = getElementsValue("username");
            payload["authorizationMethod"] = authorizationType;
            if (payload["password"] && payload["username"]) {
                isExecute = true;
            }
            break;
        case OAUTH2_PASSWORD_GRANT_TYPE:
            payload["password"] = getElementsValue("password");
            payload["username"] = getElementsValue("username");
            payload["clientId"] = getElementsValue("clientId");
            payload["clientSecret"] = getElementsValue("clientSecret");
            payload["scope"] = getElementsValue("scope");
            payload["authURL"] = getElementsValue("authURL");
            payload["authorizationMethod"] = authorizationType;
            if (payload["password"] && payload["username"] && payload["clientId"] && payload["clientSecret"]
                && payload["scope"] && payload["authURL"]) {
                isExecute = true;
            }
            break;
        case OAUTH2_CLIENT_CREDENTIALS_GRANT_TYPE:
            payload["clientId"] = getElementsValue("clientId");
            payload["clientSecret"] = getElementsValue("clientSecret");
            payload["scope"] = getElementsValue("scope");
            payload["authURL"] = getElementsValue("authURL");
            payload["authorizationMethod"] = authorizationType;
            if (payload["clientId"] && payload["clientSecret"] && payload["scope"] && payload["authURL"]) {
                isExecute = true;
            }
            break;
    }
    if (isExecute) {
        execute(HTTP_POST, url, payload, function (response) {
                genarateSuccessMessage(response);
            }, function (error) {
                if (error.responseText) {
                    error = error.responseText;
                }
                genarateErrorMessage(error);
            },
            APPLICATION_JSON,
            APPLICATION_JSON);
    }
});

var execute = function (HTTP_POST, url, payload, successCallback, errorCallback, contentType, acceptType) {
    var data = {
        url: url,
        type: HTTP_POST,
        contentType: contentType,
        accept: acceptType,
        success: successCallback,
        error: errorCallback
    };
    var paramValue = {};
    paramValue.actionMethod = "POST";
    paramValue.actionUrl = url;
    paramValue.actionPayload = payload;
    data.data = JSON.stringify(paramValue);
    $.ajax(data);
}

function genarateSuccessMessage(accesstoken) {
    var successMessage = '<div id="tokenGenerateSuccessMsg" class="alert alert-success" role="alert">' +
        '<i class="icon fw fw-success"></i><span> Access Token has been generated.</span></div>';
    $('#accessTokenButton').prepend(successMessage);
    $('#tokenGenerateSuccessMsg').fadeIn('slow').delay(1000);
}

function genarateErrorMessage(errorMessage) {
    if (!errorMessage) {
        errorMessage = "";
    }
    var errorMessageHolder = '<div id="tokenGenerateErrorMsg" class="alert alert-danger" role="alert">' +
        '<i class="icon fw fw-error"></i><span>Error while generating access token:' + errorMessage + '</span></div>';
    $('#provider-config-form').prepend(errorMessageHolder);
}

function hideElements(elementsNames) {
    $('#provider-config-form :input').each(function (index, data) {
        if (elementsNames.includes($(this).attr("name"))) {
            $(this).closest('div').fadeOut();
        }
    });
}

function showElements(elementsNames) {
    $('#provider-config-form :input').each(function (index, data) {
        if (elementsNames.includes($(this).attr("name"))) {
            $(this).closest('div').fadeIn();
        }
    });
}

function clearSuccessMessage() {
    $('#provider-config-form :input').each(function (index, data) {
        $("#" + $(this).attr("name") + '-success').remove();
    });
    $("#tokenGenerateSuccessMsg").remove();
}

function getElementsValue(elementName) {
    var elementValue = undefined;
    $('#provider-config-form :input').each(function (index, data) {
        if (elementName === $(this).attr("name")) {
            if ($(this).val() !== "") {
                elementValue = $(this).val();
            } else {
                $(this).closest('div').append('<label id="' + $(this).attr("name") + '-error" ' +
                    'class="required-error show" for="title">Required</label>');
            }
        }
    });
    return elementValue;
}

function clearErrorMessage() {
    $('#provider-config-form :input').each(function (index, data) {
        $("#" + $(this).attr("name") + '-error').remove();
    });
    $("#tokenGenerateErrorMsg").remove();
}

function includes(k, strict) {
    strict = strict !== false;
    for (var i = 0; i < this.length; i++) {
        if ((this[i] === k && strict) ||
            (this[i] == k && !strict) ||
            (this[i] !== this[i] && k !== k)
        ) {
            return true;
        }
    }
    return false;
}



