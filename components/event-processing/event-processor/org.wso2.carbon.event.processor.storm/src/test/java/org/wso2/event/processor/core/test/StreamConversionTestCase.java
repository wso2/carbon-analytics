package org.wso2.event.processor.core.test;

import junit.framework.Assert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.event.processor.core.StreamConfiguration;

public class StreamConversionTestCase {
    private static final Log log = LogFactory.getLog(StreamConversionTestCase.class);

    @Test
    public void testFromWso2EventToSiddhi1() throws MalformedStreamDefinitionException {

        StreamConfiguration streamConfiguration=new StreamConfiguration("Foo","1.0.0");
        Assert.assertEquals("Foo",streamConfiguration.getSiddhiStreamName());

    }

    @Test
    public void testFromWso2EventToSiddhi2() throws MalformedStreamDefinitionException {

        StreamConfiguration streamConfiguration=new StreamConfiguration("Foo","1.0.1");
        Assert.assertEquals("Foo_1_0_1",streamConfiguration.getSiddhiStreamName());

    }

    @Test
    public void testFromWso2EventToSiddhi3() throws MalformedStreamDefinitionException {

        StreamConfiguration streamConfiguration=new StreamConfiguration("Foo","1.0.1","Bar");
        Assert.assertEquals("Bar",streamConfiguration.getSiddhiStreamName());

    }

}
