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


var gadgets = gadgets || {};

gadgets.error = {};
gadgets.error.SUBCLASS_RESPONSIBILITY = 'subclass responsibility';
gadgets.error.TO_BE_DONE = 'to be done';

gadgets.log = function(message) {
    if (window.console && console.log) {
        console.log(message);
    } else {
        var logEntry = document.createElement('div');
        logEntry.className = 'gadgets-log-entry';
        logEntry.innerHTML = message;
        document.body.appendChild(logEntry);
    }
};

/**
 * Calls an array of asynchronous functions and calls the continuation
 * function when all are done.
 * @param {Array} functions Array of asynchronous functions, each taking
 *     one argument that is the continuation function that handles the result
 *     That is, each function is something like the following:
 *     function(continuation) {
 *       // compute result asynchronously
 *       continuation(result);
 *     }
 * @param {Function} continuation Function to call when all results are in.  It
 *     is pass an array of all results of all functions
 * @param {Object} opt_this Optional object used as "this" when calling each
 *     function
 */
gadgets.callAsyncAndJoin = function(functions, continuation, opt_this) {
    var pending = functions.length;
    var results = [];
    for (var i = 0; i < functions.length; i++) {
        // we need a wrapper here because i changes and we need one index
        // variable per closure
        var wrapper = function(index) {
            functions[index].call(opt_this, function(result) {
                results[index] = result;
                if (--pending === 0) {
                    continuation(results);
                }
            });
        };
        wrapper(i);
    }
};


// ----------
// Extensible

gadgets.Extensible = function() {
};

/**
 * Sets the dependencies.
 * @param {Object} dependencies Object whose properties are set on this
 *     container as dependencies
 */
gadgets.Extensible.prototype.setDependencies = function(dependencies) {
    for (var p in dependencies) {
        this[p] = dependencies[p];
    }
};

/**
 * Returns a dependency given its name.
 * @param {String} name Name of dependency
 * @return {Object} Dependency with that name or undefined if not found
 */
gadgets.Extensible.prototype.getDependencies = function(name) {
    return this[name];
};


// -------------
// UserPrefStore

/**
 * User preference store interface.
 * @constructor
 */
gadgets.UserPrefStore = function() {
};

/**
 * Gets all user preferences of a gadget.
 * @param {Object} gadget Gadget object
 * @return {Object} All user preference of given gadget
 */
gadgets.UserPrefStore.prototype.getPrefs = function(gadget) {
    throw Error(gadgets.error.SUBCLASS_RESPONSIBILITY);
};

/**
 * Saves user preferences of a gadget in the store.
 * @param {Object} gadget Gadget object
 * @param {Object} prefs User preferences
 */
gadgets.UserPrefStore.prototype.savePrefs = function(gadget) {
    throw Error(gadgets.error.SUBCLASS_RESPONSIBILITY);
};


// -------------
// DefaultUserPrefStore

/**
 * User preference store implementation.
 * TODO: Turn this into a real implementation that is production safe
 * @constructor
 */
gadgets.DefaultUserPrefStore = function() {
    gadgets.UserPrefStore.call(this);
};
gadgets.DefaultUserPrefStore.inherits(gadgets.UserPrefStore);

gadgets.DefaultUserPrefStore.prototype.getPrefs = function(gadget) {
};

gadgets.DefaultUserPrefStore.prototype.savePrefs = function(gadget) {
};


// -------------
// GadgetService

/**
 * Interface of service provided to gadgets for resizing gadgets,
 * setting title, etc.
 * @constructor
 */
gadgets.GadgetService = function() {
};

gadgets.GadgetService.prototype.setHeight = function(elementId, height) {
    throw Error(gadgets.error.SUBCLASS_RESPONSIBILITY);
};

gadgets.GadgetService.prototype.setTitle = function(gadget, title) {
    throw Error(gadgets.error.SUBCLASS_RESPONSIBILITY);
};

gadgets.GadgetService.prototype.setUserPref = function(id) {
    throw Error(gadgets.error.SUBCLASS_RESPONSIBILITY);
};


// ----------------
// IfrGadgetService

/**
 * Base implementation of GadgetService.
 * @constructor
 */
gadgets.IfrGadgetService = function() {
    gadgets.GadgetService.call(this);
    gadgets.rpc.register('resize_iframe', this.setHeight);
    gadgets.rpc.register('set_pref', this.setUserPref);
    gadgets.rpc.register('set_title', this.setTitle);
    gadgets.rpc.register('requestNavigateTo', this.requestNavigateTo);
};

gadgets.IfrGadgetService.inherits(gadgets.GadgetService);

