<!--
 ~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ WSO2 Inc. licenses this file to you under the Apache License,
 ~ Version 2.0 (the "License"); you may not use this file except
 ~ in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~    http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied.  See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
 -->
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
           prefix="carbon" %>
<fmt:bundle
        basename="org.wso2.carbon.dashboard.mgt.users.ui.resource.i18n.Resources">
    <div id="middle">
        <h2><fmt:message key="home.page.title"/></h2><br/><br/>
        <div id="features">
            <table cellspacing="0">
                <tr class="feature feature-top">
                    <td>
                        <img src="images/enterprise-class.png"/>
                    </td>
                    <td>
                        <h3>Enterprise Information in a Portal</h3><br/>
                        <p>The customizable dashboard allows users to organise content
                            according to their preference. Tabs can be used to keep related
                            gadgets together. </p>
                    </td>
                </tr>
                <tr class="feature">
                    <td>
                        <img src="images/easy-user.png"/>
                    </td>
                    <td>
                        <h3>Gadget Repository (a.k.a. Enterprise App Store)</h3><br/>
                        <p>Gadget Repository will list the gadgets currently logged in
                            user can have access to. Clicking 'Add' is all a user has
                            to do to get it into their dashboard. Users can also add their
                            favorite Google Gadgets from external repositories as well. </p>
                    </td>
                </tr>
                <tr class="feature">
                    <td>
                       <img src="images/author-gadgets-small.png"/>
                    </td>
                    <td>
                        <h3>Authoring Gadgets</h3><br/>
                        <p>Gadget are written using HTML, XML & Javascript. We do support
                            embedding 3<sup>rd</sup> party libraries, Flash or other embeddable
                            technologies for the creative nerds. </p>
                    </td>
                </tr>
                <tr class="feature">
                    <td>
                       <img src="images/client-side.png"/>
                    </td>
                    <td>
                        <h3>Accessible Anywhere</h3><br/>
                        <p>Once you have a personalized dashboard, you can access
                            it from anywhere. All you need is a connection to the
                            internet & a web browser.</p>
                    </td>
                </tr>
            </table>
        </div>
    </div>

</fmt:bundle>