/**
 *
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.databridge.agent.thrift.internal.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.thrift.data.ThriftEventBundle;
import org.wso2.carbon.databridge.commons.utils.EventDefinitionConverterUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Util class used to convert Events to thrift format
 */
public final class ThriftEventConverter {
    private static Log log = LogFactory.getLog(ThriftEventConverter.class);

    private ThriftEventConverter() {
    }

    public static ThriftEventBundle toThriftEventBundle(Event event,
                                                        ThriftEventBundle eventBundle,
                                                        String sessionId) {
        ThriftEventBundle thriftEventBundle = eventBundle;
        if (thriftEventBundle == null) {
            thriftEventBundle = new ThriftEventBundle();
            thriftEventBundle.setStringAttributeList(new LinkedList<String>());//adding string list
            thriftEventBundle.setLongAttributeList(new LinkedList<Long>());//adding long list
            thriftEventBundle.setSessionId(sessionId);
            thriftEventBundle.setEventNum(0);
        }
        thriftEventBundle.addToStringAttributeList(event.getStreamId());
        thriftEventBundle.addToLongAttributeList(event.getTimeStamp());

        thriftEventBundle = assignAttributes(thriftEventBundle, event.getMetaData());
        thriftEventBundle = assignAttributes(thriftEventBundle, event.getCorrelationData());
        thriftEventBundle = assignAttributes(thriftEventBundle, event.getPayloadData());
        thriftEventBundle = assignMap(thriftEventBundle, event.getArbitraryDataMap());
        thriftEventBundle.setEventNum(thriftEventBundle.getEventNum() + 1);

        return thriftEventBundle;
    }

    private static ThriftEventBundle assignAttributes(ThriftEventBundle thriftEventBundle,
                                                      Object[] attributes) {
        if (attributes != null) {
            for (Object object : attributes) {

                if (object instanceof Integer) {
                    if (!thriftEventBundle.isSetIntAttributeList()) {
                        thriftEventBundle.setIntAttributeList(new LinkedList<Integer>());
                    }
                    thriftEventBundle.addToIntAttributeList((Integer) object);
                } else if (object instanceof Float) {
                    if (!thriftEventBundle.isSetDoubleAttributeList()) {
                        thriftEventBundle.setDoubleAttributeList(new LinkedList<Double>());
                    }
                    thriftEventBundle.addToDoubleAttributeList(((Float) object).doubleValue());
                } else if (object instanceof Long) {
                    if (!thriftEventBundle.isSetLongAttributeList()) {
                        thriftEventBundle.setLongAttributeList(new LinkedList<Long>());
                    }
                    thriftEventBundle.addToLongAttributeList((Long) object);
                } else if (object instanceof String) {
                    if (!thriftEventBundle.isSetStringAttributeList()) {
                        thriftEventBundle.setStringAttributeList(new LinkedList<String>());
                    }
                    thriftEventBundle.addToStringAttributeList((String) object);
                } else if (object instanceof Boolean) {
                    if (!thriftEventBundle.isSetBoolAttributeList()) {
                        thriftEventBundle.setBoolAttributeList(new LinkedList<Boolean>());
                    }
                    thriftEventBundle.addToBoolAttributeList((Boolean) object);
                } else if (object instanceof Double) {
                    if (!thriftEventBundle.isSetDoubleAttributeList()) {
                        thriftEventBundle.setDoubleAttributeList(new LinkedList<Double>());
                    }
                    thriftEventBundle.addToDoubleAttributeList((Double) object);
                } else if (object == null) {
                    if (!thriftEventBundle.isSetStringAttributeList()) {
                        thriftEventBundle.setStringAttributeList(new LinkedList<String>());
                    }
                    thriftEventBundle.addToStringAttributeList(EventDefinitionConverterUtils.nullString);
                } else {
                    log.error("Undefined attribute type : " + object);
                }
            }
        }
        return thriftEventBundle;
    }

    private static ThriftEventBundle assignMap(ThriftEventBundle thriftEventBundle, Map<String, String> arbitraryDataMap) {
        if (null != arbitraryDataMap) {
            if (!thriftEventBundle.isSetArbitraryDataMapMap()) {
                thriftEventBundle.setArbitraryDataMapMap(new HashMap<Integer, Map<String, String>>());
            }
            thriftEventBundle.putToArbitraryDataMapMap(thriftEventBundle.getEventNum(), arbitraryDataMap);
        }
        return thriftEventBundle;
    }


}
