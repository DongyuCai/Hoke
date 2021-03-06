package org.axe.hoke.captain.impl;

import org.axe.captain.interface_.Captain;
import org.axe.hoke.captain.constant.HokeCaptainQuestionType;
import org.axe.hoke.captain.helper.HokeCaptainConfigHelper;
import org.axe.hoke.captain.interface_.HokeCaptainStrategy;
import org.axe.util.ClassUtil;
import org.axe.util.ReflectionUtil;
/**
 * Hoke 的负责代理的Captain，
 * 负责给请求返回一台Team表中的机器
 * Created by CaiDongYu on 2016年6月17日 上午10:06:45.
 */
public class HokeProxyCaptain implements Captain{
	
	private HokeCaptainStrategy hokeCaptainStrategy;
	{
		String hokeCaptainStrategyPath = HokeCaptainConfigHelper.getCaptainProxyStrategy();
		Class<?> hokeCaptainStrategyClass = ClassUtil.loadClass(hokeCaptainStrategyPath, false);
		hokeCaptainStrategy = ReflectionUtil.newInstance(hokeCaptainStrategyClass);
	}
	

	@Override
	public String accpetQuestionType() {
		return HokeCaptainQuestionType.PROXY;
	}
	
	@Override
	public Object answerQuestion(String url) {
		return hokeCaptainStrategy.doStrategy(url);
	}

}
