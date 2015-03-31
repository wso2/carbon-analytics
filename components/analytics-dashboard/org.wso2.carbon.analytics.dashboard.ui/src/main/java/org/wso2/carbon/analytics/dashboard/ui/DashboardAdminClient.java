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
package org.wso2.carbon.analytics.dashboard.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.log4j.Logger;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.analytics.dashboard.batch.stub.BatchAnalyticsDashboardAdminServiceStub;
import org.wso2.carbon.analytics.dashboard.batch.stub.data.Cell;
import org.wso2.carbon.analytics.dashboard.batch.stub.data.Row;
import org.wso2.carbon.analytics.dashboard.batch.stub.data.Table;
import org.wso2.carbon.analytics.dashboard.stub.DashboardAdminServiceStub;
import org.wso2.carbon.analytics.dashboard.stub.data.Column;
import org.wso2.carbon.analytics.dashboard.stub.data.Dashboard;
import org.wso2.carbon.analytics.dashboard.stub.data.DataView;
import org.wso2.carbon.analytics.dashboard.stub.data.Widget;
import org.wso2.carbon.analytics.dashboard.stub.data.WidgetMetaData;
import org.wso2.carbon.analytics.dashboard.ui.dto.ColumnDTO;
import org.wso2.carbon.analytics.dashboard.ui.dto.DashboardDTO;
import org.wso2.carbon.analytics.dashboard.ui.dto.DataViewDTO;
import org.wso2.carbon.analytics.dashboard.ui.dto.DimensionDTO;
import org.wso2.carbon.analytics.dashboard.ui.dto.TableDTO;
import org.wso2.carbon.analytics.dashboard.ui.dto.WidgetAndDataViewDTO;
import org.wso2.carbon.analytics.dashboard.ui.dto.WidgetDTO;
import org.wso2.carbon.analytics.dashboard.ui.dto.WidgetInstanceDTO;
import org.wso2.carbon.event.publisher.stub.EventPublisherAdminServiceStub;
import org.wso2.carbon.event.stream.stub.EventStreamAdminServiceStub;
import org.wso2.carbon.ui.CarbonUIUtil;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Admin client that performs dashboard related operations with DashboardAdminService.
 */
public class DashboardAdminClient {
    private static final Logger logger = Logger.getLogger(DashboardAdminClient.class);

    /**
     * Acquires DashboardAdminServiceStub stub
     *
     * @param config  ServletConfig
     * @param session
     * @param request
     * @return DashboardAdminServiceStub which performs dashboard/dataview related operations.
     * @throws AxisFault If there are any exceptions.
     */
    public static DashboardAdminServiceStub getDashboardAdminService(
            ServletConfig config, HttpSession session,
            HttpServletRequest request)
            throws AxisFault {

        ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
                .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        //Server URL which is defined in the server.xml
        String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(),
                session) + "DashboardAdminService.DashboardAdminServiceHttpsSoap12Endpoint";
        DashboardAdminServiceStub stub = new DashboardAdminServiceStub(configContext, serverURL);

        String cookie = (String) session.getAttribute(org.wso2.carbon.utils.ServerConstants.ADMIN_SERVICE_COOKIE);

        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

