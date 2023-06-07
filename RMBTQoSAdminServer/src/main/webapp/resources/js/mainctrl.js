var adminApp = angular.module('adminApp',['ngRoute']);

adminApp.config(function($routeProvider) {
	$routeProvider.
		when('/:table', {
			templateUrl: LINKS.RESOURCES + 'templates/view_init.html',
			controller: 'RestController'
		});
});

adminApp.controller('MainController', function($scope, $http, $routeParams) {
	$scope.title = TITLE;
	
	$scope.setTitle = function(newTitle) {
		$scope.title = newTitle;
	};
});

adminApp.controller('RestController', function($scope, $http, $routeParams) {
	$scope.title = "";
	$scope.templateUrl = "";
	$scope.items = [];
	$scope.tempItems = [];
	$scope.showOldArticle = false;
	
	$scope.$on('$routeChangeSuccess', function () {
		$scope.templateUrl = LINKS.RESOURCES + 'templates/view_' + $routeParams.table + '.html';
		load();
	});

	load = function() {
		$scope.title = $routeParams.table;
		$http.get(LINKS.REST + $routeParams.table)
			.success(function(data, status, headers, config) {
				for (var i = 0; i < data.length; i++) {
					$scope.items.push({data: data[i]});
				}
			})
			.error(function(data, status, headers, config) {
				//
			});
	};
	
	$scope.loadRestList = function(tableName, orderBy) {
		$http.get(LINKS.REST + tableName)
		.success(function(data, status, headers, config) {
			$scope[tableName] = data;
			if (angular.isDefined(orderBy) && orderBy !== "") {
//				for (var i = 0; i < data.length; i++) {
//					if (data[i] !== undefined) {
//						$scope["ordered_" + tableName][data[i][orderBy]] = data[i];
//					}
//				}				
			}
		})
		.error(function(data, status, headers, config) {
			//
		});		
	}
	
	$scope.edit = function(item) {
		var index = $scope.items.indexOf(item);
		$scope.tempItems[index] = angular.copy($scope.items[index]);
	};
	
	$scope.save = function(item) {
		var index = $scope.items.indexOf(item);
		var e = $scope.items[index].data;
		if (angular.isDefined(e.uid) && e.uid !== null) {
			$http.put(LINKS.REST + $routeParams.table + "/" + e.uid, e)
				.success(function(data, status) {
					$scope.items[index].data = data;
					$scope.showSuccess($scope.items[index], data);
				})
				.error(function(data, status){
					$scope.showError($scope.items[index], data);
				});
		}
		else {
			$http.post(LINKS.REST + $routeParams.table, e)
			.success(function(data, status) {
				$scope.items[index].data = data;
				$scope.showSuccess($scope.items[index], data);
			})
			.error(function(data, status){
				$scope.showError($scope.items[index], data);
			});			
		}
	};
	
	$scope.getIndexById = function(id) {
		for (var i = 0; i < $scope.items.length; i++) {
			var e = $scope.items[i];
			if (e.uid == id) {
				return i;
			}
		}
	}
	
	$scope.remove = function(item) {
		var index = $scope.items.indexOf(item);
		var e = $scope.items[index].data;
		if (angular.isDefined(e.uid) && e.uid !== null) {
			showDeleteDialog($routeParams.table, e.uid, function() {
				$http.delete(LINKS.REST + $routeParams.table + "/" + e.uid, e)
				.success(function(data, status) {
					$scope.items.splice(index,1);
				})
				.error(function(data, status){
					$scope.showError($scope.items[index], data);
				});				
			});
		}
		else {
			$scope.items.splice(index,1);
		}
	};
	
	$scope.restore = function(item) {
		var index = $scope.items.indexOf(item);
		$scope.items[index] = angular.copy($scope.tempItems[index]);
	};
	
	$scope.add = function() {
		$http.post(LINKS.REST + $routeParams.table)
			.success(function(data, status) {
				$scope.items.splice(0,0,{data: data});
				$scope.edit($scope.items[0]);
				$scope.items[0].isEdit = true;
			})
			.error(function(data, status) {
				
			});
	};
	
	$scope.showSuccess = function(item, data) {
		item.success = true;
		item.error = false;
		item.info = data;	
		item.isEdit = false;
	};
	
	$scope.showError = function(item, data) {
		item.success = false;
		item.error = true;
		item.info = data;
	};
	
	$scope.showErrorDetail = function(html) {
		showErrorDetail(html);
	};
	
	$scope.join = function(elementsName, joinFieldName, joinValue, returnFieldName) {
		var elements = $scope[elementsName];
		
		if (elements !== undefined) {
			for (var i = 0; i < elements.length; i++) {
				if (elements[i] !== undefined) {
					if (elements[i][joinFieldName]==joinValue) {
						return returnFieldName !== undefined ? elements[i][returnFieldName] : elements[i];
					}
				}
			}
		}
		
		return null;
	};
});


