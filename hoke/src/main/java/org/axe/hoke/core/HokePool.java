package org.axe.hoke.core;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.axe.hoke.annotation.HokeConfig;
import org.axe.hoke.bean.HokeDataPackage;
import org.axe.hoke.bean.KeyValue;
import org.axe.hoke.helper.HokeStorageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.cglib.proxy.MethodProxy;

/**
 * Hoke 数据池 提供对数据的缓存与get Created by CaiDongYu on 2016年6月7日 上午10:51:27.
 */
public final class HokePool {
	private static final Logger LOGGER = LoggerFactory.getLogger(HokePool.class);

	/**
	 * 真正的缓存
	 */
	private static final Map<String, HokeDataPackage> POOL;

	/**
	 * 等待被缓存的键
	 */
	private static final List<KeyValue<String, HokeDataPackage>> POOL_TASK_QUEE;

	static {
		POOL = new HashMap<>();
		POOL_TASK_QUEE = new ArrayList<>();
		init();
	}
	
	public static void init(){
		synchronized (POOL) {
			synchronized (POOL_TASK_QUEE) {
				POOL.clear();
				POOL_TASK_QUEE.clear();
				HokeStorageHelper.clear();
			}
		}
	}

	public static Map<String, HokeDataPackage> getPool() {
		return POOL;
	}
	
	public static Object getData(String poolKey){
		HokeDataPackage hokeDataPackage = POOL.get(poolKey);
		try {
			return hokeDataPackage == null?null:hokeDataPackage.getData();
		} catch (Throwable e) {
			return null;
		}
	}
	
	public static Object getData(Object obj, Method method, Object[] params, MethodProxy methodProxy) throws Throwable {
		// #根据参数，计算出数据存放的key
		String poolKey = generatePoolKey(method, params);
		HokeDataPackage hokeDataPackage = POOL.get(poolKey);
		if (hokeDataPackage == null) {
			/*
			//#如果Captain开着
			//#先询问Captain试试，如果有结果最好
			Object data = HokeCaptainHelper.getData(poolKey, method.getReturnType());
			if(data != null){
				return data;
			}else{
				addTask2Quee(poolKey, method, obj, params, methodProxy);
			}*/
			
			addTask2Quee(poolKey, method, obj, params, methodProxy);
			
			/*
			 * synchronized (POOL) { try { if(POOL.containsKey(poolKey)){ data =
			 * POOL.get(poolKey); }else{ //放着，等HokeThread更新 HokeConfig config =
			 * method.getAnnotation(HokeConfig.class); POOL.put(poolKey, new
			 * HokeDataPackage(obj, params, methodProxy, config.timeOut(), 0,
			 * config.cacheFile())); } } catch (Exception e) {
			 * e.printStackTrace(); LOGGER.error("Hoke get data failed: ",e); }
			 * }
			 */

			
			return getNativeData(method, obj, params, methodProxy);
		}else{
			Object data = hokeDataPackage.getData();
			if(data == null){
				return getNativeData(method, obj, params, methodProxy);
			}else{
				return data;
			}
		}
	}
	
	private static Object getNativeData(Method method, Object obj, Object[] params, MethodProxy methodProxy) throws Throwable{
		// #缓存无法提供，如果还不支持异步加载，那就强制获取
		HokeConfig annotation = method.getAnnotation(HokeConfig.class);
		if (!annotation.lazy()) {
			return methodProxy.invokeSuper(obj, params);
		}else{
			return null;
		}
	}
	
	private static void addTask2Quee(String poolKey, Method method, Object obj, Object[] params, MethodProxy methodProxy){
		HokeConfig config = method.getAnnotation(HokeConfig.class);
		long start = 0;
		if (LOGGER.isInfoEnabled()) {
			start = System.currentTimeMillis();
			LOGGER.info("Hoke add task try");
		}
		synchronized (POOL_TASK_QUEE) {
			if (LOGGER.isInfoEnabled()) {
				long end = System.currentTimeMillis() - start;
				LOGGER.info("Hoke add task start " + end + "ms");
			}
			POOL_TASK_QUEE.add(new KeyValue<String, HokeDataPackage>(poolKey,
					new HokeDataPackage(poolKey, obj, params, method, methodProxy, config.timeOut())));
		}
		if (LOGGER.isInfoEnabled()) {
			long end = System.currentTimeMillis() - start;
			LOGGER.info("Hoke add task finished " + end + "ms");
		}
	}

