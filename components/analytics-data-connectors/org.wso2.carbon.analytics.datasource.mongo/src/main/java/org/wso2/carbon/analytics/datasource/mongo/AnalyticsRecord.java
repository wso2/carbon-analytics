package org.wso2.carbon.analytics.datasource.mongo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.types.Binary;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;

/**
 * @author jmalvarezf
 * 
 *         Intermediate class to do transmormations.
 *
 */
public class AnalyticsRecord {

	private String id;

	private Integer tenantId;

	private String tableName;

	private Long timestamp;

	private Map<String, Object> values;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Integer getTenantId() {
		return tenantId;
	}

	public void setTenantId(Integer tenantId) {
		this.tenantId = tenantId;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public Map<String, Object> getValues() {
		return values;
	}

	public void setValues(Map<String, Object> values) {
		this.values = values;
	}

	public static Record toRecord(AnalyticsRecord record, List<String> columns) {
		Map<String, Object> resultValues;
		if (columns != null) {
			resultValues = new HashMap<String, Object>(record.getValues().size());
			for (Map.Entry<String, Object> entry : record.getValues().entrySet()) {
				if (columns.contains(entry.getKey())) {
					resultValues.put(entry.getKey(), entry.getValue());
				}
			}
		} else {
			resultValues = record.getValues();
		}
		Record result = new Record(record.getId(), record.getTenantId(), record.getTableName(), resultValues,
				record.getTimestamp());
		return result;
	}

	@SuppressWarnings("unchecked")
	public static AnalyticsRecord fromDocument(Document document, String tableName, Integer tenantId) {
		AnalyticsRecord result = new AnalyticsRecord();
		result.setId(document.getString("arsId"));
		result.setTimestamp(document.getLong("timestamp"));
		Map<String, Binary> map = (Map<String, Binary>) document.get("data");
		Map<String, Object> values = new HashMap<String, Object>();
		for (Map.Entry<String, Binary> entry : map.entrySet()) {
			values.put(entry.getKey(), GenericUtils.deserializeObject(entry.getValue().getData()));
		}
		result.setValues(values);
		result.setTableName(tableName);
		result.setTenantId(tenantId);
		return result;
	}

	public static Document toDocument(Record record) {
		Document document = new Document();
		document.put("arsId", record.getId());
		document.put("timestamp", record.getTimestamp());
		Map<String, Binary> result = new HashMap<String, Binary>(record.getValues().size());
		for (Map.Entry<String, Object> entry : record.getValues().entrySet()) {
			result.put(entry.getKey(), new Binary(GenericUtils.serializeObject(entry.getValue())));
		}
		document.put("data", result);
		return document;
	}

	@Override
	public String toString() {
		return "AnalyticsRecord [id=" + id + ", tenantId=" + tenantId + ", tableName=" + tableName + ", timestamp="
				+ timestamp + ", values=" + values + "]";
	}

}
