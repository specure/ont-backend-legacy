///////////////////////////////////////
// global vars and settings:
///////////////////////////////////////
var divClass = "editable";
var aDivClass = "edit-buttons";


var TABLE_NAMES = {
		"objective" : "qos_test_objective",
		"testdesc" : "qos_test_desc",
		"testserver" : "test_server",
		"settings": "settings",
		"news": "news"
};

var TABLE_ROW_ITEMS = {
		"objective" : "objective_item",
		"testdesc" : "testdesc_item",
		"testserver" : "testserver_item",
		"settings" : "settings_item",
		"news" : "news_item"
};

var OBJECT_TYPE_SAVE_FUNCTIONS = {
		"objective" : function(response, pathType) {
			var result = "";
			if (pathType == "objectives") {
				result = matchObjectiveResults(response);
			}
			else if (pathType == "parameters") {
				result = matchObjectiveParams(response);
			}
			
			return $(result);
		},
		"testdesc" : function(response) {
			return nl2br(response);
		}
};

var OBJECT_TYPE_INIT_FUNCTIONS = {};

var REMOVE_INFO_MESSAGE_AFTER_MS = 4000;

$.ajaxSetup({
	encoding:"UTF-8"
});

///////////////////////////////////////
// global functions for angular js module:
///////////////////////////////////////

var showErrorDetail = function(html) {
	$('<div></div>').appendTo('body')
	    .html(html)
	    .dialog({
	        modal: true,
	        title: 'Error',
	        zIndex: 10000,
	        autoOpen: true,
	        width: '75%',
	        resizable: false,
	        buttons: {
	            Ok: function () {
	                $(this).dialog("close");
	            }
	        },
	        close: function (event, ui) {
	            $(this).remove();
	        }
	    });
};

var showDeleteDialog = function(tableName, id, onYes) {
	$('<div></div>').appendTo('body')
	    .html('<div>Are you sure you want to delete an entry in the <b>' + tableName + '</b> table with the id: <b>' + id + '</b>?</div>')
	    .dialog({
	        modal: true,
	        title: 'Delete entry',
	        zIndex: 10000,
	        autoOpen: true,
	        width: 'auto',
	        resizable: false,
	        buttons: {
	            Yes: function () {
	            	if (onYes !== undefined && onYes !== null) {
	            		onYes(id);
	            	}
	                $(this).dialog("close");
	            },
	            No: function () {
	            	$(this).dialog("close");
	            }
	        },
	        close: function (event, ui) {
	            $(this).remove();
	        }
	    });	
};


///////////////////////////////////////
// global functions:
///////////////////////////////////////
/**
 * does what its name says: convert new lines to <br> tags
 */
function nl2br(param) {
	return param.replace(/(?:\r\n|\r|\n)/g, "<br>");
}

/**
 * returns the index of an element inside an array
 * @param needle
 * @returns
 */
function indexOf(array, needle) {
    if(typeof Array.prototype.indexOf === 'function') {
    	return array.indexOf(needle);
    } 
    else {
    	var i = -1, index = -1;

        for(i = 0; i < array.length; i++) {
        	if(array[i] === needle) {
        		index = i;
                break;
        	}
        }

        return index;
    }
}

///////////////////////////////////////
//global edit/submit/delete functions:
///////////////////////////////////////

/**
 * 
 * @param button
 * @param objType
 */
