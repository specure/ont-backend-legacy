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
		<td id="list-news-time">
			<c:out value='${item.time}' />
		</td>
		<td id="list-news-titleEn">
			<c:out value='${item.titleEn}' />
		</td>
		<td id="list-news-titleDe">
			<c:out value='${item.titleDe}' />
		</td>
		<td id="list-news-textEn" data-editable-type="textarea" data-editable-rows="7">
			<c:out value='${item.textEn}' />
		</td>
		<td id="list-news-textDe" data-editable-type="textarea" data-editable-rows="7">
			<c:out value='${item.textDe}' />
		</td>
		<td id="list-news-active" data-editable-type="checkbox">
			<c:out value='${item.active}' />
		</td>
		<td id="list-news-force" data-editable-type="checkbox"> 
			<c:out value='${item.force}' />
		</td>
		<td id="list-news-plattform">
			<c:out value='${item.plattform}' />
		</td>
		<td id="list-news-maxSoftwareVersionCode">
			<c:out value='${item.maxSoftwareVersionCode}' />
		</td>
		<td id="list-news-minSoftwareVersionCode">
			<c:out value='${item.minSoftwareVersionCode}' />
		</td>
		<td id="list-news-uuid">
			<c:out value='${item.uuid}' />
		</td>
		<td id="list-news-editable">
		</td>
	</tr>
</table>
