package org.axe.hoke.captain.helper;

import org.axe.helper.base.ConfigHelper;
import org.axe.hoke.captain.constant.HokeCaptainConfigConstant;
import org.axe.util.PropsUtil;

public final class HokeCaptainConfigHelper {


	
	/**
	 * 获取proxy策略类
	 */
	public static String getCaptainProxyStrategy(){
		return PropsUtil.getString(ConfigHelper.getCONFIG_PROPS(), HokeCaptainConfigConstant.HOKE_CAPTAIN_PROXY_STRATEGY, "org.axe.hoke.captain.strategy.DefaultHokeCaptainProxyStrategy");
	}
	

	/**
	 * 获取清理数据策略类
	 */
	public static String getCaptainClearDataStrategy(){
		return PropsUtil.getString(ConfigHelper.getCONFIG_PROPS(), HokeCaptainConfigConstant.HOKE_CAPTAIN_CLEAR_DATA_STRATEGY, "org.axe.hoke.captain.strategy.DefaultHokeCaptainClearDataStrategy");
	}
	
	/**
	 * 获取清理数据策略类
	 */
	public static String getManClearDataStrategy(){
		return PropsUtil.getString(ConfigHelper.getCONFIG_PROPS(), HokeCaptainConfigConstant.HOKE_MAN_CLEAR_DATA_STRATEGY, "org.axe.hoke.captain.strategy.DefaultHokeManClearDataStrategy");
	}
}
