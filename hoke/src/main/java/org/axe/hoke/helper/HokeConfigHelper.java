package org.axe.hoke.helper;

import org.axe.helper.base.ConfigHelper;
import org.axe.hoke.constant.HokeConfigConstant;
import org.axe.util.PropsUtil;

/**
 * Hoke 配置 助手类
 * Created by CaiDongYu on 2016年6月8日 下午12:54:45.
 */
public final class HokeConfigHelper{
	
	/**
	 * 获取配置的缓存文件路径
	 */
	public static String getCacheFileDir(){
		return PropsUtil.getString(ConfigHelper.getCONFIG_PROPS(), HokeConfigConstant.HOKE_CACHE_FILE_DIR, HokeConfigConstant.CACHE_FILE_DIR_DEFAULT);
	}
	
	/**
	 * 获取配置的缓存文件路径
	 */
	public static String getCaptainStrategy(){
		return PropsUtil.getString(ConfigHelper.getCONFIG_PROPS(), HokeConfigConstant.HOKE_CAPTAIN_STRATEGY, "org.axe.hoke.captain.strategy.DefaultHokeCaptainStrategy");
	}
}
