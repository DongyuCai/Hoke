package org.axe.hoke.listener;

import org.axe.hoke.thread.HokeThread;
import org.axe.interface_.mvc.Listener;

public class HokeListener implements Listener{

	@Override
	public void init() {
		//#启动hoke守护线程
		//#这部分不加入重新载入axe.properties重新初始化
		HokeThread.startHokeThread();
	}
}
