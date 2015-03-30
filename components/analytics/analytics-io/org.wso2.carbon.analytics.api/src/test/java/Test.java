/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

import org.apache.commons.httpclient.HttpClient;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.analytics.api.CarbonAnalyticsAPI;
import org.wso2.carbon.analytics.api.exception.AnalyticsServiceException;
import org.wso2.carbon.analytics.dataservice.commons.IndexType;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.RecordGroup;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.*;

public class Test {

//    public static void main(String[] args) throws AnalyticsServiceException, AnalyticsException, UnsupportedEncodingException {
//        SchemeRegistry schemeRegistry = new SchemeRegistry();
//        schemeRegistry.register(
//                new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
//        schemeRegistry.register(
//                new Scheme("https", 443, SSLSocketFactory.getSocketFactory()));
//
//        PoolingClientConnectionManager cm = new PoolingClientConnectionManager(schemeRegistry);
//        cm.setDefaultMaxPerRoute(100);
//        cm.setMaxTotal(200);
//
//        HttpParams params = new BasicHttpParams();
//        params.setParameter(CoreConnectionPNames.SO_TIMEOUT, 60000);
//        params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 60000);
//        DefaultHttpClient client = new DefaultHttpClient(cm, params);
//
//        List<NameValuePair> arguments = new ArrayList<>(3);
//        arguments.add(new BasicNameValuePair("username", "admin"));
//        arguments.add(new BasicNameValuePair("firstName", "System"));
//        arguments.add(new BasicNameValuePair("lastName", "Administrator"));
//
//        HttpPost get = new HttpPost("urllllll");
//        get.setEntity(new UrlEncodedFormEntity(arguments));
//
//        HttpResponse response;
//        try {
//            response = client.execute(get);
//            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
//                EntityUtils.consume(response.getEntity());
//                return null;
//            }
//
//            Reader is = new InputStreamReader(response.getEntity().getContent());
//            StringBuffer buf = new StringBuffer();
//
//            while (is.ready()) {
//                char[] b = new char[1024];
//                int c = is.read(b);
//                buf.append(b);
//            }
//            is.close();
//
//        } catch (ClientProtocolException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//            get.abort();
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//            get.abort();
//        }
//
//        CarbonAnalyticsAPI analyticsAPI = new CarbonAnalyticsAPI("/home/sinthuja/projects/my-git-repo/wso2/carbon-analytics/components/analytics/analytics-io/org.wso2.carbon.analytics.api/src/test/resources/analytics-data-config.xml");
//        Map<String, AnalyticsSchema.ColumnType> columnTypeMap = new HashMap<>();
//        columnTypeMap.put("ip", AnalyticsSchema.ColumnType.STRING);
//        columnTypeMap.put("log", AnalyticsSchema.ColumnType.STRING);
//        columnTypeMap.put("timeStamp", AnalyticsSchema.ColumnType.LONG);
//
//        List<String> primaryKeys = new ArrayList<>();
//        primaryKeys.add("timeStamp");
//        AnalyticsSchema schema = new AnalyticsSchema(columnTypeMap, primaryKeys);
////        analyticsAPI.setTableSchema(-1234, "LOGTABLE", schema);
//        analyticsAPI.createTable(-1234, "LOGTABLE2");
//        analyticsAPI.setTableSchema(-1234, "LOGTABLE2", schema);
//        System.out.println(analyticsAPI.listTables(-1234));
//        analyticsAPI.deleteTable(-1234, "LOGTABLE2");
//        System.out.println(analyticsAPI.listTables(-1234));

//

//        List<Record> records = new ArrayList<>();
//        Map<String, Object> values = new HashMap<>();
//        values.put("ip", "127.0.0.1");
//        values.put("log", "some logggggggggg");
//        values.put("timeStamp", 12345567);
//        for (int i = 0; i < 10; i++) {
//            records.add(new Record(-1234, "LOGTABLE", values));
//        }
//        analyticsAPI.put(records);
//        List<String> cols = new ArrayList<>();
//        cols.add("ip");
//        cols.add("log");
//        List<String> ids = new ArrayList<>();
//        ids.add("14274634375850.37710387577308135");
//        ids.add("14274634375860.21904066391785604");
//        ids.add("14274634375860.19010594578094642");
//        RecordGroup[] recordGroups = analyticsAPI.get(-1234, "LOGTABLE", 1,cols, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1);
//
//        Iterator<Record> recordsIter = analyticsAPI.readRecords(recordGroups[0]);
//        int count = 1;
//        while (recordsIter.hasNext()){
//            Record record = recordsIter.next();
//            System.out.println("record......" + record.getId());
//            System.out.println(count);
//            count++;
//        }

//        analyticsAPI.delete(-1234, "LOGTABLE", ids);
//        System.out.println(analyticsAPI.getRecordCount(-1234, "LOGTABLE", Long.MIN_VALUE, Long.MAX_VALUE));

//        Map<String, IndexType> indexTypeMap = new HashMap<>();
//        indexTypeMap.put("ip", IndexType.STRING);
//        indexTypeMap.put("log", IndexType.STRING);
//
//        analyticsAPI.setIndices(-1234, "LOGTABLE", indexTypeMap);
//        analyticsAPI.clearIndices(-1234, "LOGTABLE");
//        System.out.println(analyticsAPI.getIndices(-1234, "LOGTABLE"));

//        analyticsAPI
//        System.out.println(analyticsAPI.listTables(-1234));
//    }
}
