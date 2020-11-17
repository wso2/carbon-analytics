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

define(['require', 'jquery', 'lodash', 'log', 'alerts'],

    function (require, $, _, log, Alerts) {
        var QueryOrderByComponent = function (container, config) {
            this.__config = config;
            this.__container = container;
        }

        QueryOrderByComponent.prototype.constructor = QueryOrderByComponent;

        QueryOrderByComponent.prototype.render = function () {
            var self = this;
            var container = this.__container;
            var config = this.__config;

            container.empty();
            container.append(`
                <div style="font-size: 1.8rem; margin-bottom:15px">
                    Order output records<br/>
                    <small style="font-size: 1.3rem">
                        Sort output records based on fields in ascending or descending order
                    </small>
                </div>
                Order by attributes:
                <button style="background-color: #ee6719" class="btn btn-default btn-circle" 
                    id="btn-add-orderby-property" type="button" data-toggle="dropdown">
                        <i class="fw fw-add"></i>
                </button>
                <div id="orderby-options-dropdown" class="dropdown-menu-style hidden" aria-labelledby="">
                </div>
                <div style="margin-top: 15px" id="orderby-attribute-list">
                </div>
            `);

            var existingGroupByAttributes = config.query.orderby.attributes.map(function (attr) {
                return attr.attribute.name;
            });

            config.output.stream.attributes
                .filter(function (attr) {
                    return existingGroupByAttributes.indexOf(attr.name) === -1;
                })
                .forEach(function (attr) {
                    container.find('#orderby-options-dropdown')
                        .append(`
                            <a title="" class="dropdown-item" href="#">
                                <div>
                                    <div class="option-title">${attr.name}</div><br/>
                                    <small style="opacity: 0.8">${attr.type}</small><br/>
                                </div>
                            </a>
                        `);
                });

            container.find('#btn-add-orderby-property')
                .on('mouseover', function (evt) {
                    var leftOffset = evt.currentTarget.offsetLeft;
                    var elementObj = container.find('#orderby-options-dropdown');
                    elementObj.css({"left": `${leftOffset}px`})
                    elementObj
                        .removeClass('hidden')
                        .on('mouseleave', function () {
                            elementObj.addClass('hidden');
                        });
                })
                .on('mouseleave', function () {
                    setTimeout(function () {
                        var elementObj = container.find('#orderby-options-dropdown');
                        if (!(container.find('#orderby-options-dropdown:hover').length > 0)) {
                            elementObj.addClass('hidden');
                        }
                    }, 300);
                });

            container.find('#orderby-options-dropdown .dropdown-item')
                .on('click', function (evt) {
                    var attributeName = $(evt.currentTarget).find('.option-title').html();

                    var attrib = config.output.stream.attributes.find(function (attr) {
                        return attributeName === attr.name;
                    });

                    config.query.orderby.attributes.push({attribute: attrib, sort: 'asc'});
                    self.render();
                });

            config.query.orderby.attributes.forEach(function (orderByOption) {
                container.find('#orderby-attribute-list')
                    .append(`
                        <div style="display: flex; background-color: #888; padding: 10px 0; 
                            border-bottom: 1px solid rgba(255,255,255,.15);">
                            <div style="width: 60%; padding: 5px; text-align: center; color: white">
                                ${orderByOption.attribute.name}
                            </div>
                            <div style="width: 40%">
                                <button id="${orderByOption.attribute.name}-sort" 
                                    style="background: #555; min-width: 70px" class="btn btn-default btn-sort">
                                    ${orderByOption.sort === 'asc' ? `ASC&nbsp;<i class="fw fw-down">` : `DESC&nbsp;<i class="fw fw-up">`}</i>
                                </button>
                                <button id="${orderByOption.attribute.name}-delete" 
                                    style="background: #555; min-width: 70px; padding: 8px 12px;" 
                                    class="btn btn-default btn-del">
                                        <i class="fw fw-delete"></i>
                                </button>
                            </div>
                        </div>
                    `);
            });

            container.find('#orderby-attribute-list button.btn-sort').on('click', function (evt) {
                var attributeName = evt.currentTarget.id.match('([a-zA-Z0-9\_]+)-sort')[1];
                var index = config.query.orderby.attributes.map(function (attr) {
                    return attr.attribute.name;
                }).indexOf(attributeName);

                config.query.orderby.attributes[index].sort
                    = config.query.orderby.attributes[index].sort === 'asc' ?
                                                                    'desc': 'asc';
                self.render();
            });

            container.find('#orderby-attribute-list button.btn-del').on('click', function (evt) {
                var attributeName = evt.currentTarget.id.match('([a-zA-Z0-9\_]+)-delete')[1];
                var index = config.query.orderby.attributes.map(function (attr) {
                    return attr.attribute.name;
                }).indexOf(attributeName);

                config.query.orderby.attributes.splice(index, 1);
                self.render();
            });
        }

        return QueryOrderByComponent;
    });