gadgets.IfrGadgetService.prototype.setHeight = function(height) {
    if (height > gadgets.container.maxheight_) {
        height = gadgets.container.maxheight_;
    }

    var element = document.getElementById(this.f);
    if (element) {
        element.style.height = height + 'px';
    }
};

gadgets.IfrGadgetService.prototype.setTitle = function(title) {
    var element = document.getElementById(this.f + '_title');
    if (element) {
        element.innerHTML = title.replace(/&/g, '&amp;').replace(/</g, '&lt;');
    }
};

/**
 * Sets one or more user preferences
 * @param {String} editToken
 * @param {String} name Name of user preference
 * @param {String} value Value of user preference
 * More names and values may follow
 */
gadgets.IfrGadgetService.prototype.setUserPref = function(editToken, name,
                                                          value) {
    var id = getGadgetIdFromModuleId(this.f);
    var gadget = gadgets.container.getGadget(id);
    var prefs = gadget.getUserPrefs();
    for (var i = 1, j = arguments.length; i < j; i += 2) {
        prefs[arguments[i]] = arguments[i + 1];
    }
    gadget.setUserPrefs(prefs);
};

/**
 * Navigates the page to a new url based on a gadgets requested view and
 * parameters.
 */
gadgets.IfrGadgetService.prototype.requestNavigateTo = function(view,
                                                                opt_params) {
    var id = getGadgetIdFromModuleId(this.f);
    var url = getUrlForView(view);

    if (opt_params) {
        var paramStr = JSON.stringify(opt_params);
        if (paramStr.length > 0) {
            url += '&appParams=' + encodeURIComponent(paramStr);
        }
    }

    if (url && document.location.href.indexOf(url) == -1) {
        document.location.href = url;
    }
};


// -------------
// LayoutManager

/**
 * Layout manager interface.
 * @constructor
 */
gadgets.LayoutManager = function() {
};

/**
 * Gets the HTML element that is the chrome of a gadget into which the content
 * of the gadget can be rendered.
 * @param {Object} gadget Gadget instance
 * @return {Object} HTML element that is the chrome for the given gadget
 */
gadgets.LayoutManager.prototype.getGadgetChrome = function(gadget) {
    throw Error(gadgets.error.SUBCLASS_RESPONSIBILITY);
};

// -------------------
// StaticLayoutManager

/**
 * Static layout manager where gadget ids have a 1:1 mapping to chrome ids.
 * @constructor
 */
gadgets.StaticLayoutManager = function() {
    gadgets.LayoutManager.call(this);
};

gadgets.StaticLayoutManager.inherits(gadgets.LayoutManager);

/**
 * Sets chrome ids, whose indexes are gadget instance ids (starting from 0).
 * @param {Array} gadgetIdToChromeIdMap Gadget id to chrome id map
 */
gadgets.StaticLayoutManager.prototype.setGadgetChromeIds =
    function(gadgetChromeIds) {
        this.gadgetChromeIds_ = gadgetChromeIds;
    };

gadgets.StaticLayoutManager.prototype.getGadgetChrome = function(gadget) {
    var chromeId = this.gadgetChromeIds_[gadget.id];
    return chromeId ? document.getElementById(chromeId) : null;
};


// ----------------------
// FloatLeftLayoutManager

/**
 * FloatLeft layout manager where gadget ids have a 1:1 mapping to chrome ids.
 * @constructor
 * @param {String} layoutRootId Id of the element that is the parent of all
 *     gadgets.
 */
gadgets.FloatLeftLayoutManager = function(layoutRootId) {
    gadgets.LayoutManager.call(this);
    this.layoutRootId_ = layoutRootId;
};

gadgets.FloatLeftLayoutManager.inherits(gadgets.LayoutManager);

gadgets.FloatLeftLayoutManager.prototype.getGadgetChrome =
    function(gadget) {
        var layoutRoot = document.getElementById(this.layoutRootId_);
        if (layoutRoot) {
            var chrome = document.createElement('div');
            chrome.className = 'gadgets-gadget-chrome';
            chrome.style.cssFloat = 'left'
            layoutRoot.appendChild(chrome);
            return chrome;
        } else {
            return null;
        }
    };


// ------
// Gadget