        return stub;
    }

    /**
     * Acquires the BatchAnalyticsDashboardAdminServiceStub which is responsible for responding with analytical (batch)
     * data from BAM's analytical tables.
     *
     * @param config
     * @param session
     * @param request
     * @return
     * @throws AxisFault
     */
    public static BatchAnalyticsDashboardAdminServiceStub getDashboardBatchAnalyticsAdminService(
            ServletConfig config, HttpSession session,
            HttpServletRequest request)
            throws AxisFault {

        ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
                .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        //Server URL which is defined in the server.xml
        String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(),
                session) + "BatchAnalyticsDashboardAdminService" +
                ".BatchAnalyticsDashboardAdminServiceHttpsSoap12Endpoint";
        BatchAnalyticsDashboardAdminServiceStub stub = new BatchAnalyticsDashboardAdminServiceStub
                (configContext, serverURL);

        String cookie = (String) session.getAttribute(org.wso2.carbon.utils.ServerConstants.ADMIN_SERVICE_COOKIE);

        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

        return stub;
    }

    public static EventStreamAdminServiceStub getEventStreamAdminService(
            ServletConfig config, HttpSession session,
            HttpServletRequest request)
            throws AxisFault {

        ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
                .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        //Server URL which is defined in the server.xml
        String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(),
                session) + "EventStreamAdminService" +
                ".EventStreamAdminServiceHttpsSoap12Endpoint";
        EventStreamAdminServiceStub stub = new EventStreamAdminServiceStub
                (configContext, serverURL);

        String cookie = (String) session.getAttribute(org.wso2.carbon.utils.ServerConstants.ADMIN_SERVICE_COOKIE);

        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

        return stub;
    }

    public static EventPublisherAdminServiceStub getEventPublisherAdminService(
            ServletConfig config, HttpSession session,
            HttpServletRequest request)
            throws AxisFault {

        ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
                .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        //Server URL which is defined in the server.xml
        String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(),
                session) + "EventPublisherAdminService.EventPublisherAdminServiceHttpsSoap12Endpoint";
        EventPublisherAdminServiceStub stub = new EventPublisherAdminServiceStub(configContext, serverURL);

        String cookie = (String) session.getAttribute(org.wso2.carbon.utils.ServerConstants.ADMIN_SERVICE_COOKIE);

        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

        return stub;
    }

    //TODO rename displayName to name

    /**
     * Returns a DataViewDTO object from a DataView object.
     * DataViewDTO is then transformed to JSON using Gson.
     *
     * @param dataView
     * @return
     */
    public static DataViewDTO toDataViewDTO(DataView dataView) {
        DataViewDTO dto = new DataViewDTO(
                dataView.getId(), dataView.getDisplayName(), dataView.getType()
        );
        dto.setFilter(dataView.getFilter());
        dto.setDataSource(dataView.getDataSource());

        List<ColumnDTO> columnDTOs = new ArrayList<ColumnDTO>();
        for (Column column : dataView.getColumns()) {
            columnDTOs.add(new ColumnDTO(column.getName(), column.getType()));
        }
        dto.setDataSource(dataView.getDataSource());
        dto.setColumns(columnDTOs.toArray(new ColumnDTO[columnDTOs.size()]));
        if (dataView.getWidgets() != null) {
            List<WidgetDTO> widgetDTOs = new ArrayList<WidgetDTO>();
            for (Widget widget : dataView.getWidgets()) {
                widgetDTOs.add(new WidgetDTO(widget.getId(), widget.getTitle(), widget.getConfig()));
            }
            dto.setWidgets(widgetDTOs.toArray(new WidgetDTO[widgetDTOs.size()]));
        }
        return dto;
    }

    /**
     * Creates a timestamp from a given date string.
     *
     * @param date date in a string format. e.g 01/09/1985
     * @return long value
     */
    public static long timestampFrom(String date) {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date dateObj = null;
        try {
            dateObj = dateFormat.parse(date);
        } catch (ParseException e) {
            logger.error(e.getMessage(), e);
        }
        return dateObj.getTime();
    }

    /**
     * Returns a TableDTO object from  a Table.
     * TableDTO is then transformed to JSON using Gson.
     *
     * @param table dashboard object that comes from backend.
     * @return TableDTO
     */
    public static TableDTO toTableDTO(Table table) {
        TableDTO dto = new TableDTO();
        dto.setName(table.getName());
        if (table.getRows() != null && table.getRows().length > 0) {
            int j = 0;
            Object[] data = new Object[table.getRows().length];
            for (Row row : table.getRows()) {
                int i = 0;
                Object[] cells = new Object[row.getCells().length];
                for (Cell cell : row.getCells()) {
                    cells[i++] = cell.getValue();
                }
                data[j++] = cells;
            }
            dto.setData(data);
        }
        return dto;
    }

    /**
     * Returns a DashboardDTO object from  a Dashboard.
     * DashboardDTO is then transformed to JSON using Gson.
     *
     * @param dashboard object that comes from backend.
     * @return DashboardDTO
     */
    public static DashboardDTO toDashboardDTO(Dashboard dashboard) {
        DashboardDTO dto = new DashboardDTO(dashboard.getId(), dashboard.getTitle(), dashboard.getGroup());
        if (dashboard.getWidgets() != null) {
            List<WidgetInstanceDTO> widgets = new ArrayList<WidgetInstanceDTO>();
            for (WidgetMetaData meta : dashboard.getWidgets()) {
                WidgetInstanceDTO widget = new WidgetInstanceDTO(meta.getId());
                widget.setDimensions(new DimensionDTO(
                                meta.getDimensions().getRow(), meta.getDimensions().getColumn(),
                                meta.getDimensions().getWidth(), meta.getDimensions().getHeight())
                );
                widgets.add(widget);
            }
            dto.setWidgets(widgets.toArray(new WidgetInstanceDTO[widgets.size()]));
        } else {
            dto.setWidgets(null);
        }
        return dto;
    }

    public static WidgetAndDataViewDTO toWidgetAndDVDTO(DataView dataView) {
        Widget widget = dataView.getWidgets()[0];
        if (widget != null) {
            WidgetAndDataViewDTO dto = new WidgetAndDataViewDTO();
            dto.setId(dataView.getId());
            dto.setName(dataView.getDisplayName());
            dto.setType(dataView.getType());
            dto.setDatasource(dataView.getDataSource());
            dto.setFilter(dataView.getFilter());

            WidgetDTO widgetDTO = new WidgetDTO(widget.getId(), widget.getTitle(), widget.getConfig());
            dto.setWidget(widgetDTO);
            if (dataView.getColumns() != null && dataView.getColumns().length > 0) {
                for (int i = 0; i < dataView.getColumns().length; i++) {
                    Column column = dataView.getColumns()[i];
                    dto.getColumns().add(new ColumnDTO(column.getName(), column.getType()));
                }
            }
            return dto;
        }
        return null;
    }

    /**
     * Returns a DataView object from DataViewDTO.
     *
     * @param dto object that comes from backend.
     * @return DataView which is parsed to JSON using Gson.
     */
    public static DataView toDataView(DataViewDTO dto) {
        DataView dataView = new DataView();
        dataView.setId(dto.getId());
        dataView.setDisplayName(dto.getName());
        dataView.setType(dto.getType());
        dataView.setDataSource(dto.getDataSource());
        dataView.setFilter(dto.getFilter());

        if (dto.getWidgets() != null && dto.getWidgets().length > 0) {
        }
        if (dto.getColumns() != null && dto.getColumns().length > 0) {
            List<Column> columns = new ArrayList<Column>();
            for (int i = 0; i < dto.getColumns().length; i++) {
                ColumnDTO columnDTO = dto.getColumns()[i];

                Column column = new Column();
                column.setName(columnDTO.getName());
                column.setType(columnDTO.getType());
                columns.add(column);
            }
            dataView.setColumns(columns.toArray(new Column[dto.getColumns().length]));
        }
        return dataView;
    }


}
