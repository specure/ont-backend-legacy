<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<div id="edit-form">
	<form:form modelAttribute="editItem" action="${submitUrl}" autocomplete="off">
		<div class="edit-block">
			<form:textarea rows="8" path="${editPath}" />
		</div>
		<%@ include file="buttonbar.jsp" %>
	</form:form>
</div>