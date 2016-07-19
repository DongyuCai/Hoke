package org.axe.hoke.captain.strategy;

import org.axe.hoke.captain.interface_.HokeCaptainStrategy;
import org.axe.hoke.core.HokePool;

/**
 * 默认的 负责清理数据的Man的策略实现
 * Created by CaiDongYu on 2016年6月16日 上午10:09:50.
 */
public class DefaultHokeManClearDataStrategy implements HokeCaptainStrategy{

	@Override
	public String doStrategy(String poolKey) {
		//#根据poolKey清理数据
		boolean success = HokePool.removeHokeData(poolKey);
		if(success){
			return "1";
		}else{
			return "0";
		}
	}
}
