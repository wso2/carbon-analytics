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
package org.wso2.carbon.analytics.restapi.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * The Class RecordBean.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "record")
public class RecordBean {

    /** The id. */
    @XmlElement(required = false, name = "id")
    private String id;

	/** The table name. */
	@XmlElement(required = false, name = "tableName")
	private String tableName;

    /** The timestamp. */
    @XmlElement(required = false, nillable = true, name = "timestamp")
    private Long timestamp;

	/** The values. */
	@XmlElementWrapper(required = true, name = "values")
	private Map<String, Object> values;

	/**
	 * Sets the table name.
	 * @param tableName the new table name
	 */
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	/**
	 * Sets the values.
	 * @param values the values
	 */
	public void setValues(Map<String, Object> values) {
		this.values = values;
	}

	/**
	 * Sets the timestamp.
	 * @param timestamp the new timestamp
	 */
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * Sets the id.
	 * @param id the new id
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Gets the id.
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	/**
	 * Gets the table name.
	 * @return the table name
	 */
	public String getTableName() {
		return tableName;
	}

	/**
	 * Gets the values.
	 * @return the values
	 */
	public Map<String, Object> getValues() {
		return values;
	}

	/**
	 * Gets the value.
	 * @param name
	 *            the name
	 * @return the value
	 */
	public Object getValue(String name) {
		return this.values.get(name);
	}
	
	@Override
	public String toString(){
		List<String> valueList = new ArrayList<String>();
		for (Entry<String, Object> entry : values.entrySet()) {
			valueList.add(entry.getKey() + ":" + entry.getValue());
		}
		return valueList.toString();
		
	}
}