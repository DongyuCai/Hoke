package org.axe.hoke.home.rest;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import org.axe.util.JsonUtil;
import org.axe.util.StringUtil;

@FilterFuckOff
@Interceptor({ HomeInterceptor.class, SignInInterceptor.class })
@Controller(basePath = "hoke")
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
	public void home(@RequestParam("token") String token, HttpServletRequest request, HttpServletResponse response) {
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
		html.append("window.location = \"" + contextPath + "/hoke?token=" + token + "\";");
		html.append("}");
		html.append("</script>");
		html.append("</head>");
		html.append("<body>");
		html.append("<table width=\"100%\">");
		html.append("<tr><td align=\"right\">");
		html.append("<a style=\"font-size: 15px;color: #AE0000\" href=\"" + contextPath + "/axe?token=" + token
				+ "\"><b>axe</b></a>");
		html.append("</td></tr>");
		html.append("<tr><td align=\"center\"><font size=\"28\">欢迎使用 Axe Hoke!</font></td></tr>");
		html.append("");
		html.append("<!--系统运行 概览-->");
		html.append("<tr><td><table cellspacing=\"0px\"><tr><td style=\"background-color: #AE0000\">");
		html.append("&nbsp;<font color=\"white\"><b>Hoke</b></font>&nbsp;");
		html.append("</td></tr></table></td></tr>");
		html.append("");
		html.append("<tr><td height=\"2px\" style=\"background-color:#AE0000\"></td></tr>");
		html.append("<tr><td>");
		html.append("<table width=\"100%\">");
		html.append("<tr style=\"background-color: #F0F0F0;\">");
		html.append("<td align=\"left\">&nbsp;</td>");
		html.append("<td align=\"left\"><b>Hoke 个数</b></td>");
		html.append("<td align=\"left\"><b>Hoke 总耗时</b></td>");
		html.append("</tr>");
		long runTimeMS = 0;
		Map<String, HokeDataPackage> pool = HokePool.getPool();
		int dataNum = 0;
		for (Map.Entry<String, HokeDataPackage> entry : pool.entrySet()) {
			HokeDataPackage hokeDataPackage = entry.getValue();
			runTimeMS = runTimeMS + hokeDataPackage.getTakeTime();

			if (!hokeDataPackage.isEmpty()) {
				dataNum++;// 有数据的，记录下个数
			}
		}

		String runTime = "";
		if (runTimeMS < 1000) {
			runTime = runTimeMS + "ms";
		} else {
			long runTimeSec = runTimeMS / 1000;
			if (runTimeSec < 60) {
				runTime = runTimeSec + "s";
			} else {
				long runTimeMin = runTimeSec / 60;
				if (runTimeMin < 60) {
					runTimeSec = runTimeSec - (runTimeMin * 60);
					runTime = runTimeMin + "m" + runTimeSec + "s";
				} else {
					long runTimeHour = runTimeMin / 60;
					if (runTimeHour < 24) {
						runTimeMin = runTimeMin - runTimeHour * 60;
						runTimeSec = runTimeSec - ((runTimeHour * 60) + runTimeMin) * 60;
						runTime = runTimeHour + "h" + runTimeMin + "m" + runTimeSec + "s";
					} else {
						long runTimeDay = runTimeHour / 24;
						runTimeHour = runTimeHour - (runTimeDay * 24);
						runTimeMin = runTimeMin - ((runTimeDay * 24) + runTimeHour) * 60;
						runTimeSec = runTimeSec - ((((runTimeDay * 24) + runTimeHour) * 60) + runTimeMin) * 60;
						runTime = runTimeDay + "d" + runTimeHour + "h" + runTimeMin + "m" + runTimeSec + "s";
					}
				}
			}
		}
		html.append("<tr>");
		html.append("<td align=\"left\">&nbsp;</td>");
		html.append("<td align=\"left\">" + dataNum + "/" + pool.size() + "</td>");
		html.append("<td align=\"left\">" + runTime + "</td>");
		html.append("</tr>");
		html.append("</table>");
		html.append("</td></tr><tr><td>&nbsp;</td></tr>");
		html.append("");
		html.append("<!--Hoke POOL-->");
		html.append("<tr><td><table cellspacing=\"0px\"><tr><td style=\"background-color: #AE0000\">");
		html.append("&nbsp;<font color=\"white\"><b>Hoke Pool</b></font>&nbsp;");
		html.append("</td></tr></table></td></tr>");
		html.append("");
		html.append("<tr><td height=\"2px\" style=\"background-color:#AE0000\"></td></tr>");
		html.append("<tr><td>");
		html.append("<table width=\"100%\">");
		html.append("<tr style=\"background-color: #F0F0F0;\">");
		html.append("<td align=\"left\">&nbsp;</td>");
		html.append("<td align=\"left\"><b>Hoke Method</b></td>");
		html.append("<td align=\"left\"><b>timeOut(s)</b></td>");
		html.append("<td align=\"left\"><b>takeTime(ms)</b></td>");
		html.append("<td align=\"left\"><b>status</b></td>");
		html.append("<td align=\"left\"><b>操作</b></td>");
		html.append("</tr>");
		int index = 1;
		for (Map.Entry<String, HokeDataPackage> entry : HokePool.getPool().entrySet()) {
			HokeDataPackage hokeDataPackage = entry.getValue();
			html.append("<tr>");
			html.append("<td align=\"left\">" + index++ + "</td>");
			html.append("<td align=\"left\"><a href=\"" + contextPath + "/hoke/hoke_data_package/view?poolKey="
					+ hokeDataPackage.getPoolKey() + "&token=" + token + "\">" + hokeDataPackage.getPoolKey()
					+ "</a></td>");
			html.append("<td align=\"left\">" + hokeDataPackage.getTimeOut() + "</td>");
			html.append("<td align=\"left\">" + hokeDataPackage.getTakeTime() + "</td>");
			html.append("<td align=\"left\">" + hokeDataPackage.getStatus().desc + "</td>");
			html.append("<td align=\"left\"><a href=\"" + contextPath + "/hoke/hoke_data_package/delete?poolKey="
					+ hokeDataPackage.getPoolKey() + "&token=" + token + "\">删除</td>");
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

	@Request(value = "/hoke_data_package/view", method = RequestMethod.GET)
	public void hokeDataPackageView(@RequestParam("poolKey") String poolKey, @RequestParam("token") String token,
			HttpServletRequest request, HttpServletResponse response) {
		String contextPath = request.getContextPath();
		do {
			if (StringUtil.isEmpty(poolKey))
				break;
			Map<String, HokeDataPackage> pool = HokePool.getPool();
			HokeDataPackage hokeDataPackage = pool.get(poolKey);
			if (hokeDataPackage == null)
				break;
			StringBuilder html = new StringBuilder();
			html.append("<!DOCTYPE html>");
			html.append("<html>");
			html.append("<head>");
			html.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />");
			html.append("<title>hoke data package view</title>");
			html.append("</head>");
			html.append("<body>");
			html.append("<table width=\"100%\">");
			html.append("<tr><td align=\"right\">");
			html.append("<a style=\"font-size: 15px;color: #AE0000\" href=\"" + contextPath + "/hoke?token=" + token
					+ "\"><b>Hoke</b></a>");
			html.append("</td></tr>");
			html.append("<tr><td align=\"center\"><font size=\"28\">Hoke Data Package Detail</font></td></tr>");
			html.append("");
			html.append("<tr><td><table cellspacing=\"0px\"><tr><td style=\"background-color: #AE0000\">");
			html.append("&nbsp;<font color=\"white\"><b>Package Detail</b></font>&nbsp;");
			html.append("</td></tr></table></td></tr>");
			html.append("<tr><td height=\"2px\" style=\"background-color: #AE0000\"></td></tr>");
			html.append("<tr><td>");
			html.append("<table width=\"100%\">");
			html.append("<tr style=\"background-color: #F0F0F0;\">");
			html.append("<td align=\"left\">&nbsp;</td>");
			html.append("<td align=\"left\"><b>属性</b></td>");
			html.append("<td align=\"left\"><b>值</b></td>");
			html.append("</tr>");
			html.append("<tr>");
			html.append("<td align=\"left\">&nbsp;</td>");
			html.append("<td align=\"left\">状态</td>");
			html.append("<td align=\"left\">" + hokeDataPackage.getStatus().desc + "</td>");
			html.append("</tr>");
			html.append("<tr>");
			html.append("<td align=\"left\">&nbsp;</td>");
			html.append("<td align=\"left\">method</td>");
			html.append("<td align=\"left\">" + hokeDataPackage.getMethod().toString() + "</td>");
			html.append("</tr>");
			html.append("<tr>");
			html.append("<td align=\"left\">&nbsp;</td>");
			html.append("<td align=\"left\">params</td>");
			html.append("<td align=\"left\">");
			if (hokeDataPackage.getParams() != null) {
				html.append("<table>");
				for (Object param : hokeDataPackage.getParams()) {
					html.append("<tr><td>" + JsonUtil.toJson(param) + "</td></tr>");
				}
				html.append("</table>");
			}
			html.append("</td>");
			html.append("</tr>");
			html.append("<tr>");
			html.append("<td align=\"left\">&nbsp;</td>");
			html.append("<td align=\"left\">timeOut</td>");
			html.append("<td align=\"left\">" + hokeDataPackage.getTimeOut() + "秒</td>");
			html.append("</tr>");
			html.append("<tr>");
			html.append("<td align=\"left\">&nbsp;</td>");
			html.append("<td align=\"left\">data</td>");
			String dataJson = "";
			try {
				Object data = hokeDataPackage.getData();
				if (data != null) {
					dataJson = JsonUtil.toJson(data);
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
			html.append("<td align=\"left\">" + dataJson + "</td>");
			html.append("</tr>");
			html.append("<tr>");
			html.append("<td align=\"left\">&nbsp;</td>");
			html.append("<td align=\"left\">throwable</td>");
			String throwableStr = "";
			if (hokeDataPackage.getThrowable() != null) {
				throwableStr = hokeDataPackage.getThrowable().getMessage();
			}
			html.append("<td align=\"left\">" + throwableStr + "</td>");
			html.append("</tr>");
			html.append("<tr>");
			html.append("<td align=\"left\">&nbsp;</td>");
			html.append("<td align=\"left\">磁盘刷新时间</td>");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String flushDiskTime = "";
			if (hokeDataPackage.getFlushDiskTime() > 0) {
				flushDiskTime = sdf.format(new Date(hokeDataPackage.getFlushDiskTime()));
			}
			html.append("<td align=\"left\">" + flushDiskTime + "</td>");
			html.append("</tr>");
			html.append("<tr>");
			html.append("<td align=\"left\">&nbsp;</td>");
			html.append("<td align=\"left\">内存刷新时间</td>");
			String flushMemTime = "";
			if (hokeDataPackage.getFlushMemTime() > 0) {
				flushMemTime = sdf.format(new Date(hokeDataPackage.getFlushMemTime()));
			}
			html.append("<td align=\"left\">" + flushMemTime + "</td>");
			html.append("</tr>");
			html.append("<tr>");
			html.append("<td align=\"left\">&nbsp;</td>");
			html.append("<td align=\"left\">最后一次耗时</td>");
			html.append("<td align=\"left\">" + hokeDataPackage.getTakeTime() + "毫秒</td>");
			html.append("</tr>");
			html.append("</table>");
			html.append("</td></tr><tr><td>&nbsp;</td></tr>");
			html.append("");
			html.append("</table>");
			html.append("</body>");
			html.append("</html>");
			printHtml(response, html.toString());
		} while (false);
	}

	@Request(value = "/hoke_data_package/delete", method = RequestMethod.GET)
	public void hokeDataPackageDelete(@RequestParam("poolKey") String poolKey, @RequestParam("token") String token,
			HttpServletRequest request, HttpServletResponse response) {
		String contextPath = request.getContextPath();

		boolean success = HokePool.removeHokeData(poolKey);
		String result = "SUCCESS!";
		if (!success) {
			result = "FAILED!";
		}
		StringBuilder html = new StringBuilder();
		html.append("<!DOCTYPE html>");
		html.append("<html>");
		html.append("<head>");
		html.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />");
		html.append("<title>hoke data package delete</title>");
		html.append("<script type=\"text/javascript\">");
		html.append("var number = 10;");
		html.append("");
		html.append("var int=self.setInterval(\"toHome()\",1000);");
		html.append("");
		html.append("function toHome(){");
		html.append("number = number-1;");
		html.append("document.getElementById(\"number\").innerHTML = number;");
		html.append("if(number <= 0){");
		html.append("window.clearInterval(int);");
		html.append("window.location = \"" + contextPath + "/hoke?token=" + token + "\";");
		html.append("}");
		html.append("}");
		html.append("");
		html.append("</script>");
		html.append("</head>");
		html.append("<body>");
		html.append("<table width=\"100%\">");
		html.append("<tr><td align=\"right\">");
		html.append("<a style=\"font-size: 15px;color: #AE0000\" href=\"" + contextPath + "/hoke?token=" + token
				+ "\"><b>Hoke</b></a>");
		html.append("</td></tr>");
		html.append(
				"<tr><td align=\"center\"><font size=\"28\">Hoke Data Package Delete " + result + "</font></td></tr>");
		html.append("<tr><td align=\"center\"><span id=\"number\">10</span>秒后自动跳转<a href=\"" + contextPath
				+ "/hoke?token=" + token + "\">/hoke首页</a></td></tr>");
		html.append("</table>");
		html.append("</body>");
		html.append("</html>");
		printHtml(response, html.toString());
	}
}
