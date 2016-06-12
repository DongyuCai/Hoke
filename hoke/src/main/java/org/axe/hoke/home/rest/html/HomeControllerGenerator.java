package org.axe.hoke.home.rest.html;

import org.axe.util.Html2JavaUtil;

public class HomeControllerGenerator {

	public static void main(String[] args) {
		Html2JavaUtil.convertHtmlCode("src/main/java/org/axe/hoke/home/rest/html/home_controller_head.html");
		Html2JavaUtil.convertHtmlCode("src/main/java/org/axe/hoke/home/rest/html/hoke.html");
		Html2JavaUtil.convertHtmlCode("src/main/java/org/axe/hoke/home/rest/html/home_controller_foot.html");
	}
}
