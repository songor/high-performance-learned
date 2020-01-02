var page = require('webpage').create();
page.open('http://www.baidu.com', function () {
	setTimeout(function () {
		page.render('baidu.png');
		phantom.exit();
	}, 200);
});