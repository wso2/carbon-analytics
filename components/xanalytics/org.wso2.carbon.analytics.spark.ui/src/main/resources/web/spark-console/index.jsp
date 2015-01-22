<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<!--
~ Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied. See the License for the
~ specific language governing permissions and limitations
~ under the License.
-->

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<fmt:bundle basename="org.wso2.carbon.analytics.spark.ui.i18n.Resources">
    <carbon:breadcrumb label="spark.conosle.menu"
                       resourceBundle="org.wso2.carbon.analytics.spark.ui.i18n.Resources"
                       topPage="true" request="<%=request%>"/>


    <script type="text/javascript">

        $( document ).ready(function() {
          console.log("Shirunthi bae!");
      });

    </script>
    
    <div id="middle">
        <h2>Interactive Spark Console</h2>

        <div id="workArea">
            <p>Some stuff will go there bae!</p> <br/>

            <table class="styledLeft">
            	<thead>
            		<tr><th><fmt:message key="spark.console.title"/></th>
            	    </tr>
            	</thead>
            	<tbody>
	            	<tr>
	            		<td>
	            			<textarea rows="10" style="width:100%"></textarea>
	            		</td>
	            	</tr>

	            	<tr>
	            		<td>
	            			<textarea id="queryPane"rows="10" style="width:100%"></textarea>
	            		</td>
	            	</tr>

	            	<tr>
	            		<td class="buttonRow">
	            			<input type="button" value="Submit" id="btnSubmit" onclick="submitQuery()"/>
	            		</td>
	            	</tr>

	            	
            	</tbody>
           </table>
            

        </div>
    </div>

    <script type="text/javascript">

    	function submitQuery() {
    		console.log("inside button click");
    		jQuery.ajax({

    		    url : '../spark-console/execute_spark_ajaxprocessor.jsp',
    		    type : 'POST',
    		    data : {
    		        'numberOfWords' : 10
    		    },
    		    dataType:'json',
    		    success : function(data) {              
    		        
    		        alert(data.response);
    		        
    		    },
    		    error : function(request,error) {
    		        alert("Request: "+JSON.stringify(request));
    		    }
    		});
    	
    	}	//end of submitQuery

    	

    </script>


</fmt:bundle>
