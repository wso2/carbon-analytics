/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.siddhi.editor.core.util.errorhandler;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import feign.Response;
import feign.RetryableException;
import org.apache.commons.io.IOUtils;
import org.wso2.carbon.siddhi.editor.core.exception.ErrorHandlerServiceStubException;
import org.wso2.carbon.siddhi.editor.core.util.errorhandler.api.ErrorHandlerApiHelperService;
import org.wso2.carbon.siddhi.editor.core.util.errorhandler.util.HTTPSClientUtil;

import java.io.IOException;
import java.io.InputStream;

/**
 * Consists of methods that communicate with the Error Handler API, exposed from the SI server.
 */
public class ErrorHandlerApiHelper implements ErrorHandlerApiHelperService {

    private static final String CANNOT_CONNECT_TO_SERVER_ERROR_FORMAT = "Cannot connect to the server node %s.";
    private static final String FAILED_TO_READ_THE_RESPONSE_ERROR = "Failed to read the response.";

    private String getAsString(InputStream inputStream) throws IOException {
        return IOUtils.toString(inputStream);
    }

    @Override
    public JsonArray getSiddhiAppList(String hostAndPort, String username, String password)
        throws ErrorHandlerServiceStubException {
        try (Response response = HTTPSClientUtil.doGetSiddhiAppList(hostAndPort, username, password)) {
            if (response.status() == 200 && response.body() != null) {
                String responseBody = getAsString(response.body().asInputStream());
                return new JsonParser().parse(responseBody).getAsJsonArray();
            }
            throw new ErrorHandlerServiceStubException("Failed to get siddhi app list. " + response.reason());
        } catch (RetryableException e) {
            throw new ErrorHandlerServiceStubException(
                String.format(CANNOT_CONNECT_TO_SERVER_ERROR_FORMAT, hostAndPort), e);
        } catch (IOException e) {
            throw new ErrorHandlerServiceStubException(FAILED_TO_READ_THE_RESPONSE_ERROR, e);
        }
    }

    @Override
    public JsonObject getTotalErrorEntriesCount(String hostAndPort, String username, String password)
        throws ErrorHandlerServiceStubException {
        try (Response response = HTTPSClientUtil.doGetTotalErrorEntriesCount(hostAndPort, username, password)) {
            if (response.status() == 200 && response.body() != null) {
                String responseBody = getAsString(response.body().asInputStream());
                return new JsonParser().parse(responseBody).getAsJsonObject();
            }
            throw new ErrorHandlerServiceStubException("Failed to get error entries count. " + response.reason());
        } catch (RetryableException e) {
            throw new ErrorHandlerServiceStubException(
                String.format(CANNOT_CONNECT_TO_SERVER_ERROR_FORMAT, hostAndPort), e);
        } catch (IOException e) {
            throw new ErrorHandlerServiceStubException(FAILED_TO_READ_THE_RESPONSE_ERROR, e);
        }
    }

    @Override
    public JsonObject getErrorEntriesCount(String siddhiAppName, String hostAndPort, String username, String password)
        throws ErrorHandlerServiceStubException {
        try (Response response =
                 HTTPSClientUtil.doGetErrorEntriesCount(siddhiAppName, hostAndPort, username, password)) {
            if (response.status() == 200 && response.body() != null) {
                String responseBody = getAsString(response.body().asInputStream());
                return new JsonParser().parse(responseBody).getAsJsonObject();
            }
            throw new ErrorHandlerServiceStubException(
                String.format("Failed to get error entries count for Siddhi app: %s. %s",
                    siddhiAppName, response.reason()));
        } catch (RetryableException e) {
            throw new ErrorHandlerServiceStubException(
                String.format(CANNOT_CONNECT_TO_SERVER_ERROR_FORMAT, hostAndPort), e);
        } catch (IOException e) {
            throw new ErrorHandlerServiceStubException(FAILED_TO_READ_THE_RESPONSE_ERROR, e);
        }
    }

