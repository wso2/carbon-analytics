/*
* Copyright 2004,2013 The Apache Software Foundation.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.carbon.bam.webapp.stat.publisher.util;

/*
* Purpose of this class is to capture the browser related data of the request.
*/
public class BrowserInfoUtils {
    
    private static final String FIREFOX = "Firefox";
    private static final String SEAMONKEY= "Seamonkey";
    private static final String CHROME = "Chrome";
    private static final String CHROMIUM = "Chromium";
    private static final String SAFARI = "Safari";
    private static final String OPERA = "Opera";
    private static final String INTERNET_EXPLORER = "Internet Explorer";
    private static final String WGET = "Wget";

    public static String[] getBrowserInfo(String userAgent) {
        String[] browserInfo = new String[2];
        String substring;

        if(userAgent.contains(FIREFOX) && !userAgent.contains(SEAMONKEY)) {
            substring=userAgent.substring(userAgent.indexOf("Firefox")).split(" ")[0];
            browserInfo = substring.split("/");

        } else if (userAgent.contains(SEAMONKEY)) {
            substring=userAgent.substring(userAgent.indexOf(SEAMONKEY)).split(" ")[0];
            browserInfo = substring.split("/");

        } else if (userAgent.contains(CHROME) && !userAgent.contains(CHROMIUM)) {
            substring=userAgent.substring(userAgent.indexOf(CHROME)).split(" ")[0];
            browserInfo = substring.split("/");

        } else if (userAgent.contains(CHROMIUM)) {
            substring=userAgent.substring(userAgent.indexOf(CHROMIUM)).split(" ")[0];
            browserInfo = substring.split("/");

        } else if (userAgent.contains(SAFARI) &&
                !( userAgent.contains(CHROME) || userAgent.contains(CHROMIUM) )) {
            substring = userAgent.substring(userAgent.indexOf(SAFARI)).split(" ")[0];
            browserInfo = substring.split("/");

        } else if (userAgent.contains(OPERA)) {
            substring=userAgent.substring(userAgent.indexOf(OPERA)).split(" ")[0];
            browserInfo = substring.split("/");

        } else if (userAgent.contains(";MSIE")) {
            substring=userAgent.substring(userAgent.indexOf("MSIE")).split(";")[0];
            browserInfo[0] = INTERNET_EXPLORER;
            browserInfo[1] = substring.split(" ")[1];

        } else if (userAgent.contains(WGET)) {
            substring=userAgent.substring(userAgent.indexOf(WGET)).split(" ")[0];
            browserInfo = substring.split("/");
        } else {
            browserInfo[0] = "Other";
            browserInfo[1] = "Other";
        }

        return browserInfo;
    }

}
