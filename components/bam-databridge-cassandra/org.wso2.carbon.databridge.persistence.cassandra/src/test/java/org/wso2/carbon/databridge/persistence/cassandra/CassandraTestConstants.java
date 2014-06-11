package org.wso2.carbon.databridge.persistence.cassandra;

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
public class CassandraTestConstants {

    public static final String BAM_EVENT_DATA_KEYSPACE = "EVENT_KS";

    public static final String properEvent = "[\n" +
                                             "     {\n" +
                                             "      \"payloadData\" : [\"IBM\", 26.0, 848, 43.33, 2.3] ,\n" +
                                             "      \"metaData\" : [\"123.233.0.1\"] ,\n" +
                                             "      \"timeStamp\" : 1312345432\n" +
                                             "     }\n" +
                                             "    ,\n" +
                                             "     {\n" +
                                             "      \"streamId\" : \"bar::2.1.0\", \n" +
                                             "      \"payloadData\" : [\"MSFT\", 22.0, 233, 22.22, 4.3] ,\n" +
                                             "      \"metaData\" : [\"5.211.1.1\"] ,\n" +
                                             "     }\n" +
                                             "\n" +
                                             "   ]";
    public static final String properandImproperEvent = "[\n" +
                                                        "     {\n" +
                                                        "      \"payloadData\" : [\"IBM\", 26.0, 848, 43.33, 2.3] ,\n" +
                                                        "      \"metaData\" : [\"123.233.0.1\"] ,\n" +
                                                        "      \"timeStamp\" : 1312345432\n" +
                                                        "     }\n" +
                                                        "    ,\n" +
                                                        "     {\n" +
                                                        "      \"streamId\" : \"bar::2.1.0\", \n" +
                                                        "      \"payloadData\" : [\"MSFT\", 233, 22.22, 4.3] ,\n" +
                                                        "      \"metaData\" : [\"5.211.1.1\"] ,\n" +
                                                        "     }\n" +
                                                        "    ,\n" +
                                                        "     {\n" +
                                                        "      \"payloadData\" : [\"IBM\", 26.0, 848, 43.33, 2.3] ,\n" +
                                                        "      \"metaData\" : [\"123.233.0.1\"] ,\n" +
                                                        "      \"timeStamp\" : 1312345432\n" +
                                                        "     }\n" +
                                                        "\n" +
                                                        "   ]";
    public static final String tooLongdefinition = "{" +
                                                   "  'name':'org.wso2.esb.MediatorStatistics-bbbbbbbbbbbbbaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa'," +
                                                   "  'version':'2.3.0'," +
                                                   "  'nickName': 'Stock Quote Information'," +
                                                   "  'description': 'Some Desc'," +
                                                   "  'tags':['foo', 'bar']," +
                                                   "  'metaData':[" +
                                                   "          {'name':'ipAdd','type':'STRING'}" +
                                                   "  ]," +
                                                   "  'payloadData':[" +
                                                   "          {'name':'symbol','type':'string'}," +
                                                   "          {'name':'price','type':'double'}," +
                                                   "          {'name':'volume','type':'int'}," +
                                                   "          {'name':'max','type':'double'}," +
                                                   "          {'name':'min','type':'double'}" +
                                                   "  ]" +
                                                   "}";
    public static final String definition = "{" +
                                            "  'name':'org.wso2.esb.MediatorStatistics'," +
                                            "  'version':'2.3.0'," +
                                            "  'nickName': 'Stock Quote Information'," +
                                            "  'description': 'Some Desc'," +
                                            "  'tags':['foo', 'bar']," +
                                            "  'metaData':[" +
                                            "          {'name':'ipAdd','type':'STRING'}" +
                                            "  ]," +
                                            "  'payloadData':[" +
                                            "          {'name':'symbol','type':'string'}," +
                                            "          {'name':'price','type':'double'}," +
                                            "          {'name':'volume','type':'int'}," +
                                            "          {'name':'max','type':'double'}," +
                                            "          {'name':'min','type':'double'}" +
                                            "  ]" +
                                            "}";
    public static final String definition2 = "{" +
                                             "  'name':'org.wso2.esb.MediatorStatistics'," +
                                             "  'version':'2.3.4'," +
                                             "  'nickName': 'Stock Quote Information'," +
                                             "  'description': 'Some Desc'," +
                                             "  'tags':['foo', 'bar']," +
                                             "  'metaData':[" +
                                             "          {'name':'ipAdd','type':'STRING'}" +
                                             "  ]," +
                                             "  'payloadData':[" +
                                             "          {'name':'symbol','type':'string'}," +
                                             "          {'name':'price','type':'double'}," +
                                             "          {'name':'volume','type':'int'}," +
                                             "          {'name':'max','type':'double'}," +
                                             "          {'name':'min','type':'double'}," +
                                             "          {'name':'initialPrice','type':'double'}" +
                                             "  ]" +
                                             "}";
    public static final String definition3 = "{" +
                                             "  'name':'org.wso2.esb.ServiceStats'," +
                                             "  'version':'3.2.2'," +
                                             "  'nickName': 'Stock Quote Information'," +
                                             "  'description': 'Some Desc'," +
                                             "  'tags':['foo', 'bar']," +
                                             "  'metaData':[" +
                                             "          {'name':'ipAdd','type':'STRING'}" +
                                             "  ]," +
                                             "  'payloadData':[" +
                                             "          {'name':'symbol','type':'string'}," +
                                             "          {'name':'price','type':'int'}," +
                                             "          {'name':'volume','type':'string'}," +
                                             "          {'name':'max','type':'int'}," +
                                             "          {'name':'min','type':'double'}" +
                                             "  ]" +
                                             "}";
    public static final String properEvent2 = "[\n" +
                                              "     {\n" +
                                              "      \"payloadData\" : [\"IBM\", 26, \"Smart Planet\", 43, 2.3] ,\n" +
                                              "      \"metaData\" : [\"123.233.0.1\"] ,\n" +
                                              "      \"timeStamp\" : 1312345432\n" +
                                              "     }\n" +
                                              "    ,\n" +
                                              "     {\n" +
                                              "      \"streamId\" : \"bar::2.1.0\", \n" +
                                              "      \"payloadData\" : [\"MSFT\", 22, \"Buy the world\", 22, 4.3] ,\n" +
                                              "      \"metaData\" : [\"5.211.1.1\"] ,\n" +
                                              "     }\n" +
                                              "\n" +
                                              "   ]";
    public static final String multipleProperEvent1 = "[\n" +
                                                      "     {\n" +
                                                      "      \"payloadData\" : [\"IBM\", 26.0, 848, 43.33, 2.3] ,\n" +
                                                      "      \"metaData\" : [\"123.233.0.1\"] ,\n" +
                                                      "      \"timeStamp\" : 1312345432\n" +
                                                      "     }\n" +
                                                      "    ,\n" +
                                                      "     {\n" +
                                                      "      \"streamId\" : \"bar::2.1.0\", \n" +
                                                      "      \"payloadData\" : [\"MSFT\", 22.0, 233, 22.22, 4.3] ,\n" +
                                                      "      \"metaData\" : [\"5.211.1.1\"] ,\n" +
                                                      "      \"timeStamp\" : 1312345488\n" +
                                                      "     }\n" +
                                                      "\n" +
                                                      "   ]";
    public static final String multipleProperEvent2 = "[\n" +
                                                      "     {\n" +
                                                      "      \"payloadData\" : [\"IBM\", 26.0, 848, 43.33, 2.3, 34.0] ,\n" +
                                                      "      \"metaData\" : [\"123.233.0.1\"] ,\n" +
                                                      "      \"timeStamp\" : 1312345432\n" +
                                                      "     }\n" +
                                                      "    ,\n" +
                                                      "     {\n" +
                                                      "      \"streamId\" : \"bar::2.1.0\", \n" +
                                                      "      \"payloadData\" : [\"MSFT\", 22.0, 233, 22.22, 4.3, 33.9] ,\n" +
                                                      "      \"metaData\" : [\"5.211.1.1\"] ,\n" +
                                                      "      \"timeStamp\" : 1312345488\n" +
                                                      "     }\n" +
                                                      "\n" +
                                                      "   ]";
    public static final String multipleProperEvent3 = "[\n" +
                                                      "     {\n" +
                                                      "      \"payloadData\" : [\"IBM\", 26, \"Smart Planet\", 43, 2.3] ,\n" +
                                                      "      \"metaData\" : [\"123.233.0.1\"] ,\n" +
                                                      "      \"timeStamp\" : 1312345432\n" +
                                                      "     }\n" +
                                                      "    ,\n" +
                                                      "     {\n" +
                                                      "      \"streamId\" : \"bar::2.1.0\", \n" +
                                                      "      \"payloadData\" : [\"MSFT\", 22, \"Buy the world\", 22, 4.3] ,\n" +
                                                      "      \"metaData\" : [\"5.211.1.1\"] ,\n" +
                                                      "      \"timeStamp\" : 1312347668\n" +
                                                      "     }\n" +
                                                      "\n" +
                                                      "   ]";
}
