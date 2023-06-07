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
		<td>
			<a href="<c:url value="/list/testdesc/headkey/${item.getHeadKey()}" />"><c:out value="${item.getHeadKey()}" /></a>
		</td>
		<td id="list-testdesc-key">
			<c:if test="${not empty item}">
				<a href="<c:url value="/list/testdesc/key/${item.key}" />"><c:out value="${item.key}" /></a>
			</c:if>
		</td>
		<td id="list-testdesc-values" data-editable-type="textarea">
			<c:if test="${not empty item}">
				<c:out value="${item.value}" />
			</c:if>
		</td>
		<td id="list-testdesc-language">
			<c:if test="${not empty item}">
				<a href="<c:url value="/list/testdesc/lang/${item.languageCode}" />"><c:out value="${item.languageCode}" /></a>
			</c:if>
		</td>
		<td id="list-testdesc-editable">
		</td>
	</tr>
</table>