    @Override
    public JsonArray getMinimalErrorEntries(String siddhiAppName, String limit, String offset, String hostAndPort,
                                            String username, String password) throws ErrorHandlerServiceStubException {
        try (Response response = HTTPSClientUtil.doGetMinimalErrorEntries(siddhiAppName, limit, offset, hostAndPort,
            username, password)) {
            if (response.status() == 200 && response.body() != null) {
                String responseBody = getAsString(response.body().asInputStream());
                return new JsonParser().parse(responseBody).getAsJsonArray();
            }
            throw new ErrorHandlerServiceStubException(
                String.format("Failed to get minimal error entries for Siddhi app: %s. %s",
                    siddhiAppName, response.reason()));
        } catch (RetryableException e) {
            throw new ErrorHandlerServiceStubException(
                String.format(CANNOT_CONNECT_TO_SERVER_ERROR_FORMAT, hostAndPort), e);
        } catch (IOException e) {
            throw new ErrorHandlerServiceStubException(FAILED_TO_READ_THE_RESPONSE_ERROR, e);
        }
    }

    @Override
    public JsonObject getDescriptiveErrorEntry(String id, String hostAndPort, String username, String password)
        throws ErrorHandlerServiceStubException {
        try (Response response = HTTPSClientUtil.doGetDescriptiveErrorEntry(id, hostAndPort, username, password)) {
            if (response.status() == 200 && response.body() != null) {
                String responseBody = getAsString(response.body().asInputStream());
                return new JsonParser().parse(responseBody).getAsJsonObject();
            }
            throw new ErrorHandlerServiceStubException(
                String.format("Failed to get descriptive error entry with id: %s. %s", id, response.reason()));
        } catch (RetryableException e) {
            throw new ErrorHandlerServiceStubException(
                String.format(CANNOT_CONNECT_TO_SERVER_ERROR_FORMAT, hostAndPort), e);
        } catch (IOException e) {
            throw new ErrorHandlerServiceStubException(FAILED_TO_READ_THE_RESPONSE_ERROR, e);
        }
    }

    @Override
    public boolean replay(JsonArray payload, String hostAndPort, String username, String password)
        throws ErrorHandlerServiceStubException {
        try (Response response = HTTPSClientUtil.doReplay(payload.toString(), hostAndPort, username, password)) {
            if (response.status() == 200) {
                return true;
            }
            throw new ErrorHandlerServiceStubException("There were failures during the replay." + response.reason());
        } catch (RetryableException e) {
            throw new ErrorHandlerServiceStubException(
                String.format(CANNOT_CONNECT_TO_SERVER_ERROR_FORMAT, hostAndPort), e);
        }
    }

    @Override
    public boolean discardErrorEntry(String id, String hostAndPort, String username, String password)
        throws ErrorHandlerServiceStubException {
        try (Response response = HTTPSClientUtil.doDiscardErrorEntry(id, hostAndPort, username, password)) {
            if (response.status() == 200) {
                return true;
            }
            throw new ErrorHandlerServiceStubException(
                String.format("Failed to delete the error entry with id: %s. %s", id, response.reason()));
        } catch (RetryableException e) {
            throw new ErrorHandlerServiceStubException(
                String.format(CANNOT_CONNECT_TO_SERVER_ERROR_FORMAT, hostAndPort), e);
        }
    }

    @Override
    public boolean discardErrorEntries(String siddhiAppName, String hostAndPort, String username, String password)
        throws ErrorHandlerServiceStubException {
        try (Response response =
                 HTTPSClientUtil.doDiscardErrorEntries(siddhiAppName, hostAndPort, username, password)) {
            if (response.status() == 200) {
                return true;
            }
            throw new ErrorHandlerServiceStubException(
                String.format("Failed to discard error entries of Siddhi app: %s. %s", siddhiAppName,
                    response.reason()));
        } catch (RetryableException e) {
            throw new ErrorHandlerServiceStubException(
                String.format(CANNOT_CONNECT_TO_SERVER_ERROR_FORMAT, hostAndPort), e);
        }
    }

    @Override
    public boolean doPurge(int retentionDays, String hostAndPort, String username, String password)
        throws ErrorHandlerServiceStubException {
        try (Response response = HTTPSClientUtil.doPurge(retentionDays, hostAndPort, username, password)) {
            if (response.status() == 200) {
                return true;
            }
            throw new ErrorHandlerServiceStubException("Failed to purge the error store." + response.reason());
        } catch (RetryableException e) {
            throw new ErrorHandlerServiceStubException(
                String.format(CANNOT_CONNECT_TO_SERVER_ERROR_FORMAT, hostAndPort), e);
        }
    }
}
