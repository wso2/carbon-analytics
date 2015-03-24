/**
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.analytics.dashboard.admin.data;

import org.apache.axis2.AxisFault;

import java.util.Arrays;
import java.util.List;

/**
 * Displays widgets
 * Contains the links to a set of widgets, each residing inside a specific dataView object
 */
public class Dashboard {

	private String id;
	private String title;
	private String group;
	private String[] roles;
	private WidgetMetaData[] widgets;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String[] getRoles() {
		return roles;
	}

	public void setRoles(String[] roles) {
		this.roles = roles;
	}

	public WidgetMetaData[] getWidgets() {
		return widgets;
	}

	public void setWidgets(WidgetMetaData[] widgets) {
		this.widgets = widgets;
	}

	public void addWidget(WidgetMetaData widget) throws AxisFault {
		for (WidgetMetaData existingWidget : widgets) {
			if (existingWidget.getId().equals(widget.getId())) {
				throw new AxisFault("Widget with given ID already exists in the dashboard");
			}
		}
		List<WidgetMetaData> widgetMetaDataList = Arrays.asList(widgets);
		widgetMetaDataList.add(widget);
		widgets = widgetMetaDataList.toArray(new WidgetMetaData[widgetMetaDataList.size()]);
	}

	public boolean updateWidget(WidgetMetaData widget) throws AxisFault {
		boolean updateStatus = deleteWidget(widget.getId());
		addWidget(widget);
		return updateStatus;
	}

	public boolean deleteWidget(String widgetID) throws AxisFault {
		List<WidgetMetaData> widgetMetaDataList = Arrays.asList(widgets);
		for (WidgetMetaData existingWidget : widgetMetaDataList) {
			if (existingWidget.getId().equals(widgetID)) {
				widgetMetaDataList.remove(existingWidget);
				widgets = widgetMetaDataList.toArray(new WidgetMetaData[widgetMetaDataList.size()]);
				return true;
			}
		}
		return false;
	}

}
