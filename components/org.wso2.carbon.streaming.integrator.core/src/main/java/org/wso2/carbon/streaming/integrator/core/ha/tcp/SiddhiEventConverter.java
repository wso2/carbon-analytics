/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.streaming.integrator.core.ha.tcp;


import org.apache.log4j.Logger;
import org.wso2.carbon.streaming.integrator.core.util.BinaryMessageConverterUtil;
import io.siddhi.core.event.Event;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * This class is a implementation EventConverter to create the event from the Binary message.
 * This is used within data bridge to create the event from the row message received.
 */
public class SiddhiEventConverter {//todo
    static final Logger LOG = Logger.getLogger(SiddhiEventConverter.class);
    private static int count = 0;


    public static ByteBuffer decompress(ByteBuffer byteBuffer) throws IOException, DataFormatException {
        //byte[] dataBytes = Base64.decode(data);
        Inflater inflater = new Inflater();
        inflater.setInput(byteBuffer.array());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(byteBuffer.array().length);
        byte[] buffer = new byte[byteBuffer.array().length];
        //System.out.printf("REEEE            "+byteBuffer.array().length);
        while (!inflater.finished()) {
            int count = inflater.inflate(buffer);
            outputStream.write(buffer, 0, count);
        }

        outputStream.close();
        byte[] output = outputStream.toByteArray();
        return ByteBuffer.wrap(output);
    }

    public static Event getEvent(ByteBuffer byteBuffer, String[] attributeTypes) throws UnsupportedEncodingException {
        Event event = new Event();
        long timeStamp = byteBuffer.getLong();
        event.setTimestamp(timeStamp);
        event.setData(toObjectArray(byteBuffer, attributeTypes));
        return event;
    }

    static Object[] toObjectArray(ByteBuffer byteBuffer,
                                  String[] attributeTypeOrder) throws UnsupportedEncodingException {
        if (attributeTypeOrder != null) {
            Object[] objects = new Object[attributeTypeOrder.length];
            for (int i = 0; i < attributeTypeOrder.length; i++) {
                switch (attributeTypeOrder[i]) {
                    case "INT":
                        objects[i] = byteBuffer.getInt();
                        break;
                    case "LONG":
                        objects[i] = byteBuffer.getLong();
                        break;
                    case "STRING":
                        int stringSize = byteBuffer.getInt();
                        if (stringSize == 0) {
                            objects[i] = null;
                        } else {
                            objects[i] = BinaryMessageConverterUtil.getString(byteBuffer, stringSize);
                        }
                        break;
                    case "DOUBLE":
                        objects[i] = byteBuffer.getDouble();
                        break;
                    case "FLOAT":
                        objects[i] = byteBuffer.getFloat();
                        break;
                    case "BOOL":
                        objects[i] = byteBuffer.get() == 1;
                        break;
                    default:
                        // will not occur
                }
            }
            return objects;
        } else {
            return null;
        }
    }

}
