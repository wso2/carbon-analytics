/*
 * Copyright 2017 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.editor.log.appender;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.util.Booleans;
import org.wso2.carbon.editor.log.appender.internal.CircularBuffer;
import org.wso2.carbon.editor.log.appender.internal.ConsoleLogEvent;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.text.SimpleDateFormat;

/**
 * This appender will be used to capture the logs and later send to clients, if requested via the
 * logging web service.
 * This maintains a circular buffer, of some fixed amount (say 100).
 */
@Plugin(name = "EditorConsole", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE, printObject = true)
public final class EditorConsoleAppender extends AbstractAppender {

    private CircularBuffer<ConsoleLogEvent> circularBuffer;
    private static final int BUFFER_SIZE = 10;

    /**
     * Creates an instance of EditorConsoleAppender.
     *
     * @param name             appender name
     * @param filter           null if not specified
     * @param layout           pattern of log messages
     * @param ignoreExceptions default is true
     *                         <p>
     *                         Called by {@link #createAppender(String, Filter, Layout, String, String)}
     */
    private EditorConsoleAppender(final String name, final Filter filter,
                                  final Layout<? extends Serializable> layout, final boolean ignoreExceptions) {
        super(name, filter, layout, ignoreExceptions);

        activateOptions();
    }

    /**
     * Taken from the previous EditorConsoleAppender
     */
    public void activateOptions() {
        this.circularBuffer = DataHodlder.getBuffer(BUFFER_SIZE);
    }

    /**
     * Creates a EditorConsoleAppender instance with
     * attributes configured in log4j2.properties.
     *
     * @param name   appender name
     * @param filter null if not specified
     * @param layout pattern of log messages
     * @param ignore default is true
     * @return intance of EditorConsoleAppender
     */
    @PluginFactory
    public static EditorConsoleAppender createAppender(@PluginAttribute("name") final String name,
                                                       @PluginElement("Filters") final Filter filter,
                                                       @PluginElement("Layout") Layout<? extends Serializable> layout,
                                                       @PluginAttribute("ignoreExceptions") final String ignore,
                                                       @PluginAttribute("buffSize") final String buffSize) {
        if (name == null) {
            LOGGER.error("No name provided for EditorConsoleAppender");
            return null;
        } else {
            if (layout == null) {
                layout = PatternLayout.createDefaultLayout();
            }
            final boolean ignoreExceptions = Booleans.parseBoolean(ignore, true);
            return new EditorConsoleAppender(name, filter, layout, ignoreExceptions);
        }
    }

    /**
     * This is the overridden method from the Appender interface. {@link Appender}
     * This allows to write log events to preferred destination.
     * <p>
     * Converts the default log events to tenant aware log events and writes to a CircularBuffer
     *
     * @param logEvent the LogEvent object
     */
    @Override
    public void append(LogEvent logEvent) {
        if (circularBuffer != null) {
            circularBuffer.append(populateConsoleLogEvent(logEvent));
        }
    }

    private ConsoleLogEvent populateConsoleLogEvent(LogEvent logEvent) {
        ConsoleLogEvent consoleLogEvent = new ConsoleLogEvent();
        consoleLogEvent.setFqcn(logEvent.getLoggerName());
        consoleLogEvent.setLevel(logEvent.getLevel().name());
        consoleLogEvent.setMessage(logEvent.getMessage().getFormattedMessage());
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss_SSS");
        String dateString = formatter.format(logEvent.getTimeMillis());
        consoleLogEvent.setTimeStamp(dateString);
        if (logEvent.getThrown() != null) {
            consoleLogEvent.setStacktrace(getStacktrace(logEvent.getThrown()));
        }
        return consoleLogEvent;
    }

    public void close() {
        // do we need to do anything here. I hope we do not need to reset the queue
        // as it might still be exposed to others
    }

    private String getStacktrace(Throwable e) {
        StringWriter stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString().trim();
    }
}
