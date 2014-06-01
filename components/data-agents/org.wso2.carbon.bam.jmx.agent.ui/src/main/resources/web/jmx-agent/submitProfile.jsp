<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.bam.jmx.agent.stub.profiles.xsd.Profile" %>
<%@ page import="org.wso2.carbon.bam.jmx.agent.stub.profiles.xsd.MBean" %>
<%@ page import="org.wso2.carbon.bam.jmx.agent.stub.profiles.xsd.MBeanAttribute" %>
<%@ page import="org.wso2.carbon.bam.jmx.agent.stub.profiles.xsd.MBeanAttributeProperty" %>
<%@ page import="org.wso2.carbon.bam.jmx.agent.ui.JmxConnector" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.LinkedList" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Set" %>
<%@ page import="org.wso2.carbon.bam.jmx.agent.stub.JmxAgentProfileDoesNotExistExceptionException" %>
<%@ page import="org.wso2.carbon.bam.jmx.agent.stub.JmxAgentProfileAlreadyExistsExceptionException" %>
<%@ page import="org.wso2.carbon.bam.jmx.agent.stub.JmxAgentJmxProfileExceptionException" %>

<%

    //initialize
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);

    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().
                    getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    JmxConnector connector = new JmxConnector(configContext, serverURL, cookie);

    //create the profile
    Profile profile = new Profile();

    //if this is a profile update request
    if (request.getParameter("newProfile").equalsIgnoreCase("false")) {

        try {
            profile = connector.getProfile(request.getParameter("profileName"));
        } catch (JmxAgentProfileDoesNotExistExceptionException e) {
            return;
        } catch (JmxAgentJmxProfileExceptionException e) {
            return;
        }
        //set Data publisher data
        profile.setDpReceiverAddress(request.getParameter("pubAddress"));
        profile.setDpUserName(request.getParameter("pubUserName"));
        profile.setDpPassword(request.getParameter("pubUserPass"));
        profile.setDpReceiverConnectionType(request.getParameter("pubConnType"));
        profile.setDpSecureUrlConnectionType(request.getParameter("pubSecureConnType"));
        profile.setDpSecureAddress(request.getParameter("pubSecureAddress"));

        //get the cron expression
        if (request.getParameter("presetCronExpr").equalsIgnoreCase("custom")) {
            profile.setCronExpression(request.getParameter("cronExprTxtInput"));
        } else {
            profile.setCronExpression(request.getParameter("presetCronExpr"));
        }

        //increment the profile version
        if(request.getParameter("incrementVersion").equalsIgnoreCase("true")){
            profile.setVersion(profile.getVersion() + 1);
        }

        //set JMX data
        profile.setUserName(request.getParameter("jmxUserName"));
        profile.setPass(request.getParameter("jmxUserPass"));
        profile.setUrl(request.getParameter("jmxServerUrl"));

        // get attribute data
        String mBeanAttrData = request.getParameter("mBeanAttrData");

        // we receive a string like this
        // MBeanName;AttrName;keyname;Alias__-__MBeanName;AttrName;Alias;
        // (    attribute with composite data   )(      normal attribute      )

        mBeanAttrData =  mBeanAttrData.trim();
        String[] mBeanAttrPairs = mBeanAttrData.split("__-__");

        profile.setSelectedMBeans(getMBeanArray(getMBeanMap(mBeanAttrPairs)));

        //update the profile
        try {
            connector.updateProfile(profile);
        } catch (JmxAgentProfileDoesNotExistExceptionException e) {
            out.print(e.getMessage());
        } catch (JmxAgentJmxProfileExceptionException e) {
            out.print(e.getMessage());
        }
    }
    // if this is a profile creation request
    if (request.getParameter("newProfile").equalsIgnoreCase("true")) {

        //get profile name
        profile.setName(request.getParameter("profileName"));

        //set JMX data
        profile.setUserName(request.getParameter("jmxUserName"));
        profile.setPass(request.getParameter("jmxUserPass"));
        profile.setUrl(request.getParameter("jmxServerUrl"));

        //set Data publisher data
        profile.setDpReceiverAddress(request.getParameter("pubAddress"));
        profile.setDpUserName(request.getParameter("pubUserName"));
        profile.setDpPassword(request.getParameter("pubUserPass"));
        profile.setDpReceiverConnectionType(request.getParameter("pubConnType"));
        profile.setDpSecureUrlConnectionType(request.getParameter("pubSecureConnType"));
        profile.setDpSecureAddress(request.getParameter("pubSecureAddress"));

        // get the cron expression
        if (request.getParameter("presetCronExpr").equalsIgnoreCase("custom")) {
            profile.setCronExpression(request.getParameter("cronExprTxtInput"));
        } else {
            profile.setCronExpression(request.getParameter("presetCronExpr"));
        }

        //set the profile version
        profile.setVersion(1);

        //get attribute data
        String mBeanAttrData = request.getParameter("mBeanAttrData");

        // we receive a string like this
        // java.lang:type=Memory;HeapMemoryUsage;committed;java.lang:type=Memory_HeapMemoryUsage_committed - java.lang:type=Memory;HeapMemoryUsage;init;java.lang:type=Memory_HeapMemoryUsage_init - java.lang:type=Memory;HeapMemoryUsage;max;java.lang:type=Memory_HeapMemoryUsage_max - java.lang:type=Memory;HeapMemoryUsage;used;java.lang:type=Memory_HeapMemoryUsage_used - java.lang:type=OperatingSystem;ProcessCpuTime;java.lang:type=OperatingSystem_ProcessCpuTime - java.lang:type=Threading;ThreadCount;java.lang:type=Threading_ThreadCount
        // MBeanName;AttrName;keyname;Alias - MBeanName;AttrName;Alias;
        // (    attribute with composite data   )(      normal attribute      )

        String[] mBeanAttrPairs = mBeanAttrData.split(" - ");

        profile.setSelectedMBeans(getMBeanArray(getMBeanMap(mBeanAttrPairs)));

        //create new profile
        profile.setActive(true); //since profile is automatically activated initially
        try {
            connector.addProfile(profile);
        } catch (JmxAgentProfileAlreadyExistsExceptionException e) {
            return;
        } catch (JmxAgentJmxProfileExceptionException e) {
            return;
        }
    }


