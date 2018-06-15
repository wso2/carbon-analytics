/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.business.rules.core.deployer.util;

import feign.Response;
import org.wso2.carbon.business.rules.core.exceptions.SiddhiAppDeployerServiceStubException;

/**
 * Contains HTTPS client related methods
 */
public class HTTPSClientUtil {
    private static final String PROTOCOL = "https://";

    /**
     * Avoids Instantiation
     */
    private HTTPSClientUtil() {
    }

    /**
     * Generates an HTTPS URL with the given hostAndPort
     * @param hostAndPort       Host and Port of the Worker node in {Host}:{Port} format
     * @return                  HTTPS URL
     */
    private static String generateURL(String hostAndPort) {
        return PROTOCOL + hostAndPort;
    }

    /**
     * Produces a Response after doing a POST request
     * @param hostAndPort                                   Host and Port of the Worker node in {Host}:{Port} format
     * @param username                                      Username
     * @param password                                      Password
     * @param payload                                       Payload
     * @return                                              Feign Response object
     * @throws SiddhiAppDeployerServiceStubException        Error occurred within SiddhiAppDeployerServiceStub
     */
    public static Response doPostRequest(String hostAndPort, String username, String password, String payload)
            throws SiddhiAppDeployerServiceStubException {
        return SiddhiAppDeployerFactory
                .getSiddhiAppDeployerHttpsClient(generateURL(hostAndPort), username, password)
                .doPostRequest(payload);
    }

    /**
     * Produces a Response after doing a GET request
     * @param hostAndPort                                   Host and Port of the Worker node in {Host}:{Port} format
     * @param username                                      Username
     * @param password                                      Password
     * @return                                              Feign Response object
     * @throws SiddhiAppDeployerServiceStubException        Error occurred within SiddhiAppDeployerServiceStub
     */
    /**
     * Produces a Response after doing a GET request
     * @param hostAndPort                                   Host and Port of the Worker node in {Host}:{Port} format
     * @param username                                      Username
     * @param password                                      Password
     * @param siddhiAppName                                 Name of the Siddhi app
     * @return                                              Feign Response object
     * @throws SiddhiAppDeployerServiceStubException        Error occurred within SiddhiAppDeployerServiceStub
     */
    public static Response doGetRequest(String hostAndPort, String username, String password, String siddhiAppName)
            throws SiddhiAppDeployerServiceStubException {
        return SiddhiAppDeployerFactory
                .getSiddhiAppDeployerHttpsClient(generateURL(hostAndPort), username, password)
                .doGetRequest(siddhiAppName);
    }

    /**
     * Produces a Response after doing a PUT request
     * @param hostAndPort                                   Host and Port of the Worker node in {Host}:{Port} format
     * @param username                                      Username
     * @param password                                      Password
     * @param payload                                       Payload
     * @return                                              Feign Response object
     * @throws SiddhiAppDeployerServiceStubException        Error occurred within SiddhiAppDeployerServiceStub
     */
    public static Response doPutRequest(String hostAndPort, String username, String password, String payload)
            throws SiddhiAppDeployerServiceStubException {
        return SiddhiAppDeployerFactory
                .getSiddhiAppDeployerHttpsClient(generateURL(hostAndPort), username, password)
                .doPutRequest(payload);
    }

    /**
     * Produces a Response after doing a DELETE request
     * @param hostAndPort                                   Host and Port of the Worker node in {Host}:{Port} format
     * @param username                                      Username
     * @param password                                      Password
     * @param siddhiAppName                                 Name of the Siddhi App
     * @return                                              Feign Response object
     * @throws SiddhiAppDeployerServiceStubException        Error occurred within SiddhiAppDeployerServiceStub
     */
    public static Response doDeleteRequest(String hostAndPort, String username, String password, String siddhiAppName)
            throws SiddhiAppDeployerServiceStubException {
        return SiddhiAppDeployerFactory
                .getSiddhiAppDeployerHttpsClient(generateURL(hostAndPort), username, password)
                .doDeleteRequest(siddhiAppName);
    }
}