/**
 * Creates a new instance of gadget.  Optional parameters are set as instance
 * variables.
 * @constructor
 * @param {Object} params Parameters to set on gadget.  Common parameters:
 *    "specUrl": URL to gadget specification
 *    "private": Whether gadget spec is accessible only privately, which means
 *        browser can load it but not gadget server
 *    "spec": Gadget Specification in XML
 *    "viewParams": a javascript object containing attribute value pairs
 *        for this gadgets
 *    "secureToken": an encoded token that is passed on the URL hash
 *    "hashData": Query-string like data that will be added to the
 *        hash portion of the URL.
 *    "specVersion": a hash value used to add a v= param to allow for better caching
 *    "title": the default title to use for the title bar.
 *    "height": height of the gadget
 *    "width": width of the gadget
 *    "debug": send debug=1 to the gadget server, gets us uncompressed
 *        javascript
 */
gadgets.Gadget = function(params) {
    this.userPrefs_ = {};

    this.debug = 1; // todo: Turn off in production to compress JS

    if (params) {
        for (var name in params)  if (params.hasOwnProperty(name)) {

            this[name] = params[name];
            this['width'] = '100%';
        }
    }
    if (!this.secureToken) {
        // Assume that the default security token implementation is
        // in use on the server.
        this.secureToken = userId + ':' + userId + ':appid:cont:url:0:default';
    }
};

gadgets.Gadget.prototype.getUserPrefs = function() {
    return this.userPrefs_;
};

gadgets.Gadget.prototype.setUserPrefs = function(userPrefs) {
    this.userPrefs_ = userPrefs;
    gadgets.container.userPrefStore.savePrefs(this);
};

gadgets.Gadget.prototype.getUserPref = function(name) {
    return this.userPrefs_[name];
};

gadgets.Gadget.prototype.setUserPref = function(name, value) {
    this.userPrefs_[name] = value;
    gadgets.container.userPrefStore.savePrefs(this);
};

gadgets.Gadget.prototype.render = function(chrome) {
    if (chrome) {
        this.getContent(function(content) {
            chrome.innerHTML = content;
        });
    }
};

gadgets.Gadget.prototype.getContent = function(continuation) {
    gadgets.callAsyncAndJoin([
        this.getTitleBarContent, this.getUserPrefsDialogContent,
        this.getMainContent], function(results) {
        continuation(results.join(''));
    }, this);
};

/**
 * Gets title bar content asynchronously or synchronously.
 * @param {Function} continutation Function that handles title bar content as
 *     the one and only argument
 */
gadgets.Gadget.prototype.getTitleBarContent = function(continuation) {
    throw Error(gadgets.error.SUBCLASS_RESPONSIBILITY);
};

/**
 * Gets user preferences dialog content asynchronously or synchronously.
 * @param {Function} continutation Function that handles user preferences
 *     content as the one and only argument
 */
gadgets.Gadget.prototype.getUserPrefsDialogContent = function(continuation) {
    throw Error(gadgets.error.SUBCLASS_RESPONSIBILITY);
};

/**
 * Gets gadget content asynchronously or synchronously.
 * @param {Function} continutation Function that handles gadget content as
 *     the one and only argument
 */
gadgets.Gadget.prototype.getMainContent = function(continuation) {
    throw Error(gadgets.error.SUBCLASS_RESPONSIBILITY);
};

/*
 * Gets additional parameters to append to the iframe url
 * Override this method if you need any custom params.
 */
gadgets.Gadget.prototype.getAdditionalParams = function() {
    return '';
}


// ---------
// IfrGadget

gadgets.IfrGadget = function(opt_params) {
    gadgets.Gadget.call(this, opt_params);
    this.serverBase_ = getServerBaseUrl(); // default gadget server
    this.rpcRelay = 'carbon/dashboard/rpc_relay.html';
    this.debug = debugMode;
};

gadgets.IfrGadget.inherits(gadgets.Gadget);

gadgets.IfrGadget.prototype.GADGET_IFRAME_PREFIX_ = 'remote_iframe_';

gadgets.IfrGadget.prototype.CONTAINER = 'default';

gadgets.IfrGadget.prototype.cssClassGadget = 'gadgets-gadget';
gadgets.IfrGadget.prototype.cssClassTitleBar = 'gadgets-gadget-title-bar';
gadgets.IfrGadget.prototype.cssClassTitle = 'gadgets-gadget-title';
gadgets.IfrGadget.prototype.cssClassTitleButtonBar =
    'gadgets-gadget-title-button-bar';
gadgets.IfrGadget.prototype.cssClassGadgetUserPrefsDialog =
    'gadgets-gadget-user-prefs-dialog';
gadgets.IfrGadget.prototype.cssClassGadgetUserPrefsDialogActionBar =
    'gadgets-gadget-user-prefs-dialog-action-bar';
