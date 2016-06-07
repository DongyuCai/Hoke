package org.axe.hoke.bean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.cglib.proxy.MethodProxy;

/**
 * Hoke池中的 数据包
 * Created by CaiDongYu on 2016年6月7日 下午12:23:53.
 */
public class HokeDataPackage {
	private Logger LOGGER = LoggerFactory.getLogger(HokeDataPackage.class);
	
	/**
	 * 反射用
	 */
	private Object obj;
	private Object[] params;
    private MethodProxy methodProxy;
	
	/**
	 * 执行结果
	 */
	private Object data;
	/**
	 * 超时时间
	 */
	private long timeOut;
	/**
	 * 更新时间
	 */
	private long time;
	/**
	 * 缓存文件
	 */
	private String cacheFile;
	
	public HokeDataPackage(Object obj, Object[] params, MethodProxy methodProxy, long timeOut, long time,
			String cacheFile) {
		this.obj = obj;
		this.params = params;
		this.methodProxy = methodProxy;
		this.timeOut = timeOut;
		this.time = time;
		this.cacheFile = cacheFile;
	}
	
	/**
	 * 主要方法
	 * 刷新单体Hoke数据
	 * TODO:如果有缓存策略，根据策略保存缓存数据
	 */
	public void refreshData() {
		try {
			this.data = methodProxy.invokeSuper(obj, params);
			this.time = System.currentTimeMillis();
		} catch (Throwable e) {
			e.printStackTrace();
			LOGGER.error("refresh data failed",e);
		}
	}

	/**
	 * 主要方法
	 * 返回单体数据
	 * TODO:如果是空的，从缓存文件更新数据
	 */
	public Object getObj() {
		return obj;
	}

	public Object[] getParams() {
		return params;
	}
	
	public MethodProxy getMethodProxy() {
		return methodProxy;
	}

	public Object getData() {
		return data;
	}

	public long getTimeOut() {
		return timeOut;
	}

	public long getTime() {
		return time;
	}

	public String getCacheFile() {
		return cacheFile;
	}
	
	public long getTimeOutTime(){
		return getTimeOut()*1000+getTime();
	}
}
