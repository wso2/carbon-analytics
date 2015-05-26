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
package org.wso2.carbon.analytics.datasource.cassandra;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;

/**
 * This class contains utility methods related to the Cassandra data connector.
 */
public class CassandraUtils {

    public static String[] splitParentChild(String path) {
        if (path.equals("/")) {
            return new String[] { ".", "/" };
        }
        int index = path.lastIndexOf('/');
        String child = path.substring(index + 1);
        String parent = path.substring(0, index);
        if (parent.length() == 0) {
            parent = "/";
        }
        return new String[] { GenericUtils.normalizePath(parent), child };
    }
    
    public static void main(String[] args) throws Exception {
        Cluster cluster = Cluster.builder().addContactPoint("localhost").build();
        Session session = cluster.connect();
        //session.execute("CREATE KEYSPACE test WITH REPLICATION = {'class':'SimpleStrategy', 'replication_factor':3};");
        //session.execute("CREATE TABLE test.Person (id varchar, timestamp bigint, data map<varchar, blob>, primary key (id));");
        PreparedStatement stmt = session.prepare("INSERT INTO test.Person (id, timestamp, data) VALUES (?,?,?)");
        BoundStatement bstmt = new BoundStatement(stmt);
        Map<String, ByteBuffer> data = new HashMap<String, ByteBuffer>();
        data.put("k1", ByteBuffer.wrap("ABC".getBytes()));
        data.put("k3", ByteBuffer.wrap("XXX".getBytes()));
        session.execute(bstmt.bind("id1", 43535L, data));
        ResultSet rs = session.execute("SELECT id,timestamp,data FROM test.Person");
        System.out.println(rs.iterator().next().getMap(2, String.class, ByteBuffer.class));
        session.close();        
        cluster.close();
    }
    
}
