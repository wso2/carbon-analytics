<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<services>
    <service name="Service 01">
        <stats>
            <requestCount><%=(int) (5 + Math.random() * (46))%>
            </requestCount>
            <responseCount><%=(int) (5 + (Math.random() * (Math.random() * 46 + 1)))%>
            </responseCount>
            <faultCount><%= (int) (Math.random() * 46 - (Math.random() * (Math.random() * 46 + 1)))%>
            </faultCount>
            <averageResponseTime><%= (int) (200 + (Math.random() * 501 + (Math.random() * (Math.random() * (501 - Math.random() * 501)) + 1))) %>
            </averageResponseTime>
            <maximumResponseTime><%= (int) (200 + Math.random() * 501 + Math.random() * (501 - Math.random() * 501)) %>
            </maximumResponseTime>
            <minimumResponseTime><%=(int) (200 + Math.random() * 501)%>
            </minimumResponseTime>
        </stats>
        <operations>
            <operation name="getCustomerID">
                <stats>
                    <requestCount><%=(int) (5 + Math.random() * (46)) %>
                    </requestCount>
                    <responseCount><%=(int) (5 + (Math.random() * (Math.random() * 46 + 1)))%>
                    </responseCount>
                    <faultCount><%= (int) (Math.random() * 46 - (Math.random() * (Math.random() * 46 + 1))) %>
                    </faultCount>
                    <averageResponseTime><%=(int) (200 + (Math.random() * 501 + (Math.random() * (Math.random() * (501 - Math.random() * 501)) + 1)))%>
                    </averageResponseTime>
                    <maximumResponseTime><%=(int) (200 + Math.random() * 501 + Math.random() * (501 - Math.random() * 501))%>
                    </maximumResponseTime>
                    <minimumResponseTime><%=(int) (200 + Math.random() * 501)%>
                    </minimumResponseTime>
                </stats>
            </operation>
            <operation name="canCustomerRate">
                <stats>
                    <requestCount><%=(int) (5 + Math.random() * (46))%>
                    </requestCount>
                    <responseCount><%=(int) (5 + (Math.random() * (Math.random() * 46 + 1)))%>
                    </responseCount>
                    <faultCount><%= (int) (Math.random() * 46 - (Math.random() * (Math.random() * 46 + 1)))  %>
                    </faultCount>
                    <averageResponseTime><%=(int) (200 + (Math.random() * 501 + (Math.random() * (Math.random() * (501 - Math.random() * 501)) + 1)))%>
                    </averageResponseTime>
                    <maximumResponseTime><%=(int) (200 + Math.random() * 501 + Math.random() * (501 - Math.random() * 501))%>
                    </maximumResponseTime>
                    <minimumResponseTime><%=(int) (200 + Math.random() * 501)%>
                    </minimumResponseTime>
                </stats>
            </operation>
            <operation name="resetRating">
                <stats>
                    <requestCount><%=(int) (5 + Math.random() * (46)) %>
                    </requestCount>
                    <responseCount><%=(int) (5 + (Math.random() * (Math.random() * 46 + 1)))%>
                    </responseCount>
                    <faultCount><%= (int) (Math.random() * 46 - (Math.random() * (Math.random() * 46 + 1)))%>
                    </faultCount>
                    <averageResponseTime><%=(int) (200 + (Math.random() * 501 + (Math.random() * (Math.random() * (501 - Math.random() * 501)) + 1))) %>
                    </averageResponseTime>
                    <maximumResponseTime><%=(int) (200 + Math.random() * 501 + Math.random() * (501 - Math.random() * 501))%>
                    </maximumResponseTime>
                    <minimumResponseTime><%=(int) (200 + Math.random() * 501)%>
                    </minimumResponseTime>
                </stats>
            </operation>
            <operation name="updateRating">
                <stats>
                    <requestCount><%=(int) (5 + Math.random() * (46))%>
                    </requestCount>
                    <responseCount><%=(int) (5 + (Math.random() * (Math.random() * 46 + 1))) %>
                    </responseCount>
                    <faultCount><%=(int) (Math.random() * 46 - (Math.random() * (Math.random() * 46 + 1)))%>
                    </faultCount>
                    <averageResponseTime><%=(int) (200 + (Math.random() * 501 + (Math.random() * (Math.random() * (501 - Math.random() * 501)) + 1)))%>
                    </averageResponseTime>
                    <maximumResponseTime><%=(int) (200 + Math.random() * 501 + Math.random() * (501 - Math.random() * 501))%>
                    </maximumResponseTime>
                    <minimumResponseTime><%=(int) (200 + Math.random() * 501)%>
                    </minimumResponseTime>
                </stats>
            </operation>
            <operation name="setRating">
                <stats>
                    <requestCount><%=(int) (5 + Math.random() * (46))%>
                    </requestCount>
                    <responseCount><%=(int) (5 + (Math.random() * (Math.random() * 46 + 1)))%>
                    </responseCount>
                    <faultCount><%=(int) (Math.random() * 46 - (Math.random() * (Math.random() * 46 + 1)))%>
                    </faultCount>
                    <averageResponseTime><%=(int) (200 + (Math.random() * 501 + (Math.random() * (Math.random() * (501 - Math.random() * 501)) + 1)))%>
                    </averageResponseTime>
                    <maximumResponseTime><%=(int) (200 + Math.random() * 501 + Math.random() * (501 - Math.random() * 501))%>
                    </maximumResponseTime>
                    <minimumResponseTime><%=(int) (200 + Math.random() * 501)%>
                    </minimumResponseTime>
                </stats>
            </operation>
        </operations>
    </service>
    <service name="Service 02">
        <stats>
            <requestCount><%=(int) (5 + Math.random() * (46)) %>
            </requestCount>
            <responseCount><%=(int) (5 + (Math.random() * (Math.random() * 46 + 1)))%>
            </responseCount>
            <faultCount><%=(int) (Math.random() * 46 - (Math.random() * (Math.random() * 46 + 1)))%>
            </faultCount>
            <averageResponseTime><%=(int) (200 + (Math.random() * 501 + (Math.random() * (Math.random() * (501 - Math.random() * 501)) + 1)))%>
            </averageResponseTime>
            <maximumResponseTime><%=(int) (200 + Math.random() * 501 + Math.random() * (501 - Math.random() * 501))%>
            </maximumResponseTime>
            <minimumResponseTime><%=(int) (200 + Math.random() * 501)%>
            </minimumResponseTime>
        </stats>
        <operations>
            <operation name="getCustomerID">
                <stats>
                    <requestCount><%=(int) (5 + Math.random() * (46)) %>
                    </requestCount>
                    <responseCount><%=(int) (5 + (Math.random() * (Math.random() * 46 + 1)))%>
                    </responseCount>
                    <faultCount><%=(int) (Math.random() * 46 - (Math.random() * (Math.random() * 46 + 1)))%>
                    </faultCount>
                    <averageResponseTime><%= (int) (200 + (Math.random() * 501 + (Math.random() * (Math.random() * (501 - Math.random() * 501)) + 1))) %>
                    </averageResponseTime>
                    <maximumResponseTime><%=(int) (200 + Math.random() * 501 + Math.random() * (501 - Math.random() * 501))%>
                    </maximumResponseTime>
                    <minimumResponseTime><%=(int) (200 + Math.random() * 501)%>
                    </minimumResponseTime>
                </stats>
            </operation>
            <operation name="canCustomerRate">
                <stats>
                    <requestCount><%=(int) (5 + Math.random() * (46))%>
                    </requestCount>
                    <responseCount><%=(int) (5 + (Math.random() * (Math.random() * 46 + 1)))%>
                    </responseCount>
                    <faultCount><%= (int) (Math.random() * 46 - (Math.random() * (Math.random() * 46 + 1)))%>
                    </faultCount>
                    <averageResponseTime><%= (int) (200 + (Math.random() * 501 + (Math.random() * (Math.random() * (501 - Math.random() * 501)) + 1)))%>
                    </averageResponseTime>
                    <maximumResponseTime><%=(int) (200 + Math.random() * 501 + Math.random() * (501 - Math.random() * 501))%>
                    </maximumResponseTime>
                    <minimumResponseTime><%=(int) (200 + Math.random() * 501)%>
                    </minimumResponseTime>
                </stats>
            </operation>
            <operation name="resetRating">
                <stats>
                    <requestCount><%=(int) (5 + Math.random() * (46))%>
                    </requestCount>
                    <responseCount><%=(int) (5 + (Math.random() * (Math.random() * 46 + 1))) %>
                    </responseCount>
                    <faultCount><%=(int) (Math.random() * 46 - (Math.random() * (Math.random() * 46 + 1)))%>
                    </faultCount>
                    <averageResponseTime><%=(int) (200 + (Math.random() * 501 + (Math.random() * (Math.random() * (501 - Math.random() * 501)) + 1)))%>
                    </averageResponseTime>
                    <maximumResponseTime><%=(int) (200 + Math.random() * 501 + Math.random() * (501 - Math.random() * 501))%>
                    </maximumResponseTime>
                    <minimumResponseTime><%=(int) (200 + Math.random() * 501)%>
                    </minimumResponseTime>
                </stats>
            </operation>
            <operation name="updateRating">
                <stats>
                    <requestCount><%=(int) (5 + Math.random() * (46))%>
                    </requestCount>
                    <responseCount><%=(int) (5 + (Math.random() * (Math.random() * 46 + 1))) %>
                    </responseCount>
                    <faultCount><%=(int) (Math.random() * 46 - (Math.random() * (Math.random() * 46 + 1)))%>
                    </faultCount>
                    <averageResponseTime><%=(int) (200 + (Math.random() * 501 + (Math.random() * (Math.random() * (501 - Math.random() * 501)) + 1)))%>
                    </averageResponseTime>
                    <maximumResponseTime><%=(int) (200 + Math.random() * 501 + Math.random() * (501 - Math.random() * 501))%>
                    </maximumResponseTime>
                    <minimumResponseTime><%=(int) (200 + Math.random() * 501)%>
                    </minimumResponseTime>
                </stats>
            </operation>
            <operation name="setRating">
                <stats>
                    <requestCount><%=(int) (5 + Math.random() * (46))%>
                    </requestCount>
                    <responseCount><%=(int) (5 + (Math.random() * (Math.random() * 46 + 1))) %>
                    </responseCount>
                    <faultCount><%= (int) (Math.random() * 46 - (Math.random() * (Math.random() * 46 + 1))) %>
                    </faultCount>
                    <averageResponseTime><%=(int) (200 + (Math.random() * 501 + (Math.random() * (Math.random() * (501 - Math.random() * 501)) + 1))) %>
                    </averageResponseTime>
                    <maximumResponseTime><%=(int) (200 + Math.random() * 501 + Math.random() * (501 - Math.random() * 501))%>
                    </maximumResponseTime>
                    <minimumResponseTime><%=(int) (200 + Math.random() * 501)%>
                    </minimumResponseTime>
                </stats>
            </operation>
        </operations>
    </service>
    <service name="Service 03">
        <stats>
            <requestCount><%=(int) (5 + Math.random() * (46)) %>
            </requestCount>
            <responseCount><%=(int) (5 + (Math.random() * (Math.random() * 46 + 1))) %>
            </responseCount>
            <faultCount><%= (int) (Math.random() * 46 - (Math.random() * (Math.random() * 46 + 1)))%>
            </faultCount>
            <averageResponseTime><%= (int) (200 + (Math.random() * 501 + (Math.random() * (Math.random() * (501 - Math.random() * 501)) + 1)))%>
            </averageResponseTime>
            <maximumResponseTime><%=(int) (200 + Math.random() * 501 + Math.random() * (501 - Math.random() * 501))%>
            </maximumResponseTime>
            <minimumResponseTime><%=(int) (200 + Math.random() * 501)%>
            </minimumResponseTime>
        </stats>
        <operations>
            <operation name="findDNSServers">
                <stats>
                    <requestCount><%=(int) (5 + Math.random() * (46)) %>
                    </requestCount>
                    <responseCount><%=(int) (5 + (Math.random() * (Math.random() * 46 + 1))) %>
                    </responseCount>
                    <faultCount><%= (int) (Math.random() * 46 - (Math.random() * (Math.random() * 46 + 1))) %>
                    </faultCount>
                    <averageResponseTime><%=(int) (200 + (Math.random() * 501 + (Math.random() * (Math.random() * (501 - Math.random() * 501)) + 1)))%>
                    </averageResponseTime>
                    <maximumResponseTime><%=(int) (200 + Math.random() * 501 + Math.random() * (501 - Math.random() * 501))%>
                    </maximumResponseTime>
                    <minimumResponseTime><%=(int) (200 + Math.random() * 501)%>
                    </minimumResponseTime>
                </stats>
            </operation>
            <operation name="resetDNSServer">
                <stats>
                    <requestCount><%=(int) (5 + Math.random() * (46))%>
                    </requestCount>
                    <responseCount><%=(int) (5 + (Math.random() * (Math.random() * 46 + 1)))%>
                    </responseCount>
                    <faultCount><%=(int) (Math.random() * 46 - (Math.random() * (Math.random() * 46 + 1))) %>
                    </faultCount>
                    <averageResponseTime><%= (int) (200 + (Math.random() * 501 + (Math.random() * (Math.random() * (501 - Math.random() * 501)) + 1)))%>
                    </averageResponseTime>
                    <maximumResponseTime><%=(int) (200 + Math.random() * 501 + Math.random() * (501 - Math.random() * 501))%>
                    </maximumResponseTime>
                    <minimumResponseTime><%=(int) (200 + Math.random() * 501)%>
                    </minimumResponseTime>
                </stats>
            </operation>
            <operation name="startDNSServer">
                <stats>
                    <requestCount><%=(int) (5 + Math.random() * (46))%>
                    </requestCount>
                    <responseCount><%=(int) (5 + (Math.random() * (Math.random() * 46 + 1)))%>
                    </responseCount>
                    <faultCount><%= (int) (Math.random() * 46 - (Math.random() * (Math.random() * 46 + 1))) %>
                    </faultCount>
                    <averageResponseTime><%=(int) (200 + (Math.random() * 501 + (Math.random() * (Math.random() * (501 - Math.random() * 501)) + 1)))%>
                    </averageResponseTime>
                    <maximumResponseTime><%=(int) (200 + Math.random() * 501 + Math.random() * (501 - Math.random() * 501))%>
                    </maximumResponseTime>
                    <minimumResponseTime><%=(int) (200 + Math.random() * 501)%>
                    </minimumResponseTime>
                </stats>
            </operation>
            <operation name="stopDNSServer">
                <stats>
                    <requestCount><%=(int) (5 + Math.random() * (46))%>
                    </requestCount>
                    <responseCount><%=(int) (5 + (Math.random() * (Math.random() * 46 + 1)))%>
                    </responseCount>
                    <faultCount><%=(int) (Math.random() * 46 - (Math.random() * (Math.random() * 46 + 1)))%>
                    </faultCount>
                    <averageResponseTime><%=(int) (200 + (Math.random() * 501 + (Math.random() * (Math.random() * (501 - Math.random() * 501)) + 1)))%>
                    </averageResponseTime>
                    <maximumResponseTime><%=(int) (200 + Math.random() * 501 + Math.random() * (501 - Math.random() * 501))%>
                    </maximumResponseTime>
                    <minimumResponseTime><%=(int) (200 + Math.random() * 501)%>
                    </minimumResponseTime>
                </stats>
            </operation>
        </operations>
    </service>
</services>

