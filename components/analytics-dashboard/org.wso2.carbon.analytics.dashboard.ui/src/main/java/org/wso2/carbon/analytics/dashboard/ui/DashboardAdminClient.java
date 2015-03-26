package org.wso2.carbon.analytics.dashboard.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.analytics.dashboard.stub.DashboardAdminServiceStub;
import org.wso2.carbon.analytics.dashboard.stub.data.*;
import org.wso2.carbon.analytics.dashboard.ui.dto.ColumnDTO;
import org.wso2.carbon.analytics.dashboard.ui.dto.DataViewDTO;
import org.wso2.carbon.analytics.dashboard.ui.dto.TableDTO;
import org.wso2.carbon.analytics.dashboard.ui.dto.WidgetDTO;
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




}
