package org.axe.hoke.listener;

import org.axe.hoke.thread.HokeThread;
import org.axe.interface_.mvc.Listener;

public class HokeListener implements Listener{

	@Override
	public void init() {
		HokeThread.startHokeThread();
	}
}
