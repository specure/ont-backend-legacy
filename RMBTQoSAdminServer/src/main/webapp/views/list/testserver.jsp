<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"%>
<%@ include file="../header.jsp"%>

<article class="main-content">
	<section class="list-table">
		<table id="list-table">
			<colgroup>
				<col style="width:5%" />
				<col style="width:10%" />
				<col style="width:10%" />
				<col style="width:5%" />
				<col style="width:5%" />
				<col style="width:10%" />
				<col style="width:10%" />
				<col style="width:5%" />
				<col style="width:5%" />
				<col style="width:10%" />
				<col style="width:10%" />
				<col style="width:10%" />
				<col style="width:5%" />
			</colgroup>
			<thead>
				<tr class="toolbar">
					<td colspan="13">
						<%@include file="../components/listview_toolbar.jsp" %>
					</td>
				</tr>
				
				<tr class="table-header">
					<td>uid</td>
					<td>name</td>
					<td>web address</td>
					<td>port</td>
  					<td>port_ssl</td>
  					<td>city</td>
  					<td>country</td>
  					<td>geo_lat</td>
  					<td>geo_long</td>
  					<td>location</td>
  					<td>web_address_ipv4</td>
  					<td>web_address_ipv6</td>
					<td />
				</tr>
			</thead>
			<tbody>
				<c:forEach var="item" items="${list}" varStatus="cnt">
					<tr <c:if test="${cnt.count % 2 == 0}">class="even"</c:if> >
						<td>
							<c:out value="${item.uid}" />
						</td>
						<td id="list-testserver-name">
							<c:out value='${item.name}' />
						</td>
						<td id ="list-testserver-webAddress">
							<c:out value="${item.webAddress}" />
						</td>
						<td id ="list-testserver-port">
							<c:out value="${item.port}" />
						</td>
						<td id ="list-testserver-portSsl">
							<c:out value="${item.portSsl}" />
						</td>
						<td id ="list-testserver-city">
							<a href="<c:url value="/list/testserver/city/${item.city}" />"><c:out value="${item.city}" /></a>
						</td>
						<td id ="list-testserver-country">
							<a href="<c:url value="/list/testserver/country/${item.country}" />"><c:out value="${item.country}" /></a>
						</td>
						<td id ="list-testserver-geoLat">
							<c:out value="${item.geoLat}" />
						</td>
						<td id ="list-testserver-geoLong">
							<c:out value="${item.geoLong}" />
						</td>
						<td id ="list-testserver-location">
							<c:out value="${item.location}" />
						</td>
						<td id ="list-testserver-webAddressIpv4">
							<c:out value="${item.webAddressIpv4}" />
						</td>
						<td id ="list-testserver-webAddressIpv6">
							<c:out value="${item.webAddressIpv6}" />
						</td>
						<td id="list-testserver-editable">
						</td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</section>
</article>

<script>
$(function() {
	OBJECT_TYPE_INIT_FUNCTIONS["testserver"] = function (trElement, triggerEdit) {
		var elementId =  trElement.find("td:first").text().trim();
		
		trElement.editableCollection({
				id: elementId,
				tableName: TABLE_NAMES["testserver"],
				editableType: "td",
				editableIndexes: [2,3,4,5,6,7,8,9,10,11,12],
				inputFieldNames: ["name", "webAddress", "port", "portSsl", "city", "country", "geoLat", "geoLong", "location", "webAddressIpv4", "webAddressIpv6"],
				submitUrl: LINKS.SUBMIT_VLIST + "testserver/" + elementId + "/testserver_item",
				deleteUrl: LINKS.DELETE + "testserver/" + elementId,
				triggerEdit: triggerEdit,
				buttonContainerId: "list-testserver-editable",
				onDeleteDone: function(element) {
                   	element.find('td')
                   		.wrapInner('<div style="display: block;" />')
                   		.parent()
                   		.find('td > div')
                   		.slideUp(350, function(){
                       		var table = trElement.closest("tbody");
                       		element.remove();
                           	recreateOddEvenTableRows(table, "even", "");
                   	});
				},
				onSubmitDone: function(element, options) {
					options.id = element.find("td:first").text().trim();
					options.submitUrl = LINKS.SUBMIT_VLIST + "testserver/" + options.id + "/testserver_item",
					options.deleteUrl = LINKS.DELETE + "testserver/" + options.id;
				}
		});
	};
	
	$("table#list-table tbody tr").each(function (e, i) {
		OBJECT_TYPE_INIT_FUNCTIONS["testserver"]($(this), false);
	});
	
	$("tr.toolbar").find("a.button-add").click(function(e) {
		e.preventDefault();
		$.ajax({
			url: LINKS.ADD +"testserver/testserver_item",
			type: "GET"
		}).done(function(response) {
			var trElement = $(response).find("tr");
			$("table#list-table tbody").prepend(trElement);
			OBJECT_TYPE_INIT_FUNCTIONS["testserver"](trElement, true);
		});		
	});
});
</script>

<%@ include file="../footer.jsp"%>