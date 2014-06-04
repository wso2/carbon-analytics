/*
* Copyright 2004,2013 The Apache Software Foundation.
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
package org.wso2.carbon.bam.webapp.stat.publisher.conf;

import org.wso2.carbon.bam.webapp.stat.publisher.util.WebappStatisticsPublisherConstants;
import org.wso2.carbon.databridge.commons.StreamDefinition;

/*
* This class handles the BAM configurations data
*/

public class InternalEventingConfigData {

    private boolean isPublishingEnabled;
    private boolean isWebappStatsEnabled;
    private boolean isMsgDumpingEnabled;
    private String url;
    private String userName;
    private String password;

    private String streamName = WebappStatisticsPublisherConstants.STREAM_NAME;
    private String version = WebappStatisticsPublisherConstants.VERSION;
    private String nickName = WebappStatisticsPublisherConstants.NICK_NAME;
    private String description = WebappStatisticsPublisherConstants.DISCRIPTION;

    private StreamDefinition streamDefinition;

    private boolean isLoadBalancingConfig = false;

    private Property[] properties;

    public boolean isPublishingEnabled() {
        return isPublishingEnabled;
    }

    public void setPublishingEnabled(boolean publishingEnabled) {
        isPublishingEnabled = publishingEnabled;
    }

    public boolean isWebappStatsEnabled() {
        return isWebappStatsEnabled;
    }

    public void setWebappStatsEnabled(boolean webappStatsEnabled) {
        isWebappStatsEnabled = webappStatsEnabled;
    }

    public boolean isMsgDumpingEnabled() {
        return isMsgDumpingEnabled;
    }

    public void setMsgDumpingEnabled(boolean msgDumpingEnabled) {
        isMsgDumpingEnabled = msgDumpingEnabled;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
        setLoadBalancingConfig();
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getStreamName() {
        return streamName;
    }

    public void setStreamName(String streamName) {
        this.streamName = streamName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public StreamDefinition getStreamDefinition() {
        return streamDefinition;
    }

    public void setStreamDefinition(StreamDefinition streamDefinition) {
        this.streamDefinition = streamDefinition;
    }

    public boolean isLoadBalancingConfig() {
        return isLoadBalancingConfig;
    }

    private void setLoadBalancingConfig() {
        this.isLoadBalancingConfig = this.url.split(",").length > 1;
    }

    public Property[] getProperties() {
        return properties;
    }

    public void setProperties(Property[] properties) {
        this.properties = properties;
    }
}
