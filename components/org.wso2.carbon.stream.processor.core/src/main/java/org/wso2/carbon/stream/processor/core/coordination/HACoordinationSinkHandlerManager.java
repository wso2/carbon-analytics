package org.wso2.carbon.stream.processor.core.coordination;

import org.wso2.siddhi.core.stream.output.sink.SinkHandler;
import org.wso2.siddhi.core.stream.output.sink.SinkHandlerManager;

public class HACoordinationSinkHandlerManager extends SinkHandlerManager{

    @Override
    public SinkHandler generateSinkHandler() {
        return new HACoordinationSinkHandler();
    }

}
