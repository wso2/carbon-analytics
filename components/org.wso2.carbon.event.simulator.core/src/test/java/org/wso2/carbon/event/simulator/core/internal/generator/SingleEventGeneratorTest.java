package org.wso2.carbon.event.simulator.core.internal.generator;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.event.simulator.core.exception.EventGenerationException;
import org.wso2.carbon.event.simulator.core.exception.InsufficientAttributesException;
import org.wso2.carbon.event.simulator.core.exception.InvalidConfigException;
import org.wso2.carbon.event.simulator.core.service.EventSimulatorDataHolder;
import org.wso2.carbon.stream.processor.common.EventStreamService;
import org.wso2.siddhi.query.api.definition.Attribute;

import util.StreamProcessorUtil;

import java.util.ArrayList;
import java.util.Arrays;

public class SingleEventGeneratorTest {

    @BeforeMethod
    public void setUp() throws Exception {
        EventSimulatorDataHolder.getInstance().setEventStreamService(new StreamProcessorUtil(0));
        StreamProcessorUtil streamProcessorUtil = (StreamProcessorUtil) EventSimulatorDataHolder.getInstance()
                .getEventStreamService();
        streamProcessorUtil.addStreamAttributes("TestExecutionPlan", "FooStream",
                new ArrayList<Attribute>() { {
                    add(new Attribute("symbol", Attribute.Type.STRING));
                    add(new Attribute("price", Attribute.Type.FLOAT));
                    add(new Attribute("volume", Attribute.Type.LONG));
                    
                }});
        streamProcessorUtil.addStreamAttributes("TestExecutionPlan1", "FooStream1",
                new ArrayList<Attribute>() { {
                    add(new Attribute("symbol", Attribute.Type.STRING));
                    add(new Attribute("price", Attribute.Type.FLOAT));
                    add(new Attribute("volume", Attribute.Type.LONG));

                }});
    }

    @Test
    public void testSetEventStreamService() throws Exception {
        EventStreamService eventStreamService = EventSimulatorDataHolder.getInstance().getEventStreamService();
    }

    @Test
    public void testEventWithAllProperties() throws Exception {
        SingleEventGenerator.sendEvent("{\n" +
                "  \"streamName\": \"FooStream\",\n" +
                "  \"executionPlanName\": \"TestExecutionPlan\",\n" +
                "  \"timestamp\": \"1488615136958\",\n" +
                "  \"data\": [\n" +
                "    null,\n" +
                "    \"9\",\n" +
                "    \"45\"\n" +
                "  ]\n" +
                "}");
        StreamProcessorUtil streamProcessorUtil = (StreamProcessorUtil) EventSimulatorDataHolder.getInstance()
                .getEventStreamService();
//        Event event = new Event();
//        event.setTimestamp(1488615136958L);
//        event.setData(new Object[]{null, 9f, 45L});
//        LinkedList<EventData> expectedEvent = new LinkedList<>();
//        expectedEvent.add(new EventData("TestExecutionPlan", "FooStream", event));
        Assert.assertEquals("TestExecutionPlan", streamProcessorUtil.getEventsReceived().get(0)
                .getExecutionPlanName(), " Execution plan names equal");
        Assert.assertEquals("FooStream", streamProcessorUtil.getEventsReceived().get(0).getStreamName(),
                "Stream name is equal");
        Object[] data = new Object[] {null, 9f, 45L};
        Assert.assertEquals(1488615136958L, streamProcessorUtil.getEventsReceived().get(0).getEvent()
                .getTimestamp(), "Event timestamp is equal");
        Assert.assertEquals(Arrays.deepToString(data), Arrays.deepToString(streamProcessorUtil.getEventsReceived()
                .get(0).getEvent().getData()), "Event data is equal");
    }

    @Test
    public void testEventWithoutTimestamp() throws Exception {
        SingleEventGenerator.sendEvent("{\n" +
                "  \"streamName\": \"FooStream\",\n" +
                "  \"executionPlanName\": \"TestExecutionPlan\",\n" +
                "  \"timestamp\": null,\n" +
                "  \"data\": [\n" +
                "    null,\n" +
                "    \"9\",\n" +
                "    \"45\"\n" +
                "  ]\n" +
                "}");
        StreamProcessorUtil streamProcessorUtil = (StreamProcessorUtil) EventSimulatorDataHolder.getInstance()
                .getEventStreamService();
        Assert.assertEquals("TestExecutionPlan", streamProcessorUtil.getEventsReceived().get(0)
                .getExecutionPlanName(), " Execution plan names equal");
        Assert.assertEquals("FooStream", streamProcessorUtil.getEventsReceived().get(0).getStreamName(),
                "Stream name is equal");
        Object[] data = new Object[] {null, 9f, 45L};
//        todo cant check since cant get the system time of when the event is created
        Assert.assertEquals(System.currentTimeMillis(), streamProcessorUtil.getEventsReceived().get(0).getEvent()
                .getTimestamp(), "Event timestamp is equal");
        Assert.assertEquals(Arrays.deepToString(data), Arrays.deepToString(streamProcessorUtil.getEventsReceived()
                .get(0).getEvent().getData()), "Event data is equal");

    }

