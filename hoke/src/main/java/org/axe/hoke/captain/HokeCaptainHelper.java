package org.axe.hoke.captain;

import org.axe.captain.bean.TeamTable;
import org.axe.captain.helper.CaptainHelper;
import org.axe.captain.helper.ManHelper;
import org.axe.util.JsonUtil;
import org.axe.util.StringUtil;

/**
 * Hoke中负责处理与Captain相关的所有交互
 * Created by CaiDongYu on 2016年6月15日 下午5:02:28.
 */
public final class HokeCaptainHelper {

	//#打开Captain模式需要两个步骤，一个Captain地址，一个MyHost地址
	private static boolean isCaptainOpened(){
		String captain = TeamTable.getCaptain();
		return StringUtil.isNotEmpty(captain);
	}
	
	public static Object getData(String poolKey, Class<?> dataType){
		
		//#如果打开了captain模式
		if(isCaptainOpened()){
			//#询问Captain，这个poolKey的数据谁有
			try {
				String manHost = CaptainHelper.askCaptain(poolKey);
				if(StringUtil.isNotEmpty(manHost)){
					String dataStr = ManHelper.askMan(manHost, poolKey);
					if(StringUtil.isNotEmpty(dataStr)){
						Object data = JsonUtil.fromJson(dataStr, dataType);
						return data;
					}
				}
			} catch (Exception e) {}
		}
		
		return null;
	}
}
