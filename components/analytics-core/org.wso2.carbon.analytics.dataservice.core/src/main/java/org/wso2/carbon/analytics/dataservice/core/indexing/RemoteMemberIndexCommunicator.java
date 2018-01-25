/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.analytics.dataservice.core.indexing;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsServiceHolder;
import org.wso2.carbon.analytics.dataservice.core.Constants;
import org.wso2.carbon.analytics.dataservice.core.clustering.AnalyticsClusterManager;
import org.wso2.carbon.analytics.dataservice.core.indexing.IndexNodeCoordinator.IndexDataDeleteCall;
import org.wso2.carbon.analytics.dataservice.core.indexing.IndexNodeCoordinator.IndexDataPutCall;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class represents the remote member index message sending functionality in a cluster.
 */
public class RemoteMemberIndexCommunicator {
    
    private static Log log = LogFactory.getLog(RemoteMemberIndexCommunicator.class);
    
    private int remoteIndexerCommunicatorBufferSize;

    private ExecutorService executor;
    
    private Map<Object, Disruptor<RecordsHolder>> disruptorMap = new HashMap<>();

    private StagingIndexDataStore stagingIndexDataStore;

    public RemoteMemberIndexCommunicator(int remoteIndexerCommunicatorBufferSize,
                                         StagingIndexDataStore stagingIndexDataStore) {
        this.remoteIndexerCommunicatorBufferSize = remoteIndexerCommunicatorBufferSize;
        this.stagingIndexDataStore = stagingIndexDataStore;
        this.executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder().
                setNameFormat("Thread pool- component - RemoteMemberIndexCommunicator.executor").build());
    }

    public void put(Object member, List<Record> records, String nodeId) throws AnalyticsException {
        RingBuffer<RecordsHolder> buffer = this.getRingBuffer(member, nodeId);
        long sequence = buffer.next();
        try {
            RecordsHolder event = buffer.get(sequence);
            event.setRecords(records);
        } finally {
            buffer.publish(sequence);
        }
    }
    
    public void delete(Object member, int tenantId, String tableName, List<String> ids) throws AnalyticsException {
        AnalyticsClusterManager acm = AnalyticsServiceHolder.getAnalyticsClusterManager();
        acm.executeOne(Constants.ANALYTICS_INDEXING_GROUP, member, new IndexDataDeleteCall(tenantId, tableName, ids));
    }
    
    @SuppressWarnings("unchecked")
    private RingBuffer<RecordsHolder> getRingBuffer(Object member, String nodeId) {
        Disruptor<RecordsHolder> disruptor = this.disruptorMap.get(member);
        if (disruptor == null) {
            synchronized (this) {
                disruptor = this.disruptorMap.get(member);
                if (disruptor == null) {
                    disruptor = new Disruptor<>(new RecordsEventFactory(),
                            remoteIndexerCommunicatorBufferSize, this.executor, ProducerType.MULTI,
                            new BlockingWaitStrategy());
                    disruptor.handleEventsWith(new RecordsEventHandler(member, nodeId, stagingIndexDataStore));
                    this.disruptorMap.put(member, disruptor);
                    disruptor.start();
                }
            }
        }
        return disruptor.getRingBuffer();
    }
    
    public void close() {
        for (Disruptor<RecordsHolder> disruptor : this.disruptorMap.values()) {
            disruptor.shutdown();
        }
    }
    
    /**
     * Holder class for {@link Record}s.
     */
    public static class RecordsHolder {
        
        private List<Record> records;
        
        public void setRecords(List<Record> records) {
            this.records = records;
        }
        
        public List<Record> getRecords() {
            return records;
        }
        
    }
    
    /**
     * Record event creating factory class.
     */
    public static class RecordsEventFactory implements EventFactory<RecordsHolder> {

        @Override
        public RecordsHolder newInstance() {
            return new RecordsHolder();
        }
        
    }
    
    /**
     * Disruptor handler for processing record events.
     */
    public static class RecordsEventHandler implements EventHandler<RecordsHolder> {

        private List<Record> records = new ArrayList<>();
        
        private Object member;

        private String nodeId;

        private StagingIndexDataStore stagingIndexDataStore;

        public RecordsEventHandler(Object member, String nodeId, StagingIndexDataStore stagingIndexDataStore) {
            this.member = member;
            this.nodeId = nodeId;
            this.stagingIndexDataStore = stagingIndexDataStore;
        }
        
        @Override
        public void onEvent(RecordsHolder event, long sequence, boolean endOfBatch) throws Exception {
            this.records.addAll(event.getRecords());
            if (endOfBatch) {
                try {
                    AnalyticsClusterManager acm = AnalyticsServiceHolder.getAnalyticsClusterManager();
                    acm.executeOne(Constants.ANALYTICS_INDEXING_GROUP, this.member, new IndexDataPutCall(this.records));
                    if (log.isDebugEnabled()) {
                        log.debug("Remote Member Disruptor Send: " + this.records.size() + " -> " + this.member);
                    }
                } catch (Throwable t) {
                    // if the node is not reachable insert into the staging area
                    try {
                        this.stagingIndexDataStore.put(nodeId, records);
                        if (log.isDebugEnabled()) {
                            log.debug("Remote Member Disruptor Added to Staging: " + this.records.size() +
                                    " -> " + this.member);
                        }
                    } catch (Throwable e) {
                        log.error("Error in Remote Disruptor Send: ", e);
                    }
                }
                this.records.clear();
            }
        }
        
    }
    
}
