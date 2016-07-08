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
var config = {
    map: {
        zoom: 13,
        maxZoom: 18,
        maxNativeZoom: 18,
        center: [51.51323, -0.08995],
        attributionControl: true
    },
    markercluster: {
        animateAddingMarkers: false,
        singleMarkerMode: false,
        disableClusteringAtZoom: 14
        // refer to https://github.com/Leaflet/Leaflet.markercluster/blob/leaflet-0.7/README.md#customising-the-clustered-markers
    },
    constants: {
        POLLING_INTERVAL: 1000,
        SPEED_HISTORY_COUNT: 10
    },
    markers: {
        info: {
            icon: "img/markers/info.png",
            color: "blue"
        },
        offline: {
            icon: "img/markers/offline.png",
            color: "grey"
        },
        warn: {
            icon: "img/markers/warn.png",
            color: "orange"
        },
        danger: {
            icon: "img/markers/danger.png",
            color: "red"
        }
    }
};