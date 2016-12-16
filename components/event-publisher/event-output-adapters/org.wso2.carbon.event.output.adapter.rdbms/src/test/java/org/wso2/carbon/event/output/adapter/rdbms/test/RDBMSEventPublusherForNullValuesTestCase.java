/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

package org.wso2.carbon.event.output.adapter.rdbms.test;

import junit.framework.Assert;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.BeforeClass;
import org.junit.Test;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;

import javax.sql.DataSource;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RDBMSEventPublusherForNullValuesTestCase {

    private static final String CREATE_TEST_TABLE = "CREATE TABLE IF NOT EXISTS TEST_TABLE (\n" +
            "                        intValue INT,\n" +
            "                        stringValue VARCHAR(512)\n" +
            ");\n";

    private static DataSource dataSource;

    @BeforeClass
    public static void initialize() throws ClassNotFoundException {
        Class.forName("org.h2.Driver");
        JdbcDataSource h2DataSource = new JdbcDataSource();
        h2DataSource.setURL("jdbc:h2:./target/ANALYTICS_EVENT_STORE;DB_CLOSE_ON_EXIT=FALSE;LOCK_TIMEOUT=60000;AUTO_SERVER=true");
        h2DataSource.setUser("wso2carbon");
        h2DataSource.setPassword("wso2carbon");
        dataSource = h2DataSource;
        PreparedStatement stmntCreateTable = null;
        try {
            stmntCreateTable = dataSource.getConnection().prepareStatement(CREATE_TEST_TABLE);
            stmntCreateTable.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testEventPublisherForNullValues() throws SQLException, InvocationTargetException {
        try {
            Class cls = Class.forName("org.wso2.carbon.event.output.adapter.rdbms.RDBMSEventAdapter");
            Constructor constructor = cls.getConstructor(OutputEventAdapterConfiguration.class, Map.class);
            Object instance = constructor.newInstance(null,null);
            Method method = cls.getDeclaredMethod("populateStatement", new Class[] { Map.class, PreparedStatement.class,  List.class });
            method.setAccessible(true);
            PreparedStatement stmt  = this.dataSource.getConnection().prepareStatement("INSERT INTO TEST_TABLE (intValue, stringValue) VALUES (?,?)");
            List<Attribute> colOrder = new ArrayList<Attribute>();
            Map<String, Object> testMap = new HashMap<String,Object>();
            testMap.put("intValue", new Integer(4));
            testMap.put("stringValue",null);
            colOrder.add(new Attribute("intValue", AttributeType.INT));
            colOrder.add(new Attribute("stringValue", AttributeType.STRING));
            method.invoke(instance, new Object[]{testMap, stmt, colOrder});
            int inserted = stmt.executeUpdate();
            Assert.assertEquals(1,inserted);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }
}
