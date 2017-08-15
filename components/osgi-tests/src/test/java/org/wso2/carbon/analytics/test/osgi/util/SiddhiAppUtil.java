package org.wso2.carbon.analytics.test.osgi.util;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.output.StreamCallback;
import org.wso2.siddhi.core.util.EventPrinter;

public class SiddhiAppUtil {

    private static final Logger log = org.apache.log4j.Logger.getLogger(SiddhiAppUtil.class);

    private static final String SIDDHIAPP_STREAM = "@App:name('SiddhiAppPersistence')" +
            "define stream FooStream (symbol string, volume long); ";

    private static final String SIDDHIAPP_SOURCE = "" +
            "@source(type='inMemory', topic='symbol', @map(type='passThrough'))" +
            "Define stream BarStream (symbol string, max long);";

    private static final String SIDDHIAPP_QUERY = "" +
            "from FooStream#window.length(5) " +
            "select symbol, max(volume) as max " +
            "group by symbol " +
            "insert into BarStream ;";

    public static SiddhiAppRuntime createSiddhiApp(SiddhiManager siddhiManager) throws InterruptedException {
        log.info(SIDDHIAPP_STREAM + SIDDHIAPP_SOURCE + SIDDHIAPP_QUERY);
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(SIDDHIAPP_STREAM + SIDDHIAPP_SOURCE + SIDDHIAPP_QUERY);
        siddhiAppRuntime.start();
        siddhiAppRuntime.addCallback("BarStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
            }
        });

        return siddhiAppRuntime;
    }

    public static void sendDataToStream(String name, long value, SiddhiAppRuntime siddhiAppRuntime) throws InterruptedException {
        InputHandler fooStream = siddhiAppRuntime.getInputHandler("FooStream");
        fooStream.send(new Object[]{name, value});
        Thread.sleep(500);
    }

}
