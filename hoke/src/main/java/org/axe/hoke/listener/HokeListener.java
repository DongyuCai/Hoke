package org.axe.hoke.listener;

import org.axe.hoke.thread.HokeThread;
import org.axe.interface_.mvc.Listener;

public class HokeListener implements Listener{
//	private Logger LOGGER = LoggerFactory.getLogger(HokeListener.class);
	
	private Boolean inited = false;

	@Override
	public void init() {
		if(!inited){
			synchronized (inited) {
				if(!inited){
					inited = true;
					//#启动hoke守护线程
					//#这部分不加入重新载入axe.properties重新初始化
					HokeThread.startHokeThread();
					
					System.out.println(">>>>>>>>>\t Axe-Hoke started success! \t<<<<<<<<<<");
					System.out.println(">>>>>>>>>\t Manager is \"/axe-hoke\"  \t<<<<<<<<<<");
				}
			}
		}
	}
}
