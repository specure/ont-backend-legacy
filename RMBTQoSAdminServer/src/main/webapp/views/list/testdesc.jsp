<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"%>
<%@ include file="../header.jsp"%>

<article class="main-content">
	<section class="list-table">
		<table id="list-table">
			<colgroup>
				<col style="width:5%" />
				<col style="width:10%" />
				<col style="width:15%" />
				<col style="width:60%" />
				<col style="width:5%" />
				<col style="width:5%" />
			</colgroup>
			<thead>
				<tr class="toolbar">
					<td colspan="6">
						<%@include file="../components/listview_toolbar.jsp" %>
					</td>
				</tr>
				
				<tr class="table-header">
					<td>uid</td>
					<td>head key</td>
					<td>key</td>
					<td>value</td>
					<td>language code</td>
					<td></td>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="item" items="${list}" varStatus="cnt">
					<tr <c:if test="${cnt.count % 2 == 0}">class="even"</c:if>>
						<td>
							<c:out value="${item.uid}" />
						</td>
						<td>
							<a href="<c:url value="/list/testdesc/headkey/${item.getHeadKey()}" />"><c:out value="${item.getHeadKey()}" /></a>
						</td>
						<td id="list-testdesc-key">
							<a href="<c:url value="/list/testdesc/key/${item.key}" />"><c:out value="${item.key}" /></a>
						</td>
						<td id="list-testdesc-values" data-editable-type="textarea">
							<c:out value="${item.value}" />
						</td>
						<td id="list-testdesc-language">
							<a href="<c:url value="/list/testdesc/lang/${item.languageCode}" />"><c:out value="${item.languageCode}" /></a>
						</td>
						<td id="list-testdesc-editable">
						</td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</section>
</article>

<script>
$(function() {	
	OBJECT_TYPE_INIT_FUNCTIONS["testdesc"] = function (trElement, triggerEdit) {
		var elementId =  trElement.find("td:first").text().trim();
		
		trElement.editableCollection({
				id: elementId,
				tableName: TABLE_NAMES["testdesc"],
				editableType: "td",
				editableIndexes: [3,4,5],
				inputFieldNames: ["key","value","languageCode"],
				submitUrl: LINKS.SUBMIT_VLIST + "testdesc/" + elementId + "/testdesc_item",
				deleteUrl: LINKS.DELETE + "testdesc/" + elementId,
				triggerEdit: triggerEdit,
				buttonContainerId: "list-testdesc-editable",
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
					options.submitUrl = LINKS.SUBMIT_VLIST + "testdesc/" + options.id + "/testdesc_item",
					options.deleteUrl = LINKS.DELETE + "testdesc/" + options.id;
				}
		});
	};
	
	$("table#list-table tbody tr").each(function (e, i) {
		OBJECT_TYPE_INIT_FUNCTIONS["testdesc"]($(this), false);
	});
	
	$("tr.toolbar").find("a.button-add").click(function(e) {
		e.preventDefault();
		$.ajax({
			url: LINKS.ADD +"testdesc/testdesc_item",
			type: "GET"
		}).done(function(response) {
			var trElement = $(response).find("tr");
			$("table#list-table tbody").prepend(trElement);
			OBJECT_TYPE_INIT_FUNCTIONS["testdesc"](trElement, true);
		});		
	});
});
</script>

<%@ include file="../footer.jsp"%>