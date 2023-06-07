<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"%>
<%@ include file="header.jsp"%>

<script type="text/javascript">
	var PAGE_TABLE = "upload";
</script>

<article class="main-content">
		<form method="POST" enctype="multipart/form-data" action="${uploadUrl}" id="submit-form">
		<table>
			<colgroup>
				<col style="width:20%" />
				<col style="width:80%" />
			</colgroup>
			<tbody>		
				<tr>
					<td>File to upload:</td>
					<td><input type="file" name="file"></td>
				</tr>
				<tr>
					<td>Type:</td>
					<td>
						<select class="normal-size" name="filetype">
							<option value="excelBaseStation">Base station excel file</option>
						</select>
					</td>
				</tr>
				<tr>
					<td>Name (optional):</td>
					<td><input class="normal-size" type="text" name="name"></td>
				</tr>
				<tr>
					<td>Upload file:</td>
					<td><input class="normal-size" type="button" value="Upload" onclick="doUpload()"></td>
				</tr>
			</tbody>		
		</table>
		</form>
</article>

<script type="text/javascript">
function doUpload() {
	$.blockUI({ 
		css: { 
            border: 'none', 
            padding: '15px', 
            backgroundColor: '#000', 
            '-webkit-border-radius': '10px', 
            '-moz-border-radius': '10px', 
            opacity: .5, 
            color: '#fff' 
    	}
	});
	setTimeout(function () { $("#submit-form").submit(); }, 500);
}

$(function() {	
	$("#submit-form").submit(function(e) {
		e.preventDefault();
		var url = $(this).prop("action");
		var form = $(this);
		
		var formData = new FormData($(this)[0]);
		
		$.ajax({
		        type: "POST",
		        url: url,
		        data: formData, // serializes the form's elements.
	            async: false,
	            cache: false,
	            contentType: false,
	            processData: false,
		        success: function(data) {
		        	if (!data.startsWith("Error")) {
			        	form.trigger("reset");	
		        	}
		        	alert(data); // show response from the php script.
				},
				complete: function(data) {
					$.unblockUI();
				}
			}).fail(function (data, status) {
				showErrorDetail(data.responseText);
			});
	});
});
</script>

<%@ include file="footer.jsp"%>