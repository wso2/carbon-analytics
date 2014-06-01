package org.wso2.carbon.databridge.receiver.restapi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.AuthenticationException;
import org.wso2.carbon.databridge.commons.exception.DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.SessionTimeoutException;
import org.wso2.carbon.databridge.commons.utils.EventDefinitionConverterUtils;
import org.wso2.carbon.databridge.core.DataBridgeReceiverService;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionStoreException;
import org.wso2.carbon.databridge.receiver.restapi.utils.RESTUtils;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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

@Path("/")
public class StreamsService {

    private static Log log = LogFactory.getLog(StreamsService.class);

    /*
    JSON string expected:
    [
      {
       "event" : [val1, val2 ....] ,
       "meta" : [val1, val2 ....] ,
       "correlation" : [val1, val2 ....]
      }
     ,
      {
       "event" : [val1, val2 ....] ,
       "meta" : [val1, val2 ....] ,
       "correlation" : [val1, val2 ....]
      }
     , ....
    ]
     */


    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response saveStreamDefn(String requestBody, @Context HttpServletRequest request) {
        try {
            DataBridgeReceiverService dataBridgeReceiverService =
                    (DataBridgeReceiverService) PrivilegedCarbonContext.getThreadLocalCarbonContext()
                            .getOSGiService(DataBridgeReceiverService.class);
            StreamDefinition streamDefinition = EventDefinitionConverterUtils.convertFromJson(requestBody);
            dataBridgeReceiverService.saveStreamDefinition(RESTUtils.getSessionId(request),
                    streamDefinition);
            return Response.status(Response.Status.ACCEPTED).build();

        } catch (MalformedStreamDefinitionException e) {
            throw new WebApplicationException(e);
        } catch (DifferentStreamDefinitionAlreadyDefinedException e) {
            throw new WebApplicationException(e);
        } catch (StreamDefinitionStoreException e) {
            throw new WebApplicationException(e);
        } catch (SessionTimeoutException e) {
            throw new WebApplicationException(e);
        } catch (AuthenticationException e) {
            throw new WebApplicationException(e);
        }

    }

    @DELETE
    @Path("/{stream}/{version}")
    public Response publishEvent(
            @PathParam("stream") String streamName,
            @PathParam("version") String version,
            @Context HttpServletRequest request) {

        try {
            DataBridgeReceiverService dataBridgeReceiverService =
                    (DataBridgeReceiverService) PrivilegedCarbonContext.getThreadLocalCarbonContext()
                            .getOSGiService(DataBridgeReceiverService.class);

            dataBridgeReceiverService.deleteStream(RESTUtils.getSessionId(request), streamName,version);
            RESTUtils.deleteStreamDefinition(RESTUtils.extractAuthHeaders(request), streamName, version);
            return Response.status(Response.Status.ACCEPTED).build();

        } catch (SessionTimeoutException e) {
            throw new WebApplicationException(e);
        } catch (AuthenticationException e) {
            throw new WebApplicationException(e);
        }


    }

}
