/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.data.provider.websocket.endpoint;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.data.provider.endpoint.DataProviderEndPoint;
import org.wso2.carbon.data.provider.websocket.bean.WebSocketChannel;
import org.wso2.msf4j.websocket.WebSocketEndpoint;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component(
        service = WebSocketEndpoint.class,
        immediate = true
)
@ServerEndpoint(value = "/websocket-provider/{topic}")
public class WebSocketProviderEndPoint implements WebSocketEndpoint {
    private static final Logger log = LoggerFactory.getLogger(WebSocketProviderEndPoint.class);
    private static final Map<String, ArrayList<WebSocketChannel>> providerMap = new HashMap<>();
    private static final String JSON_REGEX_PATTERN = "\\{\"event\":\\{(.*?)}}";
    private static final String XML_PATTERN_PATH = "//events/event/*";

    @OnOpen
    public static void onOpen(Session session, @PathParam("topic") String topic) {
        // Not required.
    }

    @OnMessage
    public void onMessage(String message, @PathParam("topic") String topic) {
        if (providerMap.containsKey(topic)) {
            providerMap.get(topic).forEach(channel -> {
                try {
                    DataProviderEndPoint
                            .sendText(channel.getSessionId(),
                                    formatString(message, channel.getMapType(), channel.getTopic()).toString());
                } catch (IOException e) {
                    log.info("Failed to send the message : " + e.getMessage(), e);
                }
            });
        }
    }

    @OnClose
    public void onClose(Session session) {
        // Not applicable as disconnecting from the web-socket provider doesn't affect the subscribed clients of the
        // data provider
    }

    @OnError
    public void onError(Throwable throwable) {
        log.error("Error found in method : " + throwable.getMessage(), throwable);
    }

    public static void subscribeToTopic(String topic, WebSocketChannel channel) {
        if (providerMap.containsKey(topic)) {
            providerMap.get(topic).add(channel);
        } else {
            providerMap.put(topic, new ArrayList<>());
            providerMap.get(topic).add(channel);
        }
    }

    public static void unsubscribeFromTopic(String topic, String sessionID) {
        providerMap.get(topic).removeIf(channel -> channel.getSessionId().equals(sessionID));
    }

    private JsonElement formatString(String message, String mapping, String topic) {
        JsonElement element = new Gson().fromJson("{}", JsonElement.class);
        Pattern pattern = null;
        Matcher matcher = null;
        switch (mapping.toLowerCase()) {
            case "text":
                element = new Gson().fromJson(getJsonString(message, topic), JsonElement.class);
                break;
            case "json":
                pattern = Pattern.compile(JSON_REGEX_PATTERN);
                matcher = pattern.matcher(message);
                if (matcher.find()) {
                    element = new Gson().fromJson(getJsonString(matcher.group(1), topic), JsonElement.class);
                }
                break;
            case "xml":
                try {
                    DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                    Document document = documentBuilder.parse(new InputSource(new StringReader(message)));
                    XPath xPath = XPathFactory.newInstance().newXPath();
                    XPathExpression expression = xPath.compile(XML_PATTERN_PATH);
                    NodeList nodeList = (NodeList) expression.evaluate(document, XPathConstants.NODESET);
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("{data:[[\"");
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        Element el = (Element) nodeList.item(i);
                        // seach for the Text children
                        if (el.getFirstChild().getNodeType() == Node.TEXT_NODE)
                            stringBuilder.append(el.getFirstChild().getNodeValue()).append("\",\"");
                    }
                    stringBuilder.setLength(stringBuilder.length() - 2);
                    stringBuilder.append("]],topic:").append(topic).append("}");
                    element = new Gson().fromJson(stringBuilder.toString(), JsonElement.class);
                } catch (XPathExpressionException | ParserConfigurationException | SAXException | IOException e) {
                    log.error("Error occurd when parsing xml." + e.getMessage(), e);
                }
                break;
            default:
                log.error("Invalid mapping provided for the data provider configuration.");
        }
        return element;
    }

    private String getJsonString(String value, String topic) {
        StringBuilder builder = new StringBuilder();
        builder.append("{data:[[");
        Arrays.stream(value.trim().split(","))
                .forEach(keyValuePair -> builder.append(keyValuePair.split(":")[1]).append(","));
        builder.deleteCharAt(builder.length() - 1).append("]],topic:").append(topic).append("}");
        return builder.toString();
    }
}
