/*
 ~   Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.
 ~
 ~   Licensed under the Apache License, Version 2.0 (the "License");
 ~   you may not use this file except in compliance with the License.
 ~   You may obtain a copy of the License at
 ~
 ~        http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~   Unless required by applicable law or agreed to in writing, software
 ~   distributed under the License is distributed on an "AS IS" BASIS,
 ~   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ~   See the License for the specific language governing permissions and
 ~   limitations under the License.
 */

/**
 * @description Check jQuery
 * @throw  {String}  throw an exception message if jquery is not loaded
 */
if (typeof(jQuery) === 'undefined') {
    throw 'jQuery is required.';
}

(function ($) {

    /* Copyright 2014+, Federico Zivolo, LICENSE at https://github.com/FezVrasta/bootstrap-material-design/blob/master/LICENSE.md */
    /* globals jQuery, navigator */

    (function ($, window, document, undefined) {

        "use strict";

        /**
         * Define the name of the plugin
         */
        var ripples = "ripples";


        /**
         * Get an instance of the plugin
         */
        var self = null;


        /**
         * Define the defaults of the plugin
         */
        var defaults = {};


        /**
         * Create the main plugin function
         */
        function Ripples(element, options) {
            self = this;

            this.element = $(element);

            this.options = $.extend({}, defaults, options);

            this._defaults = defaults;
            this._name = ripples;

            this.init();
        }


        /**
         * Initialize the plugin
         */
        Ripples.prototype.init = function () {
            var $element = this.element;

            $element.on("mousedown touchstart", function (event) {
                /**
                 * Verify if the user is just touching on a device and return if so
                 */
                if (self.isTouch() && event.type === "mousedown") {
                    return;
                }


                /**
                 * Verify if the current element already has a ripple wrapper element and
                 * creates if it doesn't
                 */
                if (!($element.find(".ripple-container").length)) {
                    $element.append("<div class=\"ripple-container\"></div>");
                }


                /**
                 * Find the ripple wrapper
                 */
                var $wrapper = $element.children(".ripple-container");


                /**
                 * Get relY and relX positions
                 */
                var relY = self.getRelY($wrapper, event);
                var relX = self.getRelX($wrapper, event);


                /**
                 * If relY and/or relX are false, return the event
                 */
                if (!relY && !relX) {
                    return;
                }


                /**
                 * Get the ripple color
                 */
                var rippleColor = self.getRipplesColor($element);


                /**
                 * Create the ripple element
                 */
                var $ripple = $("<div></div>");

                $ripple
                    .addClass("ripple")
                    .css({
                        "left": relX,
                        "top": relY,
                        "background-color": rippleColor
                    });


                /**
                 * Append the ripple to the wrapper
                 */
                $wrapper.append($ripple);


                /**
                 * Make sure the ripple has the styles applied (ugly hack but it works)
                 */
                (function () {
                    return window.getComputedStyle($ripple[0]).opacity;
                })();


                /**
                 * Turn on the ripple animation
                 */
                self.rippleOn($element, $ripple);


                /**
                 * Call the rippleEnd function when the transition "on" ends
                 */
                setTimeout(function () {
                    self.rippleEnd($ripple);
                }, 500);


                /**
                 * Detect when the user leaves the element
                 */
                $element.on("mouseup mouseleave touchend", function () {
                    $ripple.data("mousedown", "off");

                    if ($ripple.data("animating") === "off") {
                        self.rippleOut($ripple);
                    }
                });

            });
        };


        /**
         * Get the new size based on the element height/width and the ripple width
         */
        Ripples.prototype.getNewSize = function ($element, $ripple) {

            return (Math.max($element.outerWidth(), $element.outerHeight()) / $ripple.outerWidth()) * 2.5;
        };


        /**
         * Get the relX
         */
        Ripples.prototype.getRelX = function ($wrapper, event) {
            var wrapperOffset = $wrapper.offset();

            if (!self.isTouch()) {
                /**
                 * Get the mouse position relative to the ripple wrapper
                 */
                return event.pageX - wrapperOffset.left;
            } else {
                /**
                 * Make sure the user is using only one finger and then get the touch
                 * position relative to the ripple wrapper
                 */
                event = event.originalEvent;

                if (event.touches.length === 1) {
                    return event.touches[0].pageX - wrapperOffset.left;
                }

                return false;
            }
        };


        /**
         * Get the relY
         */
        Ripples.prototype.getRelY = function ($wrapper, event) {
            var wrapperOffset = $wrapper.offset();

            if (!self.isTouch()) {
                /**
                 * Get the mouse position relative to the ripple wrapper
                 */
                return event.pageY - wrapperOffset.top;
            } else {
                /**
                 * Make sure the user is using only one finger and then get the touch
                 * position relative to the ripple wrapper
                 */
                event = event.originalEvent;

                if (event.touches.length === 1) {
                    return event.touches[0].pageY - wrapperOffset.top;
                }

                return false;
            }
        };


        /**
         * Get the ripple color
         */
        Ripples.prototype.getRipplesColor = function ($element) {

            var color = $element.data("ripple-color") ? $element.data("ripple-color") : window.getComputedStyle($element[0]).color;

            return color;
        };


        /**
         * Verify if the client browser has transistion support
         */
        Ripples.prototype.hasTransitionSupport = function () {
            var thisBody = document.body || document.documentElement;
            var thisStyle = thisBody.style;

            var support = (
                thisStyle.transition !== undefined ||
                thisStyle.WebkitTransition !== undefined ||
                thisStyle.MozTransition !== undefined ||
                thisStyle.MsTransition !== undefined ||
                thisStyle.OTransition !== undefined
            );

            return support;
        };


        /**
         * Verify if the client is using a mobile device
         */
        Ripples.prototype.isTouch = function () {
            return /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent);
        };


        /**
         * End the animation of the ripple
         */
        Ripples.prototype.rippleEnd = function ($ripple) {
            $ripple.data("animating", "off");

            if ($ripple.data("mousedown") === "off") {
                self.rippleOut($ripple);
            }
        };


        /**
         * Turn off the ripple effect
         */
        Ripples.prototype.rippleOut = function ($ripple) {
            $ripple.off();

            if (self.hasTransitionSupport()) {
                $ripple.addClass("ripple-out");
            } else {
                $ripple.animate({"opacity": 0}, 100, function () {
                    $ripple.trigger("transitionend");
                });
            }

            $ripple.on("transitionend webkitTransitionEnd oTransitionEnd MSTransitionEnd", function () {
                $ripple.remove();
            });
        };


        /**
         * Turn on the ripple effect
         */
        Ripples.prototype.rippleOn = function ($element, $ripple) {
            var size = self.getNewSize($element, $ripple);

            if (self.hasTransitionSupport()) {
                $ripple
                    .css({
                        "-ms-transform": "scale(" + size + ")",
                        "-moz-transform": "scale(" + size + ")",
                        "-webkit-transform": "scale(" + size + ")",
                        "transform": "scale(" + size + ")"
                    })
                    .addClass("ripple-on")
                    .data("animating", "on")
                    .data("mousedown", "on");
            } else {
                $ripple.animate({
                    "width": Math.max($element.outerWidth(), $element.outerHeight()) * 2,
                    "height": Math.max($element.outerWidth(), $element.outerHeight()) * 2,
                    "margin-left": Math.max($element.outerWidth(), $element.outerHeight()) * (-1),
                    "margin-top": Math.max($element.outerWidth(), $element.outerHeight()) * (-1),
                    "opacity": 0.2
                }, 500, function () {
                    $ripple.trigger("transitionend");
                });
            }
        };


        /**
         * Create the jquery plugin function
         */
        $.fn.ripples = function (options) {
            return this.each(function () {
                if (!$.data(this, "plugin_" + ripples)) {
                    $.data(this, "plugin_" + ripples, new Ripples(this, options));
                }
            });
        };

    })(jQuery, window, document);

    /* globals jQuery */

    (function ($) {
        // Selector to select only not already processed elements
        $.expr[":"].notmdproc = function (obj) {
            if ($(obj).data("mdproc")) {
                return false;
            } else {
                return true;
            }
        };

        function _isChar(evt) {
            if (typeof evt.which == "undefined") {
                return true;
            } else if (typeof evt.which == "number" && evt.which > 0) {
                return (
                    !evt.ctrlKey
                    && !evt.metaKey
                    && !evt.altKey
                    && evt.which != 8  // backspace
                    && evt.which != 9  // tab
                    && evt.which != 13 // enter
                    && evt.which != 16 // shift
                    && evt.which != 17 // ctrl
                    && evt.which != 20 // caps lock
                    && evt.which != 27 // escape
                );
            }
            return false;
        }

        function _addFormGroupFocus(element) {
            var $element = $(element);
            if (!$element.prop('disabled')) {  // this is showing as undefined on chrome but works fine on firefox??
                $element.closest(".form-group").addClass("is-focused");
            }
        }

        function _toggleDisabledState($element, state) {
            var $target;
            if ($element.hasClass('checkbox-inline') || $element.hasClass('radio-inline')) {
                $target = $element;
            } else {
                $target = $element.closest('.checkbox').length ? $element.closest('.checkbox') : $element.closest('.radio');
            }
            return $target.toggleClass('disabled', state);
        }

        function _toggleTypeFocus($input) {
            var disabledToggleType = false;
            if ($input.is($.material.options.checkboxElements) || $input.is($.material.options.radioElements)) {
                disabledToggleType = true;
            }
            $input.closest('label').hover(function () {
                    var $i = $(this).find('input');
                    var isDisabled = $i.prop('disabled'); // hack because the _addFormGroupFocus() wasn't identifying the property on chrome
                    if (disabledToggleType) {
                        _toggleDisabledState($(this), isDisabled);
                    }
                    if (!isDisabled) {
                        _addFormGroupFocus($i);     // need to find the input so we can check disablement
                    }
                },
                function () {
                    _removeFormGroupFocus($(this).find('input'));
                });
        }

        function _removeFormGroupFocus(element) {
            $(element).closest(".form-group").removeClass("is-focused"); // remove class from form-group
        }

        $.material = {
            "options": {
                // These options set what will be started by $.material.init()
                "validate": true,
                "input": true,
                "ripples": true,
                "checkbox": true,
                "togglebutton": true,
                "radio": true,
                "arrive": true,
                "autofill": false,

                "withRipples": [
                    ".btn:not(.btn-link)",
                    ".card-image",
                    ".navbar a:not(.withoutripple)",
                    ".dropdown-menu a",
                    ".nav-tabs a:not(.withoutripple)",
                    ".withripple",
                    ".pagination li:not(.active):not(.disabled) a:not(.withoutripple)"
                ].join(","),
                "inputElements": "input.form-control, textarea.form-control, select.form-control",
                "checkboxElements": ".checkbox > label > input[type=checkbox], label.checkbox-inline > input[type=checkbox]",
                "togglebuttonElements": ".togglebutton > label > input[type=checkbox]",
                "radioElements": ".radio > label > input[type=radio], label.radio-inline > input[type=radio]"
            },
            "checkbox": function (selector) {
                // Add fake-checkbox to material checkboxes
                var $input = $((selector) ? selector : this.options.checkboxElements)
                    .filter(":notmdproc")
                    .data("mdproc", true)
                    .after("<span class='checkbox-material'><span class='check'></span></span>");

                _toggleTypeFocus($input);
            },
            "togglebutton": function (selector) {
                // Add fake-checkbox to material checkboxes
                var $input = $((selector) ? selector : this.options.togglebuttonElements)
                    .filter(":notmdproc")
                    .data("mdproc", true)
                    .after("<span class='toggle'></span>");

                _toggleTypeFocus($input);
            },
            "radio": function (selector) {
                // Add fake-radio to material radios
                var $input = $((selector) ? selector : this.options.radioElements)
                    .filter(":notmdproc")
                    .data("mdproc", true)
                    .after("<span class='circle'></span><span class='check'></span>");

                _toggleTypeFocus($input);
            },
            "input": function (selector) {
                $((selector) ? selector : this.options.inputElements)
                    .filter(":notmdproc")
                    .data("mdproc", true)
                    .each(function () {
                        var $input = $(this);

                        // Requires form-group standard markup (will add it if necessary)
                        var $formGroup = $input.closest(".form-group"); // note that form-group may be grandparent in the case of an input-group
                        if ($formGroup.length === 0 && $input.attr('type') !== "hidden" && !$input.attr('hidden')) {
                            $input.wrap("<div class='form-group'></div>");
                            $formGroup = $input.closest(".form-group"); // find node after attached (otherwise additional attachments don't work)
                        }

                        // Legacy - Add hint label if using the old shorthand data-hint attribute on the input
                        if ($input.attr("data-hint")) {
                            $input.after("<p class='help-block'>" + $input.attr("data-hint") + "</p>");
                            $input.removeAttr("data-hint");
                        }

                        // Legacy - Change input-sm/lg to form-group-sm/lg instead (preferred standard and simpler css/less variants)
                        var legacySizes = {
                            "input-lg": "form-group-lg",
                            "input-sm": "form-group-sm"
                        };
                        $.each(legacySizes, function (legacySize, standardSize) {
                            if ($input.hasClass(legacySize)) {
                                $input.removeClass(legacySize);
                                $formGroup.addClass(standardSize);
                            }
                        });

                        // Legacy - Add label-floating if using old shorthand <input class="floating-label" placeholder="foo">
                        if ($input.hasClass("floating-label")) {
                            var placeholder = $input.attr("placeholder");
                            $input.attr("placeholder", null).removeClass("floating-label");
                            var id = $input.attr("id");
                            var forAttribute = "";
                            if (id) {
                                forAttribute = "for='" + id + "'";
                            }
                            $formGroup.addClass("label-floating");
                            $input.after("<label " + forAttribute + "class='control-label'>" + placeholder + "</label>");
                        }

                        // Set as empty if is empty (damn I must improve this...)
                        if ($input.val() === null || $input.val() == "undefined" || $input.val() === "") {
                            $formGroup.addClass("is-empty");
                        }

                        // Support for file input
                        if ($formGroup.find("input[type=file]").length > 0) {
                            $formGroup.addClass("is-fileinput");
                        }
                    });
            },
            "attachInputEventHandlers": function () {
                var validate = this.options.validate;

                $(document)
                    .on("keydown paste", ".form-control", function (e) {
                        if (_isChar(e)) {
                            $(this).closest(".form-group").removeClass("is-empty");
                        }
                    })
                    .on("keyup change", ".form-control", function () {
                        var $input = $(this);
                        var $formGroup = $input.closest(".form-group");
                        var isValid = (typeof $input[0].checkValidity === "undefined" || $input[0].checkValidity());

                        if ($input.val() === "") {
                            $formGroup.addClass("is-empty");
                        }
                        else {
                            $formGroup.removeClass("is-empty");
                        }

                        // Validation events do not bubble, so they must be attached directly to the input: http://jsfiddle.net/PEpRM/1/
                        //  Further, even the bind method is being caught, but since we are already calling #checkValidity here, just alter
                        //  the form-group on change.
                        //
                        // NOTE: I'm not sure we should be intervening regarding validation, this seems better as a README and snippet of code.
                        //        BUT, I've left it here for backwards compatibility.
                        if (validate) {
                            if (isValid) {
                                $formGroup.removeClass("has-error");
                            }
                            else {
                                $formGroup.addClass("has-error");
                            }
                        }
                    })
                    .on("focus", ".form-control, .form-group.is-fileinput", function () {
                        _addFormGroupFocus(this);
                    })
                    .on("blur", ".form-control, .form-group.is-fileinput", function () {
                        _removeFormGroupFocus(this);
                    })
                    // make sure empty is added back when there is a programmatic value change.
                    //  NOTE: programmatic changing of value using $.val() must trigger the change event i.e. $.val('x').trigger('change')
                    .on("change", ".form-group input", function () {
                        var $input = $(this);
                        if ($input.attr("type") == "file") {
                            return;
                        }

                        var $formGroup = $input.closest(".form-group");
                        var value = $input.val();
                        if (value) {
                            $formGroup.removeClass("is-empty");
                        } else {
                            $formGroup.addClass("is-empty");
                        }
                    })
                    // set the fileinput readonly field with the name of the file
                    .on("change", ".form-group.is-fileinput input[type='file']", function () {
                        var $input = $(this);
                        var $formGroup = $input.closest(".form-group");
                        var value = "";
                        $.each(this.files, function (i, file) {
                            value += file.name + ", ";
                        });
                        value = value.substring(0, value.length - 2);
                        if (value) {
                            $formGroup.removeClass("is-empty");
                        } else {
                            $formGroup.addClass("is-empty");
                        }
                        $formGroup.find("input.form-control[readonly]").val(value);
                    });
            },
            "ripples": function (selector) {
                $((selector) ? selector : this.options.withRipples).ripples();
            },
            "autofill": function () {
                // This part of code will detect autofill when the page is loading (username and password inputs for example)
                var loading = setInterval(function () {
                    $("input[type!=checkbox]").each(function () {
                        var $this = $(this);
                        if ($this.val() && $this.val() !== $this.attr("value")) {
                            $this.trigger("change");
                        }
                    });
                }, 100);

                // After 10 seconds we are quite sure all the needed inputs are autofilled then we can stop checking them
                setTimeout(function () {
                    clearInterval(loading);
                }, 10000);
            },
            "attachAutofillEventHandlers": function () {
                // Listen on inputs of the focused form (because user can select from the autofill dropdown only when the input has focus)
                var focused;
                $(document)
                    .on("focus", "input", function () {
                        var $inputs = $(this).parents("form").find("input").not("[type=file]");
                        focused = setInterval(function () {
                            $inputs.each(function () {
                                var $this = $(this);
                                if ($this.val() !== $this.attr("value")) {
                                    $this.trigger("change");
                                }
                            });
                        }, 100);
                    })
                    .on("blur", ".form-group input", function () {
                        clearInterval(focused);
                    });
            },
            "init": function (options) {
                this.options = $.extend({}, this.options, options);
                var $document = $(document);

                if ($.fn.ripples && this.options.ripples) {
                    this.ripples();
                }
                if (this.options.input) {
                    this.input();
                    this.attachInputEventHandlers();
                }
                if (this.options.checkbox) {
                    this.checkbox();
                }
                if (this.options.togglebutton) {
                    this.togglebutton();
                }
                if (this.options.radio) {
                    this.radio();
                }
                if (this.options.autofill) {
                    this.autofill();
                    this.attachAutofillEventHandlers();
                }

                if (document.arrive && this.options.arrive) {
                    if ($.fn.ripples && this.options.ripples) {
                        $document.arrive(this.options.withRipples, function () {
                            $.material.ripples($(this));
                        });
                    }
                    if (this.options.input) {
                        $document.arrive(this.options.inputElements, function () {
                            $.material.input($(this));
                        });
                    }
                    if (this.options.checkbox) {
                        $document.arrive(this.options.checkboxElements, function () {
                            $.material.checkbox($(this));
                        });
                    }
                    if (this.options.radio) {
                        $document.arrive(this.options.radioElements, function () {
                            $.material.radio($(this));
                        });
                    }
                    if (this.options.togglebutton) {
                        $document.arrive(this.options.togglebuttonElements, function () {
                            $.material.togglebutton($(this));
                        });
                    }

                }
            }
        };

    })(jQuery);

    /**
     * @description Dependancy injection function
     * @param  {String}     File    Name of the dependancy
     * @param  {String}     Type    Dependancy type
     * @return {Null}
     */
    $.required = function (file, filetype) {
        var markup = 'undefined';

        if (filetype == 'js') { //if filename is a external JavaScript file
            markup = document.createElement('script');
            markup.setAttribute("type", "text/javascript");
            markup.setAttribute("src", file);
        } else if (filetype == 'css') { //if filename is an external CSS file
            markup = document.createElement('link');
            markup.setAttribute("rel", "stylesheet");
            markup.setAttribute("type", "text/css");
            markup.setAttribute("href", file);
        }

        if (typeof markup != 'undefined') {
            if (filetype == 'js') {
                $('html script[src*="theme-wso2.js"]').before(markup);
            } else if (filetype == 'css') {
                $('head link[href*="main.less"]').before(markup);
            }
        }
    };

    /**
     * @description Attribute toggle function
     * @param  {String} attr    Attribute Name
     * @param  {String} val     Value to be matched
     * @param  {String} val2    Value to be replaced with
     */
    $.fn.toggleAttr = function (attr, val, val2) {
        return this.each(function () {
            var self = $(this);
            if (self.attr(attr) == val)
                self.attr(attr, val2);
            else
                self.attr(attr, val);
        });
    };


    /**
     * A function to add data attributes to HTML about the user agent
     * @return {Null}
     */
    $.browser_meta = function () {
        $('html')
            .attr('data-useragent', navigator.userAgent)
            .attr('data-platform', navigator.platform)
            .addClass(((!!('ontouchstart' in window) || !!('onmsgesturechange' in window)) ? ' touch' : ''));
    };

    /**
     * Cross browser file input controller
     * @return {Node} DOM Node
     */
    $.file_input = function () {
        var elem = '.file-upload-control';

        return $(elem).each(function () {

            //Input value change function
            $(elem + ' :file').change(function () {
                var input = $(this),
                    numFiles = input.get(0).files ? input.get(0).files.length : 1,
                    label = input.val().replace(/\\/g, '/').replace(/.*\//, '');
                input.trigger('fileselect', [numFiles, label]);
            });

            //Button click function
            $(elem + ' .browse').click(function () {
                $(this).parents('.input-group').find(':file').click();
            });

            //File select function
            $(elem + ' :file').on('fileselect', function (event, numFiles, label) {
                var input = $(this).parents('.input-group').find(':text'),
                    log = numFiles > 1 ? numFiles + ' files selected' : label;

                if (input.length) {
                    input.val(log);
                } else {
                    if (log) {
                        alert(log);
                    }
                }
            });

        });
    };

    /**
     * @description Data Loader function
     * @param  {String}     action of the loader
     */
    $.fn.loading = function (action) {

        return $(this).each(function () {
            var loadingText = ($(this).data('loading-text') === undefined) ? 'LOADING' : $(this).data('loading-text');

            var icon = ($(this).data('loading-image') === undefined) ? '' +
                '<svg version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px"' +
                'viewBox="0 0 14 14" enable-background="new 0 0 14 14" xml:space="preserve">' +
                '<path class="circle" stroke-width="1.4" stroke-miterlimit="10" d="M6.534,0.748C7.546,0.683,8.578,0.836,9.508,1.25 c1.903,0.807,3.339,2.615,3.685,4.654c0.244,1.363,0.028,2.807-0.624,4.031c-0.851,1.635-2.458,2.852-4.266,3.222 c-1.189,0.25-2.45,0.152-3.583-0.289c-1.095-0.423-2.066-1.16-2.765-2.101C1.213,9.78,0.774,8.568,0.718,7.335 C0.634,5.866,1.094,4.372,1.993,3.207C3.064,1.788,4.76,0.867,6.534,0.748z"/>' +
                '<path class="pulse-line" stroke-width="0.55" stroke-miterlimit="10" d="M12.602,7.006c-0.582-0.001-1.368-0.001-1.95,0 c-0.491,0.883-0.782,1.4-1.278,2.28C8.572,7.347,7.755,5.337,6.951,3.399c-0.586,1.29-1.338,3.017-1.923,4.307 c-1.235,0-2.38-0.002-3.615,0"/>' +
                '</svg>' +
                '<div class="signal"></div>'
                :
                '<img src="' + $(this).data('loading-image') + '" />';

            var html = '<div class="loading-animation">' +
                '<div class="logo">' +
                icon +
                '</div>' +
                '<p>' + loadingText + '</p>' +
                '</div>' +
                '<div class="loading-bg"></div>';

            if (action === 'show') {
                $(this).prepend(html).addClass('loading');
            }
            if (action === 'hide') {
                $(this).removeClass('loading');
                $('.loading-animation, .loading-bg', this).remove();
            }
        });

    };

    /**
     * @description Auto resize icons and text on browser resize
     * @param  {Number}     Compression Ratio
     * @param  {Object}     Object containing the values to override defaults
     * @return {Node}       DOM Node
     */
    $.fn.responsive_text = function (compress, options) {

        // Setup options
        var compressor = compress || 1,
            settings = $.extend({
                'minFontSize': Number.NEGATIVE_INFINITY,
                'maxFontSize': Number.POSITIVE_INFINITY
            }, options);

        return this.each(function () {

            //Cache object for performance
            var $this = $(this);

            //resize items based on the object width devided by the compressor
            var resizer = function () {
                $this.css('font-size', Math.max(Math.min($this.width() / (compressor * 10), parseFloat(settings.maxFontSize)), parseFloat(settings.minFontSize)));
            };

            //Init method
            resizer();

            //event bound to browser window to fire on window resize
            $(window).on('resize.fittext orientationchange.fittext', resizer);

        });

    };

    /**
     * Tree view function
     * @return {Null}
     */
    $.fn.tree_view = function () {
        var tree = $(this);
        tree.find('li').has("ul").each(function () {
            var branch = $(this); //li with children ul
            branch.prepend('<i class="icon"></i>');
            branch.addClass('branch');
            branch.on('click', function (e) {
                if (this == e.target) {
                    var icon = $(this).children('i:first');
                    icon.closest('li').toggleAttr('aria-expanded', 'true', 'false');
                }
            });
        });

        tree.find('.branch .icon').each(function () {
            $(this).on('click', function () {
                $(this).closest('li').click();
            });
        });

        tree.find('.branch > a').each(function () {
            $(this).on('click', function (e) {
                $(this).closest('li').click();
                e.preventDefault();
            });
        });

        tree.find('.branch > button').each(function () {
            $(this).on('click', function (e) {
                $(this).closest('li').click();
                e.preventDefault();
            });
        });
    };

    /**
     * Sidebar function
     * @return {Null}
     */
    $.sidebar_toggle = function (action, target, container) {
        var elem = '[data-toggle=sidebar]',
            button,
            container,
            conrainerOffsetLeft,
            conrainerOffsetRight,
            target,
            targetOffsetLeft,
            targetOffsetRight,
            targetWidth,
            targetSide,
            relationship,
            pushType,
            buttonParent;

        /**
         * Dynamically adjust the height of sidebar to fill parent
         */
        function sidebarHeightAdjust() {
            $('.sidebar-wrapper').each(function () {
                var elemOffsetBottom = $(this).data('offset-bottom'),
                    scrollBottom = ($(document).height() - $(window).height()),
                    offesetBottom = 0,
                    getBottomOffset = elemOffsetBottom - (scrollBottom - ($(window).scrollTop() - elemOffsetBottom) - elemOffsetBottom);

                if (getBottomOffset > 0) {
                    offesetBottom = getBottomOffset;
                }

                $(this).height(($(window).height() - ($(this).offset().top - $(window).scrollTop())) - offesetBottom);

                if ((typeof $.fn.nanoScroller == 'function') && ($('.nano-content', this).length > 0)) {
                    $(".nano-content", this).parent()[0].nanoscroller.reset();
                }
            });
        };

        var sidebar_window = {
            update: function (target, container, button) {
                conrainerOffsetLeft = $(container).data('offset-left') ? $(container).data('offset-left') : 0,
                    conrainerOffsetRight = $(container).data('offset-right') ? $(container).data('offset-right') : 0,
                    targetTop = $(target).data('top') ? $(target).data('top') : 0,
                    targetOffsetLeft = $(target).data('offset-left') ? $(target).data('offset-left') : 0,
                    targetOffsetRight = $(target).data('offset-right') ? $(target).data('offset-right') : 0,
                    targetWidth = $(target).data('width'),
                    targetSide = $(target).data("side"),
                    pushType = $(container).parent().is('body') == true ? 'padding' : 'padding'; //TODO: Remove if works everywhere

                $(container).addClass('sidebar-target');

                if (button !== undefined) {
                    relationship = button.attr('rel') ? button.attr('rel') : '';
                    buttonParent = $(button).parent();
                }

                $(target).css('top', targetTop);

                sidebarHeightAdjust();
            },
            show: function () {
                if ($(target).data('sidebar-fixed') == true) {
                    $(target).height($(window).height() - $(target).data('fixed-offset'));
                }

                $(target).off('webkitTransitionEnd otransitionend oTransitionEnd msTransitionEnd transitionend');
                $(target).trigger('show.sidebar');

                if (targetWidth !== undefined) {
                    $(target).css('width', targetWidth);
                }

                $(target).addClass('toggled');

                if (button !== undefined) {
                    if (relationship !== '') {
                        // Removing active class from all relative buttons
                        $(elem + '[rel=' + relationship + ']:not([data-handle=close])').removeClass("active");
                        $(elem + '[rel=' + relationship + ']:not([data-handle=close])').attr('aria-expanded', 'false');
                    }

                    // Adding active class to button
                    if (button.attr('data-handle') !== 'close') {
                        button.addClass("active");
                        button.attr('aria-expanded', 'true');
                    }

                    if (buttonParent.is('li')) {
                        if (relationship !== '') {
                            $(elem + '[rel=' + relationship + ']:not([data-handle=close])').parent().removeClass("active");
                            $(elem + '[rel=' + relationship + ']:not([data-handle=close])').parent().attr('aria-expanded', 'false');
                        }
                        buttonParent.addClass("active");
                        buttonParent.attr('aria-expanded', 'true');
                    }
                }

                // Sidebar open function
                if (targetSide == 'left') {
                    if ($(target).attr('data-container-divide')) {
                        $(container).css(pushType + '-' + targetSide, targetWidth + targetOffsetLeft);
                        $(target).css(targetSide, targetOffsetLeft);
                    }
                    else if ($(target).attr('data-container-push')) {
                        $(container).css(targetSide, Math.abs(targetWidth + targetOffsetLeft));
                        $(target).css(targetSide, -Math.abs(targetWidth + targetOffsetLeft));
                    }
                    else {
                        $(target).css(targetSide, Math.abs(targetOffsetLeft));
                    }
                }
                else if (targetSide == 'right') {
                    if ($(target).attr('data-container-divide')) {
                        $(container).css(pushType + '-' + targetSide, targetWidth + targetOffsetRight);
                        $(target).css(targetSide, targetOffsetRight);
                    }
                    else if ($(target).attr('data-container-push')) {
                        $(container).css(targetSide, Math.abs(targetWidth + targetOffsetRight));
                        $(target).css(targetSide, -Math.abs(targetWidth + targetOffsetRight));
                    }
                    else {
                        $(target).css(targetSide, Math.abs(targetOffsetRight));
                    }
                }

                $(target).trigger('shown.sidebar');
            },
            hide: function () {
                $(target).trigger('hide.sidebar');
                $(target).removeClass('toggled');

                if (button !== undefined) {
                    if (relationship !== '') {
                        // Removing active class from all relative buttons
                        $(elem + '[rel=' + relationship + ']:not([data-handle=close])').removeClass("active");
                        $(elem + '[rel=' + relationship + ']:not([data-handle=close])').attr('aria-expanded', 'false');
                    }
                    // Removing active class from button
                    if (button.attr('data-handle') !== 'close') {
                        button.removeClass("active");
                        button.attr('aria-expanded', 'false');
                    }

                    if ($(button).parent().is('li')) {
                        if (relationship !== '') {
                            $(elem + '[rel=' + relationship + ']:not([data-handle=close])').parent().removeClass("active");
                            $(elem + '[rel=' + relationship + ']:not([data-handle=close])').parent().attr('aria-expanded', 'false');
                        }
                    }
                }

                // Sidebar close function
                if (targetSide == 'left') {
                    if ($(target).attr('data-container-divide')) {
                        $(container).css(pushType + '-' + targetSide, targetOffsetLeft);
                        $(target).css(targetSide, -Math.abs(targetWidth + targetOffsetRight));
                    }
                    else if ($(target).attr('data-container-push')) {
                        $(container).css(targetSide, targetOffsetLeft);
                        $(target).css(targetSide, -Math.abs(targetWidth + targetOffsetLeft));
                    }
                    else {
                        $(target).css(targetSide, -Math.abs(targetWidth + targetOffsetLeft));
                    }
                }
                else if (targetSide == 'right') {
                    if ($(target).attr('data-container-divide')) {
                        $(container).css(pushType + '-' + targetSide, targetOffsetRight);
                        $(target).css(targetSide, -Math.abs(targetWidth + targetOffsetRight));
                    }
                    else if ($(target).attr('data-container-push')) {
                        $(container).css(targetSide, targetOffsetRight);
                        $(target).css(targetSide, -Math.abs(targetWidth + targetOffsetRight));
                    }
                    else {
                        $(target).css(targetSide, -Math.abs(targetWidth + targetOffsetRight));
                    }
                }

                $(target).trigger('hidden.sidebar');
                $(target).on('webkitTransitionEnd otransitionend oTransitionEnd msTransitionEnd transitionend', function (e) {
                    $(container).removeClass('sidebar-target');
                });
            }
        };

        if (action === 'show') {
            sidebar_window.update(target, container);
            sidebar_window.show();
        }
        if (action === 'hide') {
            sidebar_window.update(target, container);
            sidebar_window.hide();
        }

        // binding click function
        $('body').off('click', elem);
        $('body').on('click', elem, function (e) {
            e.preventDefault();

            button = $(this);
            target = button.data('target');
            container = $(target).data('container');
            sidebar_window.update(target, container, button);

            /**
             * Sidebar function on data container divide
             * @return {Null}
             */
            if (button.attr('aria-expanded') == 'false') {
                sidebar_window.show();
            }
            else if (button.attr('aria-expanded') == 'true') {
                sidebar_window.hide();
            }

        });

        $(window)
            .load(sidebarHeightAdjust)
            .resize(sidebarHeightAdjust)
            .scroll(sidebarHeightAdjust);

    };

    $('.sidebar-wrapper[data-fixed-offset-top]').on('affix.bs.affix', function () {
        $(this).css('top', $(this).data('fixed-offset-top'));
    });

    /**
     * collapse nav sub-level function
     * @return {Null}
     */
    $.fn.collapse_nav_sub = function () {

        var navSelector = 'ul.nav';

        if (!$(navSelector).hasClass('collapse-nav-sub')) {
            $(navSelector + ' > li', this).each(function () {
                var position = $(this).offset().left - $(this).parent().scrollLeft();
                $(this).attr('data-absolute-position', (position + 5));
            });

            $(navSelector + ' li', this).each(function () {
                if ($('ul', this).length !== 0) {
                    $(this).addClass('has-sub');
                }
            });

            $(navSelector + ' > li', this).each(function () {
                $(this).css({
                    'left': $(this).data('absolute-position'),
                    'position': 'absolute'
                });
            });

            $(navSelector + ' li.has-sub', this).on('click', function () {
                var elem = $(this);
                if (elem.attr('aria-expanded') !== 'true') {
                    elem.siblings().fadeOut(100, function () {
                        elem.animate({'left': '15'}, 200, function () {
                            $(elem).first().children('ul').fadeIn(200);
                        });
                    });
                    elem.siblings().attr('aria-expanded', 'false');
                    elem.attr('aria-expanded', 'true');
                }
                else {
                    $(elem).first().children('ul').fadeOut(100, function () {
                        elem.animate({'left': $(elem).data('absolute-position')}, 200, function () {
                            elem.siblings().fadeIn(100);
                        });
                    });
                    elem.siblings().attr('aria-expanded', 'false');
                    elem.attr('aria-expanded', 'false');
                }
            });

            $(navSelector + ' > li.has-sub ul', this).on('click', function (e) {
                e.stopPropagation();
            });

            $(navSelector).addClass('collapse-nav-sub');
        }
    };

    /**
     * Copy to clipboard function using ZeroClipboard plugin
     * @return object
     */
    $.fn.zclip = function () {

        if (typeof ZeroClipboard == 'function') {
            var client = new ZeroClipboard(this);
            client.on("ready", function (readyEvent) {
                client.on("aftercopy", function (event) {
                    var target = $(event.target);
                    target.attr("title", "Copied!")
                    target.tooltip('enable');
                    target.tooltip("show");
                    target.tooltip('disable');
                });
            });
        } else {
            console.warn('Warning : Dependency missing - ZeroClipboard Library');
        }
        return this;
    };

    /**
     * @description Random background color generator for thumbs
     * @param  {range}      Color Range Value
     * @return {Node}       DOM Node
     */
    $.fn.random_background_color = function (range) {

        if (!range) {
            range = 9;
        }

        return this.each(function () {

            var color = '#' + Math.random().toString(range).substr(-6);
            $(this).css('background', color);

        });

    };

    var responsiveTextRatio = 0.2,
        responsiveTextSleector = ".icon .text";

    (function ($) {

        /**
         * Mutationobserver wrapper function
         * @usage  $(el).MutationObserverWrapper({
     *             config: {
     *                 childList : true
     *             },
     *             callback : function(mutationObj){
     *
     *             }
     *         })
         * @return {Null}
         */
        $.fn.MutationObserverWrapper = function (options) {

            if (isSupported() === false) {
                throw 'Mutation observer is not supported by your browser.'
            }

            var defaults = {
                config: {
                    childList: true,
                    attributes: true,
                    characterData: true,
                    subtree: true,
                    attributeOldValue: true,
                    characterDataOldValue: true,
                    attributeFilter: []
                },
                callback: function (mutations) {
                    mutations.forEach(function (mutation) {
                        console.log(mutation);
                    })
                }
            };

            var target = $(this)[0];
            var plugin = $.fn.MutationObserverWrapper;
            var options = $.extend({}, defaults, options);

            var observer = new MutationObserver(options.callback);

            observer.observe(target, options.config);

        };

        function isSupported() {
            var prefixes = ['WebKit', 'Moz', 'O', 'Ms', ''];
            for (var i = 0; i < prefixes.length; i++) {
                if (prefixes[i] + 'MutationObserver' in window) {
                    return window[prefixes[i] + 'MutationObserver'];
                }
            }
            return false;
        }

    }(jQuery));

    $(document).ready(function () {

        $('.tree-view').tree_view();
        $.file_input();
        $.sidebar_toggle();

        $('.dropdown-menu input').click(function (e) {
            e.stopPropagation();
        });

        $(responsiveTextSleector).responsive_text(responsiveTextRatio);

        if (typeof $.fn.select2 == 'function') {
            $('.select2').select2();
        } else {
            console.warn('Warning : Dependency missing - Select2 Library');
        }

        if (typeof $.fn.collapse == 'function') {
            $('.navbar-collapse.tiles').on('shown.bs.collapse', function () {
                $(this).collapse_nav_sub();
            });
        }
        else {
            console.warn('Warning : Dependency missing - Bootstrap Collapse Library');
        }


        $('.media.tab-responsive [data-toggle=tab]').on('shown.bs.tab', function (e) {
            console.log("shown");
            var activeTabPane = $(e.target).attr('href'),
                activeCollpasePane = $(activeTabPane).find('[data-toggle=collapse]').data('target'),
                activeCollpasePaneSiblings = $(activeTabPane).siblings().find('[data-toggle=collapse]').data('target'),
                activeListGroupItem = $('.media .list-group-item.active');

            $(activeCollpasePaneSiblings).collapse('hide');
            $(activeCollpasePane).collapse('show');
            positionArrow(activeListGroupItem);

            $(".panel-heading .caret-updown").removeClass("fw-sort-down");
            $(".panel-heading.collapsed .caret-updown").addClass("fw-sort-up");
        });

        $('.media.tab-responsive .tab-content').on('shown.bs.collapse', function (e) {
            var activeTabPane = $(e.target).parent().attr('id');
            $('.media.tab-responsive [data-toggle=tab][href=#' + activeTabPane + ']').tab('show');
            $(".panel-heading .caret-updown").removeClass("fw-sort-up");
            $(".panel-heading.collapsed .caret-updown").addClass("fw-sort-down");
        });

        function positionArrow(selectedTab) {
            var selectedTabHeight = $(selectedTab).outerHeight();
            var arrowPosition = 0;
            var totalHeight = 0;
            var arrow = $(".media .panel-group.tab-content .arrow-left");
            var parentHeight = $(arrow).parent().outerHeight();

            if ($(selectedTab).prev().length) {
                $(selectedTab).prevAll().each(function () {
                    totalHeight += $(this).outerHeight();
                });
                arrowPosition = totalHeight + (selectedTabHeight / 2);
            } else {
                arrowPosition = selectedTabHeight / 2;
            }

            if (arrowPosition >= parentHeight) {
                parentHeight = arrowPosition + 10;
                $(arrow).parent().height(parentHeight);
            } else {
                $(arrow).parent().removeAttr("style");
            }
            $(arrow).css("top", arrowPosition - 10);
        }

    });

    $(window).scroll(function () {
        $(responsiveTextSleector).responsive_text(responsiveTextRatio);
    });

    $(document).bind('click', function () {
        $(responsiveTextSleector).responsive_text(responsiveTextRatio);
    });

    $(function () {

        /***********************************************************
         *  if body element change call responsive text function
         ***********************************************************/
        var target = document.querySelector('body');
        var observer = new MutationObserver(function (mutations) {
            mutations.forEach(function (mutation) {
                $(responsiveTextSleector).responsive_text(responsiveTextRatio);
            });
        });
        var config = {
            attributes: true,
            childList: true,
            characterData: true
        };
        observer.observe(target, config);

        if (typeof $.fn.tooltip == 'function') {
            $('[data-toggle="tooltip"]').tooltip();
        } else {
            console.warn('Warning : Dependency missing - Bootstrap Tooltip Library');
        }

    });

}(jQuery));