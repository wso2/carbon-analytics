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
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.analytics.dashboard.batch.stub.BatchAnalyticsDashboardAdminServiceStub;
import org.wso2.carbon.analytics.dashboard.batch.stub.data.Cell;
import org.wso2.carbon.analytics.dashboard.batch.stub.data.Row;
import org.wso2.carbon.analytics.dashboard.batch.stub.data.Table;
import org.wso2.carbon.analytics.dashboard.stub.DashboardAdminServiceStub;
import org.wso2.carbon.analytics.dashboard.stub.data.*;
import org.wso2.carbon.analytics.dashboard.ui.dto.*;
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

public class DashboardAdminClient {

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

    //TODO rename displayName to name
    public static DataViewDTO toDataViewDTO(DataView dataView) {
        DataViewDTO dto = new DataViewDTO(
                dataView.getId(),dataView.getDisplayName(),dataView.getType()
        );
        dto.setFilter(dataView.getFilter());
        dto.setDataSource(dataView.getDataSource());

        List<ColumnDTO> columnDTOs = new ArrayList<ColumnDTO>();
        for(Column column : dataView.getColumns()) {
            columnDTOs.add(new ColumnDTO(column.getName(),column.getType()));
        }
        dto.setDataSource(dataView.getDataSource());
        dto.setColumns(columnDTOs.toArray(new ColumnDTO[columnDTOs.size()]));
        //TODO get rid of the NPE when theres no widgets element set in DV
        if(dataView.getWidgets() != null) {
            List<WidgetDTO> widgetDTOs = new ArrayList<WidgetDTO>();
            for(Widget widget : dataView.getWidgets()) {
                widgetDTOs.add(new WidgetDTO(widget.getId(),widget.getTitle(),widget.getConfig()));
            }
            dto.setWidgets(widgetDTOs.toArray(new WidgetDTO[widgetDTOs.size()]));
        }

        return dto;
    }

    public static long timestampFrom(String date) {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date dateObj = null;
        try {
            //            dateObj = dateFormat.parse("23/09/2007");
            dateObj = dateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateObj.getTime();
    }

    public static TableDTO toTableDTO(Table table) {
        TableDTO dto = new TableDTO();
        dto.setName(table.getName());
        if(table.getRows() != null && table.getRows().length > 0) {
            int j=0;
            Object[] data = new Object[table.getRows().length];
            for(Row row : table.getRows()) {
                int i =0;
                Object[] cells = new Object[row.getCells().length];
                for(Cell cell : row.getCells()) {
                    cells[i++] = cell.getValue();
                }
                data[j++] = cells;
            }
            dto.setData(data);
        }
        return dto;
    }

    public static DashboardDTO toDashboardDTO(Dashboard dashboard) {
        DashboardDTO dto = new DashboardDTO(dashboard.getId(),dashboard.getTitle(),dashboard.getGroup());
        if(dashboard.getWidgets() != null) {
            List<WidgetInstanceDTO> widgets = new ArrayList<WidgetInstanceDTO>();
           for(WidgetMetaData meta: dashboard.getWidgets()) {
               WidgetInstanceDTO widget = new WidgetInstanceDTO(meta.getId());
               widget.setDimensions(new DimensionDTO(
                       meta.getDimensions().getRow(),meta.getDimensions().getColumn(),
                       meta.getDimensions().getWidth(),meta.getDimensions().getHeight())
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
        if(widget != null) {
            WidgetAndDataViewDTO dto = new WidgetAndDataViewDTO();
            dto.setId(dataView.getId());
            dto.setName(dataView.getDisplayName());
            dto.setType(dataView.getType());
            dto.setDatasource(dataView.getDataSource());
            dto.setFilter(dataView.getFilter());

            WidgetDTO widgetDTO = new WidgetDTO(widget.getId(),widget.getTitle(),widget.getConfig());
            dto.setWidget(widgetDTO);
            if(dataView.getColumns() != null && dataView.getColumns().length > 0) {
                for (int i = 0; i < dataView.getColumns().length; i++) {
                    Column column = dataView.getColumns()[i];
                    dto.getColumns().add(new ColumnDTO(column.getName(),column.getType()));
                }
            }
            return dto;
        }
        return null;
    }

    public static DataView toDataView(DataViewDTO dto) {
        DataView dataView = new DataView();
        dataView.setId(dto.getId());
        dataView.setDisplayName(dto.getName());
        dataView.setType(dto.getType());
        dataView.setDataSource(dto.getDataSource());
        dataView.setFilter(dto.getFilter());

        if(dto.getWidgets() != null && dto.getWidgets().length > 0) {
            //TODO Should we allow users to create DVs with widgets?
        }
        if(dto.getColumns() != null && dto.getColumns().length > 0) {
            List<Column> columns  = new ArrayList<Column>();
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
