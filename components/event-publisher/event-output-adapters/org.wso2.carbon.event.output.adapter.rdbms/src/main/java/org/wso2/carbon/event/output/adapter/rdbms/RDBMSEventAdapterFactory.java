/*
 *
 *   Copyright (c) 2014-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */

package org.wso2.carbon.event.output.adapter.rdbms;

import org.wso2.carbon.event.output.adapter.core.MessageType;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapter;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterFactory;
import org.wso2.carbon.event.output.adapter.core.Property;
import org.wso2.carbon.event.output.adapter.rdbms.internal.util.RDBMSEventAdapterConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * The rdbms event adapter factory class to create a rdbms output adapter
 */
public class RDBMSEventAdapterFactory extends OutputEventAdapterFactory {

    private ResourceBundle resourceBundle = ResourceBundle.getBundle("org.wso2.carbon.event.output.adapter.rdbms.i18n" +
            ".Resources", Locale.getDefault());

    @Override
    public String getType() {
        return RDBMSEventAdapterConstants.ADAPTER_TYPE_GENERIC_RDBMS;
    }

    @Override
    public List<String> getSupportedMessageFormats() {
        List<String> supportOutputMessageTypes = new ArrayList<String>();
        supportOutputMessageTypes.add(MessageType.MAP);
        return supportOutputMessageTypes;
    }

    @Override
    public List<Property> getStaticPropertyList() {
        List<Property> staticPropertyList = new ArrayList<Property>();

        Property datasourceName = new Property(RDBMSEventAdapterConstants.ADAPTER_GENERIC_RDBMS_DATASOURCE_NAME);
        datasourceName.setDisplayName(
                resourceBundle.getString(RDBMSEventAdapterConstants.ADAPTER_GENERIC_RDBMS_DATASOURCE_NAME));
        datasourceName.setRequired(true);
        staticPropertyList.add(datasourceName);

        Property tableName = new Property(RDBMSEventAdapterConstants.ADAPTER_GENERIC_RDBMS_TABLE_NAME);
        tableName.setDisplayName(resourceBundle.getString(RDBMSEventAdapterConstants.ADAPTER_GENERIC_RDBMS_TABLE_NAME));
        tableName.setRequired(true);
        staticPropertyList.add(tableName);

        Property executionMode = new Property(RDBMSEventAdapterConstants.ADAPTER_GENERIC_RDBMS_EXECUTION_MODE);
        executionMode.setDisplayName(
                resourceBundle.getString(RDBMSEventAdapterConstants.ADAPTER_GENERIC_RDBMS_EXECUTION_MODE));
        executionMode.setOptions(new String[]{
                resourceBundle.getString(RDBMSEventAdapterConstants.ADAPTER_GENERIC_RDBMS_EXECUTION_MODE_INSERT),
                resourceBundle.getString(RDBMSEventAdapterConstants.ADAPTER_GENERIC_RDBMS_EXECUTION_MODE_UPDATE)});
        executionMode.setHint(
                resourceBundle.getString(RDBMSEventAdapterConstants.ADAPTER_GENERIC_RDBMS_EXECUTION_MODE_HINT));
        executionMode.setRequired(true);
        staticPropertyList.add(executionMode);

        Property updateColumnKeys = new Property(RDBMSEventAdapterConstants.ADAPTER_GENERIC_RDBMS_UPDATE_KEYS);
        updateColumnKeys
                .setDisplayName(resourceBundle.getString(RDBMSEventAdapterConstants.ADAPTER_GENERIC_RDBMS_UPDATE_KEYS));
        updateColumnKeys
                .setHint(resourceBundle.getString(RDBMSEventAdapterConstants.ADAPTER_GENERIC_RDBMS_UPDATE_KEYS_HINT));
        staticPropertyList.add(updateColumnKeys);

        return staticPropertyList;
    }

    @Override
    public List<Property> getDynamicPropertyList() {
        return null;
    }

    @Override
    public String getUsageTips() {
        return null;
    }

    @Override
    public OutputEventAdapter createEventAdapter(OutputEventAdapterConfiguration eventAdapterConfiguration,
                                                 Map<String, String> globalProperties) {
        return new RDBMSEventAdapter(eventAdapterConfiguration, globalProperties);
    }
}
