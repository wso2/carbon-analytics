package org.wso2.carbon.bam.gadgetgenwizard.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.bam.gadgetgenwizard.stub.beans.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

/**
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class GGWUIUtils {

    private static Log log = LogFactory.getLog(GGWUIUtils.class);

    public static JSONObject convertToJSONObj(WSResultSet wsResultSet) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonCols = new JSONArray(wsResultSet.getColumnNames());
        jsonObject.put("ColumnNames", jsonCols);
        JSONArray allRows = new JSONArray();
        WSRow[] wsRows = wsResultSet.getRows();
        for (WSRow wsRow : wsRows) {
            JSONArray row = new JSONArray(wsRow.getRow());
            allRows.put(row);
        }
        jsonObject.put("Rows", allRows);
        return jsonObject;
    }

    public static String getSQL(HttpSession session) {
        return (session.getAttribute("sql") != null) ? ((String[]) session.getAttribute("sql"))[0] : null;
    }

    public static DBConnInfo constructDBConnInfo(HttpSession session) {
        String jdbcurl = (session.getAttribute("jdbcurl") != null) ? ((String[]) session.getAttribute("jdbcurl"))[0] : "";
        String driver = (session.getAttribute("driver") != null) ? ((String[]) session.getAttribute("driver"))[0] : "";
        String username = (session.getAttribute("username") != null) ? ((String[]) session.getAttribute("username"))[0] : "";
        String password = (session.getAttribute("password") != null) ? ((String[]) session.getAttribute("password"))[0] : "";
        DBConnInfo dbConnInfo = new DBConnInfo();
        dbConnInfo.setJdbcURL(jdbcurl);
        dbConnInfo.setDriverClass(driver);
        dbConnInfo.setUsername(username);
        dbConnInfo.setPassword(password);
        return dbConnInfo;
    }

    public static WSMap constructWSMap(HttpSession session, List<String> sessionAttrKey, HttpServletRequest request) {
        List<WSMapElement> sessionValues = new ArrayList<WSMapElement>();
        for (String key : sessionAttrKey) {
            WSMapElement wsMapElement = new WSMapElement();
            wsMapElement.setKey(key);
            wsMapElement.setValue(((String[]) session.getAttribute(key))[0]);
            sessionValues.add(wsMapElement);
        }

        WSMapElement serverUrl = new WSMapElement();
        serverUrl.setKey("jaggeryAppUrl");
        serverUrl.setValue(getServerUrl(request));
        sessionValues.add(serverUrl);
        WSMap wsMap = new WSMap();
        wsMap.setWsMapElements(sessionValues.toArray(new WSMapElement[sessionValues.size()]));

        return wsMap;
    }

    public static String getServerUrl(HttpServletRequest request) {
        String url = request.getHeader("referer");
        String[] tempUrls = url.split("/");
        String finalURL = "";
        for (int i = 0; i < tempUrls.length - 3; i++) {
            finalURL = finalURL + tempUrls[i] + "/";
        }
        return finalURL;
    }


    public static void overwriteSessionAttributes(HttpServletRequest request, HttpSession session) {
        Map parameterMap = request.getParameterMap();
        for (Object o : parameterMap.keySet()) {
            String param = (String) o;
            Object value = parameterMap.get(param);
            session.removeAttribute(param);
            session.setAttribute(param, value);
        }
        if (log.isDebugEnabled()) {
            String str = "Sessions map \n";
            Enumeration attributeNames = session.getAttributeNames();
            while (attributeNames.hasMoreElements()) {
                Object key = attributeNames.nextElement();
                if (key instanceof String) {

                    Object attribute1 = session.getAttribute((String) key);
                    if (attribute1 instanceof String[]) {
                        String[] attributes = (String[]) attribute1;
                        str += "\n key : " + key;
                        str += " values : ";
                        for (String attribute : attributes) {
                            str += attribute + ", ";
                        }
                    }
                }

            }
            log.debug(str);
        }
    }
}
