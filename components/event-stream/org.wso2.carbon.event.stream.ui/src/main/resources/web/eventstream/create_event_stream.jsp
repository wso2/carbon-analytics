<%--
  ~ Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License"); you may not
  ~ use this file except in compliance with the License. You may obtain a copy
  ~ of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software distributed
  ~ under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
  ~ CONDITIONS OF ANY KIND, either express or implied.  See the License for the
  ~ specific language governing permissions and limitations under the License.
  --%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
	prefix="carbon"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<fmt:bundle
	basename="org.wso2.carbon.event.stream.ui.i18n.Resources">

	<carbon:breadcrumb label="add"
		resourceBundle="org.wso2.carbon.event.stream.ui.i18n.Resources"
		topPage="false" request="<%=request%>" />

	<link type="text/css" href="css/eventStream.css" rel="stylesheet" />
	<script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
	<script type="text/javascript" src="../admin/js/cookies.js"></script>
	<script type="text/javascript" src="../admin/js/main.js"></script>
	<script type="text/javascript"
		src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
	<script type="text/javascript"
		src="../yui/build/connection/connection-min.js"></script>
	<script type="text/javascript" src="js/event_stream.js"></script>
	<script type="text/javascript" src="js/create_eventStream_helper.js"></script>
	<script type="text/javascript"
		src="../eventbuilder/js/create_event_builder_helper.js"></script>
	<script type="text/javascript" src="../ajax/js/prototype.js"></script>

	<script type="text/javascript">
		jQuery(document).ready(function() {
			document.getElementById("sourceWorkArea").style.display = "none";
			document.getElementById("designWorkArea").style.display = "inline";
		});
		function changeView(view) {
			var plain = "source";
			if (plain.localeCompare(view) == 0) {
				convertEventStreamInfoDtoToString();
			} else {
				convertStringToEventStreamInfoDto();
			}
		}
	</script>


	<div id="middle">
		<h2>
			<fmt:message key="title.event.stream.create" />
		</h2>

		<div id="custom_dcontainer" style="display: none"></div>
			<div id="designWorkArea">
				<div id="workArea">
	
				<form name="inputForm" action="index.jsp?ordinal=1" method="post"
					id="addEventStreamDesign">
					<table style="width: 100%" id="eventStreamAdd" class="styledLeft">
						<thead>
							<tr>
								<th colspan="2" class="middle-header"><span
									style="float: left; position: relative; margin-top: 2px;">
										<fmt:message key="title.event.stream.details" />
								</span> <a href="#" onclick="changeView('source');" class="icon-link"
									style="background-image: url(images/design-view.gif); font-weight: normal">
										switch to source view</a></th>
							</tr>
						</thead>
						<tbody>
							<tr>
								<td class="formRaw"><%@include
										file="inner_event_stream_ui.jsp"%></td>
							</tr>
							<tr>
								<td class="buttonRow"><input type="button"
									value="<fmt:message key="add.event.stream"/>"
									onclick="addEventStream(document.getElementById('addEventStreamDesign'),'add')" />
								</td>
							</tr>
						</tbody>
					</table>
				</form>
			</div>
		</div>
		
		<div id="sourceWorkArea" style="border-style: none;">
			<div id="workArea">

				<form name="inputForm2" action="index.jsp?ordinal=1" method="post"
					id="addEventStreamSource">
					<table style="width: 100%" id="eventStreamAdd2" class="styledLeft">
						<thead>
							<tr>
								<th colspan="2" class="middle-header"><span
									style="float: left; position: relative; margin-top: 2px;">
										<fmt:message key="title.event.stream.details" />
								</span> <a href="#" onclick="changeView('design');" class="icon-link"
									style="background-image: url(images/source-view.gif); font-weight: normal">
										switch to design view</a></th>
	
							</tr>
						</thead>
						<tbody>
							<tr>
								<td class="formRaw"><%@include
										file="inner_event_stream_source_ui.jsp"%>
								</td>
							</tr>
							<tr>
								<td class="buttonRow"><input type="button"
									value="<fmt:message key="add.event.stream"/>"
									onclick="addEventStreamByString(document.getElementById('addEventStreamSource'))" />
								</td>
							</tr>
						</tbody>
					</table>
				</form>
			</div>
		</div>

	</div>
</fmt:bundle>
