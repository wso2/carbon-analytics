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
package org.wso2.carbon.analytics.dashboard.admin;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.dashboard.admin.data.*;
import org.wso2.carbon.analytics.dashboard.admin.internal.ServiceHolder;
import org.wso2.carbon.analytics.dataservice.SecureAnalyticsDataService;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.RecordGroup;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.registry.api.Collection;
import org.wso2.carbon.registry.api.RegistryException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DashboardAdminService extends AbstractAdmin {

	/**
	 * Relative Registry locations for dataViews and dashboards.
	 */
	private static final String DATAVIEWS_DIR =
			"/repository/components/org.wso2.carbon.analytics.dataviews/";
	private static final String DASHBOARDS_DIR =
			"/repository/components/org.wso2.carbon.analytics.dashboards/";

    private SecureAnalyticsDataService analyticsDataService;

    public DashboardAdminService() {
        this.analyticsDataService = ServiceHolder.getAnalyticsDataService();
    }

	/**
	 * Logger
	 */
	private Log logger = LogFactory.getLog(DashboardAdminService.class);

    public Table getRecords(String tableName, long timeFrom, long timeTo, int startIndex, int recordCount,
                                       String searchQuery)
            throws AxisFault {

        if (logger.isDebugEnabled()) {
            logger.debug("Search Query: " + searchQuery);
            logger.debug("timeFrom: " + timeFrom);
            logger.debug("timeTo: " + timeTo);
            logger.debug("Start Index: " + startIndex);
            logger.debug("Page Size: " + recordCount);
        }

        String username = getUsername();

        Table table = new Table();
        table.setName(tableName);
        RecordGroup[] results = new RecordGroup[0];
        long searchCount = 0;

        if (searchQuery != null && !searchQuery.isEmpty()) {
            //TODO implement me :(
        } else {
            try {
                results = analyticsDataService.get(username, tableName, 1, null, timeFrom, timeTo, startIndex, recordCount);
            } catch (Exception e) {
                logger.error("Unable to get records from Analytics data layer for tenant: " + username +
                        " and for table:" + tableName, e);
                throw new AxisFault("Unable to get records from Analytics data layer for tenant: " + username +
                        " and for table:" + tableName, e);
            }
        }

        if (results != null) {
            List<Record> records;
            List<Row> rowList = new ArrayList<Row>();
            try {
                records = GenericUtils.listRecords(analyticsDataService, results);
            } catch (Exception e) {
                logger.error("Unable to convert result to record for tenant: " + username +
                        " and for table:" + tableName, e);
                throw new AxisFault("Unable to convert result to record for tenant: " + username +
                        " and for table:" + tableName, e);
            }
            if (records != null && !records.isEmpty()) {
                for (Record record : records) {
                    rowList.add(createRow(record));
                }
            }
            Row[] rows = new Row[rowList.size()];
            rowList.toArray(rows);
            table.setRows(rows);
        }
        return table;
    }

    private Row createRow(Record record) {
        Row row = new Row();
        Cell[] cells = new Cell[record.getValues().size()];
        int i = 0;
        for (Map.Entry<String, Object> entry : record.getValues().entrySet()) {
            cells[i++] = createCell(entry.getKey(), entry.getValue());
        }
        row.setCells(cells);
        return row;
    }

    private Cell createCell(String key,Object value) {
        Cell cell = new Cell(key,String.valueOf(value));
        return cell;
    }

	/**
	 * @return All the dataView objects saved in the registry.
	 * @throws AxisFault
	 */
	public DataView[] getDataViewsInfo() throws AxisFault {
		ArrayList<DataView> dataViews = new ArrayList<DataView>();
		Collection dataViewsCollection = RegistryUtils.readCollection(DATAVIEWS_DIR);
		String[] resourceNames;
		try {
			resourceNames = dataViewsCollection.getChildren();
		} catch (RegistryException e) {
			logger.error(e);
			throw new AxisFault(e.getMessage(), e);
		}
		for (String resourceName : resourceNames) {
			DataView dataView = getDataView(resourceName.replace(DATAVIEWS_DIR, ""));
			dataView.setWidgets(new Widget[0]);
			dataViews.add(dataView);
		}
		DataView[] dataViewArray = new DataView[dataViews.size()];
		return dataViews.toArray(dataViewArray);
	}

	/**
	 * @param dataViewID Id of the target dataView to be read.
	 * @return DataView object which is read from the registry.
	 * @throws AxisFault
	 */
	public DataView getDataView(String dataViewID) throws AxisFault {
		return (DataView) RegistryUtils.readResource(DATAVIEWS_DIR + dataViewID, DataView.class);
	}

	/**
	 * Appends a dataView object to the registry with the dataView as the resource content.
	 *
	 * @param dataView Object to be appended to the registry.
	 * @throws AxisFault
	 */
	public boolean addDataView(DataView dataView) throws AxisFault {

		if (!RegistryUtils.isResourceExist(DATAVIEWS_DIR + dataView.getId())) {
			RegistryUtils.writeResource(DATAVIEWS_DIR + dataView.getId(), dataView);
			return true;
		} else {
			String errorMessage = "DataView with ID:" + dataView.getId() + " already exists";
			logger.error(errorMessage);
			throw new AxisFault(errorMessage);
		}

	}

	/**
	 * Updates an existing dataView.
	 *
	 * @param dataView Object to be updated.
	 * @throws AxisFault If a matching dataView does not exist.
	 */
	public boolean updateDataView(DataView dataView) throws AxisFault {

		if (RegistryUtils.isResourceExist(DATAVIEWS_DIR + dataView.getId())) {
			RegistryUtils.writeResource(DATAVIEWS_DIR + dataView.getId(), dataView);
			return true;
		} else {
			String errorMessage = "DataView with ID:" + dataView.getId() + " does not exist";
			logger.debug(errorMessage);
			return false;
		}
	}

	/**
	 * Deletes the dataView resource with the given name.
	 *
	 * @param dataViewID Id of the dataView to be deleted.
	 */
	public boolean deleteDataView(String dataViewID) throws AxisFault {
		if (RegistryUtils.isResourceExist(DATAVIEWS_DIR + dataViewID)) {
			RegistryUtils.deleteResource(DATAVIEWS_DIR + dataViewID);
			return true;
		} else {
			String errorMessage = "DataView with ID:" + dataViewID + " does not exist";
			logger.debug(errorMessage);
			return false;
		}
	}

	/**
	 * Appends a widget to an existing DataView.
	 *
	 * @param dataViewID Existing dataView.
	 * @param widget     Widget to be appended.
	 * @throws AxisFault
	 */
	public boolean addWidget(String dataViewID, Widget widget) throws AxisFault {
		DataView dataView = getDataView(dataViewID);
		dataView.addWidget(widget);
		return updateDataView(dataView);
	}

	/**
	 * Updates a widget of an existing DataView.
	 *
	 * @param dataViewID Existing dataView object.
	 * @param widget     Widget to be updated.
	 * @throws AxisFault
	 */
	public boolean updateWidget(String dataViewID, Widget widget) throws AxisFault {
		DataView dataView = getDataView(dataViewID);
		dataView.updateWidget(widget);
		return updateDataView(dataView);
	}

	//TODO nice to have feature- when dimensions are not given, find a place for it from the server side

	/**
	 * Returns a dataView object with a SINGLE widget.
	 *
	 * @param dataViewID DataView name in which the target widget resides.
	 * @param widgetID   Widget to be included in the dataView object.
	 * @return DataView object with a single widget in the widget array-list.
	 * @throws AxisFault
	 */
	public DataView getWidgetWithDataViewInfo(String dataViewID, String widgetID) throws AxisFault {
		DataView dataView = getDataView(dataViewID);
		Widget widget = dataView.getWidget(widgetID);
		dataView.setWidgets(new Widget[0]);
		dataView.addWidget(widget);
		return dataView;
	}

	/**
	 * @param dataViewID Target dataView.
	 * @return Widget list of given dataView as an array.
	 * @throws AxisFault
	 */
	public Widget[] getWidgets(String dataViewID) throws AxisFault {
		return getDataView(dataViewID).getWidgets();
	}

	/**
	 * @return All the existing dashboards as an Array-list.
	 * @throws AxisFault
	 */
	public Dashboard[] getDashboards() throws AxisFault {
		try {
			List<Dashboard> dashboards = new ArrayList<Dashboard>();
			Collection dashboardsCollection = RegistryUtils.readCollection(DASHBOARDS_DIR);
			String[] resourceNames =
					dashboardsCollection.getChildren();
			for (String resourceName : resourceNames) {
				Dashboard dashboard = getDashboard(resourceName.replace(DASHBOARDS_DIR, ""));
				dashboards.add(dashboard);
			}
			Dashboard[] dashboardArray = new Dashboard[dashboards.size()];
			return dashboards.toArray(dashboardArray);
		} catch (Exception e) {
			String errorMessage = "Unable to extract resources from collection";
			logger.error(errorMessage);
			throw new AxisFault(errorMessage);
		}
	}

	/**
	 * @param dashboardID Target dashboard ID.
	 * @return Dashboard with widget-meta-Data.
	 * @throws AxisFault
	 */
	public Dashboard getDashboard(String dashboardID) throws AxisFault {
		return (Dashboard) RegistryUtils
				.readResource(DASHBOARDS_DIR + dashboardID, Dashboard.class);
	}

	/**
	 * Adds a new dashboard to the registry, does not allow to replace existing dashboard.
	 *
	 * @param dashboard Object to be appended to the registry.
	 * @throws AxisFault
	 */
	public boolean addDashboard(Dashboard dashboard) throws AxisFault {
		if (!RegistryUtils.isResourceExist(DASHBOARDS_DIR + dashboard.getId())) {
			RegistryUtils.writeResource(DASHBOARDS_DIR + dashboard.getId(), dashboard);
			return true;
		} else {
			String errorMessage = "Dashboard with name:" + dashboard.getId() + " already exists";
			logger.debug(errorMessage);
			throw new AxisFault(errorMessage);
		}
	}

	/**
	 * Updates an existing dashboard.
	 *
	 * @param dashboard Object to be updated.
	 * @throws AxisFault If a matching dashboard does not exist.
	 */
	public boolean updateDashboard(Dashboard dashboard) throws AxisFault {

		if (RegistryUtils.isResourceExist(DASHBOARDS_DIR + dashboard.getId())) {
			RegistryUtils.writeResource(DASHBOARDS_DIR + dashboard.getId(), dashboard);
			return true;
		} else {
			String errorMessage = "Dashboard with name:" + dashboard.getId() + " does not exist";
			logger.debug(errorMessage);
			throw new AxisFault(errorMessage);
		}

	}

	/**
	 * @param dashboardID Id of the dashboard to which the widget-meta-data will be appended.
	 * @param widget      Metadata to be appended.
	 * @throws AxisFault
	 */
	public boolean addWidgetToDashboard(String dashboardID, WidgetMetaData widget)
			throws AxisFault {
		Dashboard dashboard = getDashboard(dashboardID);
		dashboard.addWidget(widget);
		return updateDashboard(dashboard);
	}

	/**
	 * @param dashboardID Id of the dashboard to which the widget-meta-data will be updated.
	 * @param widget      Metadata to be updated.
	 * @throws AxisFault
	 */
	public boolean updateWidgetInDashboard(String dashboardID, WidgetMetaData widget)
			throws AxisFault {
		Dashboard dashboard = getDashboard(dashboardID);
		dashboard.updateWidget(widget);
		return updateDashboard(dashboard);
	}

    @Override
    protected String getUsername() {
        return super.getUsername() + "@" + getTenantDomain();
    }
}
