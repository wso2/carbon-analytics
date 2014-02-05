/*
 * Copyright 2004-2014 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.event.input.adaptor.file;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.agent.thrift.internal.utils.AgentConstants;
import org.wso2.carbon.event.input.adaptor.core.AbstractInputEventAdaptor;
import org.wso2.carbon.event.input.adaptor.core.InputEventAdaptorListener;
import org.wso2.carbon.event.input.adaptor.core.MessageType;
import org.wso2.carbon.event.input.adaptor.core.Property;
import org.wso2.carbon.event.input.adaptor.core.config.InputEventAdaptorConfiguration;
import org.wso2.carbon.event.input.adaptor.core.message.config.InputEventAdaptorMessageConfiguration;
import org.wso2.carbon.event.input.adaptor.file.internal.listener.FileTailerListener;
import org.wso2.carbon.event.input.adaptor.file.internal.util.FileEventAdaptorConstants;
import org.wso2.carbon.event.input.adaptor.file.internal.util.FileTailerManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public final class FileEventAdaptorType extends AbstractInputEventAdaptor {

    private static final Log log = LogFactory.getLog(FileEventAdaptorType.class);
    private ResourceBundle resourceBundle;
    private ConcurrentHashMap<String, ConcurrentHashMap<String, FileTailerManager>> tailerMap = new ConcurrentHashMap<String, ConcurrentHashMap<String, FileTailerManager>>();
    private ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(FileEventAdaptorConstants.MIN_THREAD, FileEventAdaptorConstants.MAX_THREAD, AgentConstants.DEFAULT_KEEP_ALIVE_TIME, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(1000));

    @Override
    protected String getName() {
        return FileEventAdaptorConstants.EVENT_ADAPTOR_TYPE_FILE;
    }

    @Override
    protected List<String> getSupportedInputMessageTypes() {
        List<String> supportInputMessageTypes = new ArrayList<String>();
        supportInputMessageTypes.add(MessageType.TEXT);
        return supportInputMessageTypes;
    }

    @Override
    protected void init() {
        this.resourceBundle = ResourceBundle.getBundle("org.wso2.carbon.event.input.adaptor.file.i18n.Resources", Locale.getDefault());
    }

    @Override
    protected List<Property> getInputAdaptorProperties() {
        return null;
    }

    @Override
    protected List<Property> getInputMessageProperties() {

        List<Property> propertyList = new ArrayList<Property>();
        Property filePath = new Property(FileEventAdaptorConstants.EVENT_ADAPTOR_CONF_FILEPATH);
        filePath.setDisplayName(
                resourceBundle.getString(FileEventAdaptorConstants.EVENT_ADAPTOR_CONF_FILEPATH));
        filePath.setRequired(true);
        filePath.setHint(resourceBundle.getString(FileEventAdaptorConstants.EVENT_ADAPTOR_CONF_FILEPATH_HINT));
        propertyList.add(filePath);

        return propertyList;

    }

    @Override
    public String subscribe(
            InputEventAdaptorMessageConfiguration inputEventAdaptorMessageConfiguration,
            InputEventAdaptorListener inputEventAdaptorListener,
            InputEventAdaptorConfiguration inputEventAdaptorConfiguration,
            AxisConfiguration axisConfiguration) {

        String subscriptionId = UUID.randomUUID().toString();

        log.info("New subscriber added for " + inputEventAdaptorConfiguration.getName());

        ConcurrentHashMap<String, FileTailerManager> tailerManagerConcurrentHashMap = tailerMap.get(inputEventAdaptorConfiguration.getName());
        if (tailerManagerConcurrentHashMap == null) {
            tailerManagerConcurrentHashMap = new ConcurrentHashMap<String, FileTailerManager>();
            if (null != tailerMap.putIfAbsent(inputEventAdaptorConfiguration.getName(), tailerManagerConcurrentHashMap)) {
                tailerManagerConcurrentHashMap = tailerMap.get(inputEventAdaptorConfiguration.getName());
            }
        }

        String filepath = inputEventAdaptorMessageConfiguration.getInputMessageProperties().get(FileEventAdaptorConstants.EVENT_ADAPTOR_CONF_FILEPATH);
        FileTailerManager fileTailerManager = tailerManagerConcurrentHashMap.get(filepath);

        if (fileTailerManager == null) {
            FileTailerListener listener = new FileTailerListener(new File(filepath).getName());
            Tailer tailer = new Tailer(new File(filepath), listener);
            fileTailerManager = new FileTailerManager(tailer, listener);
            listener.addListener(subscriptionId, inputEventAdaptorListener);
            tailerManagerConcurrentHashMap.put(filepath, fileTailerManager);
            threadPoolExecutor.submit(tailer);
        } else {
            fileTailerManager.getListener().addListener(subscriptionId, inputEventAdaptorListener);
        }
        return subscriptionId;
    }

    @Override
    public void unsubscribe(
            InputEventAdaptorMessageConfiguration inputEventAdaptorMessageConfiguration,
            InputEventAdaptorConfiguration inputEventAdaptorConfiguration,
            AxisConfiguration axisConfiguration, String subscriptionId) {
        ConcurrentHashMap<String, FileTailerManager> tailerManagerConcurrentHashMap = tailerMap.get(inputEventAdaptorConfiguration.getName());
        FileTailerManager fileTailerManager = null;
        if (tailerManagerConcurrentHashMap != null) {
            fileTailerManager = tailerManagerConcurrentHashMap.get(inputEventAdaptorMessageConfiguration.getInputMessageProperties().get(FileEventAdaptorConstants.EVENT_ADAPTOR_CONF_FILEPATH));
        }

        if (fileTailerManager != null) {
            fileTailerManager.getListener().removeListener(subscriptionId);
            if (fileTailerManager.getListener().hasNoSubscriber()) {
                fileTailerManager.getTailer().stop();
                tailerMap.remove(inputEventAdaptorConfiguration.getName());
            }
        }
    }


}

