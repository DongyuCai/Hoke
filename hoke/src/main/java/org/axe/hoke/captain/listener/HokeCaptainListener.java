package org.axe.hoke.captain.listener;

import org.axe.helper.ioc.BeanHelper;
import org.axe.hoke.captain.impl.HokeCaptain;
import org.axe.hoke.captain.interface_.HokeCaptainStrategy;
import org.axe.hoke.helper.HokeConfigHelper;
import org.axe.interface_.mvc.Listener;
import org.axe.util.ClassUtil;
import org.axe.util.ReflectionUtil;

public class HokeCaptainListener implements Listener{

	private Boolean inited = false;
	
	@Override
	public void init() throws Exception {
		//#初始化HokeCaptainStrategy配置
		//#不需要重复初始化
		if(!inited){
			synchronized (inited) {
				if(!inited){
					inited = true;
					//#初始化开发者自我实现的Captain
					String hokeCaptainStrategyPath = HokeConfigHelper.getCaptainStrategy();
					Class<?> hokeCaptainStrategyClass = ClassUtil.loadClass(hokeCaptainStrategyPath, false);
					HokeCaptainStrategy hokeCaptainStrategy = ReflectionUtil.newInstance(hokeCaptainStrategyClass);
					BeanHelper.getBean(HokeCaptain.class).setHokeCaptainStrategy(hokeCaptainStrategy);
				}
			}
		}
	}

	
}
