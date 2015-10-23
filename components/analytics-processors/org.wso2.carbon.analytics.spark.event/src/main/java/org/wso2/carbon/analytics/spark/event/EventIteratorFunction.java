/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.analytics.spark.event;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.StructType;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.commons.Event;
import scala.collection.Iterator;
import scala.runtime.AbstractFunction1;
import scala.runtime.BoxedUnit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Iterates over a resultset from the select query, builds events from them and
 * publishes to the event stream.
 */
public class EventIteratorFunction extends AbstractFunction1<Iterator<Row>, BoxedUnit>
        implements Serializable {

    private static final Log log = LogFactory.getLog(EventIteratorFunction.class);
    private static final long serialVersionUID = 4048303072566432397L;

    private int tenantId;
    private StructType schema;
    private String streamId;
    private String receiverURLSet;
    private String authURLSet;
    private String username;
    private String password;
    private DataPublisher dataPublisher;

    public EventIteratorFunction(int tenantId,String streamId, StructType schema,
                                 String receiverURLSet, String authURLSet, String username, String password) {
        this.tenantId = tenantId;
        this.streamId = streamId;
        this.schema = schema;
        this.receiverURLSet = receiverURLSet;
        this.authURLSet = authURLSet;
        this.username = username;
        this.password = password;
    }

    //note : this method is invoked by apply() method instead of constructor due to some kryo serialization issues
    private void initDataPublisherClient() {
        String key = receiverURLSet + "_" +  authURLSet + "_" + username  + "_" + password;
        dataPublisher = DataPublisherHolder.getInstance().getDataPublisher(key);
        if (dataPublisher == null) {
            try {
                dataPublisher = new DataPublisher(EventingConstants.THRIFT_AGENT_TYPE, receiverURLSet, authURLSet, username, password);
                DataPublisherHolder.getInstance().addDataPublisher(key, dataPublisher);
            } catch (Exception e) {
                log.warn("Failed to create data publisher for publishing events to streamId: " + this.streamId, e);
            }
        }
    }

    @Override
    public BoxedUnit apply(Iterator<Row> iterator) {
        initDataPublisherClient();
        while (iterator.hasNext()) {
            Row row = iterator.next();
            List<Object> result = new ArrayList<Object>();
            for (int i = 0; i < row.length(); i++) {
                result.add(row.get(i));
            }
            if (dataPublisher != null) {
                dataPublisher.publish(buildEvent(result));
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Publisher is not initialized properly. Couldn't publish events to streamId: " + this.streamId);
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("Published event to streamId: " + this.streamId);
            }
        }
        return BoxedUnit.UNIT;
    }

    private Event buildEvent(List<Object> payloadData) {
        Event event = new Event();
        event.setTimeStamp(System.currentTimeMillis());
        event.setStreamId(this.streamId);
        event.setPayloadData(payloadData.toArray());
        return event;
    }
}
