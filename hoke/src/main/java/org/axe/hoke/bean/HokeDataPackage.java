package org.axe.hoke.bean;

import java.lang.reflect.Method;

import org.axe.hoke.annotation.HokeConfig;
import org.axe.hoke.captain.helper.HokeCaptainHelper;
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
	 * HokeConfig配置
	 */
    private HokeConfig hokeConfig;
	
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
	private long flushDiskTime;
	/**
	 * 数据从磁盘到内存的时间
	 */
	private long flushMemTime;
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
			HokeConfig hokeConfig) {
		this.poolKey = poolKey;
		this.obj = obj;
		this.params = params;
		this.method = method;
		this.methodProxy = methodProxy;
		this.hokeConfig = hokeConfig;
		
		long now  = System.currentTimeMillis();
		this.flushDiskTime = now;
		this.flushMemTime = now;
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
				
				//#这里是Hoke代码唯一与Captain有联系的地方，删除Hoke里的captain包，这里会报错
				//#请求到这里取数据，说明其他组员的“这个数据”客户抛弃了
				//#而且必须是等到这里的磁盘真的准备好数据了，这样中间有段时间，其他组员还可以提供数据支持
				//#因为从这里娶不到数据，那么真正客户端请求的组员会自己缓存，等到这里准备好了，才会弃用那边的
				HokeCaptainHelper.askCaptain4ClearData(poolKey);
			}
		}
		return this.data;
	}

	public long getNextFlushDiskTime(){
		return this.hokeConfig.refreshSeconds()*1000+this.flushDiskTime;
	}

	public long getNextFlushMemTime(){
		return this.hokeConfig.refreshSeconds()*1000+this.flushMemTime;
	}
	
	public long getTimeOutTime(){
		if(this.hokeConfig.timeOut() <= 0){
			//#默认配置是0，但是小于0同样么有意义
			return 0;
		}else{
			return this.hokeConfig.timeOut()*1000+this.flushMemTime;
		}
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

	public HokeConfig getHokeConfig() {
		return hokeConfig;
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
