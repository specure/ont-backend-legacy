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
</table>
