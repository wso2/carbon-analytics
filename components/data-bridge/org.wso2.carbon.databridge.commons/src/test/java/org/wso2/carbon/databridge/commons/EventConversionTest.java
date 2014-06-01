package org.wso2.carbon.databridge.commons;

import junit.framework.TestCase;
import org.junit.Test;
import org.wso2.carbon.databridge.commons.exception.MalformedEventException;
import org.wso2.carbon.databridge.commons.utils.EventConverterUtils;

import java.util.List;
import java.util.UUID;

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

public class EventConversionTest extends TestCase {

    private String properJSON2 = "[\n" +
                                 "     {\n" +
                                 "      \"payloadData\" : [\"val1\", \"val2\"] ,\n" +
                                 "      \"metaData\" : [\"val1\", \"val2\", \"val3\"] ,\n" +
                                 "      \"correlationData\" : [\"val1\"],\n" +
                                 "      \"timeStamp\" : 1339496299900\n" +
                                 "     }\n" +
                                 "    ,\n" +
                                 "     {\n" +
                                 "      \"payloadData\" : [\"val1\", \"val2\"] ,\n" +
                                 "      \"metaData\" : [\"val1\", \"val2\", \"val3\"] ,\n" +
                                 "      \"correlationData\" : [\"val1\", \"val2\"]\n" +
                                 "     }\n" +
                                 "\n" +
                                 "   ]";


    private String properJSON = "[\n" +
                                "     {\n" +
                                "      \"streamId\" : \"foo::1.0.0\",\n" +
                                "      \"payloadData\" : [\"val1\", \"val2\"] ,\n" +
                                "      \"metaData\" : [\"val1\", \"val2\", \"val3\"] ,\n" +
                                "      \"correlationData\" : [\"val1\"],\n" +
                                "      \"timeStamp\" : 1312345432\n" +
                                "     }\n" +
                                "    ,\n" +
                                "     {\n" +
                                "      \"streamId\" : \"bar::2.1.0\", \n" +
                                "      \"payloadData\" : [\"val1\", \"val2\"] ,\n" +
                                "      \"metaData\" : [\"val1\", \"val2\", \"val3\"] ,\n" +
                                "      \"correlationData\" : [\"val1\", \"val2\"]\n" +
                                "     }\n" +
                                "\n" +
                                "   ]";

    private String noStreamIdJSON = "[\n" +
                                    "     {\n" +
                                    "      \"payloadData\" : [\"val1\", \"val2\"] ,\n" +
                                    "      \"metaData\" : [\"val1\", \"val2\", \"val3\"] ,\n" +
                                    "      \"correlationData\" : [\"val1\", \"val2\"],\n" +
                                    "      \"timeStamp\" : 1312345432\n" +
                                    "     }\n" +
                                    "    ,\n" +
                                    "     {\n" +
                                    "      \"streamId\" : \"bar::2.1.0\", \n" +
                                    "      \"payloadData\" : [\"val1\", \"val2\"] ,\n" +
                                    "      \"metaData\" : [\"val1\", \"val2\", \"val3\"] ,\n" +
                                    "      \"correlationData\" : [\"val1\", \"val2\"]\n" +
                                    "     }\n" +
                                    "\n" +
                                    "   ]";

    private String emptyStreamIdJSON = "[\n" +
                                       "     {\n" +
                                       "      \"streamId\" : \"\", \n" +
                                       "      \"payloadData\" : [\"val1\", \"val2\"] ,\n" +
                                       "      \"metaData\" : [\"val1\", \"val2\", \"val3\"] ,\n" +
                                       "      \"correlationData\" : [\"val1\", \"val2\"],\n" +
                                       "      \"timeStamp\" : 1312345432\n" +
                                       "     }\n" +
                                       "    ,\n" +
                                       "     {\n" +
                                       "      \"streamId\" : \"bar::2.1.0\", \n" +
                                       "      \"payloadData\" : [\"val1\", \"val2\"] ,\n" +
                                       "      \"metaData\" : [\"val1\", \"val2\", \"val3\"] ,\n" +
                                       "      \"correlationData\" : [\"val1\", \"val2\"]\n" +
                                       "     }\n" +
                                       "\n" +
                                       "   ]";


    private String emptyArrayJSON = "[\n" +
                                    "     {\n" +
                                    "      \"streamId\" : \"foo::1.0.0\", \n" +
                                    "      \"payloadData\" : [] ,\n" +
                                    "      \"metaData\" : [\"val1\", \"val2\", \"val3\"] ,\n" +
                                    "      \"correlationData\" : [\"val1\", \"val2\"],\n" +
                                    "      \"timeStamp\" : 1312345432\n" +
                                    "     }\n" +
                                    "    ,\n" +
                                    "     {\n" +
                                    "      \"streamId\" : \"bar::2.1.0\", \n" +
                                    "      \"payloadData\" : [\"val1\", \"val2\"] ,\n" +
                                    "      \"metaData\" : [\"val1\", \"val2\", \"val3\"] ,\n" +
                                    "      \"correlationData\" : [\"val1\", \"val2\"]\n" +
                                    "     }\n" +
                                    "\n" +
                                    "   ]";


    @Test
    public void testConversion() {
        List<Event> eventList = EventConverterUtils.convertFromJson(properJSON);
        assertEquals(2, eventList.size());
        Event event = eventList.get(0);
        assertEquals(event.getCorrelationData().length, 1);
        assertEquals(event.getPayloadData().length, 2);
        assertEquals(event.getMetaData().length, 3);

    }

    @Test(expected = MalformedEventException.class)
    public void testNoStreamId() {
        try {
            EventConverterUtils.convertFromJson(noStreamIdJSON);
        } catch (MalformedEventException e) {
        }
    }

    @Test(expected = MalformedEventException.class)
    public void testEmptyStreamId() {
        try {
            EventConverterUtils.convertFromJson(emptyStreamIdJSON);
        } catch (MalformedEventException e) {
        }
    }

    @Test
    public void testEmptyEventArray() {
        List<Event> eventList = EventConverterUtils.convertFromJson(emptyArrayJSON);
        Event event = eventList.get(0);
        assertEquals(event.getPayloadData().length, 0);
    }

    @Test
    public void testRESTEventConversion() {
        List<Event> eventList = EventConverterUtils.convertFromJson(properJSON2, "foo::1.0.0-" + UUID.randomUUID());
        assertEquals(2, eventList.size());
        Event event = eventList.get(0);
        assertEquals(event.getCorrelationData().length, 1);
        assertEquals(event.getPayloadData().length, 2);
        assertEquals(event.getMetaData().length, 3);

    }

    @Test(expected = MalformedEventException.class)
    public void testNullRESTEvents() {
        try {
            EventConverterUtils.convertFromJson(properJSON2, null);
        } catch (MalformedEventException e) {

        }

    }

    @Test(expected = MalformedEventException.class)
    public void testEmptyRESTEvents() {
        try {
            EventConverterUtils.convertFromJson(properJSON2, "");
        } catch (MalformedEventException e) {
            return;
        }
    }


}
