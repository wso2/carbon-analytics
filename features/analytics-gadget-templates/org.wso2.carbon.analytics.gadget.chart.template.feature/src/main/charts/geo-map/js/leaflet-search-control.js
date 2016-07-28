/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

var currentMarkers = [];

function sortParks(a, b) {
    var _a = a;
    var _b = b;
    if (_a < _b) {
        return -1;
    }
    if (_a > _b) {
        return 1;
    }
    return 0;
}

L.Control.Search = L.Control.extend({
    options: {
        // topright, topleft, bottomleft, bottomright
        position: 'topright',
    },
    initialize: function (options /*{ data: {...}  }*/) {
        // constructor
        L.Util.setOptions(this, options);
    },
    onAdd: function (map) {
        // happens after added to map
        var container = L.DomUtil.create('div', 'search-container');
        this.form = L.DomUtil.create('form', 'form', container);
        var group = L.DomUtil.create('div', 'form-group', this.form);
        this.input = L.DomUtil.create('input', 'form-control input-sm', group);
        this.input.type = 'text';
        this.input.placeholder = "Search form device id";
        this.results = L.DomUtil.create('div', 'list-group', group);
        L.DomEvent.addListener(this.input, 'keyup', _debounce(this.keyup, 300), this);
        L.DomEvent.addListener(this.form, 'submit', this.submit, this);
        L.DomEvent.disableClickPropagation(container);
        return container;
    },
    onRemove: function (map) {
        // when removed
        L.DomEvent.removeListener(this._input, 'keyup', this.keyup, this);
        L.DomEvent.removeListener(form, 'submit', this.submit, this);
    },
    keyup: function(e) {
        if (e.keyCode === 38 || e.keyCode === 40) {
            // do nothing
        } else {
            this.results.innerHTML = '';
            if (this.input.value.length > 2) {
                var value = this.input.value;
                var results = _.take(_.filter(currentMarkers, function(x) {
                                                                    return x.toUpperCase().indexOf(value.toUpperCase()) > -1;
                                                                }).sort(sortParks), 10);
                _.map(results, function(x) {
                    var a = L.DomUtil.create('a', 'list-group-item');
                    a.href = '';
                    a.setAttribute('data-result-name', x);
                    a.innerHTML = x;
                    this.results.appendChild(a);
                    L.DomEvent.addListener(a, 'click', this.itemSelected, this);
                    return a;
                } , this);
            }
        }
    },
    itemSelected: function(e) {
        L.DomEvent.preventDefault(e);
        var elem = e.target;
        var value = elem.innerHTML;
        this.input.value = elem.getAttribute('data-result-name');
        console.log(this.input.value);
        var feature = _.find(this.options.data, function(x) {
            return x.feature.properties.park === this.input.value;
        }, this);
        if (feature) {
            this._map.fitBounds(feature.getBounds());
        }
        focusOnSpatialObject(this.input.value);
        this.results.innerHTML = '';

    },
    submit: function(e) {
        focusOnSpatialObject(this.input.value);
        L.DomEvent.preventDefault(e);
    }
});

L.control.search = function() {
    return new L.Control.Search();
};


function _debounce(func, wait, immediate) {
    var timeout;
    return function() {
        var context = this, args = arguments;
        var later = function() {
            timeout = null;
            if (!immediate) func.apply(context, args);
        };
        if (immediate && !timeout) func.apply(context, args);
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

function updateMarkers(markerId){
    currentMarkers.push(markerId);
}

