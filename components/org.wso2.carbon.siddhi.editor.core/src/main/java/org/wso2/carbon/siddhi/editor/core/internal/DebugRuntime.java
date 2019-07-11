/*
 * Copyright (c)  2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.siddhi.editor.core.internal;

import org.wso2.carbon.siddhi.editor.core.exception.InvalidExecutionStateException;
import org.wso2.carbon.siddhi.editor.core.exception.NoSuchStreamException;
import org.wso2.carbon.siddhi.editor.core.util.DebugCallbackEvent;
import io.siddhi.core.SiddhiAppRuntime;
import io.siddhi.core.debugger.SiddhiDebugger;
import io.siddhi.core.stream.input.InputHandler;
import io.siddhi.query.api.definition.Attribute;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class DebugRuntime {
    private String siddhiAppName;
    private Mode mode = Mode.STOP;
    private transient String siddhiApp;
    private transient SiddhiAppRuntime siddhiAppRuntime;
    private transient SiddhiDebugger debugger;
    private transient LinkedBlockingQueue<DebugCallbackEvent> callbackEventsQueue;

    public DebugRuntime(String siddhiAppName, String siddhiApp) {
        this.siddhiAppName = siddhiAppName;
        this.siddhiApp = siddhiApp;
        callbackEventsQueue = new LinkedBlockingQueue<>(10);
        createRuntime();
    }

    public String getSiddhiAppName() {
        return siddhiAppName;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public SiddhiDebugger getDebugger() {
        return debugger;
    }

    public void start() {
        if (Mode.STOP.equals(mode)) {
            try {
                siddhiAppRuntime.start();
                mode = Mode.RUN;
            } catch (Throwable e) {
                mode = Mode.FAULTY;
                throw new InvalidExecutionStateException("Siddhi App " + siddhiAppName + " is in faulty state.", e);
            }
        } else {
            throw new InvalidExecutionStateException("Siddhi App " + siddhiAppName + " is in faulty state.");
        }
    }

    public void debug() {
        if (Mode.STOP.equals(mode)) {
            try {
                debugger = siddhiAppRuntime.debug();
                debugger.setDebuggerCallback((event, queryName, queryTerminal, debugger) -> {
                    String[] queries = getQueries().toArray(new String[getQueries().size()]);
                    int queryIndex = Arrays.asList(queries).indexOf(queryName);
                    callbackEventsQueue.add(new DebugCallbackEvent(queryName, queryIndex, queryTerminal, event));
                });
                mode = Mode.DEBUG;
            } catch (Throwable e) {
                mode = Mode.FAULTY;
                throw new InvalidExecutionStateException("Siddhi App " + siddhiAppName + " is in faulty state.", e);
            }
        } else {
            throw new InvalidExecutionStateException("Siddhi App" + siddhiAppName + " is in faulty state.");
        }
    }

    public void stop() {
        if (debugger != null) {
            debugger.releaseAllBreakPoints();
            debugger.play();
            debugger = null;
        }
        if (siddhiAppRuntime != null) {
            siddhiAppRuntime.shutdown();
            siddhiAppRuntime = null;
        }
        callbackEventsQueue.clear();
        createRuntime();
    }

    public void reload(String siddhiApp) {
        this.siddhiApp = siddhiApp;
        stop();
    }

    public List<String> getStreams() {
        if (!Mode.FAULTY.equals(mode)) {
            return new ArrayList<>(siddhiAppRuntime.getStreamDefinitionMap().keySet());
        } else {
            throw new InvalidExecutionStateException("Siddhi App" + siddhiAppName + " is in faulty state.");
        }
    }

    public List<String> getQueries() {
        if (!Mode.FAULTY.equals(mode)) {
            return new ArrayList<>(siddhiAppRuntime.getQueryNames());
        } else {
            throw new InvalidExecutionStateException("Siddhi App" + siddhiAppName + " is in faulty state.");
        }
    }

    public InputHandler getInputHandler(String streamName) {
        if (!Mode.FAULTY.equals(mode)) {
            return siddhiAppRuntime.getInputHandler(streamName);
        } else {
            throw new InvalidExecutionStateException("Siddhi App" + siddhiAppName + " is in faulty state.");
        }
    }

    public List<Attribute> getStreamAttributes(String streamName) {
        if (!Mode.FAULTY.equals(mode)) {
            if (siddhiAppRuntime.getStreamDefinitionMap().containsKey(streamName)) {
                return siddhiAppRuntime.getStreamDefinitionMap().get(streamName).getAttributeList();
            } else {
                throw new NoSuchStreamException(String.format(
                        "Stream definition %s does not exists in Siddhi app %s", streamName, siddhiAppName));
            }
        } else {
            throw new InvalidExecutionStateException("Siddhi App" + siddhiAppName + " is in faulty state.");
        }
    }

    public LinkedBlockingQueue<DebugCallbackEvent> getCallbackEventsQueue() {
        return callbackEventsQueue;
    }

    private void createRuntime() {
        try {
            if (siddhiApp != null && !siddhiApp.isEmpty()) {
                siddhiAppRuntime = EditorDataHolder.getSiddhiManager()
                        .createSiddhiAppRuntime(siddhiApp);
                mode = Mode.STOP;
            } else {
                mode = Mode.FAULTY;
            }
        } catch (Exception e) {
            mode = Mode.FAULTY;
        }
    }

    public SiddhiAppRuntime getSiddhiAppRuntime() {
        return siddhiAppRuntime;
    }

    public enum Mode {RUN, DEBUG, STOP, FAULTY}

}
