package org.wso2.event.processor.core.test;

import junit.framework.Assert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.event.processor.core.StreamConfiguration;
import org.wso2.carbon.event.processor.core.internal.stream.EventConsumer;
import org.wso2.carbon.event.processor.core.internal.stream.EventJunction;
import org.wso2.carbon.event.processor.core.internal.util.EventProcessorUtil;
import org.wso2.siddhi.core.event.Event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EventJunctionTestCase {
    private static final Log log = LogFactory.getLog(EventJunctionTestCase.class);

    @Test
    public void testEventJunction1() throws MalformedStreamDefinitionException {
        StreamDefinition def = new StreamDefinition("stockStream", "1.1.0");
        ArrayList<Attribute> payload = new ArrayList<Attribute>();
        payload.add(new Attribute("symbol", AttributeType.STRING));
        payload.add(new Attribute("price", AttributeType.DOUBLE));
        payload.add(new Attribute("volume", AttributeType.LONG));
        def.setPayloadData(payload);
        EventJunction junction = new EventJunction(def);
        junction.addConsumer(new EventConsumer() {

            @Override
            public void consumeEvents(Object[][] events) {
                for (Object[] eventData : events) {
                    log.info("Event data received from junction: " + Arrays.deepToString(eventData));
                }
            }

            @Override
            public void consumeEvents(Event[] events) {
                for (Object o : events) {
                    log.info("Event received from junction: " + Arrays.deepToString((Object[]) o));
                }
            }

            @Override
            public void consumeEvent(Object[] eventData) {
                log.info("Event data received from junction: " + Arrays.deepToString(eventData));
            }

            @Override
            public void consumeEvent(Event event) {
                log.info("Event received from junction: " + event);
            }

            @Override
            public Object getOwner() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }
        });


        junction.dispatchEvents(new Object[][]{{"goog", 877.90, 12000}, {"aapl", 456.60, 1345}});
//        junction.streamRemoved(def, null, StreamModificationSourcePriority.BUILDER);

        StreamDefinition def2 = new StreamDefinition("stockStream", "1.1.0");
        ArrayList<Attribute> payload2 = new ArrayList<Attribute>();
        payload2.add(new Attribute("symbol", AttributeType.STRING));
        payload2.add(new Attribute("price", AttributeType.DOUBLE));
        payload2.add(new Attribute("volume", AttributeType.LONG));
        payload2.add(new Attribute("trades", AttributeType.INT));
        def2.setPayloadData(payload);

//        junction.streamAdded(def2, null, StreamModificationSourcePriority.BUILDER);

        junction.dispatchEvents(new Object[][]{{"goog", 877.90, 12000, 8}, {"aapl", 456.60, 1345, 3}});

    }

    @Test
    public void testStreamDefinitionConversion() throws MalformedStreamDefinitionException {
        StreamDefinition streamDef = new StreamDefinition("stockStream", "1.1.0");
        List<Attribute> meta = new ArrayList<Attribute>(1);
        meta.add(new Attribute("symbol", AttributeType.STRING));
        List<Attribute> payload = new ArrayList<Attribute>(1);
        payload.add(new Attribute("price", AttributeType.DOUBLE));
        streamDef.setMetaData(meta);
        streamDef.setPayloadData(payload);

        org.wso2.siddhi.query.api.definition.StreamDefinition siddhiDefinition = EventProcessorUtil.convertToSiddhiStreamDefinition(streamDef, new StreamConfiguration("stockStream", "1.1.0"));
        Assert.assertEquals(siddhiDefinition.getAttributeList().size(), 2);
        log.info(siddhiDefinition);

/*
        StreamDefinition databrigeDefinition = EventProcessorUtil.convertToDatabridgeStreamDefinition(siddhiDefinition, "stockStream/1.1.0");
        Assert.assertEquals(databrigeDefinition.getPayloadData().size(), 2);
        log.info(databrigeDefinition);
*/
    }


}
