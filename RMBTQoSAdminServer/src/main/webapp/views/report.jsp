<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"%>
<%@ include file="header.jsp"%>

<article class="main-content">
	<section class="list-table">
		<div class="box-container">
			<div class="box-content">
				<div class="desc">
					All test servers found in all tests<br>
					Detailed description: <b>test_server</b> used in <b>qos_test_objective</b>
				</div>
				<ul>
					<c:forEach items="${testServerList}" var="item">
						<li>test_class: <a href="<c:url value="/list/objective/testserver/${item[1]}" />">'<c:out value="${item[1]}" />'</a>, count: <c:out value="${item[0]}" /></li>
					</c:forEach>
				</ul>
			</div>
		
			<div class="box-content">
				<div class="desc">
					All test classes found (including '0' = deactivated)<br>
					Detailed description: <b>test_class</b> used in <b>qos_test_objective</b>
				</div>
				<ul>
					<c:forEach items="${testClassList}" var="item">
						<li>test_class: <a href="<c:url value="/list/objective/testclass/${item[1]}" />">'<c:out value="${item[1]}" />'</a>, count: <c:out value="${item[0]}" /></li>
					</c:forEach>
				</ul>
			</div>
			
			<div class="box-content">
				<div class="desc">
					All test description keys used by active tests (test_class != 0)<br>
					Detailed description: <b>test_desc</b> used in <b>qos_test_objective</b> where <b>test_class != 0</b>
				</div>
				<ul>
					<c:forEach items="${testDescList}" var="item">
						<li>test_desc: '<a href="<c:url value="/list/testdesc/key/${item[1]}" />"><c:out value="${item[1]}" /></a>', count: <a href="<c:url value="/list/objective/testdesc/${item[1]}" />"><c:out value="${item[0]}" /></a></li>
					</c:forEach>
				</ul>
			</div>

			<div class="box-content">
				<div class="desc">
					All test summary keys used by active tests (test_class != 0)<br>
					Detailed description: <b>test_sumamry</b> used in <b>qos_test_objective</b> where <b>test_class != 0</b>
				</div>
				<ul>
					<c:forEach items="${testSummaryList}" var="item">
						<li>test_desc: '<a href="<c:url value="/list/testdesc/key/${item[1]}" />"><c:out value="${item[1]}" /></a>', count: <a href="<c:url value="/list/objective/testsummary/${item[1]}" />"><c:out value="${item[0]}" /></a></li>
					</c:forEach>
				</ul>
			</div>
			
			<div class="box-content">
				<div class="desc">
					All test groups used by active tests (test_class != 0)<br>
					Detailed description: <b>test</b> used in <b>qos_test_objective</b> where <b>test_class != 0</b>
				</div>
				<ul>
					<c:forEach items="${testGroupList}" var="item">
						<li>
							test: '<a href="<c:url value="/list/objective/test/${item[1]}" />"><c:out value="${item[1]}" /></a>', count: <c:out value="${item[0]}" />
						</li>
					</c:forEach>
				</ul>
			</div>
			
			<div class="box-content">
				<div class="desc">
					All test groups used by inactive tests (test_class = 0)<br>
					Detailed description: <b>test</b> used in <b>qos_test_objective</b> where <b>test_class = 0</b>
				</div>
				<ul>
					<c:forEach items="${testGroupInactiveList}" var="item">
						<li>
							test: '<a href="<c:url value="/list/objective/test/${item[1]}" />"><c:out value="${item[1]}" /></a>', count: <c:out value="${item[0]}" />
						</li>
					</c:forEach>
				</ul>
			</div>												
		</div>
	</section>
</article>
<%@ include file="footer.jsp"%>