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

var getConfig, validate, isProviderRequired, draw, update;

var mapInitialized = false;
var mapLayers = {};
var level_groups = {};
var object_type_groups = {};
var map = null;
var selected_marker = null;
var markers = {};
var markerTypes = {};
var mapChartConfig = {};
var layerGroupControl = null;
var initialScenario = true;
var initialLayerSelected = false;
var currentSelectedLevelLayer = "defaultLevel";
var useDefaultValueLabel = "Use default";

(function () {

    var CHART_LOCATION = '/extensions/chart-templates/';

    /**
     * return the config to be populated in the chart configuration UI
     * @param schema
     */
    getConfig = function (schema) {
        var chartConf = require(CHART_LOCATION + '/geo-map/config.json').config;

        //Adding new column which says "use default value" since there's no option available in DS for column types
        var columns = [];
        columns.push(useDefaultValueLabel);
        for(var j=0; j < schema.length; j++) {
            columns.push(schema[j]["fieldName"]);
        }

        for(var i=0; i < chartConf.length; i++) {
            if (chartConf[i]["fieldName"] == "level" || chartConf[i]["fieldName"] == "type"
                || chartConf[i]["fieldName"] == "state" || chartConf[i]["fieldName"] == "information"
                || chartConf[i]["fieldName"] == "speed" || chartConf[i]["fieldName"] == "heading"){
                chartConf[i]["valueSet"] = columns;
            }
        }
        return chartConf;
    };

    /**
     * validate the user inout for the chart configuration
     * @param chartConfig
     */
    validate = function(chartConfig) {
        return true;
    };

    /**
     * TO be used when provider configuration steps need to be skipped
     */
    isProviderRequired = function () {

    };


    /**
     * return the gadget content
     * @param chartConfig
     * @param schema
     * @param data
     */
    draw = function (placeholder, chartConfig, _schema, data) {
        if (!mapInitialized) {
            mapInitialized = true;
            mapChartConfig = buildChartConfig(chartConfig);
            createSearchDivElements(placeholder);
            initLayers();
            while(document.getElementById('map') == null){
                setTimeout(function(){
                }, 500);
            }
            initMap(data, placeholder);
        } else {
            addDataToMap(data);
        }
    };

    /**
     *
     * @param data
     */
    update = function (data) {
        addDataToMap(data);
    };


    function initLayers() {
        //Add default map layer.
        mapLayers["default"] = L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
            maxZoom: config.map.maxZoom,
            attribution: '© <a href="http://www.openstreetmap.org/copyright" target="_blank">' +
            'OpenStreetMap</a>'
        });
    }

    //creating new overlay cluster level if a object comes with a different level
    function addNewLevelLayer(newLayerId) {
        level_groups[newLayerId] = new L.MarkerClusterGroup(config.markercluster);
        layerGroupControl.removeFrom(map);
        layerGroupControl = L.control.layers(level_groups, object_type_groups).addTo(map);
        if(!initialLayerSelected) {
            map.addLayer(level_groups[newLayerId]);

            initialLayerSelected = true;
        }
    }

    //creating new overlay cluster level if a object comes with a different level
    function addNewObjectLayer(newLayerId) {
        object_type_groups[newLayerId] = new L.MarkerClusterGroup(config.markercluster);
        layerGroupControl.removeFrom(map);
        layerGroupControl = L.control.layers(level_groups, object_type_groups).addTo(map);
        object_type_groups[newLayerId].addTo(map);

    }

    function initMap(data) {
        if (map != null && typeof(map) !== 'undefined') {
            map.remove();
        }
        map = L.map("map", {
            zoom: config.map.zoom,
            center: config.map.center,
            layers: getLayers(),
            zoomControl: false,
            attributionControl: config.map.attributionControl,
            maxZoom: config.map.maxZoom,
            maxNativeZoom: config.map.maxNativeZoom
        });

        new L.Control.Zoom({position: 'bottomright'}).addTo(map);    // Add zoom controller
        layerGroupControl = L.control.layers(level_groups, object_type_groups).addTo(map);


        // Add sub marker groups
        for (var i in level_groups) {
            if (level_groups.hasOwnProperty(i)) {
                level_groups[i].addTo(map);
            }
        }

        // Zoom callbacks.
        map.on('zoomend', function () {
            if (map.getZoom() < config.markercluster.disableClusteringAtZoom) {
                if (selected_marker) {
                    clearFocus();
                }
            }
        });

        //Marker popup open callback.
        map.on('popupopen', function (e) {
            if (selected_marker) {
                clearFocus();
            }
            selected_marker = e.popup._source.feature.id;
            console.log("initial on marker!!" + e.popup._source.feature.id);
        });

        // Map click callbacks.
        map.on('click', function (e) {
            if (selected_marker) {
                clearFocus();
                console.log("cleared focus");
            }
        });

        var searchObject = L.control.search();
        map.addControl(searchObject);
        if(data.length > 0) {
            map.setView([data[0].latitude, data[0].longitude]);
        }
        addDataToMap(data);

        map.on('overlayadd', onOverlayAdd);
        map.on('overlayremove', onOverlayRemove);
        map.on('baselayerchange', onBaseLayerChange);

        embedCustomMaps();

    }

    function embedCustomMaps() {
        var areaArray = floorConfig;
        for (var i = 0; i < areaArray.length; i++) {
            var tempImageBounds = areaArray[i]["mapDetails"]["imageBounds"];
            var imageBounds = new L.LatLngBounds([tempImageBounds.northEast.lat, tempImageBounds.northEast.long],
                [tempImageBounds.southWest.lat, tempImageBounds.southWest.long]);

            var imageUrls = areaArray[i]["mapDetails"]["urls"];
            for(var j = 0; j < imageUrls.length; j++) {
                var layerName = imageUrls[j]["layerName"];
                var markerClusterGroup;
                if (layerName in level_groups){
                    markerClusterGroup = level_groups[layerName];
                } else {
                    markerClusterGroup = new L.MarkerClusterGroup(config.markercluster);
                }

                markerClusterGroup.addLayer(L.imageOverlay(rebaseRelativeUrl(imageUrls[j]["url"]), imageBounds, {opacity: 1.0}));
                level_groups[layerName] = markerClusterGroup;

                layerGroupControl.removeFrom(map);
                layerGroupControl = L.control.layers(level_groups, object_type_groups).addTo(map);

                if(!initialLayerSelected) {
                    map.addLayer(level_groups[layerName]);
                    initialLayerSelected = true;
                    currentSelectedLevelLayer = layerName;
                }
            }
        }
    }

    function addDataToMap(data) {
        for (var i = 0; i < data.length; i++) {
            if(initialScenario && 1 == data.length) {
                //if at the beginning, only single object is getting updated, it will be treated as single object and will get zoomed
                mapChartConfig.single_marker_mode = true;
                initialScenario = false;
            } else if (mapChartConfig.single_marker_mode && 1 < data.length) {
                //if several objects are getting updated, the focus will be cancelled from single object until a marker is selected
                mapChartConfig.single_marker_mode = false;
                clearFocus();
                initialScenario = false;
            }

            var device = data[i];
            var levelId;
            var objectTypeId;

            if (mapChartConfig.type) {
                device.type = mapChartConfig.type;
            }
            if (mapChartConfig.state) {
                device.state = mapChartConfig.state;
            }
            if (mapChartConfig.information) {
                device.information = mapChartConfig.information;
            }
            if (mapChartConfig.speed) {
                device.speed = mapChartConfig.speed;
            }
            if (mapChartConfig.heading) {
                device.heading = mapChartConfig.heading;
            }
            if (mapChartConfig.level) {
                device.level = mapChartConfig.level;
            }

            if(null == device.level){
                levelId = "defaultLevel";
            } else {
                levelId = device.level;
                if (!(levelId in level_groups)){
                    console.log("new level added");
                    addNewLevelLayer(levelId);
                }
            }

            if(null == device.type){
                objectTypeId = "defaultType";
            } else {
                objectTypeId = device.type;
                if (!(objectTypeId in object_type_groups)){
                    console.log("new object type added");
                    addNewObjectLayer(objectTypeId);
                }
            }

            processPointMessage({
                "id": device.id,
                "levelId": levelId,
                "objectTypeId": objectTypeId,
                "type": "Feature",
                "properties": {
                    "name": device.type,
                    "state": device.state,
                    "information": levelId + ": " + device.information,
                    "speed": device.speed,
                    "heading": device.heading
                },
                "geometry": {
                    "type": "Point",
                    "coordinates": [device.longitude, device.latitude]
                }
            });
        }
    }

    function processPointMessage(geoJson) {
        if (mapChartConfig.single_marker_mode) {
            selected_marker = geoJson.id;
        }
        if (geoJson.id in markers) {
            var existingObject = markers[geoJson.id];
            existingObject.update(geoJson, selected_marker, mapChartConfig);
        } else {
            var receivedObject;
            receivedObject = new GeoMarker(geoJson, level_groups[geoJson.levelId]);
            receivedObject.update(geoJson, selected_marker, mapChartConfig);
            markers[geoJson.id] = receivedObject;
            markers[geoJson.id].addToLayer();
            updateMarkers(geoJson.id);

            if(!(geoJson.objectTypeId in markerTypes)) {
                markerTypes[geoJson.objectTypeId] = [];
            }
            var objectTypeMarkers = markerTypes[geoJson.objectTypeId];
            objectTypeMarkers.push(geoJson.id);
        }
    }

    //called when searched from the map
    focusOnSpatialObject = function (objectId){
        console.log("focusing on marker!!");
        var spatialObject = markers[objectId];
        if (!spatialObject) {
            $.UIkit.notify({
                message: "Spatial Object <span style='color:red'>" + objectId + "</span> not in the Map!!",
                status: 'warning',
                timeout: 2000,
                pos: 'top-center'
            });
            return false;
        }
        selected_marker = objectId;
        clearFocus();
        console.log("Selected "+objectId+ " type " + spatialObject.type);
        console.log(spatialObject);
        console.log(spatialObject.levelId);
        if (currentSelectedLevelLayer != spatialObject.levelId) {
            console.log(spatialObject.levelId);
            map.removeLayer(level_groups[currentSelectedLevelLayer]);
            map.addLayer(level_groups[spatialObject.levelId]);
            currentSelectedLevelLayer = spatialObject.levelId;

        }
        map.setView(spatialObject.marker.getLatLng(), 15, {animate: true}); // TODO: check the map._layersMaxZoom and set the zoom level accordingly
        spatialObject.marker.openPopup();

    };

    function clearFocus() {
        if (selected_marker && !mapChartConfig.single_marker_mode) {
            var spatialObject = markers[selected_marker];
            //removes path and closes the popup
            spatialObject.removeFocusFromMap();
            console.log("Marker focus removed");
            if (!mapChartConfig.single_marker_mode) {
                selected_marker = null;
            }
        }
    }

    //called when the user clicks on the marker
    focusOnMarker = function (){
        var spatialObject = markers[selected_marker];
        if (!spatialObject) {
            console.log("marker with id : " + selected_marker + " not in map");
            return false;
        }
        clearFocus();
        map.setView(spatialObject.marker.getLatLng(), map.getZoom(), {animate: true});
        spatialObject.marker.openPopup();
        spatialObject.drawPath();
    };

    //getting maps layer (open maps)
    function getLayers() {
        var layers = [];
        for (var j in mapLayers) {
            if (mapLayers.hasOwnProperty(j)) {
                layers.push(mapLayers[j]);
            }
        }
        return layers;
    }

    function buildChartConfig(_chartConfig) {
        var conf = {};
        conf.single_marker_mode = false;

        if (_chartConfig.type == useDefaultValueLabel) {
            conf.type = "defaultType";
        }
        if (_chartConfig.state == useDefaultValueLabel) {
            conf.state = "normal";
        }
        if (_chartConfig.information == useDefaultValueLabel) {
            conf.information = "not available";
        }
        if (_chartConfig.speed == useDefaultValueLabel) {
            conf.speed = 0;
        }
        if (_chartConfig.heading == useDefaultValueLabel) {
            conf.heading = 400;
        }
        if (_chartConfig.level == useDefaultValueLabel) {
            conf.level = "defaultLevel";
        }
        return conf;
    }

    function onOverlayAdd(e){
        if (e.name in markerTypes) {
            var markerIds = markerTypes[e.name];
            for(var i = 0; i < markerIds.length; i++){
                var existingObject = markers[markerIds[i]];
                existingObject.addToLayer();
            }
        }
    }
    function onOverlayRemove(e) {
        if (e.name in markerTypes) {
            var markerIds = markerTypes[e.name];
            for(var i = 0; i < markerIds.length; i++){
                if(selected_marker) {
                    clearFocus();
                }
                var existingObject = markers[markerIds[i]];
                existingObject.removeFromLayer();
            }
        }
    }

    function onBaseLayerChange(e) {
        if (e.name in level_groups) {
            currentSelectedLevelLayer = e.name;
        }
    }


    function createSearchDivElements(placeholderCanvas){
        var placeholder = document.getElementById(placeholderCanvas.replace("#", ""));

        if(!document.getElementById('container')){

            //adding the container which leaflet draws the map
            var map = document.createElement("div");
            map.setAttribute("id", "map");
            map.setAttribute("style", "display: inline-block;");
            var container = document.createElement("div");
            container.setAttribute("id", "container");
            container.appendChild(map);
            placeholder.appendChild(container);

            //adding the html for the marker popup
            var popupDivContainer = document.createElement("div");
            popupDivContainer.setAttribute("id", "popupDivContainer");
            popupDivContainer.setAttribute("style", "display: none;");
            popupDivContainer.innerHTML = "<div id=\"markerPopup\" class=\"popover top\">" +
                "<div class=\"arrow\"></div>" +
                "<h3 class=\"popover-title\"><span id=\"objectName\"></span></h3>" +
                "<div class=\"popover-content\">" +
                "ID : <span id=\"objectId\"></span>" +
                "<hr />"+
                "<h6>Information</h6>"+
                "<p id=\"information\" class=\"bg-primary\" style=\"margin:0px;padding:0px;\"></p>" +
                "<h6>Speed<span class=\"label label-primary pull-right\"><span id=\"speed\"></span> km/h</span></h6>" +
                "<h6>Heading<span id=\"heading\" class=\"label label-primary pull-right\"></span></h6>" +
                "<button type=\"button\" class=\"btn btn-info btn-xs\" onClick=\"focusOnMarker();return false;\">History</button>" +
                "</div>"+
                "</div>";
            placeholder.appendChild(popupDivContainer);
        }
    }

}());