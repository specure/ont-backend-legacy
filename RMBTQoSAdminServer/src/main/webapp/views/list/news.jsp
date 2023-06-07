<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"%>
<%@ include file="../header.jsp"%>

<script type="text/javascript">
	var PAGE_TABLE = "news";
</script>

<article class="main-content">
	<section class="list-table">
		<table id="list-table">
			<colgroup>
				<col style="width:5%" />
				<col style="width:10%" />
				<col style="width:10%" />
				<col style="width:10%" />
				<col style="width:10%" />
				<col style="width:10%" />
				<col style="width:5%" />
				<col style="width:5%" />
				<col style="width:10%" />
				<col style="width:5%" />
				<col style="width:5%" />
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
					<td>time</td>
					<td>title_en</td>
					<td>title_de</td>
					<td>text_en</td>
					<td>text_de</td>
					<td>active</td>
					<td>force</td>
					<td>plattform</td>
					<td>max ver. code</td>
					<td>min ver. code</td>
					<td>uuid</td>
					<td />
				</tr>
			</thead>
			<tbody>
				<c:forEach var="item" items="${list}" varStatus="cnt">
					<tr <c:if test="${cnt.count % 2 == 0}">class="even"</c:if> >
						<td>
							<c:out value="${item.uid}" />
						</td>
						<td id="list-news-time">
							<c:out value='${item.time}' />
						</td>
						<td id="list-news-titleEn">
							<c:out value='${item.titleEn}' />
						</td>
						<td id="list-news-titleDe">
							<c:out value='${item.titleDe}' />
						</td>
						<td id="list-news-textEn" data-editable-type="textarea" data-editable-rows="7">
							<c:out value='${item.textEn}' />
						</td>
						<td id="list-news-textDe" data-editable-type="textarea" data-editable-rows="7">
							<c:out value='${item.textDe}' />
						</td>
						<td id="list-news-active" data-editable-type="checkbox">
							<c:out value='${item.active}' />
						</td>
						<td id="list-news-force" data-editable-type="checkbox">
							<c:out value='${item.force}' />
						</td>
						<td id="list-news-plattform">
							<c:out value='${item.plattform}' />
						</td>
						<td id="list-news-maxSoftwareVersionCode">
							<c:out value='${item.maxSoftwareVersionCode}' />
						</td>
						<td id="list-news-minSoftwareVersionCode">
							<c:out value='${item.minSoftwareVersionCode}' />
						</td>
						<td id="list-news-uuid">
							<c:out value='${item.uuid}' />
						</td>
						<td id="list-news-editable">
						</td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</section>
</article>

<script>
$(function() {
	OBJECT_TYPE_INIT_FUNCTIONS[PAGE_TABLE] = function (trElement, triggerEdit) {
		var elementId =  trElement.find("td:first").text().trim();
		
		trElement.editableCollection({
				id: elementId,
				tableName: TABLE_NAMES[PAGE_TABLE],
				editableType: "td",
				textareaIndexes: [5,6],
				editableIndexes: [2,3,4,5,6,7,8,9,10,11,12],
				inputFieldNames: ["time", "titleEn", "titleDe", "textEn", "textDe", "active", "force", "plattform", "maxSoftwareVersionCode", "minSoftwareVersionCode", "uuid"],
				submitUrl: LINKS.SUBMIT_VLIST + PAGE_TABLE + "/" + elementId + "/" + PAGE_TABLE + "_item",
				deleteUrl: LINKS.DELETE + PAGE_TABLE + "/" + elementId,
				triggerEdit: triggerEdit,
				buttonContainerId: "list-" + PAGE_TABLE + "-editable",
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
					options.submitUrl = LINKS.SUBMIT_VLIST + PAGE_TABLE + "/" + options.id + "/" + PAGE_TABLE + "_item",
					options.deleteUrl = LINKS.DELETE + PAGE_TABLE + "/" + options.id;
				}
		});
	};
	
	$("table#list-table tbody tr").each(function (e, i) {
		OBJECT_TYPE_INIT_FUNCTIONS[PAGE_TABLE]($(this), false);
	});
	
	$("tr.toolbar").find("a.button-add").click(function(e) {
		e.preventDefault();
		$.ajax({
			url: LINKS.ADD + PAGE_TABLE + "/" + PAGE_TABLE + "_item",
			type: "GET"
		}).done(function(response) {
			var trElement = $(response).find("tr");
			$("table#list-table tbody").prepend(trElement);
			OBJECT_TYPE_INIT_FUNCTIONS[PAGE_TABLE](trElement, true);
		});		
	});
});
</script>

<%@ include file="../footer.jsp"%>