adminApp.directive('a', function() {
    return {
        restrict: 'E',
        link: function(scope, elem, attrs) {
        	if (attrs.view) {
	        	elem.on('click', function(e){
	                e.preventDefault();
	        	});
        	}
        }
   };
});

/*
 * directive: 
 * 	empty-to-null
 * 
 * restricted to: 
 * 	element attribute
 * 
 * function: 
 * 	change empty string value to NULL
 * 
 * value:
 *  none
 */
adminApp.directive('emptyToNull', function () {
    return {
        restrict: 'A',
        require: 'ngModel',
        link: function (scope, elem, attrs, ctrl) {
            ctrl.$parsers.push(function(viewValue) {
                if(viewValue === "") {
                    return null;
                }
                return viewValue;
            });
        }
    };
});

/*
 * directive: 
 * 	ng-rest-loadlist
 * 
 * restricted to: 
 * 	element attribute
 * 
 * function: 
 * 	additionally load a full entity list by using rest service
 * 
 * value:
 *  table (=rest service name) to be loaded
 */
adminApp.directive('ngRestLoadlist', function() {
	return {
		restrict: 'A',
		link: function(scope, element, attrs) {
			var name = attrs.ngRestLoadlist;
			var orderBy = attrs.ngRestOrderby;
			if (!angular.isDefined(scope[name])) {
				scope.loadRestList(name, orderBy);
			}
		}
	};
})

/*
 * directive: 
 * 	ng-tri-state
 * 
 * restricted to: 
 * 	element
 * 
 * function: 
 * 	creates a checkbox with three states (true, false, indeterminate)
 * 
 * value:
 *  none
 */
adminApp.directive('ngTriState', function() {
	return {
		restrict: 'E',
		replace: true,
		scope: {
			val: '=ngModel'
		},
		template: '<input type="checkbox" data-ng-click="toggle()" indeterminate="true" />',
		link: function(scope, element, attrs) {
            if (!angular.isDefined(scope.val)) {
                scope.val = null;
            }

            scope.state = (scope.val == null ? 0 : scope.val == 1 ? 1 : 2);
            
            scope.toggle = function() {
                scope.state = scope.state >= 2 ? 0 : scope.state+1;
                
                if (scope.state === 0) {
                	element.prop("indeterminate", true);
                	element.prop("checked", false);
                	scope.val = null;
                }
                else if (scope.state === 1) {
                	element.prop("indeterminate", false);
                	element.prop("checked", true);
                }
                else if (scope.state === 2) {
                	element.prop("indeterminate", false);
                	element.prop("checked", false);
                }
                
                if (typeof scope.ngChange != 'undefined') {
                    $timeout(function() {
                        scope.ngChange(scope.val);
                    });
                }
            };		
		}
	};
});

/*
 * directive: 
 * 	date-format
 * 
 * restricted to: 
 * 	element attribute
 * 
 * function: 
 * 	adds a parser and formatter to the model controller, to change a timestamp to a human readable and editable date expression (and vice versa)
 * 
 * value:
 *  none
 */
adminApp.directive('dateFormat', function($filter) {
	  return {
	    require: 'ngModel',
	    link: function(scope, element, attr, ngModelCtrl) {
	      ngModelCtrl.$formatters.unshift(function(valueFromModel) {
	    	  return $filter('date')(valueFromModel, "dd.MM.yyyy");
	      });

	      ngModelCtrl.$parsers.push(function(valueFromInput) {
	    	  if (valueFromInput === undefined || valueFromInput === null) {
	    		  return valueFromInput;
	    	  }
	    	  var date = valueFromInput.split(".").reverse().join("/");
	    	  var d = Date.parse(date);
	    	  if (isNaN(d) === false) {
	    		  return new Date(d);
	    	  }
	    	 
	    	  return valueFromInput;
	      });
	    }
	  };
});


adminApp.directive('ngPageTitle', function() {
	return {
		restrict: 'A',
		link: function(scope, element, attrs) {
			var title = attrs.ngPageTitle;
			scope.setTitle(title);
		}
	};
});


adminApp.filter('getOrdered', function() {
    return function(input) {
        var ordered = {};
        for (var key in input){            
            ordered[input[key]["uid"]] = input[key];
        }           
        return ordered;
    };
});
