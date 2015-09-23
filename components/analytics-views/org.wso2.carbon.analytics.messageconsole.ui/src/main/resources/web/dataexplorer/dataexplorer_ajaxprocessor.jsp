<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.messageconsole.ui.MessageConsoleConnector" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>

<%
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext = (ConfigurationContext) config.getServletContext().
            getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    MessageConsoleConnector connector = new MessageConsoleConnector(configContext, serverURL, cookie);
    String typeParam = request.getParameter("type");
    int type = 0;
    if (typeParam != null && !typeParam.isEmpty()) {
        type = Integer.parseInt(typeParam);
    }

    String tableName = request.getParameter("tableName");
    if (MessageConsoleConnector.TYPE_LIST_TABLE != type) {
        if (tableName == null || tableName.isEmpty()) {
            out.print("{ \"Result\": \"ERROR\", \"Message\": \"Table name param is empty\" }");
        }
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
                jtPageSize = "50";
            }
            int pageSize = Integer.parseInt(jtPageSize);
            String query = request.getParameter("query");
            long from = Long.MIN_VALUE + 1;
            String timeFrom = request.getParameter("timeFrom");
            if (timeFrom != null && !timeFrom.isEmpty()) {
                from = Long.parseLong(timeFrom);
            }
            long to = Long.MAX_VALUE;
            String timeTo = request.getParameter("timeTo");
            if (timeTo != null && !timeTo.isEmpty()) {
                to = Long.parseLong(timeTo);
            }
            String facets = request.getParameter("facets");
            String primary = request.getParameter("primary");
            int resultCountLimit = 100;
            if (request.getParameter("resultCountLimit") != null) {
                resultCountLimit = Integer.parseInt(request.getParameter("resultCountLimit"));
            }
            out.print(connector.getRecords(tableName, from, to, startIndex, pageSize, query, facets, primary, resultCountLimit));
            break;
        }
        case MessageConsoleConnector.TYPE_TABLE_INFO: {
            out.print(connector.getTableInfo(tableName));
            break;
        }
        case MessageConsoleConnector.TYPE_LIST_ARBITRARY_RECORD: {
            String recordId = request.getParameter("_unique_rec_id");
            out.print(connector.getArbitraryFields(tableName, recordId));
            break;
        }
        case MessageConsoleConnector.TYPE_GET_PURGING_TASK_INFO: {
            out.print(connector.getDataPurgingDetails(tableName));
            break;
        }
        case MessageConsoleConnector.TYPE_SAVE_PURGING_TASK_INFO: {
            out.print(connector.scheduleDataPurging(tableName, request.getParameter("cron"),
                                                    request.getParameter("retention"),
                                                    Boolean.parseBoolean(request.getParameter("enable"))));
            break;
        }
        case MessageConsoleConnector.TYPE_LIST_TABLE: {
            out.print(connector.getTableList());
            break;
        }
        case MessageConsoleConnector.TYPE_GET_FACET_NAME_LIST: {
            out.print(connector.getFacetColumnNameList(tableName));
            break;
        }
        case MessageConsoleConnector.TYPE_GET_FACET_CATEGORIES: {
            out.print(connector.getFacetCategoryList(tableName,
                                                     request.getParameter("fieldName"),
                                                     request.getParameter("categoryPath")));
            break;
        }
        case MessageConsoleConnector.TYPE_GET_PRIMARY_KEY_LIST: {
            out.print(connector.getPrimaryKeys(tableName));
            break;
        }
        case MessageConsoleConnector.TYPE_CHECK_TOTAL_COUNT_SUPPORT: {
            out.print(connector.isRecordCountSupported(tableName));
            break;
        }

    }

    // Preventing page getting cache
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
    response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
    response.setDateHeader("Expires", 0); // Proxies.

%>
