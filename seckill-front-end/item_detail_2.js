var page = require("webpage").create();
var fs = require("fs");
page.open("http://39.104.234.19/resources/item_detail.html?id=2", function (status) {
	console.log("status: " + status);
	var is_init = "0";
	setInterval(function () {
		if (is_init != "1") {
			page.evaluate(function () {
				init_view();
			});
			is_init = page.evaluate(function () {
					return get_init();
				});
		} else {
			fs.write("item_detail_2.html", page.content, "w");
			phantom.exit();
		}
	}, 1000);
});
