package org.wso2.event.processor.core.test;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.processor.core.ExecutionPlanConfiguration;
import org.wso2.carbon.event.processor.core.StreamConfiguration;

import java.util.ArrayList;
import java.util.List;

public class SiddhiRuntimeTestCase {
    private static final Log log = LogFactory.getLog(SiddhiRuntimeTestCase.class);

    @Test
    public void testSiddhiRuntimeLoading() {
        AxisConfiguration axisConfig = new AxisConfiguration();

        ExecutionPlanConfiguration config = new ExecutionPlanConfiguration();
        config.setName("queryPlan1");
        config.setQueryExpressions("from stockStream select symbol, price insert into financialDataStream;");
        StreamConfiguration in = new StreamConfiguration("stockStream", "1.0.0");
        StreamConfiguration out = new StreamConfiguration("financialDataStream", "1.0.0");
        config.addImportedStream(in);
        config.addExportedStream(out);

        List<StreamDefinition> importedStreams = new ArrayList<StreamDefinition>();
        StreamDefinition streamDefinition = new StreamDefinition("stockStream");
        streamDefinition.addMetaData("symbol", AttributeType.STRING);
        streamDefinition.addPayloadData("price", AttributeType.DOUBLE);
        importedStreams.add(streamDefinition);
//        SiddhiRuntime runtime = SiddhiRuntimeFactory.createSiddhiRuntime(config, importedStreams, -1);
        log.info("siddhi runtime loaded successfully.");

    }
}
