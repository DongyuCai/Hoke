/*package org.axe.hoke.captain.impl;

import org.axe.annotation.ioc.Component;
import org.axe.captain.interface_.Man;
import org.axe.hoke.core.HokePool;
import org.axe.util.JsonUtil;

@Component
public class HokeMan implements Man{

	@Override
	public Object answerQuestion(String poolKey) {
		return doYouHaveHokeData(poolKey);
	}
	
	private String doYouHaveHokeData(String poolKey){
		Object data = HokePool.getData(poolKey);
		String result = "";
		if(data != null){
			try {
				result = JsonUtil.toJson(data);
			} catch (Exception e) {}
		}
		return result;
	}

}
*/