gadgets.IfrGadget.prototype.cssClassTitleButton = 'gadgets-gadget-title-button';
gadgets.IfrGadget.prototype.cssClassGadgetContent = 'gadgets-gadget-content';
gadgets.IfrGadget.prototype.rpcToken = (0x7FFFFFFF * Math.random()) | 0;
gadgets.IfrGadget.prototype.rpcRelay = 'carbon/dashboard/rpc_relay.html';

gadgets.IfrGadget.prototype.getTitleBarContent = function(continuation) {

    var titleHTML = '<table width="100%" id="' + this.cssClassTitleBar + '-' + this.id +
        '" class="' + this.cssClassTitleBar +
        '"><tr><td style="*padding-left: 5px;" width="100%" id="' +
        this.getIframeId() + '_title" class="' +
        this.cssClassTitle + '">' + (this.title ? this.title : 'Title') +
        '</td>' ;

    if (dashboardName == null || !isReadOnly) {
        // We only display these buttons in read-write mode in GS and embeded dashboard
        titleHTML +=
            '<td width="19px"><a href="javascript:;" class="gadgets-gadget-title-button" title="settings"><img id="' + this.id + '" class="opts" src="images/gadget-settings.gif" /></a></td><td width="19px"><a style="background-image: url(images/gadget-toggle-up.gif);" onclick="gadgets.container.getGadget(' +
                this.id + ').handleToggle(this);return false;" class="' +
                this.cssClassTitleButton +
                '" title="toggle"></a></td><td width="19px"><a id="' + this.getMaximizeButtonId() +
                '" style="background-image: url(images/maximize.gif);" onclick="gadgets.container.getGadget(' +
                this.id + ').handleMaximize();return false;" class="' +
                this.cssClassTitleButton +
                '" title="maximize"></a><a id="' + this.getMinimizeButtonId() +
                '" style="background-image: url(images/minimize.gif); display:none;" onclick="gadgets.container.getGadget(' +
                this.id + ').handleMinimize();return false;" class="' +
                this.cssClassTitleButton +
                '" title="minimize"></a></td><td width="19px"><a style="background-image: url(images/gadget-close.gif);"  onclick="gadgets.container.getGadget(' +
                this.id + ').handleRemove();return false;" class="' + this.cssClassTitleButton +
                '" title="remove"></a></td>';

    } else if(dashboardName != null && isReadOnly) {
        // In read-only mode we only display minimize and maximize buttons
        titleHTML +=
            '<td width="19px"><a id="' + this.getMaximizeButtonId() +
                '" style="background-image: url(images/maximize.gif);" onclick="gadgets.container.getGadget(' +
                this.id + ').handleMaximize();return false;" class="' +
                this.cssClassTitleButton +
                '" title="maximize"></a><a id="' + this.getMinimizeButtonId() +
                '" style="background-image: url(images/minimize.gif); display:none;" onclick="gadgets.container.getGadget(' +
                this.id + ').handleMinimize();return false;" class="' +
                this.cssClassTitleButton +
                '" title="minimize"></a></td></tr>'
    }

    titleHTML += '</table>';

    continuation(titleHTML);
};

gadgets.IfrGadget.prototype.getUserPrefsDialogContent = function(continuation) {
    continuation('<div id="' + this.getUserPrefsDialogId() + '" class="' +
        this.cssClassGadgetUserPrefsDialog + '"></div>');

};

gadgets.IfrGadget.prototype.setServerBase = function(url) {
    this.serverBase_ = url;
};

gadgets.IfrGadget.prototype.getServerBase = function() {
    return this.serverBase_;
};

gadgets.IfrGadget.prototype.getMainContent = function(continuation) {
    var iframeId = this.getIframeId();
    gadgets.rpc.setRelayUrl(iframeId, this.serverBase_ + this.rpcRelay);
    gadgets.rpc.setAuthToken(iframeId, this.rpcToken);
    continuation('<div class="' + this.cssClassGadgetContent + '"><iframe id="' +
        iframeId + '" name="' + iframeId + '" class="' + this.cssClassGadget +
        '" src="' + this.getIframeUrl() +
        '" frameborder="no" scrolling="no"' +
        (this.height ? ' height="' + this.height + '"' : '') +
        (this.width ? ' width="' + this.width + '"' : '') +
        '></iframe></div>');
};

gadgets.IfrGadget.prototype.getIframeId = function() {
    return this.GADGET_IFRAME_PREFIX_ + this.id;
};

gadgets.IfrGadget.prototype.getUserPrefsDialogId = function() {
    return this.getIframeId() + '_userPrefsDialog';
};

