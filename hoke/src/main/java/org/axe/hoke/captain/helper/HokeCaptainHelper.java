package org.axe.hoke.captain.helper;

import org.axe.annotation.ioc.Component;
import org.axe.captain.bean.TeamTable;
import org.axe.captain.helper.CaptainHelper;
import org.axe.util.StringUtil;

/**
 * Hoke中负责处理与Captain相关的所有交互
 * Created by CaiDongYu on 2016年6月15日 下午5:02:28.
 */
@Component
public final class HokeCaptainHelper {
	
	//#打开Captain模式需要两个步骤，一个Captain地址，一个MyHost地址
	private boolean isCaptainOpened(){
		String captain = TeamTable.getCaptain();
		return StringUtil.isNotEmpty(captain);
	}
	
	public String askCaptainProxyUrl(String url){
		
		//#如果打开了captain模式
		if(isCaptainOpened()){
			try {
				String result = CaptainHelper.askCaptain(url);
				return result == null?"":result;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return "";
	}
}
