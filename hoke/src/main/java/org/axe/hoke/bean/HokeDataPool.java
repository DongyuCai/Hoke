package org.axe.hoke.bean;

import java.util.HashMap;

/**
 * 真正的缓存池
 * Created by CaiDongYu on 2016年6月18日 上午10:08:40.
 */
public final class HokeDataPool extends HashMap<String,HokeDataPackage>{
	private static final long serialVersionUID = 1L;

	private boolean fullGc = false;

	public boolean needFullGc() {
		return fullGc;
	}

	public void setFullGc(boolean fullGc) {
		this.fullGc = fullGc;
	}
}
