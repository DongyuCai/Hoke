package org.axe.hoke.captain.impl;

import org.axe.captain.interface_.Man;
import org.axe.hoke.captain.constant.HokeCaptainQuestionType;
import org.axe.hoke.captain.helper.HokeCaptainConfigHelper;
import org.axe.hoke.captain.interface_.HokeCaptainStrategy;
import org.axe.util.ClassUtil;
import org.axe.util.ReflectionUtil;

/**
 * Hoke 的负责清理内存的Man，
 * 
 * Created by CaiDongYu on 2016年6月17日 上午10:07:25.
 */
public final class HokeClearDataMan implements Man{
	
	private HokeCaptainStrategy hokeCaptainStrategy;
	{
		String hokeCaptainStrategyPath = HokeCaptainConfigHelper.getManClearDataStrategy();
		Class<?> hokeCaptainStrategyClass = ClassUtil.loadClass(hokeCaptainStrategyPath, false);
		hokeCaptainStrategy = ReflectionUtil.newInstance(hokeCaptainStrategyClass);
	}
	

	@Override
	public String accpetQuestionType() {
		return HokeCaptainQuestionType.CLEAR_DATA;
	}

	@Override
	public Object answerQuestion(String poolKey) {
		return hokeCaptainStrategy.doStrategy(poolKey);
	}

}
