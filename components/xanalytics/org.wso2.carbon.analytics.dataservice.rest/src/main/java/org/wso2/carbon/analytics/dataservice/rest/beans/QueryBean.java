package org.wso2.carbon.analytics.dataservice.rest.beans;

import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "tenantId", "tableName", "columns", "language", "query", "start", "count" })
@XmlRootElement(name = "query")
public class QueryBean {
	@XmlElement(required = true)
	private int tenantId;
	@XmlElement(required = true)
	private String tableName;
	@XmlElement(required = false)
	private Map<String, IndexTypeBean> columns;
	@XmlElement(required = false)
	private String language;
	@XmlElement(required = false)
	private String query;
	@XmlElement(required = false)
	private int start;
	@XmlElement(required = false)
	private int count;

	public int getTenantId() {
		return tenantId;
	}

	public void setTenantId(int tenantId) {
		this.tenantId = tenantId;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public Map<String, IndexTypeBean> getColumns() {
		return columns;
	}

	public void setColumns(Map<String, IndexTypeBean> columns) {
		this.columns = columns;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
}
