<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<div id="content-aside">
	<div id="navigation">
		<header>
			<h2>Navigation</h2>
		</header>

		<nav id="main-nav">
			<div class="backlink">
				<h4>Tables:</h4>
				<ul>
					<li><a class="menu" id="menu_testserver_list" href="${listUrl}/testserver">test_server</a></li>
					<li><a class="menu" id="menu_settings_list" href="${listUrl}/settings">settings</a></li>
					<li><a class="menu" id="menu_news_list" href="${listUrl}/news">news</a></li>
					<li><a class="menu" id="menu_objective_list" href="${listUrl}/objective">qos_test_objectives</a></li>
					<li><a class="menu" id="menu_testdesc_list" href="${listUrl}/testdesc">qos_test_desc</a></li>
					<li><a class="menu" id="menu_providers" href="${listUrl}/ngview#providers" data-view="" data-ng-click="setTitle('provider');">provider</a></li>
					<li><a class="menu" id="menu_as2providers" href="${listUrl}/ngview#as2providers" data-view="" data-ng-click="setTitle('as2provider');">as2provider</a></li>
					<li><a class="menu" id="menu_mccmnc2providers" href="${listUrl}/ngview#mccmnc2providers" data-view="" data-ng-click="setTitle('mccmnc2provider');">mccmnc2provider</a></li>
					<li><a class="menu" id="menu_advertised_speed_option" href="${listUrl}/ngview#advertised_speed_option" data-view="" data-ng-click="setTitle('advertised_speed_option');">advertised_speed_option</a></li>
				</ul>
				
				<h4>Services:</h4>
				<ul>
					<li><a class="menu" id="menu_fileupload" href="${uploadUrl}">File upload</a></li>
				</ul>
				
				<h4>Reports:</h4>
				<ul>
					<li><a class="menu" id="menu_objective_resport_list" href="${reportUrl}/list/objective">Report: qos_test_objectives</a></li>
				</ul>				
			</div>
		</nav>
		
	</div>
	
</div>