<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<!--[if lt IE 7]>      <html class="no-js lt-ie9 lt-ie8 lt-ie7"> <![endif]-->
<!--[if IE 7]>         <html class="no-js lt-ie9 lt-ie8"> <![endif]-->
<!--[if IE 8]>         <html class="no-js lt-ie9"> <![endif]-->
<!--[if gt IE 8]><!-->
<html class="no-js">
<!--<![endif]-->
<head>
<meta charset="utf-8" />
<title>RMBT QoS Administration Tool</title>

<link rel="stylesheet" href="<c:url value="/resources/css/normalize.min.css" />">
<link rel="stylesheet" href="<c:url value="/resources/js/vendor/jquery-ui/jquery-ui.min.css" />">
<link rel="stylesheet" href="<c:url value="/resources/css/main.css" />">

<%@ include file="header_definitions.jsp" %>

<script	src="<c:url value="/resources/js/vendor/modernizr-2.6.2-respond-1.1.0.min.js" />"></script>
<script	src="<c:url value="/resources/js/vendor/angular.min.js" />"></script>
<script	src="<c:url value="/resources/js/vendor/ui-utils.min.js" />"></script>
<script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.2.5/angular-route.min.js"></script>
<script	src="<c:url value="/resources/js/vendor/jquery-1.11.1.min.js" />"></script>
<script	src="<c:url value="/resources/js/vendor/jquery-ui/jquery-ui.min.js" />"></script>
<script	src="<c:url value="/resources/js/vendor//jquery.blockui.js" />"></script>

<script	src="<c:url value="/resources/js/functions.js" />"></script>
<script	src="<c:url value="/resources/js/main.js" />"></script>
<script	src="<c:url value="/resources/js/mainctrl.js" />"></script>
<script	src="<c:url value="/resources/js/plugin_editable_item.js" />"></script>
<script	src="<c:url value="/resources/js/plugin_editable_collection.js" />"></script>
</head>
<body>	
	<div class="wrapper" data-ng-app="adminApp" data-ng-controller="MainController">
		
		<%@ include file="navigation.jsp" %>

		<div id="content" class="main-container">
		
				<div class="header-container">
					<header>
						<h4 data-ng-bind="title"></h4>
					</header>		
				</div>