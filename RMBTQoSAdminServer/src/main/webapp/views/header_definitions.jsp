<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<c:url var="listUrl" value="/list" />
<c:url var="reportUrl" value="/report" />
<c:url var="submitUrl" value="/submit" />
<c:url var="uploadUrl" value="/upload" />

<script>
	/////////////////////////////////////////
	//global definitions:
	///////////////////////////////////////
	LINKS = {
			RESOURCES : '<c:url value="/resources/" />',
			REST : '<c:url value="/rest/" />',
			TESTDESC_LIST : '<c:url value="/list/testdesc/key/" />',
			LIST : '<c:url value="/list/" />',
			EDIT : '<c:url value="/edit/get/" />',
			ADD : '<c:url value="/edit/add/" />',
			SUBMIT : '<c:url value="/submit/item/" />',
			SUBMIT_LIST : '<c:url value="/submit/list/" />',
			SUBMIT_VLIST : '<c:url value="/submit/vlist/" />',
			DELETE : '<c:url value="/delete/item/" />',
	};
	
	TITLE =  '<c:out value="${title}" />';
</script>
