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
 * Main object which contains an array of widgets and other data required for widget creation
 * Points to a data-source which could be a BAM table or a CEP stream
 */
public class DataView {
	private String id;
	private String displayName;
	private String type;
	private String dataSource;
	private Column[] columns;
	private String filter;
	private Widget[] widgets;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDataSource() {
		return dataSource;
	}

	public void setDataSource(String dataSource) {
		this.dataSource = dataSource;
	}

	public Column[] getColumns() {
		return columns;
	}

	public void setColumns(Column[] columns) {
		this.columns = columns;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	public Widget[] getWidgets() {
		return widgets;
	}

	public void setWidgets(Widget[] widgets) {
		this.widgets = widgets;
	}

	public void addWidget(Widget widget) throws AxisFault {
		for (Widget ExistingWidget : widgets) {
			if (ExistingWidget.getId().equals(widget.getId())) {
				throw new AxisFault("Widget with given ID already exists");
			}
		}
		List<Widget> widgetList = Arrays.asList(widgets);
		widgetList.add(widget);
		widgets = widgetList.toArray(new Widget[widgetList.size()]);
	}

	public boolean updateWidget(Widget widget) throws AxisFault {
		try {
			boolean updateStatus = deleteWidget(widget.getId());
			addWidget(widget);
			return updateStatus;
		} catch (Exception e) {
			throw new AxisFault("Failed to update Widget");
		}
	}

	public Widget getWidget(String widgetID) throws AxisFault {
		//		if(widgets==null){
		//			widgets=new ArrayList<>();
		//		}
		for (Widget widget : widgets) {
			if (widget.getId().equals(widgetID)) {
				return widget;
			}
		}
		throw new AxisFault("Widget With given ID does not exist in the dashboard");
	}

	public boolean deleteWidget(String widgetID) throws AxisFault {
		List<Widget> widgetList = Arrays.asList(widgets);
		for (Widget existingWidget : widgetList) {
			if (existingWidget.getId().equals(widgetID)) {
				widgetList.remove(existingWidget);
				widgets = widgetList.toArray(new Widget[widgetList.size()]);
				return true;
			}
		}
		return false;
	}

}
