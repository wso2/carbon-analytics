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
function GeoMarker(json, layerGroup) {
    this.id = json.id;
    this.objectTypeId = json.objectTypeId;
    this.type = json.properties.name;
    this.name = json.properties.name;
    this.state = json.properties.state;
    this.heading = json.properties.heading;
    this.layerGroup = layerGroup;
    this.levelId = json.levelId;
    this.pathGeoJsons = [];     // Have to store the coordinates , to use when user wants to draw path.
    this.path = [];             // Path is an array of sections, where each section is a notified state of the path.
    this.speedHistory = new LocalStorageArray(this.id);
    var iconUrl = this.getMarkerIconUrl();
    this.geoJson = L.geoJson(json, {
        pointToLayer: function (feature, latlng) {
            return L.marker(
                latlng,
                {
                    icon: L.icon({
                        iconUrl: iconUrl,
                        shadowUrl: false,
                        iconSize: [24, 24],
                        iconAnchor: [+12, +12],
                        popupAnchor: [-2, -5]
                    }), iconAngle: this.heading
                });
        }
    });
    this.marker = this.geoJson.getLayers()[0];
    this.marker.options.title = this.id;
    this.popupTemplate = $('#markerPopup');
    this.marker.bindPopup(this.popupTemplate.html());
    return this;
}

GeoMarker.prototype.update = function (geoJSON, selected_marker, chartConfig) {
    this.latitude = geoJSON.geometry.coordinates[1];
    this.longitude = geoJSON.geometry.coordinates[0];
    this.setSpeed(geoJSON.properties.speed);
    this.state = geoJSON.properties.state;
    this.heading = geoJSON.properties.heading;
    this.information = geoJSON.properties.information;
    this.type = geoJSON.properties.name;
    this.name = geoJSON.properties.name;
    this.displayName = this.name;

    // Update the spatial object leaflet marker
    this.marker.setLatLng([this.latitude, this.longitude]);
    this.marker.setIconAngle(this.heading);
    this.marker.setIcon(this.stateIcon());

    if (this.pathGeoJsons.length > 0) {
        this.pathGeoJsons[this.pathGeoJsons.length - 1].geometry.coordinates.push(
            [geoJSON.geometry.coordinates[1], geoJSON.geometry.coordinates[0]]);
    } else {
        newLineStringGeoJson = this.createLineStringFeature(
            this.state,
            this.information,
            [geoJSON.geometry.coordinates[1], geoJSON.geometry.coordinates[0]]);
        this.pathGeoJsons.push(newLineStringGeoJson);
    }

    if (selected_marker == this.id) {
        this.updatePath([geoJSON.geometry.coordinates[1], geoJSON.geometry.coordinates[0]]);
        map.setView([this.latitude, this.longitude], map.getZoom(), {animate: true});
    }

    this.popupTemplate.find('#objectId').html(this.id);
    this.popupTemplate.find('#objectName').html(this.displayName);
    this.popupTemplate.find('#information').html(this.information);
    this.popupTemplate.find('#speed').html(Math.round(this.speed * 10) / 10);
    this.popupTemplate.find('#heading').html(angleToHeading(this.heading));
    this.marker.setPopupContent(this.popupTemplate.html())
};

GeoMarker.prototype.removeFocusFromMap = function () {
    this.removePath();
    this.marker.closePopup();
};

GeoMarker.prototype.createLineStringFeature = function (state, information, coordinates) {
    return {
        "type": "Feature",
        "properties": {
            "state": state,
            "information": information
        },
        "geometry": {
            "type": "LineString",
            "coordinates": [coordinates]
        }
    };
};

GeoMarker.prototype.setSpeed = function (speed) {
    this.speed = speed;
    this.speedHistory.push(speed);
    if (this.speedHistory.length > config.constants.SPEED_HISTORY_COUNT) {
        this.speedHistory.splice(1, 1);
    }
};

GeoMarker.prototype.addToLayer = function () {
    this.layerGroup.addLayer(this.geoJson);
};

GeoMarker.prototype.removeFromLayer = function () {
    this.layerGroup.removeLayer(this.geoJson);
};

GeoMarker.prototype.updatePath = function (LatLng) {
    if (this.path.length > 0) {
        this.path[this.path.length - 1].addLatLng(LatLng); // add LatLng to last section
    }
};

