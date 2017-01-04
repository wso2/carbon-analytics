package es.eci.carbon.analytics.datasource.mongo;

import java.util.List;

import org.wso2.carbon.analytics.datasource.commons.RecordGroup;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;

/**
 * @author jmalvarezf
 *
 *         We are going to leave partitioning to Mongo sharded cluster, and to
 *         mongos the decision of where to query. Implementation copied from
 *         Cassandra.
 *
 */
public class GlobalMongoRecordGroup implements RecordGroup {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2532969701191543642L;

	private boolean byIds;

	private int tenantId;

	private String tableName;

	private List<String> columns;

	private long timeFrom;

	private long timeTo;

	private List<String> ids;

	private int count;

	public GlobalMongoRecordGroup(int tenantId, String tableName, List<String> columns, long timeFrom, long timeTo,
			int count) {
		this.tenantId = tenantId;
		this.tableName = tableName;
		this.columns = columns;
		this.timeFrom = timeFrom;
		this.timeTo = timeTo;
		this.count = count;
		this.byIds = false;
	}

	public GlobalMongoRecordGroup(int tenantId, String tableName, List<String> columns, List<String> ids) {
		this.tenantId = tenantId;
		this.tableName = tableName;
		this.columns = columns;
		this.ids = ids;
		this.byIds = true;
	}

	@Override
	public String[] getLocations() throws AnalyticsException {
		return new String[] { "localhost" };
	}

	public boolean isByIds() {
		return byIds;
	}

	public int getTenantId() {
		return tenantId;
	}

	public String getTableName() {
		return tableName;
	}

	public List<String> getColumns() {
		return columns;
	}

	public long getTimeFrom() {
		return timeFrom;
	}

	public long getTimeTo() {
		return timeTo;
	}

	public List<String> getIds() {
		return ids;
	}

	public int getCount() {
		return count;
	}

}
