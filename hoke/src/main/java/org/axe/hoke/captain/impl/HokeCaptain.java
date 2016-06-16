package org.axe.hoke.captain.impl;

import org.axe.annotation.ioc.Autowired;
import org.axe.annotation.ioc.Component;
import org.axe.captain.interface_.Captain;
import org.axe.hoke.captain.interface_.HokeCaptainStrategy;

@Component
public class HokeCaptain implements Captain{
	
	@Autowired
	private HokeCaptainStrategy hokeCaptainStrategy;
	
	public void setHokeCaptainStrategy(HokeCaptainStrategy hokeCaptainStrategy) {
		this.hokeCaptainStrategy = hokeCaptainStrategy;
	}

	@Override
	public Object answerQuestion(String url) {
		return hokeCaptainStrategy.doStrategy(url);
	}
}
