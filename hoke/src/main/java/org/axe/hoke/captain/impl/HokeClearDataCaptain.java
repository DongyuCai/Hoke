package org.axe.hoke.captain.impl;

import org.axe.captain.interface_.Captain;
import org.axe.hoke.captain.constant.HokeCaptainQuestionType;
import org.axe.hoke.captain.helper.HokeCaptainConfigHelper;
import org.axe.hoke.captain.interface_.HokeCaptainStrategy;
import org.axe.util.ClassUtil;
import org.axe.util.ReflectionUtil;
/**
 * Hoke 的负责清理数据 的 Captain，
 * 负责接受某个组员的清理请求，再将清理请求转发给其他组员
 * Created by CaiDongYu on 2016年6月17日 上午10:06:45.
 */
public class HokeClearDataCaptain implements Captain{
	
	private HokeCaptainStrategy hokeCaptainStrategy;
	{
		String hokeCaptainStrategyPath = HokeCaptainConfigHelper.getCaptainClearDataStrategy();
		Class<?> hokeCaptainStrategyClass = ClassUtil.loadClass(hokeCaptainStrategyPath, false);
		hokeCaptainStrategy = ReflectionUtil.newInstance(hokeCaptainStrategyClass);
	}
	

	@Override
	public String accpetQuestionType() {
		return HokeCaptainQuestionType.CLEAR_DATA;
	}
	
	@Override
	public Object answerQuestion(String hostAndPoolKey) {
		return hokeCaptainStrategy.doStrategy(hostAndPoolKey);
	}

}
