<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"%>
<%@ include file="../header.jsp"%>

<article class="main-content">
	<section class="list-table">
		<table id="list-table">
			<colgroup>
				<col style="width:5%" />
				<col style="width:30%" />
				<col style="width:30%" />
				<col style="width:30%" />
				<col style="width:5%" />
			</colgroup>
			<thead>
				<tr class="toolbar">
					<td colspan="5">
						<%@include file="../components/listview_toolbar.jsp" %>
					</td>
				</tr>
				
				<tr class="table-header">
					<td>uid</td>
					<td>param</td>
					<td>results</td>
					<td>field summary</td>
					<td />
				</tr>
			</thead>
			<tbody>
				<c:forEach var="item" items="${list}" varStatus="cnt">
					<tr <c:if test="${cnt.count % 2 == 0}">class="even"</c:if> >
						<td>
							<c:out value="${item.uid}" />
						</td>
						<td id="list-objective-params" data-editable-type="textarea">
							<c:out value='${item.parameters}' />
						</td>
						<td id ="list-objective-results" data-editable-type="textarea">
							<c:out value="${item.objectives}" />
						</td>
						<td id="list-objective-data" data-editable-type="multiedit">
							<ul>
								<li><i>test:</i> <a href="<c:url value="/list/objective/test/${item.qosTestType}" />"><c:out value="${item.qosTestType.getValue()}" /></a></li>
								<li><i>test_class:</i>  <a href="<c:url value="/list/objective/testclass/${item.testClass}" />"><c:out value="${item.testClass}" /></a></li>
								<li><i>test_server:</i>  <a href="<c:url value="/list/objective/testserver/${item.testServer.uid}" />"><c:out value="${item.testServer.uid}" /></a></li>
								<li><i>concurrency_group:</i> <a href="<c:url value="/list/objective/concurrencygroup/${item.concurrencyGroup}" />"><c:out value="${item.concurrencyGroup}" /></a></li>
								<li><i>test_desc:</i> <a href="<c:url value="/list/testdesc/key/${item.testDescriptionKey}" />"><c:out value="${item.testDescriptionKey}" /></a></li>
								<li><i>test_summary:</i> <a href="<c:url value="/list/testdesc/key/${item.testSummaryKey}" />"><c:out value="${item.testSummaryKey}" /></a></li>
							</ul>
						</td>
						<td id="list-objective-editable">
						</td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</section>
</article>

<script>
$(function() {
	OBJECT_TYPE_INIT_FUNCTIONS["objective"] = function (trElement, triggerEdit) {
		var elementId =  trElement.find("td:first").text().trim();
		
		trElement.editableCollection({
				id: elementId,
				tableName: TABLE_NAMES["objective"],
				editableType: "td",
				editableIndexes: [2,3,4],
				inputFieldNames: ["parameters","objectives", LINKS.EDIT + "objective/" + elementId + "/multiedit"],
				fieldParserFunc: {
					2: matchObjectiveParams,
					3: matchObjectiveResults
				},
				submitUrl: LINKS.SUBMIT_VLIST + "objective/" + elementId + "/objective_item",
				deleteUrl: LINKS.DELETE + "objective/" + elementId,
				triggerEdit: triggerEdit,
				buttonContainerId: "list-objective-editable",
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
					options.deleteUrl = LINKS.DELETE + "objective/" + options.id;
					options.submitUrl = LINKS.SUBMIT_VLIST + "objective/" + options.id + "/objective_item",
					options.inputFieldNames = ["parameters","objectives", LINKS.EDIT + "objective/" + options.id + "/multiedit"];
				}
		});
	};
		
	$("table#list-table tbody tr").each(function(i) {
		OBJECT_TYPE_INIT_FUNCTIONS["objective"]($(this), false);
	});
	
	$("tr.toolbar").find("a.button-add").click(function(e) {
		e.preventDefault();
		$.ajax({
			url: LINKS.ADD +"objective/objective_item",
			type: "GET"
		}).done(function(response) {
			var trElement = $(response).find("tr");
			$("table#list-table tbody").prepend(trElement);
			OBJECT_TYPE_INIT_FUNCTIONS["objective"](trElement, true);
		});		
	});
});
</script>

<%@ include file="../footer.jsp"%>