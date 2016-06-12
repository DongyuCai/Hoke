package org.axe.hoke.home.rest;

import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.axe.annotation.ioc.Controller;
import org.axe.annotation.mvc.FilterFuckOff;
import org.axe.annotation.mvc.Interceptor;
import org.axe.annotation.mvc.Request;
import org.axe.annotation.mvc.RequestParam;
import org.axe.constant.CharacterEncoding;
import org.axe.constant.ContentType;
import org.axe.constant.RequestMethod;
import org.axe.hoke.bean.HokeDataPackage;
import org.axe.hoke.core.HokePool;
import org.axe.home.interceptor.HomeInterceptor;
import org.axe.home.interceptor.SignInInterceptor;

@FilterFuckOff
@Interceptor({ HomeInterceptor.class, SignInInterceptor.class })
@Controller(basePath = "/hoke")
public class HomeController {

private void printHtml(HttpServletResponse response, String html) {
try {
response.setCharacterEncoding(CharacterEncoding.UTF_8.CHARACTER_ENCODING);
response.setContentType(ContentType.APPLICATION_HTML.CONTENT_TYPE);
PrintWriter writer = response.getWriter();
writer.write(html);
writer.flush();
writer.close();
} catch (Exception e) {
e.printStackTrace();
}
}
@Request(value = "", method = RequestMethod.GET)
public void home(@RequestParam("token")String token, HttpServletRequest request, HttpServletResponse response) {
String contextPath = request.getContextPath();
StringBuilder html = new StringBuilder();
html.append("<!DOCTYPE html>");
html.append("<html>");
html.append("<head>");
html.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />");
html.append("<title>hoke homepage</title>");
html.append("<script type=\"text/javascript\">");
html.append("var refreshInt = setInterval(\"refresh()\",1000*60);");
html.append("function refresh(){");
html.append("window.location = \""+contextPath+"/hoke?token="+token+"\";");
html.append("}");
html.append("</script>");
html.append("</head>");
html.append("<body>");
html.append("<table width=\"100%\">");
html.append("<tr><td align=\"right\">");
html.append("<a style=\"font-size: 15px;color: #AE0000\" href=\""+contextPath+"/axe?token="+token+"\"><b>axe</b></a>");
html.append("</td></tr>");
html.append("<tr><td align=\"center\"><font size=\"28\">欢迎使用 Axe Hoke!</font></td></tr>");
html.append("");
html.append("<!--系统运行 概览-->");
html.append("<tr><td><table cellspacing=\"0px\"><tr><td style=\"background-color: #AE0000\">");
html.append("&nbsp;<font color=\"white\"><b>Hoke Pool</b></font>&nbsp;");
html.append("</td></tr></table></td></tr>");
html.append("");
html.append("<tr><td height=\"2px\" style=\"background-color:#AE0000\"></td></tr>");
html.append("<tr><td>");
html.append("<table width=\"100%\">");
html.append("<tr style=\"background-color: #F0F0F0;\">");
html.append("<td align=\"left\">&nbsp;</td>");
html.append("<td align=\"left\"><b>poolKey</b></td>");
html.append("<td align=\"left\"><b>takeTime</b></td>");
html.append("<td align=\"left\"><b>status</b></td>");
html.append("</tr>");
for (Map.Entry<String, HokeDataPackage> entry : HokePool.getPool().entrySet()) {
HokeDataPackage hokeDataPackage = entry.getValue();
html.append("<tr>");
html.append("<td align=\"left\">&nbsp;</td>");
html.append("<td align=\"left\">"+hokeDataPackage.getPoolKey()+"</td>");
html.append("<td align=\"left\">"+hokeDataPackage.getTakeTime()+"</td>");
if (hokeDataPackage.getThrowable() == null){
html.append("<td align=\"left\">正常</td>");
} else {
html.append("<td align=\"left\">报错</td>");
}
html.append("</tr>");
}
html.append("</table>");
html.append("</td></tr><tr><td>&nbsp;</td></tr>");
html.append("");
html.append("</table>");
html.append("</body>");
html.append("</html>");
printHtml(response, html.toString());
}
}
