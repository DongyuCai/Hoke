package org.axe.hoke.captain.impl;

import org.axe.captain.interface_.Captain;
import org.axe.hoke.captain.constant.HokeCaptainQuestionType;
import org.axe.hoke.captain.interface_.HokeCaptainStrategy;
import org.axe.hoke.helper.HokeConfigHelper;
import org.axe.util.ClassUtil;
import org.axe.util.ReflectionUtil;

public class HokeProxyCaptain implements Captain{
	
	private HokeCaptainStrategy hokeCaptainStrategy;
	{
		String hokeCaptainStrategyPath = HokeConfigHelper.getCaptainStrategy();
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
