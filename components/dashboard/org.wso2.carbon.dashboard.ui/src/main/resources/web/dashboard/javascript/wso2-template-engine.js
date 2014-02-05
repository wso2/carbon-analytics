// This default layout is used as a template to populate a new layout.
var layout3ColumnJSON = {
    "layout":
            [
                {
                    "type": "columnContainer",
                    "width": "33%",
                    "layout":
                            [

                            ]
                },
                {
                    "type": "columnContainer",
                    "width": "33%",
                    "layout":
                            [

                            ]
                },
                {
                    "type": "columnContainer",
                    "width": "33%",
                    "layout":
                            [

                            ]
                }
            ]
};


function layout2json(parentDOMElement) {
    var layout = new MasterLayout();
    layout.layout = inferLayoutFromDOM(parentDOMElement);

    return JSON.stringify(layout);
}

function inferLayoutFromDOM(parentDOMElement) {
    var response = [];

    for (var x = 0; x < parentDOMElement.childNodes.length; x++) {
        var currentChildNode = parentDOMElement.childNodes[x];

        var currentLayout = new Layout();

        // Store this layout in an object
        if (currentChildNode.className.indexOf("gadgets-gadget-chrome") > 0) {
            // This is a gadget
            currentLayout.type = "gadget";
            currentLayout.id = currentChildNode.id.split("_")[1];
        } else {
            currentLayout.type = inferLayoutTypeFromClassName(currentChildNode.className);
            currentLayout.width = currentChildNode.style.width;
            currentLayout.isCont = isContainer(currentChildNode.className);
        }

        if ((currentChildNode.childNodes.length > 0) && (currentLayout.type != "gadget")) {
            // We have more content, recurse.
            currentLayout.layout = inferLayoutFromDOM(currentChildNode);
        }

        response[response.length] = currentLayout;
    }

    return response;
}

function isContainer(className) {
    var classes = className.split(" ");
    var response = "";

    for (var x = 0; x < classes.length; x++) {
        if (classes[x] == "connect") {
            response = "false";
            break;
        } else {
            response = "true";
        }
    }
    return response;
}

function inferLayoutTypeFromClassName(className) {
    var classes = className.split(" ");
    var response = "";

    for (var x = 0; x < classes.length; x++) {
        if (classes[x] == "columnContainer") {
            response = "columnContainer";
            break;
        } else if (classes[x] == "rowContainer") {
            response = "rowContainer";
            break;
        }
    }

    return response;
}

function Layout() {
    this.type = "";
    this.id = "";
    this.layout = [];
}

function MasterLayout() {
    this.layout = [];
}

function json2layout(parentJSONElement) {
    var responseHTML = "";

    for (var x = 0; x < parentJSONElement.layout.length; x++) {
        var currentLayoutElement = parentJSONElement.layout[x];

        if (currentLayoutElement.type == "gadget") {
            // We need to add a gadget place holder
            responseHTML += "<div id=\"gadget-chrome_" + currentLayoutElement.id + "\" class=\"lineitem gadget gadgets-gadget-chrome\" style=\"width:" + currentLayoutElement.width + ";\"><img src='images/loading.gif'/></div>";
        }

        else if (currentLayoutElement.type == "columnContainer") {
            // We need to add a new column to the parent element
            if (currentLayoutElement.isCont == "true") {
                responseHTML += "<div class=\"columnContainer col\" style=\"float:left; width:" + currentLayoutElement.width + ";\"><div class=\"sectionTop\"/></div>";
            } else {
                responseHTML += "<div class=\"columnContainer col connect\" style=\"float:left; width:" + currentLayoutElement.width + ";\"><div class=\"sectionTop\"/></div>";
            }

            if ((currentLayoutElement.layout != undefined) && (currentLayoutElement.layout.length > 0)) {

                // There are more layout elements, recurse.
                responseHTML += json2layout(currentLayoutElement);
            }

            responseHTML += "</div>";

        } else if (currentLayoutElement.type == "rowContainer") {
            // We need to add a new column to the parent element
            if (currentLayoutElement.isCont == "true") {
                responseHTML += "<div class='rowContainer row'><div class=\"sectionTop\"/></div>"
            } else {
                responseHTML += "<div class='rowContainer row connect'><div class=\"sectionTop\"/></div>"
            }

            if ((currentLayoutElement.layout != undefined) && (currentLayoutElement.layout.length > 0)) {
                // There are more layout elements, recurse.
                responseHTML += json2layout(currentLayoutElement);
            }

            responseHTML += "</div>";
        }
    }

    return responseHTML;
}


