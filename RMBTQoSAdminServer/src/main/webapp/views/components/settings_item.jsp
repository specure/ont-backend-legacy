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
		<td id="list-settings-key">
			<c:out value='${item.key}' />
		</td>
		<td id ="list-settings-lang">
			<a href="<c:url value="/list/settings/lang/${item.lang}" />"><c:out value="${item.lang}" /></a>
		</td>
		<td id ="list-settings-value">
			<c:out value="${item.value}" />
		</td>
		<td id="list-settings-editable">
		</td>
	</tr>
</table>
