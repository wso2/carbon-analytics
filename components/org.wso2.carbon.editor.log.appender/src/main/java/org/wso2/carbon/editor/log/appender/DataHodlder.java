package org.wso2.carbon.editor.log.appender;

import org.wso2.carbon.editor.log.appender.internal.CircularBuffer;
import org.wso2.carbon.editor.log.appender.internal.ConsoleLogEvent;

public class DataHodlder {
    private static CircularBuffer circularBuffer;

    public static CircularBuffer getBuffer(int bufferSize) {
        if (circularBuffer == null) {
            circularBuffer = new CircularBuffer<ConsoleLogEvent>(bufferSize);
        }
        return circularBuffer;
    }

    public static CircularBuffer getBuffer() {
        if (circularBuffer == null) {
            circularBuffer = new CircularBuffer<ConsoleLogEvent>();
        }
        return circularBuffer;
    }

}
