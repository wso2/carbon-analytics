/**
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.analytics.dashboard.admin;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.dashboard.admin.data.*;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.registry.api.Collection;
import org.wso2.carbon.registry.api.RegistryException;

import java.util.ArrayList;
import java.util.List;

public class DashboardAdminService extends AbstractAdmin {


    private Log logger = LogFactory.getLog(DashboardAdminService.class);


    /**
     * @return All the existing dashboards as an Array-list.
     * @throws AxisFault
     */
    public Dashboard[] getDashboards() throws AxisFault {
        try {
            createCollections();
            List<Dashboard> dashboards = new ArrayList<Dashboard>();
            Collection dashboardsCollection = RegistryUtils.readCollection(DashboardConstants.DASHBOARDS_DIR);
            String[] resourceNames = dashboardsCollection.getChildren();
            for (String resourceName : resourceNames) {
                Dashboard dashboard = getDashboard(resourceName.replace(DashboardConstants.DASHBOARDS_DIR, ""));
                dashboards.add(dashboard);
            }
            Dashboard[] dashboardArray = new Dashboard[dashboards.size()];
            return dashboards.toArray(dashboardArray);
        } catch (Exception e) {
            logger.error("An error occurred while retrieving dashboards.", e);
            throw new AxisFault("An error occurred while retrieving dashboards.", e);
        }
    }

    /**
     * @param dashboardID Target dashboard ID.
     * @return Dashboard with widget-meta-Data.
     * @throws AxisFault
     */
    public Dashboard getDashboard(String dashboardID) throws AxisFault {
        Dashboard dashboard = null;
        try {
            dashboard = (Dashboard) RegistryUtils.readResource(DashboardConstants.DASHBOARDS_DIR
                    + dashboardID, Dashboard.class);
            if (dashboard.getWidgets() == null) {
                dashboard.setWidgets(new WidgetMetaData[0]);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Returning Dashboard with title " + dashboard.getTitle());
            }
        } catch (RegistryException e) {
            logger.error("Error while reading dashboard with id: " + dashboardID, e);
            throw new AxisFault("Error while reading dashboard with id: " + dashboardID, e);
        }
        return dashboard;
    }

    /**
     * Adds a new dashboard to the registry, does not allow to replace existing dashboard.
     *
     * @param dashboard Object to be appended to the registry.
     * @throws AxisFault
     */
    public boolean addDashboard(Dashboard dashboard) throws AxisFault {
        try {
            if (!RegistryUtils.isResourceExist(DashboardConstants.DASHBOARDS_DIR + dashboard.getId())) {
                RegistryUtils.writeResource(DashboardConstants.DASHBOARDS_DIR + dashboard.getId(), dashboard);
                return true;
            } else {
                String errorMessage = "Dashboard with id:" + dashboard.getId() + " already exists";
                logger.error(errorMessage);
                throw new AxisFault(errorMessage);
            }
        } catch (RegistryException e) {
            String message = "Error occurred while adding dashboard with id: " + dashboard.getId();
            logger.error(message, e);
            throw new AxisFault(message, e);
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
     * @return All the dataView objects saved in the registry.
     * @throws AxisFault
     */
    public DataView[] getDataViewsInfo() throws AxisFault {
        ArrayList<DataView> dataViews = new ArrayList<DataView>();
        Collection dataViewsCollection = RegistryUtils.readCollection(DashboardConstants.DATAVIEWS_DIR);
        String[] resourceNames;
        try {
            createCollections();
            resourceNames = dataViewsCollection.getChildren();
        } catch (RegistryException e) {
            logger.error(e);
            throw new AxisFault(e.getMessage(), e);
        }
        for (String resourceName : resourceNames) {
            DataView dataView = getDataView(resourceName.replace(DashboardConstants.DATAVIEWS_DIR, ""));
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
        DataView dataView = null;
        try {
            dataView = (DataView) RegistryUtils.readResource(DashboardConstants.DATAVIEWS_DIR +
                    dataViewID, DataView.class);
            if (dataView.getWidgets() == null) {
                dataView.setWidgets(new Widget[0]);
            }
            if (dataView.getColumns() == null) {
                dataView.setColumns(new Column[0]);
            }
        } catch (RegistryException e) {
            String message = "Error occurred while retrieving Dataview with id: " + dataViewID;
            logger.error(message, e);
            throw new AxisFault(message, e);
        }
        return dataView;
    }

    /**
     * Appends a dataView object to the registry with the dataView as the resource content.
     *
     * @param dataView Object to be appended to the registry.
     * @throws AxisFault
     */
    public boolean addDataView(DataView dataView) throws AxisFault {
        try {
            if (!RegistryUtils.isResourceExist(DashboardConstants.DATAVIEWS_DIR + dataView.getId())) {
                RegistryUtils.writeResource(DashboardConstants.DATAVIEWS_DIR + dataView.getId(), dataView);
                return true;
            } else {
                String errorMessage = "DataView with ID:" + dataView.getId() + " already exists";
                logger.error(errorMessage);
                throw new AxisFault(errorMessage);
            }
        } catch (RegistryException e) {
            String message = "Error occurred while adding Dataview : " + dataView.getId();
            logger.error(message, e);
            throw new AxisFault(message, e);
        }
    }

    /**
     * Updates an existing dataView.
     *
     * @param dataView Object to be updated.
     * @throws AxisFault If a matching dataView does not exist.
     */
    public boolean updateDataView(DataView dataView) throws AxisFault {
        try {
            if (RegistryUtils.isResourceExist(DashboardConstants.DATAVIEWS_DIR + dataView.getId())) {
                RegistryUtils.writeResource(DashboardConstants.DATAVIEWS_DIR + dataView.getId(), dataView);
                return true;
            } else {
                String errorMessage = "DataView with ID:" + dataView.getId() + " does not exist";
                logger.debug(errorMessage);
                return false;
            }
        } catch (RegistryException e) {
            logger.error("Error occurred while updating the Dataview : " + dataView.getId(), e);
            throw new AxisFault("Error occurred while updating the Dataview : " + dataView.getId(), e);
        }
    }

    /**
     * Deletes the dataView resource with the given name.
     *
     * @param dataViewID Id of the dataView to be deleted.
     */
    public boolean deleteDataView(String dataViewID) throws AxisFault {
        try {
            if (RegistryUtils.isResourceExist(DashboardConstants.DATAVIEWS_DIR + dataViewID)) {
                RegistryUtils.deleteResource(DashboardConstants.DATAVIEWS_DIR + dataViewID);
                return true;
            } else {
                String errorMessage = "DataView with ID:" + dataViewID + " does not exist";
                logger.debug(errorMessage);
                return false;
            }
        } catch (RegistryException e) {
            logger.error("Error occurred while deleting the Dataview [" + dataViewID + "]", e);
            throw new AxisFault("Error occurred while deleting the Dataview [" + dataViewID + "]", e);
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
     * Updates an existing dashboard.
     *
     * @param dashboard Object to be updated.
     * @throws AxisFault If a matching dashboard does not exist.
     */
    public boolean updateDashboard(Dashboard dashboard) throws AxisFault {
        try {
            if (RegistryUtils.isResourceExist(DashboardConstants.DASHBOARDS_DIR + dashboard.getId())) {
                RegistryUtils.writeResource(DashboardConstants.DASHBOARDS_DIR + dashboard.getId(), dashboard);
                return true;
            } else {
                String errorMessage = "Dashboard with name:" + dashboard.getId() + " does not exist";
                logger.debug(errorMessage);
                throw new AxisFault(errorMessage);
            }
        } catch (RegistryException e) {
            logger.error(e);
            throw new AxisFault(e.getMessage(), e);
        }
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

    private void createCollections() throws RegistryException {
        if (!RegistryUtils.isResourceExist(DashboardConstants.DASHBOARDS_DIR)) {
            logger.info("Creating Registry collection for Dashboards");
            RegistryUtils.createCollection(DashboardConstants.DASHBOARDS_DIR);
        }
        if (!RegistryUtils.isResourceExist(DashboardConstants.DATAVIEWS_DIR)) {
            logger.info("Creating Registry collection for Dataviews");
            RegistryUtils.createCollection(DashboardConstants.DATAVIEWS_DIR);
        }
    }

}