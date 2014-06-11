package org.wso2.carbon.bam.toolbox.deployer;

import java.io.File;
import java.util.ArrayList;

/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
public class BasicToolBox {
    //MESSAGE_TRACING(1, "Message_Tracing.tbox", "Message Tracing in wso2 ESB", "some description"),
//    KPI_PHONE_RETAIL_STORE(1, "KPI_Phone_Retail_Store.tbox", "Phone Retail Store", "Phone Retail Store toolbox is intended to monitor the KPI(Key Performance Indication) of a Phone Retail Store.\n" +
//           "                            And also this  includes analytics and visualization gadgets for Phone Retail Store."),
//
//     HTTPD_LOGS(2, "HTTPD_Logs.tbox", "HTTPD Logs Analysis", "HTTPD Logs toolbox is intended to show the capability of WSO2 BAM which can analyze the raw httpd logs and produce useful result.\n" +
//           "                            And also this  includes analytics and visualization gadgets for analyzed data of HTTPD Logs.");

    private static ArrayList<BasicToolBox> availableToolBox = new ArrayList<BasicToolBox>();

    private int sampleId;
    private String location;
    private String displayName;
    private String description;

    public BasicToolBox(int sampleId, String location, String displayName, String description) {
        this.sampleId = sampleId;
        this.location = location;
        this.displayName = displayName;
        if (null == description) this.description = "";
        else this.description = description;
    }

    public int getSampleId() {
        return sampleId;
    }

    public String getLocation() {
        return location;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public static void addToAvailableToolBox(BasicToolBox basicToolBox) {
        availableToolBox.add(basicToolBox);
    }

    public static BasicToolBox[] getAvailableToolBoxes() {
        return availableToolBox.toArray(new BasicToolBox[availableToolBox.size()]);
    }

    public String getTBoxFileName() {
        if (null != location) {
            int slashIndex = location.lastIndexOf(File.separator);
            return location.substring(slashIndex + 1);
        } else {
            return "";
        }
    }

}
