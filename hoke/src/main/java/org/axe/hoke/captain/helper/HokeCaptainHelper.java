package org.axe.hoke.captain.helper;

import java.util.HashMap;
import java.util.Map;

import org.axe.captain.bean.TeamTable;
import org.axe.captain.helper.CaptainHelper;
import org.axe.captain.helper.ManHelper;
import org.axe.helper.ioc.BeanHelper;
import org.axe.hoke.captain.constant.HokeCaptainQuestionType;
import org.axe.util.JsonUtil;
import org.axe.util.StringUtil;

/**
 * Hoke中负责处理与Captain相关的所有交互
 * Created by CaiDongYu on 2016年6月15日 下午5:02:28.
 */
public final class HokeCaptainHelper {
	
	private static CaptainHelper captainHelper = BeanHelper.getBean(CaptainHelper.class);
	private static ManHelper manHelper = BeanHelper.getBean(ManHelper.class);
	
	//#打开Captain模式需要两个步骤，一个Captain地址，一个MyHost地址
	private static boolean isCaptainOpened(){
		String captain = TeamTable.getCaptain();
		return StringUtil.isNotEmpty(captain);
	}
	
	/**
	 * 询问Captain 代理后的host
	 */
	public static String askCaptain4ProxyUrl(String url){
		
		//#如果打开了captain模式
		if(isCaptainOpened() && captainHelper != null){
			try {
				String result = captainHelper.askCaptain(HokeCaptainQuestionType.PROXY,url);
				return result == null?"":result;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return "";
	}
	
	/**
	 * 向Captain发起清理Hoke data请求
	 */
	public static String askCaptain4ClearData(String poolKey){
		if(isCaptainOpened() && captainHelper != null){
			try {
				Map<String,String> map = new HashMap<>();
				//#如果是组员，那么host是自己的host，如果是Captain，myhost是空的，但是没问题，只要获取Captain就可以
				String host = StringUtil.isNotEmpty(TeamTable.myHost)?TeamTable.myHost:TeamTable.getCaptain();
				map.put("host", host);
				map.put("poolKey", poolKey);
				String question = JsonUtil.toJson(map);
				String result = captainHelper.askCaptain(HokeCaptainQuestionType.CLEAR_DATA,question);
				return result == null?"":result;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return "";
	}
	

	/**
	 * 向Man发起清理Hoke data请求
	 */
	public static String askMan4ClearData(String manHost, String poolKey){
		String result = "";
		if(isCaptainOpened() && manHelper != null){
			result = manHelper.askMan(manHost, HokeCaptainQuestionType.CLEAR_DATA, poolKey);
		}
		return result == null?"":result;
	}
}
