/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.siddhi.editor.core.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.zaxxer.hikari.HikariDataSource;
import net.minidev.json.JSONArray;
import org.apache.axiom.om.OMElement;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.datasource.core.api.DataSourceService;
import org.wso2.carbon.datasource.core.exception.DataSourceException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class MetaInfoRetrieverUtils {

    private static final Logger log = LoggerFactory.getLogger(MetaInfoRetrieverUtils.class);

    private MetaInfoRetrieverUtils() {

    }

    public static JsonObject createResponseForCSV(String[] attributeNameArray, String[] values) {
        JsonObject response = new JsonObject();
        com.google.gson.JsonArray attributes = new com.google.gson.JsonArray();
        int count = 0;
        for (String value : values) {
            JsonObject attribute = new JsonObject();
            if (attributeNameArray != null) {
                attribute.addProperty("name", attributeNameArray[count].
                        replaceAll("\\s",""));
            } else {
                attribute.addProperty("name", "attr" + (count + 1));
            }
            attribute.addProperty("type", findDataTypeFromString(value));
            attributes.add(attribute);
            count ++;
        }
        response.addProperty("attributes", attributes.toString());
        return response;
    }

    public static JsonObject createResponseForJSON(Object obj) {
        JsonObject response = new JsonObject();
        JsonArray attributes = new JsonArray();
        Map map = (LinkedHashMap) obj;
        Iterator it = map.entrySet().iterator();
        while(it.hasNext()) {
            JsonObject attribute = new JsonObject();
            Map.Entry entry = (Map.Entry) it.next();
            if (entry.getValue() instanceof JSONArray) {
                String message = "A complex object found for attribute key : \"" + entry.getKey() +
                        "\". Hence ignoring the attribute.";
                log.warn(message);
                response.addProperty(Constants.WARNING, message);
            } else {
                attribute.addProperty("name", ((String) entry.getKey()).
                        replaceAll("\\s",""));

                attribute.addProperty("type", findDataTypeFromString(entry.getValue().toString()));
                attributes.add(attribute);
            }

        }
        response.addProperty("attributes", attributes.toString());
        return response;
    }

    public static void buildNamespaceMap(Map namespaceMap, String namespace) {
        String[] namespaces = namespace.split(",");
        for (String ns : namespaces) {
            String[] splits = ns.split("=");
            if (splits.length != 2) {
                log.warn("Malformed namespace mapping found: " + ns + ". Each namespace has to have format: "
                        + "<prefix>=<uri>");
            }
            namespaceMap.put(splits[0].trim(), splits[1].trim());
        }
    }


    public static boolean isJsonValid(String jsonInString) {
        Gson gson = new Gson();
        try {
            gson.fromJson(jsonInString, Object.class);
            return true;
        } catch (com.google.gson.JsonSyntaxException ex) {
            return false;
        }
    }

    public static String findDataTypeFromString(String value) {
        if (checkIfValueIsInteger(value)) {
            return Constants.ATTR_TYPE_INTEGER;
        } else if (checkIfValueIsLong(value)) {
            return Constants.ATTR_TYPE_LONG;
        } else if (checkIfValueIsDouble(value)) {
            return Constants.ATTR_TYPE_DOUBLE;
        } else if (checkIfValueIsFloat(value)) {
            return Constants.ATTR_TYPE_FLOAT;
        } else if (checkIfValueIsBool(value)) {
            return Constants.ATTR_TYPE_BOOL;
        } else {
            return Constants.ATTR_TYPE_STRING;
        }
    }

    private static boolean checkIfValueIsInteger(String value) {
        try {
            Integer.parseInt(value);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    private static boolean checkIfValueIsLong(String value) {
        try {
            Long.parseLong(value);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    private static boolean checkIfValueIsDouble(String value) {
        try {
            Double.parseDouble(value);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    private static boolean checkIfValueIsFloat(String value) {
        try {
            Float.parseFloat(value);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    private static boolean checkIfValueIsBool(String value) {
        return Boolean.parseBoolean(value);
    }

    public static JsonObject createResponseForXML(OMElement obj) {
        JsonObject response = new JsonObject();
        JsonArray attributes = new JsonArray();

        Iterator iterator = obj.getChildElements();
        while (iterator.hasNext()) {
            JsonObject attribute = new JsonObject();
            OMElement attrOMElement = (OMElement) iterator.next();
            if (null != attrOMElement.getFirstElement()) {
                String message = "A nested xml structure : " + attrOMElement.toString() + " found for key : \"" +
                        attrOMElement.getLocalName() + "\". Hence ignoring the attribute.";
                log.warn(message);
                response.addProperty(Constants.WARNING, message);
            } else {
                attribute.addProperty("name", attrOMElement.getLocalName());
                attribute.addProperty("type", findDataTypeFromString(attrOMElement.getText()));
                attributes.add(attribute);
            }

        }
        response.addProperty("attributes", attributes.toString());
        return response;
    }

    public static Connection getDbConnection(String url, String username, String password) throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    public static String[] getDataSourceConfiguration(String name) {
        BundleContext bundleContext = FrameworkUtil.getBundle(DataSourceService.class).getBundleContext();
        ServiceReference serviceRef = bundleContext.getServiceReference(DataSourceService.class.getName());
        DataSourceService dataSourceService = (DataSourceService) bundleContext.getService(serviceRef);
        HikariDataSource dataSource;
        try {
            dataSource = (HikariDataSource) dataSourceService.getDataSource(name);
        } catch (DataSourceException e) {
            return null;
        }
        return new String[]{dataSource.getJdbcUrl(), dataSource.getUsername(), dataSource.getPassword()};
    }

}
