/**
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.analytics.dataservice;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.IteratorUtils;
import org.wso2.carbon.analytics.dataservice.indexing.AnalyticsDataIndexer.IndexBatchOperation;
import org.wso2.carbon.analytics.datasource.core.AnalyticsDataCorruptionException;
import org.wso2.carbon.analytics.datasource.core.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.Record;
import org.wso2.carbon.analytics.datasource.core.RecordGroup;

/**
 * This class contains utility methods for analytics data service.
 */
public class AnalyticsDSUtils {

    @SuppressWarnings("unchecked")
    public static List<Record> listRecords(AnalyticsDataService ads, 
            RecordGroup[] rgs) throws AnalyticsException {
        List<Record> result = new ArrayList<Record>();
        for (RecordGroup rg : rgs) {
            result.addAll(IteratorUtils.toList(ads.readRecords(rg)));
        }
        return result;
    }
    
    public static byte[] indexBatchOpToBinary(IndexBatchOperation indexBatchOp) {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream objOut = null;
        try {
            objOut = new ObjectOutputStream(byteOut);
            objOut.writeObject(indexBatchOp);
            return byteOut.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error in indexBatchOpToBinary: " + e.getMessage(), e);
        } finally {
            try {
                if (objOut != null) {
                    objOut.close();
                }
                byteOut.close();
            } catch (Exception ignore) { /* ignore */ }
        }
    }
    
    public static IndexBatchOperation binaryToIndexBatchOp(byte[] data) throws AnalyticsDataCorruptionException {
        ByteArrayInputStream byteIn = new ByteArrayInputStream(data);
        ObjectInputStream objIn = null;
        try {            
            objIn = new ObjectInputStream(byteIn);
            return (IndexBatchOperation) objIn.readObject();
        } catch (Exception e) {
            throw new AnalyticsDataCorruptionException(
                    "Error in converting binary data to index batch operation: " + e.getMessage(), e);
        } finally {
            try {
                if (objIn != null) {
                    objIn.close();
                }
                byteIn.close();
            } catch (Exception ignore) { /* ignore */ }
        }
    }
    
}
