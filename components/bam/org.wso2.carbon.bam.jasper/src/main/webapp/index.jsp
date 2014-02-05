<%@ page import="org.wso2.carbon.bam.toolbox.deployer.config.ToolBoxConfigurationManager" %>
<%@ page import="org.wso2.carbon.bam.toolbox.deployer.util.JasperTabDTO" %>
<%@ page import="org.wso2.carbon.bam.toolbox.deployer.util.ToolBoxDTO" %>
<%@ page import="org.wso2.carbon.context.CarbonContext" %>
<%@ page import="java.util.List" %>

<link type="text/css" href="jquery.ui/css/start/jquery-ui-1.8.21.custom.css" rel="stylesheet" />
<link rel="stylesheet" type="text/css" href="css/jasperDashboardApp.css" />
<script type="text/javascript" src="jquery.ui/js/jquery-1.7.2.min.js"></script>
<script type="text/javascript" src="jquery.ui/js/jquery-ui-1.8.21.custom.min.js"></script>
<script type="text/javascript ">
    var iframeId = "iframe0";
    $(function() {
        $("#tabs").tabs();
        $('#tabs').bind('tabsselect', function(event, ui) {

            // Objects available in the function context:
            //ui.tab     // anchor element of the selected (clicked) tab
            //ui.panel   // element, that contains the selected/clicked tab contents
            //ui.index   // zero-based index of the selected (clicked) tab
            iframeId = "iframe"+ui.index;
            document.getElementById(iframeId).contentWindow.location.reload();
            //autoResize(iframeId);

        });
        $(".leftPanelLink:first-child").addClass('ui-state-active');
        $(".leftPanelLink").live("click",function(){
            $(".leftPanelLink").each(function(){
                $(this).removeClass('ui-state-active');
            });
            $(this).addClass('ui-state-active');
        });

    });
    window.setInterval("reloadIFrame();", 30000);
    function reloadIFrame(){
         document.getElementById(iframeId).contentWindow.location.reload();
    }

function autoResize(id){


    var newHeight;

        newHeight=document.getElementById(id).contentWindow.document .body.scrollHeight;


    document.getElementById(id).height= (newHeight) + "px";
    document.getElementById(id).width= '100%';
}

</script>

<div id="header" class="ui-corner-all ui-widget-header">
    <img src="images/bamlogo.png" alt="bam logo">
</div>
<div id="jasperWrapper">
    <%

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        ToolBoxConfigurationManager mgr = ToolBoxConfigurationManager.getInstance();
        List<String> toolBoxNames = mgr.getAllToolBoxNames(tenantId);

    %>

    <div id="leftNav" class="ui-corner-all">
        <%

                    for (String toolBoxName : toolBoxNames) {
                ToolBoxDTO toolBoxDTO = mgr.getToolBox(toolBoxName, tenantId);

                String toolBox = toolBoxDTO.getName();
                        %>
                <a href="#" class="ui-state-default ui-corner-all leftPanelLink"><%= toolBox %></a>

                   <% } %>
    </div>
        <%

            for (String toolBoxName : toolBoxNames) {
                ToolBoxDTO toolBoxDTO = mgr.getToolBox(toolBoxName, tenantId);

                List<JasperTabDTO> tabs = toolBoxDTO.getJasperTabs();

        %>



    <div id="tabs">
        <ul>
            <%
                for (int i = 0; i < tabs.size(); i++) {
                    JasperTabDTO tab = tabs.get(i);
                    String tabName = tab.getTabName();
                    String tabId = "tabs-" + i;
                    String iframeId = "iframe" + i;
    /*                String registryPath = jasperPath + RegistryConstants.PATH_SEPARATOR + jrxml;

                    Resource jrxmlResource = null;
                    try {
                        jrxmlResource = registry.get(registryPath);
                    } catch (RegistryException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }*/

            %>
            <li id="link-<%= tabId %>"><a href="#<%=tabId%>" title="<%= tabName %>"><%= tabName %></a>
            </li>
            <%
                }
            %>
        </ul>
        <%

            String renderJsp = "render.jsp";
            String dataSource = toolBoxDTO.getDataSource();

            for (int i = 0; i < tabs.size(); i++) {
                JasperTabDTO tab = tabs.get(i);
                String jrxml = tab.getJrxmlFileName();
                String renderUrl = renderJsp + "?jrxml=" + jrxml + "&datasource=" + dataSource;
                String iframeId = "iframe" + i;
                String tabId = "tabs-" + i;


        %>
        <div style="clear:both"></div>
        <div id='<%= tabId %>' class="tabContainer">
            <iframe style="border:none;" id="<%= iframeId %>" src='<%= renderUrl %>'
                    onLoad="autoResize('<%= iframeId %>');"></iframe>
        </div>

        <%
                }
            }
        %>

    </div>
</div>
<div id="footer" class="ui-state-default ui-corner-all leftPanelLink">
    © 2008 - 2012 WSO2 Inc. All Rights Reserved.
</div>