%>
<script type="text/javascript">
    //redirect to the index page
    location.href = "../jmx-agent/index.jsp";
</script>

%>

<%--functions--%>
<%!

    private Map<String, Map<String, List<String[]>>> getMBeanMap(String[] mBeanAttrPairs){

        Map<String, Map<String, List<String[]>>> mBeanMap = new HashMap<String, Map<String, List<String[]>>>();

        for (String pair : mBeanAttrPairs) {
            String[] data = pair.split(";");
            //iterate over the data and add it to the map

            // trim white spaces
            for(int i = 0; i < data.length; i++) {
                data[i] = data[i].trim();
            }

            //if the mBean exists
            if (mBeanMap.containsKey(data[0])) {
                //get the attributes
                Map<String, List<String[]>> attr = mBeanMap.get(data[0]);

                // if attribute is available
                if (attr.containsKey(data[1])) {
                    List<String[]> list = attr.get(data[1]);
                    // add new property to existing list
                    list.add(Arrays.copyOfRange(data, 2, data.length));
                    attr.put(data[1], list);
                    mBeanMap.put(data[0], attr);
                } else {
                    // create new property list
                    List<String[]> list = new LinkedList<String[]>();
                    // add property to list
                    list.add(Arrays.copyOfRange(data, 2, data.length));
                    attr.put(data[1], list);
                    mBeanMap.put(data[0], attr);
                }

            } //if the mBean does not exit
            else {
                List<String[]> list = new LinkedList<String[]>();
                list.add(Arrays.copyOfRange(data, 2, data.length));
                Map<String, List<String[]>> prop = new HashMap<String, List<String[]>>();
                prop.put(data[1], list);
                mBeanMap.put(data[0], prop);
            }
        }

        return mBeanMap;
    }

    private MBean[] getMBeanArray(Map<String, Map<String, List<String[]>>> mBeanMap) {

        if (!mBeanMap.isEmpty()) {

            // MBean names set (Eg:- java.lang:type=Memory)
            Set<String> keys = mBeanMap.keySet();

            // Array for MBeans
            MBean[] mBeansArray = new MBean[mBeanMap.size()];
            int mBeanCount = 0;
            for (String mBeanName : keys) {
                MBean mBeanDTO = new MBean();
                mBeanDTO.setMBeanName(mBeanName);

                // Getting attribute map for each MBean
                Map<String, List<String[]>> mBean = mBeanMap.get(mBeanName);

                // Attribute name set (Eg:- NonHeapMemoryUsage, HeapMemoryUsage)
                Set<String> attributes = mBean.keySet();

                MBeanAttribute[] mBeanAttributesArray = new MBeanAttribute[mBean.size()];
                int mBeanAttributeCount = 0;
                for (String attributesName : attributes) {

                    // Getting attribute properties array for each attribute
                    List<String[]> attributeProperties = mBean.get(attributesName);

                    MBeanAttribute mBeanAttribute = new MBeanAttribute();
                    mBeanAttribute.setAttributeName(attributesName);

                    if ((attributeProperties != null) && (!attributeProperties.isEmpty())) {

                        // if MBean is a simple type. So only alias is available for MBean
                        if (attributeProperties.get(0).length == 1) {
                            // Linked list has only one element that has only single array element
                            mBeanAttribute.setAliasName(attributeProperties.get(0)[0]);
                        } else {
                            // This is for composite type. Each property has property name and alias
                            MBeanAttributeProperty[] mBeanAttributePropertiesArray = new MBeanAttributeProperty[attributeProperties.size()];
                            int mBeanAttributePropertyCount = 0;
                            for (String[] attributeProperty : attributeProperties) {
                                if (attributeProperty != null) {
                                    if (attributeProperty.length == 2) {
                                        MBeanAttributeProperty mBeanAttributeProperty = new MBeanAttributeProperty();
                                        mBeanAttributeProperty.setPropertyName(attributeProperty[0]);
                                        mBeanAttributeProperty.setAliasName(attributeProperty[1]);
                                        mBeanAttributePropertiesArray[mBeanAttributePropertyCount++] = mBeanAttributeProperty;
                                    }
                                }
                            }
                            mBeanAttribute.setProperties(mBeanAttributePropertiesArray);
                        }
                    }
                    mBeanAttributesArray[mBeanAttributeCount++] = mBeanAttribute;
                }
                mBeanDTO.setAttributes(mBeanAttributesArray);
                mBeansArray[mBeanCount++] = mBeanDTO;
            }

            return mBeansArray;
        }

        return null;
    }
%>

