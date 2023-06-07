<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<div id="edit-form">
	<form:form modelAttribute="editItem" action="${submitUrl}" autocomplete="off">
		<div class="edit-block form-row">
			<c:forEach items="${editPathMap}" var="path">
				<p class="edit-row-entry">
					<c:choose>
						<c:when test="${path.tagType == 'options'}">
							
							<form:label path="${path.key}"><c:out value="${path.value}" /></form:label>
							<select name="<c:out value="${path.key}" />">
								<c:forEach var="item" items="${path.tag.options}">
									<option <c:if test="${path.tag.selected == item}">selected</c:if>><c:out value="${item}" /></option>							
								</c:forEach>
							</select>
						</c:when>
						<c:otherwise>
							<form:label path="${path.key}"><c:out value="${path.value}" /></form:label>
							<form:input path="${path.key}" />
						</c:otherwise>
					</c:choose>
				</p>			
			</c:forEach>
		</div>
		
	</form:form>
</div>