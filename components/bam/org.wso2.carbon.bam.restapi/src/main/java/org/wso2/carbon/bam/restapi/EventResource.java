package org.wso2.carbon.bam.restapi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

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
public class EventResource {

    private static Log log = LogFactory.getLog(EventResource.class);

    @POST
    @Path("/{eventStreamName}/{version}")
    public void publishEvent(
            @PathParam("eventStreamName") String eventStreamName,
            @PathParam("version") String version) {
        log.info("Event Stream Name : " + eventStreamName + " Version : " + version + " Received!");

    }

    @GET
    @Path("/")
    @Produces(MediaType.TEXT_PLAIN)
    public String getSampleText() {
        return "My Sample Text";
    }


}
