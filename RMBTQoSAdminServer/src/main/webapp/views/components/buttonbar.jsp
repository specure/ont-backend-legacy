<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<c:choose>
	<c:when test="${not empty id}">
		<c:set var="itemId" value="${id}" />
	</c:when>
	<c:otherwise>
		<c:set var="itemId" value="0" />
	</c:otherwise>
</c:choose>
<div class="edit-buttons">
	<a href="#" id="${itemId}" class="edit-button button-save"></a>
	<a href="#" id="${itemId}" class="edit-button button-cancel"></a>
</div>
