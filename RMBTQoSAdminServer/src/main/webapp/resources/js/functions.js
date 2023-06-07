$(function() {
	$.fn.exists = function() {
		return this.length > 0 ? this : false;
	};

	$.fn.disableSelection = function() {
		return this.attr('unselectable', 'on').css('user-select', 'none').on('selectstart', false);
	};

	// create case insensitive contains selector
	$.expr[":"].containsi = $.expr.createPseudo(function(arg) {
		return function( elem ) {
			return $(elem).text().toUpperCase().indexOf(arg.toUpperCase()) >= 0;
		};
	});

	$.expr[":"].hasHtml = $.expr.createPseudo(function(arg) {
		return function( elem ) {
			return $(elem).text() === arg.toUpperCase();
		};
	});

	$.fn.changeElementType = function(newType) {
		this.each(function() {
			var attrs = {};
		 
			$.each(this.attributes, function(idx, attr) {
				attrs[attr.nodeName] = attr.nodeValue;
			});

			$(this).replaceWith(function() {
				return $("<" + newType + "/>", attrs).append($(this).contents());
			});
		});
    };
});