gadgets.IfrGadget.prototype.getIframeUrl = function() {
    return this.serverBase_ + 'ifr?' +
        'container=' + this.CONTAINER +
        '&mid=' + this.id +
        '&nocache=' + gadgets.container.nocache_ +
        '&country=' + gadgets.container.country_ +
        '&lang=' + gadgets.container.language_ +
        '&view=' + gadgets.container.view_ +
        (this.specVersion ? '&v=' + this.specVersion : '') +
        (gadgets.container.parentUrl_ ?
            '&parent=' + encodeURIComponent(gadgets.container.parentUrl_) : '') +
        (this.debug ? '&debug=1' : '') +
        this.getAdditionalParams() +
        this.getUserPrefsParams() +
        (this.secureToken ? '&st=' + this.secureToken : '') +
        '&url=' + encodeURIComponent(this.specUrl) +
        '#rpctoken=' + this.rpcToken +
        (this.viewParams ?
            '&view-params=' + encodeURIComponent(JSON.stringify(this.viewParams)) : '') +
        (this.hashData ? '&' + this.hashData : '');
};

gadgets.IfrGadget.prototype.getUserPrefsParams = function() {
    var params = '';
    if (this.getUserPrefs()) {
        for (var name in this.getUserPrefs()) {
            var value = this.getUserPref(name);
            params += '&up_' + encodeURIComponent(name) + '=' +
                encodeURIComponent(value);
        }
    }
    return params;
}

gadgets.IfrGadget.prototype.handleToggle = function(button) {
    var gadgetIframe = document.getElementById(this.getIframeId());
    if (gadgetIframe) {
        var gadgetContent = gadgetIframe.parentNode;
        var display = gadgetContent.style.display;
        gadgetContent.style.display = display ? '' : 'none';
        if (button.style.backgroundImage == "url(images/gadget-toggle-down.gif)") {
            button.style.backgroundImage = "url(images/gadget-toggle-up.gif)";
        } else
        {
            button.style.backgroundImage = "url(images/gadget-toggle-down.gif)";
        }
    }
};

gadgets.IfrGadget.prototype.handleRemove = function() {
    confirmDialog(jsi18n["remove.gadget"], gadgets.IfrGadget.prototype.removeGadget, this.id);
};

gadgets.IfrGadget.prototype.removeGadget = function(rmId) {
    var response = dashboardService.removeGadget(userId, currentActiveTab, rmId, dashboardName);

    // Refresh the gadget container page
    if (response) {
        //Ajaxifing the gadget removeing
        jQuery('#gadget-chrome_' + rmId).remove();
        if (document.getElementById("maximizedGadget").style.display == 'block') {
            window.location.href = "index.jsp?tab=" + currentActiveTab + '&name=' + dashboardName;
        }
    }
}

gadgets.IfrGadget.prototype.getId = function() {
    return "gadget-chrome_" + this.id;
};

gadgets.IfrGadget.prototype.getTitleBarId = function() {
    return "gadgets-gadget-title-bar-" + this.id;
};

gadgets.IfrGadget.prototype.getMaximizeButtonId = function() {
    return this.getIframeId() + "_maximizeButton";
};
gadgets.IfrGadget.prototype.getMinimizeButtonId = function() {
    return this.getIframeId() + "_minimizeButton";
};

gadgets.IfrGadget.prototype.handleMaximize = function() {

    //hide the columns and display the div that's full screen
    dojo.style("tabContent", "display", "none");
    dojo.style("maximizedGadget", "display", "block");

    //insert a placeholder before the gadget in question
    var placeholder = dojo.doc.createElement("div");
    dojo.attr(placeholder, "id", "maximizedGadgetPlaceholder");
    dojo.place(placeholder, this.getId(), "after");

    //adjust the style on the maximized gadget so that the maximize "changes"
    //to a minimize icon
    dojo.style(this.getMaximizeButtonId(), "display", "none");
    dojo.style(this.getMinimizeButtonId(), "display", "block");

    //move the gadget into the maximizedGadget div
    dojo.place(this.getId(), "maximizedGadget", "first");

    //IE doesn't necessarily refresh, so force it
    dojo.style(this.getIframeId(), "display", "block");

    // Store original height and widths
    this.original_gadget_width = document.getElementById(this.getId()).offsetWidth;
    this.original_gadget_height = document.getElementById(this.getId()).offsetHeight;

    this.original_iframe_height = document.getElementById(this.getIframeId()).offsetHeight;
    this.original_iframe_width = document.getElementById(this.getIframeId()).offsetWidth;

    // Expand Gadget real estate
    document.getElementById(this.getId()).style.width = "100%";
    document.getElementById(this.getIframeId()).style.width = "100%"

    var titlebarHeight = document.getElementById(this.getTitleBarId()).offsetHeight;

    document.getElementById(this.getId()).style.height =
        document.getElementById("maximizedGadget").style.height;
    document.getElementById(this.getIframeId()).style.height =
        (document.getElementById("maximizedGadget").offsetHeight - titlebarHeight - 7) + "px";

    // Set the container view to canvas
    gadgets.container.setView("canvas");

    // Refresh the gadget
    this.refresh();
};

