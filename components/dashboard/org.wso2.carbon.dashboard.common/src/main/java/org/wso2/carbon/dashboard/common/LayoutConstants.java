/*
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.dashboard.common;


public class LayoutConstants {
    public static String ONE_COLUMN_LAYOUT="{\n" +
                                           "    \"layout\":\n" +
                                           "            [\n" +
                                           "                {\n" +
                                           "                    \"type\": \"columnContainer\",\n" +
                                           "                    \"width\": \"100%\",\n" +
                                           "                     \"id\": \"\",\n" +
                                           "                    \"layout\":\n" +
                                           "                            [\n" +
                                           "                            ]\n" +
                                           "                }\n" +
                                           "            ]\n" +
                                           "}";
    public static String TWO_COLUMN_SYMETRIC_LAYOUT="{\n" +
                    "    \"layout\":\n" +
                    "            [\n" +
                    "                {\n" +
                    "                    \"type\": \"columnContainer\",\n" +
                    "                    \"width\": \"50%\",\n" +
                    "                     \"id\": \"\",\n" +
                    "                    \"layout\":\n" +
                    "                            [\n" +
                    "                            ]\n" +
                    "                },\n" +
                    "                {\n" +
                    "                    \"type\": \"columnContainer\",\n" +
                    "                    \"width\": \"50%\",\n" +
                    "                     \"id\": \"\",\n" +
                    "                    \"layout\":\n" +
                    "                            [\n" +
                    "\n" +
                    "                            ]\n" +
                    "                }\n" +
                    "            ]\n" +
                    "}";
    public static String TWO_COLUMN_ASYMETRIC1_LAYOUT="{\n" +
                       "    \"layout\":\n" +
                       "            [\n" +
                       "                {\n" +
                       "                    \"type\": \"columnContainer\",\n" +
                       "                    \"width\": \"33%\",\n" +
                       "                     \"id\": \"\",\n" +
                       "                    \"layout\":\n" +
                       "                            [\n" +
                       "                            ]\n" +
                       "                },\n" +
                       "                {\n" +
                       "                    \"type\": \"columnContainer\",\n" +
                       "                    \"width\": \"66%\",\n" +
                       "                     \"id\": \"\",\n" +
                       "                    \"layout\":\n" +
                       "                            [\n" +
                       "\n" +
                       "                            ]\n" +
                       "                }\n" +
                       "            ]\n" +
                       "}";
    public static  String TWO_COLUMN_ASYMETRIC2_LAYOUT="{\n" +
                       "    \"layout\":\n" +
                       "            [\n" +
                       "                {\n" +
                       "                    \"type\": \"columnContainer\",\n" +
                       "                    \"width\": \"66%\",\n" +
                       "                     \"id\": \"\",\n" +
                       "                    \"layout\":\n" +
                       "                            [\n" +
                       "                            ]\n" +
                       "                },\n" +
                       "                {\n" +
                       "                    \"type\": \"columnContainer\",\n" +
                       "                    \"width\": \"33%\",\n" +
                       "                     \"id\": \"\",\n" +
                       "                    \"layout\":\n" +
                       "                            [\n" +
                       "\n" +
                       "                            ]\n" +
                       "                }\n" +
                       "            ]\n" +
                       "}";
    public static String DEFAULT_THREE_COLUMN_LAYOUT="{\n" +
                    "    \"layout\":\n" +
                    "            [\n" +
                    "                {\n" +
                    "                    \"type\": \"columnContainer\",\n" +
                    "                    \"width\": \"33%\",\n" +
                    "                                    \"id\": \" \",\n" +
                    "                    \"layout\":\n" +
                    "                            [\n" +
                    "                            ]\n" +
                    "                },\n" +
                    "                {\n" +
                    "                    \"type\": \"columnContainer\",\n" +
                    "                    \"width\": \"33%\",\n" +
                    "                                    \"id\": \" \",\n" +
                    "                    \"layout\":\n" +
                    "                            [\n" +
                    "                            ]\n" +
                    "                },\n" +
                    "                {\n" +
                    "                    \"type\": \"columnContainer\",\n" +
                    "                    \"width\": \"33%\",\n" +
                    "                                    \"id\": \" \",\n" +
                    "                    \"layout\":\n" +
                    "                            [\n" +
                    "                            ]\n" +
                    "                }\n" +
                    "            ]\n" +
                    "}";
    public static String CUSTOM_THREE_COLUMN_LAYOUT="{\n" +
                    "\"layout\":\n" +
                    "   [\n" +
                    "   {\n" +
                    "       \"type\": \"rowContainer\",\n" +
                    "       \"height\": \"33%\",\n" +
                    "       \"isCont\": \"false\", \n" +
                    "        \"id\": \" \",\n" +
                    "         \"layout\":\n" +
                    "           [\n" +
                    "           ]\n" +
                    "      },\n" +
                    "      {\n" +
                    "           \"type\": \"rowContainer\",\n" +
                    "           \"height\": \"66%\",\n" +
                    "           \"isCont\": \"true\", \n" +
                    "               \"layout\":\n" +
                    "               [\n" +
                    "               {\n" +
                    "                           \"type\": \"columnContainer\",\n" +
                    "                           \"width\": \"33%\",\n" +
                    "                            \"id\": \" \",\n" +
                    "                           \"layout\":\n" +
                    "                               [\n" +
                    "                               ]\n" +
                    "                   },\n" +
                    "                   {\n" +
                    "                               \"type\": \"columnContainer\",\n" +
                    "                               \"width\": \"66%\",\n" +
                    "                               \"id\": \" \",\n" +
                    "                               \"layout\":\n" +
                    "                                   [\n" +
                    "                                   ]\n" +
                    "                      }\n" +
                    "                      ]\n" +
                    "               }\n" +
                    "]\n" +
                    "}";
}

