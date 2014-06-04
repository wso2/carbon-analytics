package org.wso2.carbon.bam.webapp.stat.publisher.publish;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.webapp.stat.publisher.conf.EventPublisherConfig;
import org.wso2.carbon.bam.webapp.stat.publisher.conf.InternalEventingConfigData;
import org.wso2.carbon.bam.webapp.stat.publisher.conf.Property;
import org.wso2.carbon.bam.webapp.stat.publisher.data.BAMServerInfo;
import org.wso2.carbon.bam.webapp.stat.publisher.data.WebappStatEvent;
import org.wso2.carbon.bam.webapp.stat.publisher.data.WebappStatEventData;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Keeps info on whether the webapp stat publishing is enabled or not and
 * the event publisher configurations which includes the data publisher, stream def etc.
 */
public class WebappAgentUtil {

    private static Log log = LogFactory.getLog(WebappAgentUtil.class);

    private static Map<String,EventPublisherConfig> eventPublisherConfigMap =
            new HashMap<String, EventPublisherConfig>();

    private static boolean isPublishingEnabled = false;

    private static boolean isGlobalPublishingEnabled = false;

    public static void setPublishingEnabled(boolean isPublishingEnabled) {
        WebappAgentUtil.isPublishingEnabled = isPublishingEnabled;
    }

    public static boolean getPublishingEnabled() {
        return isPublishingEnabled;
    }

    public static EventPublisherConfig getEventPublisherConfig(String key) {
        return eventPublisherConfigMap.get(key);
    }

    public static Map<String, EventPublisherConfig> getEventPublisherConfigMap() {
        return eventPublisherConfigMap;
    }

    public static void removeExistingEventPublisherConfigValue(String key) {
        if (eventPublisherConfigMap != null) {
            eventPublisherConfigMap.put(key, null);
        }
    }

    public static WebappStatEvent makeEventList(WebappStatEventData webappStatEventData,
                                      InternalEventingConfigData eventingConfigData) {

        List<Object> correlationData = new ArrayList<Object>();
        List<Object> metaData = new ArrayList<Object>();
        List<Object> eventData = new ArrayList<Object>();

        eventData = addCommonEventData(webappStatEventData, eventData);

        eventData = addStatisticEventData(webappStatEventData, eventData);
        metaData = addStatisticsMetaData(webappStatEventData, metaData);

        if(eventingConfigData != null) {
            metaData = addPropertiesAsMetaData(eventingConfigData, metaData);
        }

        WebappStatEvent publishEvent = new WebappStatEvent();
        publishEvent.setCorrelationData(correlationData);
        publishEvent.setMetaData(metaData);

        // change the event data to pay load data -----------------.
        publishEvent.setEventData(eventData);

        return publishEvent;
    }

    private static List<Object> addPropertiesAsMetaData(InternalEventingConfigData eventingConfigData,
                                                List<Object> metaData) {
        Property[] properties = eventingConfigData.getProperties();
        if (properties != null) {
            for (int i = 0; i < properties.length; i++) {
                Property property = properties[i];
                if (property.getKey() != null && !property.getKey().isEmpty()) {
                    metaData.add(property.getValue());
                }
            }
        }

        return metaData;
    }


    private static List<Object> addCommonEventData(WebappStatEventData event, List<Object> eventData) {

        eventData.add(event.getWebappName());
        eventData.add(event.getWebappVersion());
        eventData.add(event.getUserId());
        eventData.add(event.getResourcePath());
        eventData.add(event.getWebappType());
        eventData.add(event.getWebappDisplayName());
        eventData.add(event.getWebappContext());
        eventData.add(event.getSessionId());
        eventData.add(event.getHttpMethod());
        eventData.add(event.getContentType());
        eventData.add(event.getResponseContentType());
        eventData.add(event.getRemoteAddress());
        eventData.add(event.getReferer());
        eventData.add(event.getRemoteUser());
        eventData.add(event.getAuthType());
        eventData.add(event.getUserAgent());
        eventData.add(event.getBrowser());
        eventData.add(event.getBrowserVersion());
        eventData.add(event.getOperatingSystem());
        eventData.add(event.getOperatingSystemVersion());
        eventData.add(event.getSearchEngine());
        eventData.add(event.getCountry());
        eventData.add(event.getTimestamp());
        eventData.add(event.getResponseHttpStatusCode());
        eventData.add(event.getResponseTime());
        eventData.add(event.getRequestCount());
        eventData.add(event.getResponceCount());
        eventData.add(event.getFaultCount());
        eventData.add(event.getRequestSizeBytes());
        eventData.add(event.getResponseSizeBytes());


      return eventData;

    }

    private static List<Object> addStatisticEventData(WebappStatEventData event, List<Object> eventData) {
        return eventData;
    }

    private static List<Object> addStatisticsMetaData(WebappStatEventData event, List<Object> metaData) {
        metaData.add(event.getServerAddess());
        metaData.add(event.getServerName());
        metaData.add(event.getTenantId());
        metaData.add(event.getWebappOwnerTenant());
        metaData.add(event.getUserTenant());

        return metaData;
    }


    public static void extractInfoFromHttpHeaders(WebappStatEventData eventData, Object requestProperty) {

        if (requestProperty instanceof HttpServletRequest) {
        }

    }

    public static BAMServerInfo addBAMServerInfo(InternalEventingConfigData eventingConfigData) {
        BAMServerInfo bamServerInfo = new BAMServerInfo();
        bamServerInfo.setBamServerURL(eventingConfigData.getUrl());
        bamServerInfo.setBamUserName(eventingConfigData.getUserName());
        bamServerInfo.setBamPassword(eventingConfigData.getPassword());
        return bamServerInfo;
    }

    public static boolean isGlobalPublishingEnabled() {
        return isGlobalPublishingEnabled;
    }

    public static void setGlobalPublishingEnabled(boolean globalPublishingEnabled) {
        isGlobalPublishingEnabled = globalPublishingEnabled;
    }
}
