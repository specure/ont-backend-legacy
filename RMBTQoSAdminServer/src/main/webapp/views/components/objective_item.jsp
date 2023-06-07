<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<table>
	<tr>
		<td>
			<c:choose>
				<c:when test="${not empty item.uid}"><c:out value="${item.uid}" /></c:when>
				<c:otherwise>0</c:otherwise>
			</c:choose>
		</td>
		<td id="list-objective-params" data-editable-type="textarea" data-editable-rows="7">
			<c:out value='${item.parameters}' />
		</td>
		<td id ="list-objective-results" data-editable-type="textarea" data-editable-rows="7">
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
</table>