function renderLayout() {
    var parentDOMElement = document.getElementById("tabContent");

    // Retrieving the stored layout
    if (userId != "null") {
        if ($.isArray(gadgetserver.gadgetLayout)) {
            var storedLayoutJSON = gadgetserver.gadgetLayout.join();
        } else {
            var storedLayoutJSON = gadgetserver.gadgetLayout
        }
    } else {
        var storedLayoutJSON = dashboardService.getGadgetLayout(userId, currentActiveTab, dashboardName);
    }

    try {
        storedLayoutJSON = JSON.parse(storedLayoutJSON);
    } catch(e) {
    }

    if (storedLayoutJSON.layout == undefined) {
        // This is a legacy layout. Convert to the JSON format
        storedLayoutJSON = migrateLegacyLayout(storedLayoutJSON);

        // Immediately store new layout to prevent issues later
        var migratedLayout = encodeURI(JSON.stringify(storedLayoutJSON));
        dashboardService.setGadgetLayout(userId, currentActiveTab, migratedLayout, dashboardName);
    }

    gadgetserver.gadgetLayout = new Array();
    convertJsonLayoutToMemoryLayout(storedLayoutJSON);

    // Retrieving the URLs to layout
    gadgetserver.gadgetSpecUrls = dashboardService.getGadgetUrlsToLayout(userId, currentActiveTab, dashboardName);

    // Rendering
    parentDOMElement.innerHTML = json2layout(storedLayoutJSON);
}

function convertJsonLayoutToMemoryLayout(layoutJSON) {
    var layouts = layoutJSON.layout;

    for (var x = 0; x < layouts.length; x++) {
        var currentLayoutElement = layouts[x];

        if (currentLayoutElement.type == undefined) {
            return;
        } else if (currentLayoutElement.type == "gadget") {
            gadgetserver.gadgetLayout[gadgetserver.gadgetLayout.length] = currentLayoutElement.id;
        } else {
            // Looks like a container element recurse.
            convertJsonLayoutToMemoryLayout(currentLayoutElement);
        }
    }
}

function persistLayout() {
    var layout = layout2json(document.getElementById("tabContent"));

    // Making JSON safer over the wire
    layout = encodeURI(layout);
    dashboardService.setGadgetLayout(userId, currentActiveTab, layout, dashboardName);
}

// This ports old layouts to the new JSON model
function migrateLegacyLayout(legacyLayout) {
    // "G1#0,G1#1,G1#2,G1#3,G2#4,G2#5,G2#6,G2#7,G3#8,G3#9,G3#10"

    var combos;

    if (legacyLayout instanceof Array) {
        combos = legacyLayout;
    } else {
        combos = legacyLayout.split(",");
    }

    // First collect gadget ids of each column
    var column1Gadgets = new Array();
    var column2Gadgets = new Array();
    var column3Gadgets = new Array();

    for (var x = 0; x < combos.length; x++) {

        var tokenisedCombo = combos[x].split("#");

        if (tokenisedCombo[0] == "G1") {
            column1Gadgets[column1Gadgets.length] = tokenisedCombo[1];
        } else if (tokenisedCombo[0] == "G2") {
            column2Gadgets[column2Gadgets.length] = tokenisedCombo[1];
        }
        if (tokenisedCombo[0] == "G3") {
            column3Gadgets[column3Gadgets.length] = tokenisedCombo[1];
        }
    }

    // Convert to JSON 3 column layout
    var jsonLayout = new MasterLayout();

    // Creating column 1
    var column1Layout = new Layout();
    column1Layout.type = "columnContainer";
    column1Layout.width = "33%";

    for (x = 0; x < column1Gadgets.length; x++) {
        if (column1Gadgets[x].length > 0) {
            var gadgetLayout = new Layout();
            gadgetLayout.type = "gadget";
            gadgetLayout.id = column1Gadgets[x];
            column1Layout.layout[column1Layout.layout.length] = gadgetLayout;
        }
    }

    jsonLayout.layout[jsonLayout.layout.length] = column1Layout;

    // Creating column 2
    var column2Layout = new Layout();
    column2Layout.type = "columnContainer";
    column2Layout.width = "33%";

    for (x = 0; x < column2Gadgets.length; x++) {
        if (column2Gadgets[x].length > 0) {
            var gadgetLayout = new Layout();
            gadgetLayout.type = "gadget";
            gadgetLayout.id = column2Gadgets[x];
            column2Layout.layout[column2Layout.layout.length] = gadgetLayout;
        }
    }

    jsonLayout.layout[jsonLayout.layout.length] = column2Layout;

    // Creating column 3
    var column3Layout = new Layout();
    column3Layout.type = "columnContainer";
    column3Layout.width = "33%";

    for (x = 0; x < column3Gadgets.length; x++) {
        if (column1Gadgets[x].length > 0) {
            var gadgetLayout = new Layout();
            gadgetLayout.type = "gadget";
            gadgetLayout.id = column3Gadgets[x];
            column3Layout.layout[column3Layout.layout.length] = gadgetLayout;
        }
    }

    jsonLayout.layout[jsonLayout.layout.length] = column3Layout;

    return jsonLayout;
}
