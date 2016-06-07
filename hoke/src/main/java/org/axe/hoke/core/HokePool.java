package org.axe.hoke.core;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.axe.hoke.annotation.HokeConfig;
import org.axe.hoke.bean.HokeDataPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.cglib.proxy.MethodProxy;

/**
 * Hoke 数据池
 * 提供对数据的缓存与get
 * Created by CaiDongYu on 2016年6月7日 上午10:51:27.
 */
public final class HokePool {
	private static final Logger LOGGER = LoggerFactory.getLogger(HokePool.class);

	private static final Map<String,HokeDataPackage> POOL;
	
	static{
		POOL = new HashMap<>();
	}
	
	
	public static Object getData(Object obj,Method method, Object[] params, MethodProxy methodProxy){
    	//#根据参数，计算出数据存放的key
		String poolKey = generatePoolKey(method, params);
		Object data = POOL.get(poolKey);
		if(data == null){
			synchronized (POOL) {
				try {
					if(POOL.containsKey(poolKey)){
						data = POOL.get(poolKey);
					}else{
						//放着，等HokeThread更新
						HokeConfig config = method.getAnnotation(HokeConfig.class);
						POOL.put(poolKey, new HokeDataPackage(obj, params, methodProxy, config.timeOut(), 0, config.cacheFile()));
					}
				} catch (Exception e) {
					e.printStackTrace();
					LOGGER.error("Hoke get data failed: ",e);
				}
			}
			
			//#同步之后如果还是空的
			if(data == null){
				//#是否需要等待
				HokeConfig annotation = method.getAnnotation(HokeConfig.class);
				if(!annotation.lazy()){
					try {
						data = methodProxy.invokeSuper(obj, params);
					} catch (Throwable e) {
						e.printStackTrace();
						LOGGER.error("get data failed",e);
					}
				}
			}
		}
		
		return data != null && data instanceof HokeDataPackage?
					((HokeDataPackage)data).getData()
					:
					data;
	}
	
	/**
	 * 刷新POOL里的数据
	 */
	public static void flush(){
			try {
				//TODO:这里是单线程刷新所有数据，有待改成数据线程一对一
				for(Map.Entry<String, HokeDataPackage> entry:POOL.entrySet()){
					HokeDataPackage dataPackage = entry.getValue();
					long now = System.currentTimeMillis();
					//#超时判断
					if(dataPackage.getTimeOutTime() < now){
						synchronized (POOL) {
							dataPackage.refreshData();
						}
					}
					now = System.currentTimeMillis()-now;
					LOGGER.debug("Hoke flush ["+entry.getKey()+"] "+(now)+"ms");
				}
				Thread.sleep(1000);
			} catch (Exception e) {
				e.printStackTrace();
				LOGGER.error("Hoke flush failed: ",e);
			}
	}
	
	private static String generatePoolKey(Method method, Object[] params){
		StringBuilder poolKey = new StringBuilder();
		poolKey.append(method.toString()).append("-");
		int hashCode = 0;
		for(Object obj:params){
			hashCode = hashCode + obj.hashCode();
		}
		poolKey.append(hashCode);
		return poolKey.toString();
	}
}
