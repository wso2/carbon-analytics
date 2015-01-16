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
package org.wso2.carbon.analytics.dataservice.rest.beans;

import javax.xml.bind.annotation.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
                      "id",
                      "tenantId",
                      "tableName",
                      "timestamp",
                      "values"
})

@XmlRootElement(name = "record")
public class RecordBean {

	@XmlElement(required = true)
    private int tenantId;
	@XmlElement(required = true)
    private String tableName;
	@XmlElement(required = true)
    private Map<String, Object> values;
	@XmlElement(required = true)
    private long timestamp;
	@XmlElement(required = true)
    private String id;
    
    private int hashCode = -1;

    public void setTenantId(int tenantId) {
		this.tenantId = tenantId;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public void setValues(Map<String, Object> values) {
		this.values = values;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public void setId(String id) {
		this.id = id;
	}
    
    public String getId() {
        return id;
    }

    public int getTenantId() {
        return tenantId;
    }
    
    public String getTableName() {
        return tableName;
    }
    
    public Map<String, Object> getValues() {
        return values;
    }
    
    public Object getValue(String name) {
        return this.values.get(name);
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof RecordBean)) {
            return false;
        }
        RecordBean rhs = (RecordBean) obj;
        if (this.getTenantId() != rhs.getTenantId()) {
            return false;
        }
        if (!this.getTableName().equals(rhs.getTableName())) {
            return false;
        }
        if (!this.getId().equals(rhs.getId())) {
            return false;
        }
        if (this.getTimestamp() != rhs.getTimestamp()) {
            return false;
        }
        return this.getNotNullValues().equals(rhs.getNotNullValues());
    }
    
    @Override
    public int hashCode() {
        if (this.hashCode == -1) {
            this.hashCode = ((Integer) this.getTenantId()).hashCode();
            this.hashCode += this.getTableName().hashCode() >> 2;
            this.hashCode += this.getId().hashCode() >> 4;
            this.hashCode += String.valueOf(this.getTimestamp()).hashCode() >> 8;
            this.hashCode += this.getNotNullValues().hashCode() >> 16;
        }
        return this.hashCode;
    }

    public Map<String, Object> getNotNullValues() {
        Map<String, Object> result = new HashMap<String, Object>(this.getValues());
        Iterator<Map.Entry<String, Object>> itr = result.entrySet().iterator();
        while (itr.hasNext()) {
            if (itr.next().getValue() == null) {
                itr.remove();
            }
        }
        return result;
    }
    
}