	/**
	 * 刷新taskQuee到Poll里
	 */
	public static void flushTaskQuee() {
		try {
			long start = 0;
			if (LOGGER.isInfoEnabled()) {
				start = System.currentTimeMillis();
				LOGGER.info("Hoke flush task quee try");
			}
			synchronized (POOL) {
				if (LOGGER.isInfoEnabled()) {
					long end = System.currentTimeMillis() - start;
					LOGGER.info("Hoke flush task quee start " + end + "ms");
				}
				// ##只要抢到了锁，这段代码执行时间会较短
				synchronized (POOL_TASK_QUEE) {
					for (KeyValue<String, HokeDataPackage> task : POOL_TASK_QUEE) {
						String key = task.getKey();
						if (POOL.containsKey(key))
							continue;

						POOL.put(key, task.getValue());
					}
					// 清空队列
					POOL_TASK_QUEE.clear();
				}
			}
			if (LOGGER.isInfoEnabled()) {
				long end = System.currentTimeMillis() - start;
				LOGGER.info("Hoke flush task quee finished " + end + "ms");
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("Hoke flush task quee failed: ", e);
		}
	}

	/**
	 * 刷新POOL里的数据
	 */
	public static void flushPool() {
		try {

			long start = 0;
			if (LOGGER.isInfoEnabled()) {
				start = System.currentTimeMillis();
				LOGGER.info("Hoke flush pool try");
			}
			synchronized (POOL) {
				if (LOGGER.isInfoEnabled()) {
					long end = System.currentTimeMillis() - start;
					LOGGER.info("Hoke flush pool start " + end + "ms");
				}

				// #刷数据
				
				//#刷新POOL总共耗时
				long takeTime = 0;
				// #有data数据的个数
				// TODO:这里是单线程刷新所有数据，有待改成数据线程一对一
				int dataNum = 0;
				for (Map.Entry<String, HokeDataPackage> entry : POOL.entrySet()) {
					HokeDataPackage hokeDataPackage = entry.getValue();
					long now = System.currentTimeMillis();
					// #内存数据超时判断
					if (hokeDataPackage.getFlushMemTimeOutTime() < now) {
						hokeDataPackage.clearData();
					}
					// #磁盘数据超时判断
					if (hokeDataPackage.getFlushDiskTimeOutTime() < now) {
						hokeDataPackage.flushData();
					}
					if (LOGGER.isInfoEnabled()) {
						LOGGER.info("Hoke flush [" + hokeDataPackage.getPoolKey() + "] " + (hokeDataPackage.getTakeTime()) + "ms");
						
						takeTime = takeTime+hokeDataPackage.getTakeTime();
						
						if (!hokeDataPackage.isEmpty()) {
							dataNum++;// 有数据的，记录下个数
						}
						
					}
				}
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info(
							"Hoke flush pool finished " + takeTime + "ms, pool size=" + POOL.size() + ", data size=" + dataNum);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("Hoke flush pool failed: ", e);
		}
	}

	/**
	 * 移除HokeData，守护线程不在对此数据做Hoke，
	 * 释放内存、磁盘的资源，
	 * 但是当方法重新被调用时，还会重新Hoke
	 * TODO:如果数据量过多，flushPool线程会长时间持有POOL锁，导致这里删除巨慢，有待改进
	 */
	public static boolean removeHokeData(String poolKey){
		synchronized (POOL) {
			if(POOL.containsKey(poolKey)){
				//#删除键值
				POOL.remove(poolKey);
			}else{
				return false;
			}
		}
		//#删除磁盘文件
		HokeStorageHelper.deleteCacheFile(poolKey);
		return true;
	}
	
	private static String generatePoolKey(Method method, Object[] params) {
		StringBuilder poolKey = new StringBuilder();
		poolKey.append(method.getDeclaringClass().getName()).append(".").append(method.getName()).append("_");
		int hashCode = 0;
		for (Object obj : params) {
			hashCode = hashCode + obj.hashCode();
		}
		poolKey.append(hashCode);
		return poolKey.toString();
	}
}
