package org.wso2.carbon.databridge.receiver.restapi;

import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.AuthenticationException;
import org.wso2.carbon.databridge.commons.exception.SessionTimeoutException;
import org.wso2.carbon.databridge.commons.exception.UndefinedEventTypeException;
import org.wso2.carbon.databridge.commons.utils.EventConverterUtils;
import org.wso2.carbon.databridge.core.DataBridgeReceiverService;
import org.wso2.carbon.databridge.core.EventConverter;
import org.wso2.carbon.databridge.core.StreamTypeHolder;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionNotFoundException;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionStoreException;
import org.wso2.carbon.databridge.receiver.restapi.utils.RESTUtils;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class StreamService {
    @POST
    @Path("/{stream}/{version}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response publishEvent(
            @PathParam("stream") String streamName,
            @PathParam("version") String version, String requestBody,
            @Context HttpServletRequest request) {

        try {
            DataBridgeReceiverService dataBridgeReceiverService =
                    (DataBridgeReceiverService) PrivilegedCarbonContext.getThreadLocalCarbonContext()
                            .getOSGiService(DataBridgeReceiverService.class);

            final String streamId =
                    dataBridgeReceiverService.findStreamId(RESTUtils.getSessionId(request), streamName
                            , version);
            final StreamDefinition streamDefinition = RESTUtils.getStreamDefinition(
                    RESTUtils.getSessionId(request), RESTUtils.extractAuthHeaders(request), streamName, version);
            if(streamId==null){
                throw new WebApplicationException(new RuntimeException("No stream definitions exist for "+streamName+" "+version+", to process "+requestBody));
            }
            dataBridgeReceiverService.publish(requestBody, RESTUtils.getSessionId(request),
                    new EventConverter() {
                        @Override
                        public List<Event> toEventList(Object jsonEvents,
                                                       StreamTypeHolder streamTypeHolder) {
                            return EventConverterUtils.convertFromJson((String) jsonEvents, streamId, streamDefinition);
                        }
                    });
            return Response.status(Response.Status.ACCEPTED).build();
        } catch (UndefinedEventTypeException e) {
            throw new WebApplicationException(e);
        } catch (SessionTimeoutException e) {
            throw new WebApplicationException(e);
        } catch (AuthenticationException e) {
            throw new WebApplicationException(e);
        } catch (StreamDefinitionStoreException e) {
            throw new WebApplicationException(e);
        } catch (StreamDefinitionNotFoundException e) {
            throw new WebApplicationException(e);
        }


    }
}
