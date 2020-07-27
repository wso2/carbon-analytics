/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

define(['require', 'jquery', 'lodash', 'log', 'alerts', 'scopeModel', 'operatorModel',
        'attributeModel', 'customValueModel', 'dataMapperUtil'],

    function (require, $, _, log, Alerts, ScopeModel, OperatorModel, AttributeModel,
              CustomValueModel, DataMapperUtil) {
        var AdvancedOutputConfigurationComponent = function (container, config) {
            this.__container = container;
            this.__config = config;
        }

        AdvancedOutputConfigurationComponent.prototype.constructor = AdvancedOutputConfigurationComponent;

        AdvancedOutputConfigurationComponent.prototype.render = function () {
            var self = this;
            var config = this.__config;
            var container = this.__container;

            var offsetDescription = "When events are emitted as a batch, offset allows you to offset " +
                "beginning of the output event batch";
            var limitDescription = "When events are emitted as a batch, limit allows you to limit the " +
                "number of events in the batch from the defined offset(default=0)"
            var rateDescription = "Output rate limiting allows queries to output events periodically based on " +
                "a specified condition."

            container.empty();
            container.append(`
                <div style="font-size: 1.8rem; margin-bottom: 15px">
                    Configure output rate & limits<br/>
                    <small style="font-size: 1.3rem">
                        Advanced options to configure output rate and limit where data will be published to 
                        the destination
                    </small>
                </div>
                <div class="offset-container">
                    <div style="display: flex">
                        <div>
                            Set offset for batch outputs
                            <span title="${offsetDescription}">
                                <i class="fw fw-info"></i>
                            </span>
                        </div>
                        <div style="margin-left: 15px">
                            <div id="allow-offset" class="btn-group btn-group-toggle btn-enable" data-toggle="buttons">
                                <label class="btn" 
                                        style="${
                                        Object.keys(config.query.advanced.offset).length > 0 ?
                                            "background-color: rgb(91,203,92); color: white;"
                                            : "background-color: rgb(100,109,118); color: white;"}" 
                                 >
                                    <input type="radio" name="options" id="enable" autocomplete="off"> 
                                    <i class="fw fw-check"></i>
                                </label>
                                <label class="btn" 
                                        style="${
                                        !(Object.keys(config.query.advanced.offset).length > 0) ?
                                            "background-color: red; color: white;"
                                            : "background-color: rgb(100,109,118); color: white;"}" 
                                >
                                    <input type="radio" name="options" id="disable" autocomplete="off"> 
                                    <i class="fw fw-cancel"></i>
                                </label>
                            </div>
                        </div>
                    </div>
                    
                    
                </div>
                <div class="limit-container" style="margin-top: 15px;">
                    <div style="display: flex">
                        <div>
                            Set limit to batch outputs 
                            <span title="${limitDescription}"><i class="fw fw-info"></i></span>
                        </div>
                        <div style="margin-left: 15px">
                            <div id="allow-limit" class="btn-group btn-group-toggle btn-enable" data-toggle="buttons">
                                <label class="btn" 
                                        style="${
                                            Object.keys(config.query.advanced.limit).length > 0 ?
                                                "background-color: rgb(91,203,92); color: white;"
                                                : "background-color: rgb(100,109,118); color: white;"}" 
                                 >
                                    <input type="radio" name="options" id="enable" autocomplete="off"> 
                                    <i class="fw fw-check"></i>
                                </label>
                                <label class="btn" 
                                        style="${
                                            !(Object.keys(config.query.advanced.limit).length > 0) ?
                                                "background-color: red; color: white;"
                                                : "background-color: rgb(100,109,118); color: white;"}" 
                                >
                                    <input type="radio" name="options" id="disable" autocomplete="off"> 
                                    <i class="fw fw-cancel"></i>
                                </label>
                            </div>
                        </div>
                    </div>
                    
                </div>
                <div class="rate-container" style="margin-top: 15px;">
                    <div style="display: flex">
                        <div>
                            Set rate of output events
                            <span title="${rateDescription}"><i class="fw fw-info"></i></span>
                        </div>
                        <div style="margin-left: 15px">
                            <div id="allow-rate" class="btn-group btn-group-toggle btn-enable" data-toggle="buttons">
                                <label class="btn" 
                                        style="${
                                            Object.keys(config.query.advanced.ratelimit).length > 0 ?
                                                "background-color: rgb(91,203,92); color: white;"
                                                : "background-color: rgb(100,109,118); color: white;"}"
                                 >
                                    <input type="radio" name="options" id="enable" autocomplete="off"> 
                                    <i class="fw fw-check"></i>
                                </label>
                                <label class="btn" 
                                        style="${
                                            !(Object.keys(config.query.advanced.ratelimit).length > 0) ?
                                                "background-color: red; color: white;"
                                                : "background-color: rgb(100,109,118); color: white;"}" 
                                >
                                    <input type="radio" name="options" id="disable" autocomplete="off"> 
                                    <i class="fw fw-cancel"></i>
                                </label>
                            </div>
                        </div>
                    </div>
                </div>
            `);

            container.find('div .btn-enable').on('click', function (evt) {
                var btnType = evt.currentTarget.id.match('allow-([a-z]+)')[1];

                switch (btnType) {
                    case 'offset':
                        if (Object.keys(config.query.advanced.offset).length > 0) {
                            config.query.advanced.offset={};
                        } else {
                            config.query.advanced.offset['value'] = 0;
                        }

                        self.render();
                        break;
                    case 'limit':
                        if (Object.keys(config.query.advanced.limit).length > 0) {
                            config.query.advanced.limit={};
                        } else {
                            config.query.advanced.limit['value'] = 0;
                        }

                        self.render();
                        break;
                    case 'rate':
                        if (Object.keys(config.query.advanced.ratelimit).length > 0) {
                            config.query.advanced.ratelimit={};
                        } else {
                            config.query.advanced.ratelimit={};
                            config.query.advanced.ratelimit['enabled'] = true;
                        }

                        self.render();
                        break;
                }

            });

            if(Object.keys(config.query.advanced.offset).length > 0) {
                container.find('.offset-container')
                    .append(`
                        <div>
                            <label style="margin-bottom: 0" class="" for="txt-offset">offset index</label>
                            <input id="txt-offset" style="width: 100%; border: none; background-color: transparent; 
                            border-bottom: 1px solid #333" placeholder="Type here to enter" 
                            type="number" value="${config.query.advanced.offset.value}">
                        </div>
                    `);

                container.find('.offset-container #txt-offset')
                    .on('keyup', function (evt) {
                        config.query.advanced.offset.value = Number($(evt.currentTarget).val());
                    });
            }

            if(Object.keys(config.query.advanced.limit).length > 0) {
                container.find('.limit-container')
                    .append(`
                        <div>
                            <label style="margin-bottom: 0" class="" for="txt-limit">event limit</label>
                            <input id="txt-limit" style="width: 100%; border: none; background-color: transparent; 
                                border-bottom: 1px solid #333" placeholder="Type here to enter" 
                            type="number" value="${config.query.advanced.limit.value}">
                        </div>
                    `);

                container.find('.limit-container #txt-offset')
                    .on('keyup', function (evt) {
                        config.query.advanced.limit.value = Number($(evt.currentTarget).val());
                    });
            }

            if(Object.keys(config.query.advanced.ratelimit).length > 0) {
                container.find('.rate-container').append(`
                    <div style="margin-top: 15px">
                        <label for="rate-limit-type">Select output rate limit type</label>
                        <select id="rate-limit-type">
                            <option disabled selected value> -- select an option -- </option>
                            <option value="time-based">Time based</option>
                            <option value="no-of-events">Number of events</option>
                            <option value="snapshot">Snapshot based</option>
                        </select>    
                    </div>
                     
                `);

                if(config.query.advanced.ratelimit['type']) {
                    container.find('.rate-container #rate-limit-type').val(config.query.advanced.ratelimit['type']);

                    switch (config.query.advanced.ratelimit['type']) {
                        case 'time-based':
                            container.find('.rate-container')
                                .append(`
                                    <div>
                                        <span>output type</span>
                                        <select id="select-event-type">
                                            <option value="every">every event</option>
                                            <option value="first">first event</option>
                                            <option value="last">last event</option>
                                        </select>
                                        </br>
                                        <span style="margin-top: 5px">output time limit</span>
                                        <div>
                                            <input id="txt-rate-val" style="width: 75%; border: none; 
                                            background-color: transparent; border-bottom: 1px solid #333" 
                                            placeholder="Type here to enter" type="number" 
                                            value="${config.query.advanced.ratelimit['value']}">
                                            <select id="select-granularity">
                                                <option value="sec">second</option>
                                                <option value="min">minute</option>
                                                <option value="hour">hour</option>
                                                <option value="day">day</option>
                                                <option value="month">month</option>
                                                <option value="year">year</option>
                                            </select>    
                                        </div>
                                    </div>
                                `);
                            
                            if(!config.query.advanced.ratelimit['event-selection']) {
                                config.query.advanced.ratelimit['event-selection'] = 'every';
                            }
                            self.__container.find('.rate-container #select-granularity')
                                .val(config.query.advanced.ratelimit['granularity'].toLowerCase());
                            self.__container.find('#select-event-type')
                                .val(config.query.advanced.ratelimit['event-selection']);
                            
                            if(config.query.advanced.ratelimit['event-selection']
                                && config.query.advanced.ratelimit['event-selection'].length > 0) {
                                self.__container.find('.select-event-type')
                                    .val(config.query.advanced.ratelimit['event-selection']);
                            }
                            self.__container.find('#select-event-type').on('change', function(evt) {
                                config.query.advanced.ratelimit['event-selection'] = $(evt.currentTarget).val();
                            });
                            break;
                        case 'no-of-events':
                            container.find('.rate-container')
                                .append(`
                                    <div>
                                        <span>output type</span>
                                        <select id="select-event-type">
                                            <option value="every">every event</option>
                                            <option value="first">first event</option>
                                            <option value="last">last event</option>
                                        </select>
                                        </br>
                                        <span>output events every</span>
                                        <div>
                                            <input id="txt-rate-val" style="width: 75%; border: none; 
                                                background-color: transparent; border-bottom: 1px solid #333" 
                                                placeholder="Type here to enter" type="number" 
                                                value="${config.query.advanced.ratelimit['value']}">
                                            <span>&nbsp;events</span>    
                                        </div>
                                    </div>
                                `);
                            
                            if(!config.query.advanced.ratelimit['event-selection']) {
                                config.query.advanced.ratelimit['event-selection'] = 'every';
                            }
                            
                            self.__container.find('#select-event-type')
                                .val(config.query.advanced.ratelimit['event-selection']);
                            self.__container.find('#select-event-type').on('change', function(evt) {
                                config.query.advanced.ratelimit['event-selection'] = $(evt.currentTarget).val();
                            });
                            break;
                        case 'snapshot':
                            container.find('.rate-container')
                                .append(`
                                    <div>
                                        <span>output events every</span>
                                        <div>
                                            <input id="txt-rate-val" style="width: 75%; border: none; 
                                            background-color: transparent; border-bottom: 1px solid #333" 
                                            placeholder="Type here to enter" type="number" 
                                            value="${config.query.advanced.ratelimit['value']}">
                                            <select id="select-granularity">
                                                <option value="sec">second</option>
                                                <option value="min">minute</option>
                                                <option value="hour">hour</option>
                                                <option value="day">day</option>
                                                <option value="month">month</option>
                                                <option value="year">year</option>
                                            </select>    
                                        </div>
                                    </div>
                                `);
                            self.__container.find('.rate-container #select-granularity')
                                .val(config.query.advanced.ratelimit['granularity'].toLowerCase() );
                            break;
                    }

                    if(!config.query.advanced.ratelimit['value']) {
                        config.query.advanced.ratelimit['value'] = 0;
                    }

                    container.find('.rate-container #txt-rate-val')
                        .on('keyup', function (evt) {
                            config.query.advanced.ratelimit['value'] = $(evt.currentTarget).val();
                        });

                    self.__container.find('.rate-container #select-granularity').on('change', function (evt) {
                        config.query.advanced.ratelimit['granularity'] = $(evt.currentTarget).val();
                    });
                }

                container.find('.rate-container #rate-limit-type')
                    .on('change', function (evt) {
                        config.query.advanced.ratelimit = {};
                        config.query.advanced.ratelimit['type'] = $(evt.currentTarget).val();

                        if ($(evt.currentTarget).val() !== 'no-of-events') {
                            config.query.advanced.ratelimit['value'] = 5;
                            config.query.advanced.ratelimit['granularity'] = 'second';
                        }
                        self.render();
                    });
            }

        }

        return AdvancedOutputConfigurationComponent;
    });