$(function() {
	$.fn.editableItem = function(parameters) {
		var options = $.extend({
			id: null,
			replaceTag: null,
			parentTag: null,
			bodyElement: null,
			tableName: null,
			editUrl: null,
			submitUrl: null,
			deleteUrl: null,
			buttonContainerId: null,
			triggerEdit: false,
			multiEdit: false,
			onCancel: function(e, o) {},
			onDeleteDone: function(e, o) {},
			onDeleteError: function(e, o) {},
			onSubmitDone: function (r, e, o) {},
			onSubmitError: function (e, o) {}
    	}, parameters);
	
		var onEdit = function(element, options) {
			$.ajax({
				url: options.editUrl,
				type: "GET"
			}).done(function(response) {
				var oldHtml = element.html();
				element.empty();
				element.append($(response));
				element.find("a.button-save").click(function(e) {
					e.preventDefault();
					onSubmit(element, options);
				});
				element.find("a.button-cancel").click(function(e) {
					e.preventDefault();
					onRestoreElement(element, options, oldHtml);
				});
			});
		};
		
		var onRestoreElement = function(element, options, oldHtml) {
			element.empty();
			element.append(oldHtml);
			element.find("a.edit-button").click(function (e) {
				e.preventDefault();
				onEdit(element, options);
			});
		};
		
		var onSubmit = function(element, options) {
			var postData = element.find("form").serializeArray();
			$.ajax({
				url: options.submitUrl,
				type: "POST",
				data: postData
			}).done(function(response) {
				options.onSubmitDone(response, element, options);
				init(element, options);
	        	createInfoTrElement(element.closest(options.parentTag), "Changes saved successfully.", "success-row ignore-odd-even", false, REMOVE_INFO_MESSAGE_AFTER_MS);
			}).fail(function(response) {
				options.onSubmitError(element, options);
	        	createInfoTrElement(element.closest(options.parentTag), "Error during saving. Could not complete request. Detailed error report: <a id='detailed_error_report' href='#'>click here</a>" + ". " +
	        			"Click on this element to remove it." , "error-row ignore-odd-even", false);
	        	$('a#detailed_error_report').click(function(e) {
	        		e.preventDefault();
	        		showErrorDetail(options, response.responseText);
	        	});
			});		
		};
		
		var onDelete = function(options, element) {
			if (options.id == 0) {
				options.onDeleteDone(element, options);
			}
			else {
				$('<div></div>').appendTo('body')
			    .html('<div>Are you sure you want to delete an entry in the <b>' + options.tableName + '</b> table with the id: <b>' + options.id + '</b>?</div>')
			    .dialog({
			        modal: true,
			        title: 'Delete entry',
			        zIndex: 10000,
			        autoOpen: true,
			        width: 'auto',
			        resizable: false,
			        buttons: {
			            Yes: function () {
			            	var msgDialog = $(this);
			            	$.ajax({
			            		url: options.deleteUrl,
			            		type: "GET",
			            	}).done(function(response) {
			                    msgDialog.dialog("close");
			                    if (response == "ERROR") {
			                    	options.onDeleteError(element, options);
			        	        	createInfoTrElement(element.closest(options.parentTag), "Error during deletion. Could not complete request. Detailed error report: <a id='detailed_error_report' href='#'>click here</a>" + ". " +
			        	        			"Click on this element to remove it." , "error-row ignore-odd-even", false);
			        	        	$('a#detailed_error_report').click(function(e) {
			        	        		e.preventDefault();
			        	        		showErrorDetail(options, response.responseText);
			        	        	});
			                    }
			                    else if (response == "OK") {
		                    		options.onDeleteDone(element, options);
			                    	createInfoElement(element.closest(options.parentTag), "Entry deleted successfully." , "success-row ignore-odd-even", false, REMOVE_INFO_MESSAGE_AFTER_MS);
			                    }
			            	}).fail(function(response) {
			            		options.onDeleteError(element, options);
			    	        	createInfoTrElement(element.closest(options.parentTag), "Error during deletion. Could not complete request. Detailed error report: <a id='detailed_error_report' href='#'>click here</a>" + ". " +
			    	        			"Click on this element to remove it." , "error-row ignore-odd-even", false);
			    	        	$('a#detailed_error_report').click(function(e) {
			    	        		e.preventDefault();
			    	        		showErrorDetail(options, response.responseText);
			    	        	});
			            	});		
			            },
			            No: function () {
			                $(this).dialog("close");
			            }
			        },
			        close: function (event, ui) {
			            $(this).remove();
			        }
			    });
			}
		};
		
		var showErrorDetail = function(options, html) {
			$('<div></div>').appendTo('body')
		    .html(html)
		    .dialog({
		        modal: true,
		        title: 'Detailed error message',
		        zIndex: 10000,
		        autoOpen: true,
		        width: '75%',
		        resizable: false,
		        close: function (event, ui) {
		            $(this).remove();
		        }
		    });
		};
		
		var createInfoElement = function(element, message, cssClass, appendAfter, removeTimeout) {
			var errorElement = "";
			
			if (options.editableType == "td") {
				var columns = element.find("td:last").index();
				errorElement = $("<tr class='" + cssClass + "'><td colspan='" + (columns+1) + "'><i>" + message + "</i></td></tr>");
			}
			else {
				errorElement = $("<div class='" + cssClass + "'><i>" + message + "</i></div>");
			}
	
	
			errorElement.click(function(e) {
				$(this).remove();
			});		
	
			if (removeTimeout !== undefined && removeTimeout > 0) {
				setTimeout(function() {
					errorElement.fadeOut(500, function() {
						$(this).remove();
					});
				}, removeTimeout);
			}
			
			if (appendAfter) {
				element.after(errorElement);
			}
			else {
				element.before(errorElement);
			}
		};
		
		var init = function(tdElement, options) {
			var id = options.id;
			
			var buttonsDiv = $('<div class="' + aDivClass + '">');
			var editButton = $('<a href="#" id="' + id + '" class="edit-button button-edit"></a>');
			var deleteButton = $('<a href="#" id="' + id + '" class="edit-button button-delete"></a>');
			
			buttonsDiv.append(editButton);
	
			if (options.deleteUrl != null) {
				buttonsDiv.append(deleteButton);
				deleteButton.click(function (e) {
					onDelete(options, trElement);
				});
			}
			
			tdElement.empty();	
			
			var bodyElement = options.bodyElement;
			if (bodyElement instanceof jQuery) {
				if (bodyElement.is("div")) {
					var div = $('<div class="' + divClass +'">');
					div.append(bodyElement);
					tdElement.append(div);
				}
				else {
					bodyElement.addClass(divClass);
					tdElement.append(bodyElement);	
				}
			}
			else {
				var div = $('<div class="' + divClass +'">');
				div.append(bodyElement);
				tdElement.append(div);	
			}
	
			tdElement.append(buttonsDiv);
			
			tdElement.find("a.edit-button").click(function (e) {
				e.preventDefault();
				onEdit(tdElement, options);
			});		
		};
		
		init($(this), options);
	};
});
