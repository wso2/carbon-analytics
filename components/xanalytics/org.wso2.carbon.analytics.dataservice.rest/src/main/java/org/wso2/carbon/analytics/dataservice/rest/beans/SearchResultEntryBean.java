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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
                      "id",
                      "score"
})

@XmlRootElement( name = "SearchResultEntry")
public class SearchResultEntryBean implements Comparable<SearchResultEntryBean> {
	@XmlElement(required = true)
	private String id;
	@XmlElement(required = true)
	private float score;

	public void setId(String id) {
		this.id = id;
	}

	public void setScore(float score) {
		this.score = score;
	}
	
	public String getId() {
		return id;
	}

	public float getScore() {
		return score;
	}

	public int compareTo(SearchResultEntryBean obj) {
		if (this.score > obj.score) {
			return 1;
		} else if (this.score < obj.score) {
			return -1;
		} else {
			return 0;
		}
    }
}
