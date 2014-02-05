package org.wso2.carbon.event.formatter.test;

import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.event.formatter.core.EventFormatterListener;
import org.wso2.carbon.event.formatter.core.EventSource;
import org.wso2.carbon.event.formatter.core.exception.EventFormatterConfigurationException;

import java.util.ArrayList;
import java.util.List;

/*
* Copyright 2004,2005 The Apache Software Foundation.
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
*/public class EventSourceImpl implements EventSource {


    @Override
    public List<String> getStreamNames() {
        List<String> streamNames = new ArrayList<String>();
        streamNames.add("summarizedStatistics/1.0.0");

        return streamNames;

    }

    @Override
    public StreamDefinition getStreamDefinition(String StreamName, String StreamVersion) {
        StreamDefinition streamDefinition = null;
        try {
            streamDefinition = new StreamDefinition("summarizedStatistics", "1.0.0");
            streamDefinition.addPayloadData("ipAdd", AttributeType.STRING);
            streamDefinition.addCorrelationData("userID", AttributeType.STRING);
            streamDefinition.addPayloadData("searchTerms", AttributeType.STRING);
            streamDefinition.addPayloadData("test", AttributeType.INT);


        } catch (MalformedStreamDefinitionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return streamDefinition;
    }

//    @Override
//    public StreamDefinition getStreamDefinition(String StreamNameWithVersion) {
//        StreamDefinition streamDefinition = null;
//        try {
//            streamDefinition = new StreamDefinition("summarizedStatistics", "1.0.0");
//            streamDefinition.addPayloadData("ipAdd", AttributeType.STRING);
//            streamDefinition.addCorrelationData("userID", AttributeType.STRING);
//            streamDefinition.addPayloadData("searchTerms", AttributeType.STRING);
//            streamDefinition.addPayloadData("test", AttributeType.INT);
//
//
//        } catch (MalformedStreamDefinitionException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        }
//
//        return streamDefinition;
//    }


    @Override
    public void subscribe(StreamDefinition streamDefinition, EventFormatterListener eventListener)
            throws EventFormatterConfigurationException {
        //To change body of implemented methods use File | Settings | File Templates.

        Object[] arrayObject = new Object[4];
        arrayObject[0] = "ipp0";
        arrayObject[1] = "user1";
        arrayObject[2] = "search2";
        arrayObject[3] = 4;

        eventListener.onEvent(arrayObject);
    }
}