gadgets.IfrGadget.prototype.handleMinimize = function() {
    //restore the gadget to its placeholder
    dojo.place(this.getId(), "maximizedGadgetPlaceholder", "after");
    dojo.query("#maximizedGadgetPlaceholder").orphan();

    //adjust the style on the maximized gadget so that the minimize "changes"
    //to a maximize icon
    dojo.style(this.getMaximizeButtonId(), "display", "block");
    dojo.style(this.getMinimizeButtonId(), "display", "none");

    // Revert to original Gadget size
    document.getElementById(this.getId()).style.width = "100%";
    document.getElementById(this.getIframeId()).style.width = "100%";

    document.getElementById(this.getId()).style.height = "auto";
    document.getElementById(this.getIframeId()).style.height = "auto";

    //hide the columns and display the div that's full screen
    dojo.style("tabContent", "display", "block");
    dojo.style("maximizedGadget", "display", "none");

    // Set the container view to default
    gadgets.container.setView("default");

    // Refresh the gadget
    this.refresh();
};

gadgets.IfrGadget.prototype.handleOpenUserPrefsDialog = function() {
    jQuery.Menu.closeAll();
    if (this.userPrefsDialogContentLoaded) {
        this.showUserPrefsDialog();

    } else {
        var userPrefs = this.userPrefs;
        if (userPrefs == undefined)
            return;

        var content = '<div style="background-color: #F5F5F5; padding-left: 5px; padding-top: 1px;" id="ig_edit_div' + this.id + '"><table><tbody>';
        var hiddenContent = '';
        var numFields = 0;

        for (var name in userPrefs) {
            var dataType = userPrefs[name].type

            // If there already is a set preference get that. Else default to values in gadget.xml
            var defaultValue = this.getUserPref(name);
            if (!defaultValue) {
                defaultValue = userPrefs[name]['default'];
            }

            if (dataType == "hidden") {
                // Hidden field
                hiddenContent +=
                    '<input type="hidden" value="' + defaultValue +
                        '" name="m_' + this.id + '_up_' + name + '" id="m_' + this.id + '_' +
                        numFields +
                        '"/>';

            } else {
                content +=
                    '<tr><td class="m_fieldname_' + this.id + '">' + userPrefs[name].displayName +
                        '</td><td colspan="2">';


                if (dataType == "enum") {
                    // Enumeration
                    content += '<select class="c_enum_' + this.id + '" name="m_' + this.id +
                        '_up_' + name + '" id="m_' + this.id + '_' + numFields + '">';
                    var orderedEnumVals = userPrefs[name].orderedEnumValues;
                    for (var y = 0; y < orderedEnumVals.length; y++) {
                        if (defaultValue == orderedEnumVals[y].value) {
                            content +=
                                '<option value="' + orderedEnumVals[y].value + '" selected="">' +
                                    orderedEnumVals[y].displayValue +
                                    '</option>';
                        }
                        else
                        {
                            content +=
                                '<option value="' + orderedEnumVals[y].value + '">' +
                                    orderedEnumVals[y].displayValue +
                                    '</option>';
                        }
                    }
                    content += '</select>';
                } else if (dataType == "string") {
                    // String
                    content +=
                        '<input type="text" class="c_textbox_"' + this.id + ' value="' + defaultValue +
                            '" name="m_' + this.id + '_up_' + name + '" id="m_' + this.id + '_' +
                            numFields +
                            '" maxlen="200"/>';
                } else if (dataType == "bool") {
                    // Boolean
                    content +=
                        '<input type="checkbox" value="' + defaultValue +
                            '" checked="' + defaultValue +
                            '" class="c_checkbox_' + this.id + '" name="m_' + this.id + '_up_' + name + '" id="m_' + this.id + '_' +
                            numFields + '" onClick="this.value=this.checked ? \'true\':\'false\';"/>';
                }

                content += '</td></tr>';
            }

            numFields++;
        }

        content += '</tbody></table>' +
            '<input type="hidden" value="' + numFields + '" id="m_' + this.id +
            '_numfields"/>' + hiddenContent +
            '</div>';

        this.userPrefsDialogContentLoaded = true;
        this.buildUserPrefsDialog(content);
        this.showUserPrefsDialog();
    }
};

