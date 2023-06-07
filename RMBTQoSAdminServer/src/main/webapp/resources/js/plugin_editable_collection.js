$(function() {
	$.fn.editableCollection = function(parameters) {
		var options = $.extend({
			id: null,
			tableName: null,
			editableType: null,
			editableIndexes: [],
			inputFieldNames: [],
			fieldParserFunc: [],
			submitUrl: null,
			deleteUrl: null,
			buttonContainerId: null,
			infoMessageAppendAfter: true,
			triggerEdit: false,
			onCancel: function(e, o) {},
			onDeleteDone: function(e, o) {},
			onDeleteError: function(e, o) {},
			onSubmitDone: function (e, o) {},
			onSubmitError: function (e, o) {}
    	}, parameters);
		
		var onEdit = function(element, editUrl) {
			$.ajax({
				async: false,
				url: editUrl,
				type: "GET"
			}).done(function(response) {
				element.empty();
				element.append($(response));
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
			                    if (response == "OK") {
	                        		options.onDeleteDone(element, options);
			                    	createInfoElement(element, "Entry deleted successfully." , "success-row ignore-odd-even", options.infoMessageAppendAfter, REMOVE_INFO_MESSAGE_AFTER_MS);
			                    }
			                    else {
			                    	options.onDeleteError(element, options);
						        	createInfoElement(element, "Error during deletion. Could not complete request. Detailed error report: <a id='detailed_error_report' href='#'>click here</a>" + ". " +
						        			"Click on this element to remove it." , "error-row ignore-odd-even", options.infoMessageAppendAfter);
						        	$('a#detailed_error_report').click(function(e) {
						        		e.preventDefault();
						        		showErrorDetail(options, response);
						        	});
			                    }
			            	}).fail(function(response) {
			            		options.onDeleteError(element, options);
					        	createInfoElement(element, "Error during deletion. Could not complete request. Detailed error report: <a id='detailed_error_report' href='#'>click here</a>" + ". " +
					        			"Click on this element to remove it." , "error-row ignore-odd-even", options.infoMessageAppendAfter);
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
		
		var serializeEditable = function(element) {
			var elementArray = {};
			element.find("[name]").each(function (i, e) {
				if ($(this).is(':checkbox')) {
					elementArray[$(this).attr("name")] = $(this).is(':checked');	
				}
				else {
					elementArray[$(this).attr("name")] = $(this).val();
				}
			});
			return $.param(elementArray);
		};
		
		var init = function(element, options) {
			var trElement = element;
			var trClone = trElement.clone();
			var id = options.id;
			var buttonsDiv = $('<div class="' + aDivClass + '">');
			var editButton = $('<a href="#" id="' + id + '" class="edit-button button-edit"></a>');
			var deleteButton = $('<a href="#" id="' + id + '" class="edit-button button-delete"></a>');
			var saveButton = $('<a href="#" id="' + id + '" class="edit-button button-save"></a>');
			var cancelButton = $('<a href="#" id="' + id + '" class="edit-button button-cancel"></a>');
			var buttonContainer = element.find("#" + options.buttonContainerId);
			
			buttonsDiv.append(editButton);
			
			if (options.deleteUrl != null) {
				buttonsDiv.append(deleteButton);
				deleteButton.click(function (e) {
					e.preventDefault();
					onDelete(options, trElement);
				});
			}
			
			buttonContainer.empty();
			buttonContainer.append(buttonsDiv);
			
			editButton.click(function (e) {
				e.preventDefault();
				
				trElement.find(options.editableType).each(function (i) {
					var indexOfName = indexOf(options.editableIndexes, i+1);
					if (indexOfName >= 0) {
						var editableTdElement = $(this);
						
						var editType = editableTdElement.attr("data-editable-type");
						if (editType === undefined) {
							editType = "input";
						}
						
						if (editType != "multiedit") {
							var value = editableTdElement.text().trim();
							
							if (editableTdElement.data("originalText") !== undefined) {
								value = editableTdElement.data("originalText").trim();
							}
							
							var inputElement = $("<input>");
							
							if (editType == "checkbox") {
								if (value == "true") {
									inputElement = $("<input type='checkbox' checked>");
								}
								else {
									inputElement = $("<input type='checkbox' >");
								}
							}
							else if (editType == "textarea") {
								var rows = editableTdElement.attr("data-editable-rows");
								if (rows === undefined) { 
									rows = 8;
								}
								inputElement = $("<textarea rows='" + rows + "'>");
							}
							
							if (!inputElement.is(':checkbox')) {
								inputElement.val(value);
							}
							
							inputElement.attr("name", options.inputFieldNames[indexOfName]);
	
							$(this).empty();
							$(this).append(inputElement);
						}
						else {
							onEdit($(this), options.inputFieldNames[indexOfName]);
						}
					}
				});
					
				buttonsDiv.empty();
				buttonsDiv.append(saveButton);
				buttonsDiv.append(cancelButton);
				
				buttonContainer.empty();
				buttonContainer.append(buttonsDiv);
								
				buttonContainer.find("a.button-save").click(function (e) {
					e.preventDefault();
					var postData = serializeEditable(trElement);
					$.ajax({
						url: options.submitUrl,
						type: "POST",
						data: postData
					}).done(function(response) {
						var newElement = $(response);
						trElement.html(newElement.find(trElement.get(0).tagName).html());
						createInfoElement(trElement, "Changes saved successfully.", "success-row ignore-odd-even", options.infoMessageAppendAfter, REMOVE_INFO_MESSAGE_AFTER_MS);
			        	options.triggerEdit = false;
						options.onSubmitDone(trElement, options);
						init(trElement, options);
					}).fail(function(response) {
						console.log(response);
			        	createInfoElement(trElement, "Error during saving. Could not complete request. Detailed error report: <a id='detailed_error_report' href='#'>click here</a>" + ". " +
			        			"Click on this element to remove it." , "error-row ignore-odd-even", options.infoMessageAppendAfter);
			        	$('a#detailed_error_report').click(function(e) {
			        		e.preventDefault();
			        		showErrorDetail(options, response.responseText);
			        	});
			        	options.onSubmitError(trElement, options);
					});		
				});
				
				buttonContainer.find("a.button-cancel").click(function (e) {
					e.preventDefault();
					for (var i = 0; i < options.editableIndexes.length; i++) {
						var oldTdElement = trClone.find(options.editableType +":nth-of-type(" + options.editableIndexes[i] +")");
						trElement.find(options.editableType + ":nth-of-type(" + options.editableIndexes[i] +")").html(oldTdElement.html());
					}
					
					options.onCancel(trElement, options);
					options.triggerEdit = false;
					init(trElement, options);
				});
			});
			
			trElement.find(options.editableType).each(function (i) {
				var parserFunc = options.fieldParserFunc[i+1];
				if (parserFunc !== undefined) {
					$(this).data("originalText", $(this).text());
					var bodyElement = parserFunc($(this).data("originalText"));
					$(this).empty();
					
					if (bodyElement instanceof jQuery) {
						if (bodyElement.is("div")) {
							var div = $('<div class="' + divClass +'">');
							div.append(bodyElement);
							$(this).append(div);
						}
						else {
							bodyElement.addClass(divClass);
							$(this).append(bodyElement);	
						}
					}
					else {
						var div = $('<div class="' + divClass +'">');
						div.append(bodyElement);
						$(this).append(div);	
					}
				}
			});
			
			if (options.triggerEdit) {
				editButton.trigger("click");
			}
		};
		
		init($(this), options);
	};
});