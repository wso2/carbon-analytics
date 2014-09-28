package org.wso2.carbon.analytics.datasource.rdbms.mysql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.wso2.carbon.analytics.datasource.core.DataType;
import org.wso2.carbon.analytics.datasource.core.DirectAnalyticsDataSource;
import org.wso2.carbon.analytics.datasource.core.Record;
import org.wso2.carbon.analytics.datasource.core.RecordGroup;
import org.wso2.carbon.analytics.datasource.core.DataType.Type;
import org.wso2.carbon.analytics.datasource.core.Record.Column;

public class MySQLAnalyticsDSTester {

	public static void main(String[] args) throws Exception {
		test0();
	}
	
	private static void test0() throws Exception {
		DirectAnalyticsDataSource ds = getDS();
		
		Map<String, DataType> columns = new HashMap<String, DataType>();
		columns.put("server_name", new DataType(Type.STRING, 100));
		columns.put("ip", new DataType(Type.STRING, 50));
		columns.put("tenant", new DataType(Type.INTEGER, -1));
		columns.put("log", new DataType(Type.STRING, 10000));
		
		ds.dropTable("Log");
		ds.addTable("Log", columns);
		
		long t1 = System.currentTimeMillis();
		int c = 10000, d = 50;
		for (int i = 0; i < c; i++) {
			ds.put(generateRecords(i, d));
		}
		long t2 = System.currentTimeMillis();
		System.out.println("Time: " + (t2 - t1) + " Record Write: " + c * 100 + " TPS:" + (double) (c * d) / (t2 - t1) * 1000.0);
		
		t1 = System.currentTimeMillis();
		RecordGroup[] x = ds.get("Log", null, -1, -1, 0, -1);
		System.out.println("X:" + x[0].getRecords().size());
		t2 = System.currentTimeMillis();
		System.out.println("Time: " + (t2 - t1) + " Record Read: " + c * 100 + " TPS:" + (double) (c * d) / (t2 - t1) * 1000.0);
	}
	
	private static List<Record> generateRecords(int i, int c) {
		List<Record> result = new ArrayList<Record>();
		List<Column> values;
		long time = System.currentTimeMillis();
		for (int j = 0; j < c; j++) {
			values = new ArrayList<Record.Column>();
			values.add(new Column("server_name", "ESB-" + i));
			values.add(new Column("ip", "192.168.0." + (i % 256)));
			values.add(new Column("tenant", i));
			values.add(new Column("log", "Exception in Sequence" + i));
			result.add(new Record("Log", values, time));
		}
		return result;
	}
	
	private static DirectAnalyticsDataSource getDS() throws Exception {
		PoolProperties pps = new PoolProperties();
		pps.setDriverClassName("com.mysql.jdbc.Driver");
		pps.setUrl("jdbc:mysql://localhost/bam3");
		pps.setUsername("root");
		pps.setPassword("root");
		DataSource dsx = new DataSource(pps);
		MySQLAnalyticsDataSource ds = new MySQLAnalyticsDataSource();
		Map<String, Object> props = new HashMap<String, Object>();
		props.put("datasource", dsx);
		props.put("storage_engine", "MYISAM");
		ds.init(props);
		return ds;
	}
	
}