gadgets.IfrGadget.prototype.copyGadget = function() {
    jQuery.Menu.closeAll();
    if (document.getElementById("maximizedGadget").style.display == 'block') {
        gadgets.container.getGadget(this.id).handleMinimize();
    }
    dashboardService.copyGadget(userId, currentActiveTab, dashboardName, this.id);
    makeActive(currentActiveTab);
}

gadgets.IfrGadget.prototype.moveGadgetToTab = function(tabIdToMove) {
    jQuery.Menu.closeAll();
    dashboardService.moveGadgetToTab(userId, tabIdToMove, dashboardName, this.id);
    makeActive(tabIdToMove);
}

gadgets.IfrGadget.prototype.buildUserPrefsDialog = function(content) {
    var userPrefsDialog = document.getElementById(this.getUserPrefsDialogId());
    userPrefsDialog.innerHTML = content +
        '<div class="' + this.cssClassGadgetUserPrefsDialogActionBar +
        '"><input type="button" value="Save" class="button" onclick="gadgets.container.getGadget(' +
        this.id +
        ').handleSaveUserPrefs()"> <input type="button" value="Cancel" class="button" onclick="gadgets.container.getGadget(' +
        this.id + ').handleCancelUserPrefs()"></div>';
    userPrefsDialog.childNodes[0].style.display = '';
};

gadgets.IfrGadget.prototype.showUserPrefsDialog = function(opt_show) {
    var userPrefsDialog = document.getElementById(this.getUserPrefsDialogId());
    userPrefsDialog.style.display = (opt_show || opt_show == undefined)
        ? '' : 'none';
}

gadgets.IfrGadget.prototype.hideUserPrefsDialog = function() {
    this.showUserPrefsDialog(false);
};

gadgets.IfrGadget.prototype.handleSaveUserPrefs = function() {
    this.hideUserPrefsDialog();

    var prefs = {};
    var numFields = document.getElementById('m_' + this.id +
        '_numfields').value;
    for (var i = 0; i < numFields; i++) {
        var input = document.getElementById('m_' + this.id + '_' + i);
        if (input != null) {
            if (input.type != 'hidden') {
                var userPrefNamePrefix = 'm_' + this.id + '_up_';
                var userPrefName = input.name.substring(userPrefNamePrefix.length);
                var userPrefValue = input.value;
                prefs[userPrefName] = userPrefValue;
            }
        }
    }

    this.setUserPrefs(prefs);
    this.refresh();
};

gadgets.IfrGadget.prototype.handleABoutGadget = function() {
    jQuery.Menu.closeAll();
    var message = 'Gadget Name : ' + this.title + '<br/>' + 'Author : ' + this.author + '<br/>' + 'Gadget URL : ' + '<a href="' + this.specUrl + '">' + this.specUrl + '</a><br />'

    //Including the info dialog box inline to fix an IE issue
    var strDialog = "<div id='dialog' title='WSO2 Carbon'><div id='messagebox-info' style='width:600px;'><p>" +
        message + "</p></div></div>";
    jQuery("#dcontainer").html(strDialog);

    jQuery("#dialog").dialog({
        close:function() {
            jQuery(this).dialog('destroy').remove();
            jQuery("#dcontainer").empty();
        },
        buttons:{
            "OK":function() {
                jQuery(this).dialog("destroy").remove();
                jQuery("#dcontainer").empty();
            }
        },
        height:160,
        width:620,
        minHeight:160,
        minWidth:330,
        modal:true
    });
};

gadgets.IfrGadget.prototype.handleCancelUserPrefs = function() {
    this.hideUserPrefsDialog();
};

gadgets.IfrGadget.prototype.refresh = function() {
    var iframeId = this.getIframeId();
    document.getElementById(iframeId).src = this.getIframeUrl();
};


// ---------
// Container

/**
 * Container interface.
 * @constructor
 */
gadgets.Container = function() {
    this.gadgets_ = {};
    this.parentUrl_ = 'http://' + document.location.host;
    this.country_ = 'ALL';
    this.language_ = 'ALL';
    this.view_ = 'default';
    this.nocache_ = 0;

    // Disable gadget caching in debug mode.
    if(debugMode){
        this.nocache_ = 1;
    }

    // signed max int
    this.maxheight_ = 0x7FFFFFFF;
};

gadgets.Container.inherits(gadgets.Extensible);

/**
 * Known dependencies:
 *     gadgetClass: constructor to create a new gadget instance
 *     userPrefStore: instance of a subclass of gadgets.UserPrefStore
 *     gadgetService: instance of a subclass of gadgets.GadgetService
 *     layoutManager: instance of a subclass of gadgets.LayoutManager
 */