function onDeleteButtonClicked(button, objType) {
	if (button.attr("id") == 0) {
		removeTrElement(button.closest("tr"));	
	}
	else {
		$('<div></div>').appendTo('body')
	    .html('<div>Are you sure you want to delete an entry in the <b>' + TABLE_NAMES[objType] + '</b> table with the id: <b>' + button.attr("id") + '</b>?</div>')
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
	            		url: LINKS.DELETE + objType + "/" + button.attr("id"),
	            		type: "GET",
	            	}).done(function(response) {
	                    msgDialog.dialog("close");
	                	var trElement = button.closest("tr");
	                    if (response == "ERROR") {                   	
	                    	createInfoTrElement(trElement, "Error during deletion. Could not complete request. " +
	                    			"Click on this element to remove it." , "error-row ignore-odd-even", false);
	                    }
	                    else if (response == "OK") {
	                    	createInfoTrElement(trElement, "Entry deleted successfully." , "success-row ignore-odd-even", false, REMOVE_INFO_MESSAGE_AFTER_MS);
	                    	removeTrElement(trElement);
	                    }
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
}

function removeTrElement(trElement) {
	trElement.find('td')
	.wrapInner('<div style="display: block;" />')
	.parent()
	.find('td > div')
	.slideUp(350, function(){
		var table = trElement.closest("tbody");
		trElement.remove();
    	recreateOddEvenTableRows(table, "even", "");
	});                    	
}

///////////////////////////////////////
//global ui methods:
///////////////////////////////////////

/**
 * 
 * @param buttonContainer
 * @param replace
 * @param clickFunc
 */
function constructDeletableTr(buttonContainer, replace, clickFunc) {
	var id = buttonContainer.closest("tr").find("td:first").text().trim();
	var deleteButton = $('<a href="#" id="' + id + '" class="edit-button button-delete"></a>');
	var buttonDiv = buttonContainer.find("div." + aDivClass);
	if (!buttonDiv.exists()) {
		buttonDiv = $("<div class='" + aDivClass + "'>");
		buttonContainer.append(buttonDiv);
	}
	else if(replace) {
		var oldDelButton = buttonDiv.find("a.button-delete");
		if (oldDelButton.exists()) {
			oldDelButton.remove();
		}
	}
	buttonDiv.append(deleteButton);
	deleteButton.click(function(e) {
		e.preventDefault();
		clickFunc($(this));
	});
}

/**
 * 
 */
function recreateOddEvenTableRows(tableBody, evenClassName, oddClassName) {
	tableBody.find("tr").not(".ignore-odd-even").each(function (i, e) {
		if (i % 2 == 0) {
			$(this).addClass(oddClassName);
			$(this).removeClass(evenClassName);
		}
		else {
			$(this).addClass(evenClassName);
			$(this).removeClass(oddClassName);
		}
	});
}

/**
 * 
 * @param trElementWithError
 * @param message
 */
function createInfoTrElement(trElement, message, cssClass, appendAfter, removeTimeout) {
	var columns = trElement.find("td:last").index();
	var errorTrElement = $("<tr class='" + cssClass + "'><td colspan='" + (columns+1) + "'><i>" + message + "</i></td></tr>");


	errorTrElement.click(function(e) {
		$(this).remove();
	});		

	if (removeTimeout !== undefined && removeTimeout > 0) {
		setTimeout(function() {
			errorTrElement.fadeOut(500, function() {
				$(this).remove();
			});
		}, removeTimeout);
	}
	

	if (appendAfter) {
		trElement.after(errorTrElement);
	}
	else {
		trElement.before(errorTrElement);
	}
}

///////////////////////////////////////
// global parser functions
///////////////////////////////////////

/**
 * serializes a TD element
 */
function serializeTd(tdElement) {
	var elementArray = {};
	tdElement.closest("tr").find("[name]").each(function (i, e) {
		elementArray[$(this).attr("name")] = $(this).val();
	});
	return $.param(elementArray);
}

/**
 * matches a qos_test_objective.params field and parses it into a human readable list
 * @param params
 * @returns
 */
function matchObjectiveParams(params) {
	var ulElement = $("<ul>");
	var paramJson = JSON.parse(params);
	if (paramJson !== undefined) {
		for (var k in paramJson) {
			var liElement = $("<li>");
			liElement.text(k + ": " + paramJson[k]);
			ulElement.append(liElement);		
		}

		return ulElement;
	}

	return "";
}

/**
 * matches a qos_test_objective.results field and parses it into a human readable list
 * @param params
 * @returns
 */
function matchObjectiveResults(params) {
	var paramJson = JSON.parse(params);
	var divElement = $("<div>");
	if (paramJson !== undefined && paramJson.length > 0) {
		var i=1;
		for (var param in paramJson) {
			var curElement = paramJson[param];
			var ulElement = $("<ul>");
			for (var element in curElement) {				
				var liElement = $("<li>");
				liElement.append(element + ": ");
				liElement.append(checkForObjectiveAnchor(element, curElement[element]));
				ulElement.append(liElement);
			}
			
			divElement.append("<i>condition " + (i++) + ":</i>");
			divElement.append(ulElement);			
		}
	}

	return divElement;
}

/**
 * splits a hstore array string into a js array
 * @param params
 * @returns {Array}
 */
function getGroupedObjectivesResults(params) {
	var matcher = /("[^"]*")/g;
	var result = new Array();
	var resultString = "";
	
	while ((resultString = matcher.exec(params)) != null) {
		result.push(resultString[0]);
	}
	
	return result;
}

/**
 * checks objective key/value pair if an <a> tag should be generated
 * @param key
 * @param value
 * @returns
 */
function checkForObjectiveAnchor(key, value) {
	if (key == "on_failure" || key == "on_success") {
 		var anchorTag = $("<a>");
 		anchorTag.attr("href", LINKS.TESTDESC_LIST + value);
 		anchorTag.text(value);
 		
 		return anchorTag;
	}
	
	return value;
}

/**
 * replaces all test description keys found in on_success and on_failure with their representative <a> tags
 * @param descEntry
 * @returns
 */
function replaceTestDescKeyWithAnchorTag(descEntry) {
 	var matcher = /"on_(failure|success)"=>"([^"]*)"/g;
 	var matcherResult = matcher.exec(descEntry);
 	var liTag = $("<li>");
 	if (matcherResult != null) {
 		var anchorTag = $("<a>");
 		anchorTag.attr("href", LINKS.TESTDESC_LIST + matcherResult[2]);
 		anchorTag.text(matcherResult[2]);

 		liTag.append("\"on_" + matcherResult[1] + "\"=>\"");
 		liTag.append(anchorTag);
 		liTag.append("\"");
 	}
 	else {
 		liTag.text(descEntry);
 	}
 	
 	return liTag;
}