/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.event.input.adapter.filetail;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.input.adapter.core.EventAdapterConstants;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapter;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterConfiguration;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterListener;
import org.wso2.carbon.event.input.adapter.core.exception.InputEventAdapterException;
import org.wso2.carbon.event.input.adapter.core.exception.TestConnectionNotSupportedException;
import org.wso2.carbon.event.input.adapter.filetail.internal.listener.FileTailerListener;
import org.wso2.carbon.event.input.adapter.filetail.internal.util.FileTailEventAdapterConstants;
import org.wso2.carbon.event.input.adapter.filetail.internal.util.FileTailerManager;

import java.io.File;
import java.util.*;
import java.util.concurrent.*;

/**
 * Input FileTailEventAdapter will be used to receive events from specified event file.
 */
public class FileTailEventAdapter implements InputEventAdapter {

    private final InputEventAdapterConfiguration eventAdapterConfiguration;
    private final Map<String, String> globalProperties;
    private InputEventAdapterListener eventAdapterListener;
    private final String id = UUID.randomUUID().toString();
    private static final Log log = LogFactory.getLog(FileTailEventAdapter.class);
    private FileTailerManager fileTailerManager;
    private ExecutorService singleThreadedExecutor;

    public FileTailEventAdapter(InputEventAdapterConfiguration eventAdapterConfiguration,
                                Map<String, String> globalProperties) {
        this.eventAdapterConfiguration = eventAdapterConfiguration;
        this.globalProperties = globalProperties;
        this.singleThreadedExecutor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void init(InputEventAdapterListener eventAdapterListener) throws InputEventAdapterException {
        validateInputEventAdapterConfigurations();
        this.eventAdapterListener = eventAdapterListener;
    }

    @Override
    public void testConnect() throws TestConnectionNotSupportedException {
        throw new TestConnectionNotSupportedException("not-supported");
    }

    @Override
    public void connect() {
        createFileAdapterListener();
    }

    @Override
    public void disconnect() {

        if (fileTailerManager != null) {
            fileTailerManager.getTailer().stop();
        }
    }

    @Override
    public void destroy() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileTailEventAdapter)) return false;

        FileTailEventAdapter that = (FileTailEventAdapter) o;

        return id.equals(that.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    private void createFileAdapterListener() {
        if(log.isDebugEnabled()){
            log.debug("New subscriber added for " + eventAdapterConfiguration.getName());
        }

        String delayInMillisProperty = eventAdapterConfiguration.getProperties().get(FileTailEventAdapterConstants.EVENT_ADAPTER_DELAY_MILLIS);
        int delayInMillis = FileTailEventAdapterConstants.DEFAULT_DELAY_MILLIS;
        if (delayInMillisProperty != null && (!delayInMillisProperty.trim().isEmpty())) {
            delayInMillis = Integer.parseInt(delayInMillisProperty);
        }

        boolean startFromEnd = false;
        String startFromEndProperty = eventAdapterConfiguration.getProperties().get(FileTailEventAdapterConstants.EVENT_ADAPTER_START_FROM_END);
        if (startFromEndProperty != null && (!startFromEndProperty.trim().isEmpty())) {
            startFromEnd = Boolean.parseBoolean(startFromEndProperty);
        }

        String filePath = eventAdapterConfiguration.getProperties().get(
                FileTailEventAdapterConstants.EVENT_ADAPTER_CONF_FILEPATH);

        FileTailerListener listener = new FileTailerListener(new File(filePath).getName(), eventAdapterListener);
        Tailer tailer = new Tailer(new File(filePath), listener, delayInMillis, startFromEnd);
        fileTailerManager = new FileTailerManager(tailer, listener);
        singleThreadedExecutor.execute(tailer);
    }

    @Override
    public boolean isEventDuplicatedInCluster() {
        return Boolean.parseBoolean(globalProperties.get(EventAdapterConstants.EVENTS_DUPLICATED_IN_CLUSTER));
    }

    @Override
    public boolean isPolling() {
        return true;
    }

    private void validateInputEventAdapterConfigurations() throws InputEventAdapterException {
        String delayInMillisProperty = eventAdapterConfiguration.getProperties().get(FileTailEventAdapterConstants.EVENT_ADAPTER_DELAY_MILLIS);
        try{
            if(delayInMillisProperty != null){
                Integer.parseInt(delayInMillisProperty);
            }
        } catch (NumberFormatException e){
            throw new InputEventAdapterException("Invalid value set for property Delay: " + delayInMillisProperty, e);
        }
    }

}