gadgets.Container.prototype.gadgetClass = gadgets.Gadget;

gadgets.Container.prototype.userPrefStore = new gadgets.DefaultUserPrefStore();

gadgets.Container.prototype.gadgetService = new gadgets.GadgetService();

gadgets.Container.prototype.layoutManager =
    new gadgets.StaticLayoutManager();

gadgets.Container.prototype.setParentUrl = function(url) {
    this.parentUrl_ = url;
};

gadgets.Container.prototype.setCountry = function(country) {
    this.country_ = country;
};

gadgets.Container.prototype.setNoCache = function(nocache) {
    this.nocache_ = nocache;
};

gadgets.Container.prototype.setLanguage = function(language) {
    this.language_ = language;
};

gadgets.Container.prototype.setView = function(view) {
    this.view_ = view;
};

gadgets.Container.prototype.setMaxHeight = function(maxheight) {
    this.maxheight_ = maxheight;
};

gadgets.Container.prototype.getGadgetKey_ = function(instanceId) {
    return 'gadget_' + instanceId;
};

gadgets.Container.prototype.getGadget = function(instanceId) {
    return this.gadgets_[this.getGadgetKey_(instanceId)];
};

gadgets.Container.prototype.createGadget = function(opt_params) {
    return new this.gadgetClass(opt_params);
};

gadgets.Container.prototype.addGadget = function(gadget) {
    gadget.id = this.getNextGadgetInstanceId();

    if (gadget.id) {
        gadget.setUserPrefs(this.userPrefStore.getPrefs(gadget));
        this.gadgets_[this.getGadgetKey_(gadget.id)] = gadget;
    }
};

gadgets.Container.prototype.addGadgets = function(gadgets) {
    for (var i = 0; i < gadgets.length; i++) {
        this.addGadget(gadgets[i]);
    }
};

/**
 * Renders all gadgets in the container.
 */
gadgets.Container.prototype.renderGadgets = function() {
    for (var key in this.gadgets_) {
        this.renderGadget(this.gadgets_[key]);
    }
};

/**
 * Renders a gadget.  Gadgets are rendered inside their chrome element.
 * @param {Object} gadget Gadget object
 */
gadgets.Container.prototype.renderGadget = function(gadget) {
    throw Error(gadgets.error.SUBCLASS_RESPONSIBILITY);
};

gadgets.Container.prototype.nextGadgetInstanceId_ = 0;

/**
 * Ensures a unique ID corresponfing to a Gadget is returned when asked for
 */
gadgets.Container.prototype.getNextGadgetInstanceId = function() {
    var nextGadgetId = this.nextGadgetInstanceId_;

    // Incrementing the global counter by 1
    this.nextGadgetInstanceId_++;

    // Returning the corresponding Id from the global Gadget ID array
    if (gadgetserver.gadgetLayout != '' && gadgetserver.gadgetLayout != 'NA') {
        //Gadget layout return with its group id; it's been split and Id is taken
        return gadgetserver.gadgetLayout[nextGadgetId];
    }

    return null;
};

/**
 * Refresh all the gadgets in the container.
 */
gadgets.Container.prototype.refreshGadgets = function() {
    for (var key in this.gadgets_) {
        this.gadgets_[key].refresh();
    }
};


// ------------
// IfrContainer

/**
 * Container that renders gadget using ifr.
 * @constructor
 */
gadgets.IfrContainer = function() {
    gadgets.Container.call(this);

    // We need to break the URL up to the last /carbon  and set it as parent URL
    var currentUrl = document.location.href;
    this.parentUrl_ = currentUrl.substring(0, currentUrl.lastIndexOf("carbon"));

};

gadgets.IfrContainer.inherits(gadgets.Container);

gadgets.IfrContainer.prototype.gadgetClass = gadgets.IfrGadget;

gadgets.IfrContainer.prototype.gadgetService = new gadgets.IfrGadgetService();

gadgets.IfrContainer.prototype.setParentUrl = function(url) {
    if (!url.match(/^http[s]?:\/\//)) {
        url = document.location.href.match(/^[^?#]+\//)[0] + url;
    }

    this.parentUrl_ = url;
};

/**
 * Renders a gadget using ifr.
 * @param {Object} gadget Gadget object
 */
gadgets.IfrContainer.prototype.renderGadget = function(gadget) {
    var chrome = this.layoutManager.getGadgetChrome(gadget);
    gadget.render(chrome);
};

/**
 * Default container.
 */
gadgets.container = new gadgets.IfrContainer();
