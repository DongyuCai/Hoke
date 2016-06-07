package org.axe.hoke.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hoke 的守护线程
 * 负责维护 HokePool 中的数据的新鲜 
 * Created by CaiDongYu on 2016年6月7日 下午12:19:26.
 */
public final class HokeThread {

	private static Thread HOKE_THREAD;
	
	public static synchronized void startHokeThread(){
		if(HOKE_THREAD == null){
			HOKE_THREAD = new Thread("HOKE_THREAD"){
				private Logger LOGGER = LoggerFactory.getLogger(HokeThread.class);
				
				@Override
				public void run() {
					while(true){
						try {
							HokePool.flush();
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
							LOGGER.error("Hoke thread error: ",e);
						}
					}
				}
			};
			HOKE_THREAD.start();
		}
	}
}