     @Test
    public void testEventWithoutTimestamp2() throws Exception {
        SingleEventGenerator.sendEvent("{\n" +
                "  \"streamName\": \"FooStream\",\n" +
                "  \"executionPlanName\": \"TestExecutionPlan\",\n" +
                "  \"data\": [\n" +
                "    null,\n" +
                "    \"9\",\n" +
                "    \"45\"\n" +
                "  ]\n" +
                "}");
        StreamProcessorUtil streamProcessorUtil = (StreamProcessorUtil) EventSimulatorDataHolder.getInstance()
                .getEventStreamService();
        Assert.assertEquals("TestExecutionPlan", streamProcessorUtil.getEventsReceived().get(0)
                .getExecutionPlanName(), " Execution plan names equal");
        Assert.assertEquals("FooStream", streamProcessorUtil.getEventsReceived().get(0).getStreamName(),
                "Stream name is equal");
        Object[] data = new Object[] {null, 9f, 45L};
//        todo cant check since cant get the system time of when the event is created
        Assert.assertEquals(System.currentTimeMillis(), streamProcessorUtil.getEventsReceived().get(0).getEvent()
                .getTimestamp(), "Event timestamp is equal");
        Assert.assertEquals(Arrays.deepToString(data), Arrays.deepToString(streamProcessorUtil.getEventsReceived()
                .get(0).getEvent().getData()), "Event data is equal");

    }

    @Test(expectedExceptions = EventGenerationException.class)
    public void testExecutionPlanNotDeployed() throws Exception {
        SingleEventGenerator.sendEvent("{\n" +
                "  \"streamName\": \"FooStream\",\n" +
                "  \"executionPlanName\": \"ExecutionPlan\",\n" +
                "  \"timestamp\": \"1488615136958\",\n" +
                "  \"data\": [\n" +
                "    null,\n" +
                "    \"9\",\n" +
                "    \"45\"\n" +
                "  ]\n" +
                "}");
    }

    @Test(expectedExceptions = EventGenerationException.class)
    public void testStreamNotDeployed() throws Exception {
        SingleEventGenerator.sendEvent("{\n" +
                "  \"streamName\": \"FooStream1\",\n" +
                "  \"executionPlanName\": \"TestExecutionPlan\",\n" +
                "  \"timestamp\": \"1488615136958\",\n" +
                "  \"data\": [\n" +
                "    null,\n" +
                "    \"9\",\n" +
                "    \"45\"\n" +
                "  ]\n" +
                "}");
    }

    @Test(expectedExceptions = InvalidConfigException.class)
    public void testStreamNotAvailable() throws Exception {
        SingleEventGenerator.sendEvent("{\n" +
                "  \"streamName\": \"\",\n" +
                "  \"executionPlanName\": \"TestExecutionPlan\",\n" +
                "  \"timestamp\": \"1488615136958\",\n" +
                "  \"data\": [\n" +
                "    null,\n" +
                "    \"9\",\n" +
                "    \"45\"\n" +
                "  ]\n" +
                "}");
    }

    @Test(expectedExceptions = InvalidConfigException.class)
    public void testExecutionPlanNameNotAvailable() throws Exception {
        SingleEventGenerator.sendEvent("{\n" +
                "  \"streamName\": \"FooStream1\",\n" +
                "  \"executionPlanName\": null,\n" +
                "  \"timestamp\": \"1488615136958\",\n" +
                "  \"data\": [\n" +
                "    null,\n" +
                "    \"9\",\n" +
                "    \"45\"\n" +
                "  ]\n" +
                "}");
    }

    @Test(expectedExceptions = InvalidConfigException.class)
    public void testExecutionPlanNameNotAvailable2() throws Exception {
        SingleEventGenerator.sendEvent("{\n" +
                "  \"streamName\": \"FooStream1\",\n" +
                "  \"timestamp\": \"1488615136958\",\n" +
                "  \"data\": [\n" +
                "    null,\n" +
                "    \"9\",\n" +
                "    \"45\"\n" +
                "  ]\n" +
                "}");
    }

    @Test(expectedExceptions = InsufficientAttributesException.class)
    public void testInsufficientEventData() throws Exception {
        SingleEventGenerator.sendEvent("{\n" +
                "  \"streamName\": \"FooStream\",\n" +
                "  \"executionPlanName\": \"TestExecutionPlan\",\n" +
                "  \"timestamp\": \"1488615136958\",\n" +
                "  \"data\": [\n" +
                "    null,\n" +
                "    \"9\"\n" +
                "  ]\n" +
                "}");

    }

    @AfterMethod
    public void tearDown() throws Exception {
        EventSimulatorDataHolder.getInstance().setEventStreamService(null);
    }

}
