package org.axe.hoke.captain.interface_;

/**
 * Hoke Captain 策略接口，
 * 预留给开发者扩展，
 * axe.properties参数为：axe.hoke.captain.strategy
 * Created by CaiDongYu on 2016年6月16日 上午10:15:08.
 */
public interface HokeCaptainStrategy {

	/**
	 * 现在你是captain，
	 * 根据组员提问过来的问题：就是url，自己想办法给个答案
	 */
	public String doStrategy(String url);
}
