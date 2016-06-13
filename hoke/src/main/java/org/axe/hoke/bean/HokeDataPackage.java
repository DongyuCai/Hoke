package org.axe.hoke.bean;

import java.lang.reflect.Method;

import org.axe.hoke.constant.HokeDataStatus;
import org.axe.hoke.helper.HokeStorageHelper;
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
	 * Hoke POOL中存放的key
	 */
	private String poolKey;
	
	/**
	 * 反射用
	 */
	private Object obj;
	private Object[] params;
	private Method method;
    private MethodProxy methodProxy;
	/**
	 * 超时时间
	 */
	private long timeOut;
	
	/**
	 * 执行结果
	 */
	private Object data = null;
	/**
	 * 执行异常
	 */
	private Throwable throwable = null;
	/**
	 * 更新到磁盘的时间
	 */
	private long flushDiskTime = 0;
	/**
	 * 数据从磁盘到内存的时间
	 */
	private long flushMemTime = 0;
	/**
	 * 统计耗时
	 * 暂无实际用处，给debug统计用
	 */
	private long takeTime = 0;
	/**
	 * 状态
	 */
	private HokeDataStatus status = HokeDataStatus.WAITING;
	
	public HokeDataPackage(
			String poolKey, 
			Object obj, 
			Object[] params, 
			Method method, 
			MethodProxy methodProxy, 
			long timeOut) {
		this.poolKey = poolKey;
		this.obj = obj;
		this.params = params;
		this.method = method;
		this.methodProxy = methodProxy;
		this.timeOut = timeOut;
	}
	
	/**
	 * 主要方法
	 * 刷新单体Hoke数据
	 */
	public void flushData() {
		long start = System.currentTimeMillis();
		try {
			this.status = HokeDataStatus.FLUSHING;
			Object data = methodProxy.invokeSuper(obj, params);
			HokeStorageHelper.saveData(poolKey, data);
			this.flushDiskTime = System.currentTimeMillis();
			this.throwable = null;
			this.status = HokeDataStatus.SUCCESS;
			this.takeTime = this.flushDiskTime - start;
		} catch (Throwable e) {
			this.status = HokeDataStatus.FAILED;
			this.throwable = e;
			e.printStackTrace();
			LOGGER.error("refresh data failed",e);
		}
	}
	
	public Object getData() throws Throwable {
		if(this.throwable != null){
			throw throwable;
		}
		if(this.data == null){
			this.data = HokeStorageHelper.getData(poolKey, method.getReturnType());
			if(data != null){
				//记录数据从磁盘拉取的时间
				this.flushMemTime = System.currentTimeMillis();
			}
		}
		return this.data;
	}

	public long getFlushDiskTimeOutTime(){
		return this.timeOut*1000+this.flushDiskTime;
	}

	public long getFlushMemTimeOutTime(){
		return this.timeOut*1000+this.flushMemTime;
	}
	
	public void clearData(){
		this.data = null;
	}
	
	public boolean isEmpty(){
		return this.data == null;
	}

	
	/**
	 * ************************** GET 方法 区域  ********************************
	 */

	public String getPoolKey() {
		return poolKey;
	}

	public Object[] getParams() {
		return params;
	}

	public Method getMethod() {
		return method;
	}

	public long getTimeOut() {
		return timeOut;
	}

	public Throwable getThrowable() {
		return throwable;
	}

	public long getFlushDiskTime() {
		return flushDiskTime;
	}

	public long getFlushMemTime() {
		return flushMemTime;
	}

	public long getTakeTime() {
		return takeTime;
	}

	public HokeDataStatus getStatus() {
		return status;
	}
	
	
}
