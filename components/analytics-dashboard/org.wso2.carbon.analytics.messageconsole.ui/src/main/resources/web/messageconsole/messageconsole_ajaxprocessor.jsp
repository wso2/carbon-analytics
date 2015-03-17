<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.messageconsole.ui.MessageConsoleConnector" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.Map" %>

<%
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext = (ConfigurationContext) config.getServletContext().
            getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    MessageConsoleConnector connector = new MessageConsoleConnector(configContext, serverURL, cookie);

    String tableName = request.getParameter("tableName");

    if (tableName == null || tableName.isEmpty()) {
        out.print("{ \"Result\": \"ERROR\", \"Message\": \"Table name param is empty\" }");
    }
    String typeParam = request.getParameter("type");
    int type = 0;
    if (typeParam != null && !typeParam.isEmpty()) {
        type = Integer.parseInt(typeParam);
    }

    switch (type) {
        case MessageConsoleConnector.TYPE_LIST_RECORD:
            String jtStartIndex = request.getParameter("jtStartIndex");
            if (jtStartIndex == null || jtStartIndex.isEmpty()) {
                jtStartIndex = "0";
            }
            int startIndex = Integer.parseInt(jtStartIndex);
            String jtPageSize = request.getParameter("jtPageSize");
            if (jtPageSize == null || jtPageSize.isEmpty()) {
                jtPageSize = "25";
            }
            int pageSize = Integer.parseInt(jtPageSize);
            String query = request.getParameter("query");
            long from = 0;
            String timeFrom = request.getParameter("timeFrom");
            if (timeFrom != null && !timeFrom.isEmpty()) {
                timeFrom = timeFrom.concat("000");
                from = Long.parseLong(timeFrom);
            }
            long to = Long.MAX_VALUE;
            String timeTo = request.getParameter("timeTo");
            if (timeTo != null && !timeTo.isEmpty()) {
                timeTo = timeTo.concat("000");
                to = Long.parseLong(timeTo);
            }
            out.print(connector.getRecords(tableName, from, to, startIndex, pageSize, query));
            break;
        case MessageConsoleConnector.TYPE_UPDATE_RECORD:
            break;
        case MessageConsoleConnector.TYPE_CREATE_RECORD:
            Map<String, String[]> parameters = request.getParameterMap();
            String[] columns = new String[parameters.size() - 2];
            String[] values = new String[parameters.size() - 2];
            int i = 0;
            for (Map.Entry<String, String[]> column : parameters.entrySet()) {
                if (!"tableName".equals(column.getKey()) && !"type".equals(column.getKey()) && column.getValue().length >= 1) {
                    columns[i] = column.getKey();
                    values[i] = column.getValue()[0];
                    i++;
                }
            }
            out.print(connector.addRecord(tableName, columns, values));
            break;
        case MessageConsoleConnector.TYPE_DELETE_RECORD:
            String recordsIdString = request.getParameter(MessageConsoleConnector.RECORD_ID);
            String[] recordsIds = new String[]{recordsIdString};
            out.print(connector.deleteRecords(tableName, recordsIds));
            break;
        case MessageConsoleConnector.TYPE_TABLE_INFO:
            out.print(connector.getTableInfo(tableName));
            break;
    }
%>