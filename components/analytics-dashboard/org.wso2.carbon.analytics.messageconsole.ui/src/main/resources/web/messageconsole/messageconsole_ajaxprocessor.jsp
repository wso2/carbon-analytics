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
        case MessageConsoleConnector.TYPE_LIST_RECORD: {
            String jtStartIndex = request.getParameter("jtStartIndex");
            if (jtStartIndex == null || jtStartIndex.isEmpty()) {
                jtStartIndex = "0";
            }
            int startIndex = Integer.parseInt(jtStartIndex);
            String jtPageSize = request.getParameter("jtPageSize");
            if (jtPageSize == null || jtPageSize.isEmpty()) {
                jtPageSize = "500";
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
        }
        case MessageConsoleConnector.TYPE_UPDATE_RECORD: {
            Map<String, String[]> parameters = request.getParameterMap();
            Properties properties = new Properties(parameters).invoke(UPDATE_RECORD_ACTION);
            String[] columns = properties.getColumns();
            String[] values = properties.getValues();

            String recordID = request.getParameter(MessageConsoleConnector.RECORD_ID);
            out.print(connector.updateRecord(tableName, columns, values, recordID));
            break;
        }
        case MessageConsoleConnector.TYPE_CREATE_RECORD: {
            Map<String, String[]> parameters = request.getParameterMap();
            Properties properties = new Properties(parameters).invoke(CREATE_RECORD_ACTION);
            String[] columns = properties.getColumns();
            String[] values = properties.getValues();

            out.print(connector.addRecord(tableName, columns, values));
            break;
        }
        case MessageConsoleConnector.TYPE_DELETE_RECORD: {
            String recordsIdString = request.getParameter(MessageConsoleConnector.RECORD_ID);
            String[] recordsIds = new String[]{recordsIdString};
            out.print(connector.deleteRecords(tableName, recordsIds));
            break;
        }
        case MessageConsoleConnector.TYPE_TABLE_INFO: {
            out.print(connector.getTableInfo(tableName));
            break;
        }
        case MessageConsoleConnector.TYPE_LIST_ARBITRARY_RECORD: {
            String recordId = request.getParameter("bam_unique_rec_id");
            out.print(connector.getArbitraryFields(tableName, recordId));
            break;
        }
        case MessageConsoleConnector.TYPE_CRATE_ARBITRARY_RECORD: {
            String recordId = request.getParameter("bam_unique_rec_id");
            String fieldName = request.getParameter("Name");
            String fieldValue = request.getParameter("Value");
            String fieldType = request.getParameter("Type");
            out.print(connector.putArbitraryField(tableName, recordId, fieldName, fieldValue, fieldType));
            break;
        }
        case MessageConsoleConnector.TYPE_UPDATE_ARBITRARY_RECORD: {
            String recordId = request.getParameter("bam_unique_rec_id");
            String fieldName = request.getParameter("Name");
            String fieldValue = request.getParameter("Value");
            String fieldType = request.getParameter("Type");
            out.print(connector.putArbitraryField(tableName, recordId, fieldName, fieldValue, fieldType));
            break;
        }
        case MessageConsoleConnector.TYPE_DELETE_ARBITRARY_RECORD: {
            String recordId = request.getParameter("bam_unique_rec_id");
            String fieldName = request.getParameter("Name");
            out.print(connector.deleteArbitraryField(tableName, recordId, fieldName));
            break;
        }
        case MessageConsoleConnector.TYPE_CREATE_TABLE: {
            String tableInfo = request.getParameter("tableInfo");
            String action = request.getParameter("action");
            if ("add".equals(action)) {
                out.print(connector.createTable(tableName, tableInfo));
            } else if ("edit".equals(action)) {
                out.print(connector.editTable(tableName, tableInfo));
            }
            break;
        }
        case MessageConsoleConnector.TYPE_DELETE_TABLE: {
            out.print(connector.deleteTable(tableName));
            break;
        }
        case MessageConsoleConnector.TYPE_GET_TABLE_INFO: {
            out.print(connector.getTableInfoWithIndexInfo(tableName));
            break;
        }
    }

%><%! public static final int UPDATE_RECORD_ACTION = 2;
    public static final int CREATE_RECORD_ACTION = 1;

    private class Properties {
        private Map<String, String[]> parameters;
        private String[] columns;
        private String[] values;

        public Properties(Map<String, String[]> parameters) {
            this.parameters = parameters;
        }

        public String[] getColumns() {
            return columns;
        }

        public String[] getValues() {
            return values;
        }

        public Properties invoke(int action) {
            if (CREATE_RECORD_ACTION == action) {
                columns = new String[parameters.size() - 2];
                values = new String[parameters.size() - 2];
            } else {
                columns = new String[parameters.size() - 3];
                values = new String[parameters.size() - 3];
            }
            int i = 0;
            for (Map.Entry<String, String[]> column : parameters.entrySet()) {
                String columnName = column.getKey();
                if (checkUnwantedFields(columnName) && column.getValue().length >= 1) {
                    columns[i] = column.getKey();
                    values[i] = column.getValue()[0];
                    i++;
                }
            }
            return this;
        }

        private boolean checkUnwantedFields(String columnName) {
            return !"tableName".equals(columnName) && !"type".equals(columnName) &&
                   !MessageConsoleConnector.RECORD_ID.equals(columnName) && !MessageConsoleConnector.TIMESTAMP.equals(columnName);
        }
    }
%>