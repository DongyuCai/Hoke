package org.axe.hoke.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hoke 的守护线程
 * 负责维护 HokePool 中的数据的新鲜 
 * Created by CaiDongYu on 2016年6月7日 下午12:19:26.
 */
public final class HokeThread {

	private static Thread HOKE_POOL_THREAD;
	private static Thread HOKE_POOL_TASK_QUEE_THREAD;
	
	public static synchronized void startHokeThread(){
		if(HOKE_POOL_THREAD == null){
			HOKE_POOL_THREAD = new Thread("HOKE_POOL_THREAD"){
				private Logger LOGGER = LoggerFactory.getLogger(HokeThread.class);
				
				@Override
				public void run() {
					while(true){
						try {
							HokePool.flushPool();
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
							LOGGER.error("HOKE_POOL_THREAD error: ",e);
						}
					}
				}
			};
			HOKE_POOL_THREAD.start();
		}
		//#同步POOL_KEY任务线程
		if(HOKE_POOL_TASK_QUEE_THREAD == null){
			HOKE_POOL_TASK_QUEE_THREAD = new Thread("HOKE_POOL_TASK_QUEE_THREAD"){
				private Logger LOGGER = LoggerFactory.getLogger(HokeThread.class);
				
				@Override
				public void run() {
					while(true){
						try {
							HokePool.flushTaskQuee();
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
							LOGGER.error("HOKE_POOL_TASK_QUEE_THREAD error: ",e);
						}
					}
				}
			};
			HOKE_POOL_TASK_QUEE_THREAD.start();
		}
	}
}