GeoMarker.prototype.drawPath = function () {
    var previousSectionLastPoint = []; // re init all the time when calls the function
    if (this.path.length > 0) {
        this.removePath();
    }
    for (var lineString in this.pathGeoJsons) {
        if (!this.pathGeoJsons.hasOwnProperty(lineString)) {
            continue
        }
        var currentSectionState = this.pathGeoJsons[lineString].properties.state;
        var currentSection = new L.polyline(this.pathGeoJsons[lineString].geometry.coordinates, this.getSectionStyles(currentSectionState));
        var currentSectionFirstPoint = this.pathGeoJsons[lineString].geometry.coordinates[0];

        console.log("DEBUG: previousSectionLastPoint = " + previousSectionLastPoint + " currentSectionFirstPoint = " + currentSectionFirstPoint);

        previousSectionLastPoint.push(currentSectionFirstPoint);
        var sectionJoin = new L.polyline(previousSectionLastPoint, this.getSectionStyles());
        sectionJoin.setStyle({className: "sectionJointStyle"}); // Make doted line for section join
        previousSectionLastPoint = [this.pathGeoJsons[lineString].geometry.coordinates[this.pathGeoJsons[lineString].geometry.coordinates.length - 1]];
        this.layerGroup.addLayer(sectionJoin);
        this.path.push(sectionJoin);

        console.log("DEBUG: Alert Information: " + this.pathGeoJsons[lineString].properties.information);

        currentSection.bindPopup("Alert Information: " + this.pathGeoJsons[lineString].properties.information);
        this.layerGroup.addLayer(currentSection);
        this.path.push(currentSection);
    }
};

GeoMarker.prototype.removePath = function () {
    for (var section in this.path) {
        if (this.path.hasOwnProperty(section)) {
            this.layerGroup.removeLayer(this.path[section]);
        }
    }
    this.path = [];
};

GeoMarker.prototype.getSectionStyles = function (state) {
    var pathColor;
    if (state != null && typeof state != 'undefined') {
        switch (state.toLowerCase()) {
            case "offline":
                pathColor = config.markers.offline.color;
                break;
            case "warn":
                pathColor = config.markers.warn.color;
                break;
            case "danger":
                pathColor = config.markers.danger.color;
                break;
            default:
                pathColor = config.markers.info.color;
                break;
        }
    } else {
        pathColor = config.markers.info.color;
    }
    return {color: pathColor, weight: 8};
};

GeoMarker.prototype.stateIcon = function () {
    return L.icon({
        iconUrl: this.getMarkerIconUrl(),
        shadowUrl: false,
        iconSize: [24, 24],
        iconAnchor: [+12, +12],
        popupAnchor: [-2, -5]
    });
};

var headings = ["North", "NorthEast", "East", "SouthEast", "South", "SouthWest", "West", "NorthWest"];

GeoMarker.prototype.getMarkerIconUrl = function () {
    var iconUrl = "libs/img/markers/object-types/" + this.type.toLowerCase();
    if(0 < this.speed && (-360 <= this.heading && 360 >= this.heading)) {
        iconUrl = iconUrl + "/moving/" + this.state.toLowerCase();
    } else {
        iconUrl = iconUrl + "/non-moving/" + this.state.toLowerCase();
    }
    return rebaseRelativeUrl(iconUrl + ".png", true);
};

function angleToHeading(angle) {
    angle = (angle + 360 + 22.5 ) % 360;
    angle = Math.floor(angle / 45);
    return headings[angle];
}

function LocalStorageArray(id) {
    var DELIMITER = ',';
    if (typeof (sessionStorage) === 'undefined') {
        return ['speed']; // LocalStorage not supported.
    }
    if (id == null || id === undefined) {
        throw 'Should provide an id to create a local storage!';
    } else {
        this.storageId = id;
    }
    sessionStorage.setItem(id, 'speed');

    this.getArray = function () {
        return sessionStorage.getItem(this.storageId).split(DELIMITER);
    };

    this.length = this.getArray().length;

    this.push = function (value) {
        var currentStorageValue = sessionStorage.getItem(this.storageId);
        var updatedStorageValue;
        if (currentStorageValue === null) {
            updatedStorageValue = value;
        } else {
            updatedStorageValue = currentStorageValue + DELIMITER + value;
        }
        sessionStorage.setItem(this.storageId, updatedStorageValue);
        this.length += 1;
    };

    this.isEmpty = function () {
        return (this.getArray().length === 0);
    };

    this.splice = function (index, howmany) {
        var currentArray = this.getArray();
        currentArray.splice(index, howmany);
        var updatedStorageValue = currentArray.toString();
        sessionStorage.setItem(this.storageId, updatedStorageValue);
        this.length -= howmany;
    };
}

function rebaseRelativeUrl(relativeUrl, cached) {
    var moduleBase = getModuleBase();
    var absUrl = moduleBase + relativeUrl;
    if (cached && _IG_GetCachedUrl) {
        absUrl = _IG_GetCachedUrl(absUrl);
    }
    return absUrl;
}

function getModuleBase() {
    if (window.__moduleBase) return window.__moduleBase;
    if (_args) {
        var moduleBase = _args()['url'];
        moduleBase = moduleBase.substring(0, moduleBase.lastIndexOf('/') + 1);
        window.__moduleBase = moduleBase;
        return window.__moduleBase;
    }
    console.error('Can not find module base. Gadget may not work properly.');
    return '';
}