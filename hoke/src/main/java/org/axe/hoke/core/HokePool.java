package org.axe.hoke.core;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.axe.hoke.annotation.HokeConfig;
import org.axe.hoke.bean.HokeDataPackage;
import org.axe.hoke.bean.KeyValue;
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
	}

	public static Object getData(Object obj, Method method, Object[] params, MethodProxy methodProxy) {
		// #根据参数，计算出数据存放的key
		String poolKey = generatePoolKey(method, params);
		Object data = POOL.get(poolKey);
		if (data == null) {
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

			/*
			 * synchronized (POOL) { try { if(POOL.containsKey(poolKey)){ data =
			 * POOL.get(poolKey); }else{ //放着，等HokeThread更新 HokeConfig config =
			 * method.getAnnotation(HokeConfig.class); POOL.put(poolKey, new
			 * HokeDataPackage(obj, params, methodProxy, config.timeOut(), 0,
			 * config.cacheFile())); } } catch (Exception e) {
			 * e.printStackTrace(); LOGGER.error("Hoke get data failed: ",e); }
			 * }
			 */

			// #缓存无法提供，如果还不支持异步加载，那就强制获取
			HokeConfig annotation = method.getAnnotation(HokeConfig.class);
			if (!annotation.lazy()) {
				try {
					data = methodProxy.invokeSuper(obj, params);
				} catch (Throwable e) {
					e.printStackTrace();
					LOGGER.error("force get data failed", e);
				}
			}

		}

		return data != null && data instanceof HokeDataPackage ? ((HokeDataPackage) data).getData() : data;
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
			// #有data数据的个数
			int dataNum = 0;

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
				// TODO:这里是单线程刷新所有数据，有待改成数据线程一对一
				for (Map.Entry<String, HokeDataPackage> entry : POOL.entrySet()) {
					HokeDataPackage dataPackage = entry.getValue();
					long now = System.currentTimeMillis();
					// #内存数据超时判断
					if (dataPackage.getFlushMemTimeOutTime() < now) {
						dataPackage.clearData();
					}
					// #磁盘数据超时判断
					if (dataPackage.getFlushDiskTimeOutTime() < now) {
						dataPackage.flushData();
					}
					if (dataPackage.isNotEmpty()) {
						dataNum++;// 有数据的，记录下个数
					}
					if (LOGGER.isDebugEnabled()) {
						now = System.currentTimeMillis() - now;
						LOGGER.debug("Hoke flush [" + entry.getKey() + "] " + (now) + "ms");
					}
				}
			}
			if (LOGGER.isInfoEnabled()) {
				long end = System.currentTimeMillis() - start;
				LOGGER.info(
						"Hoke flush pool finished " + end + "ms, pool size=" + POOL.size() + ", data size=" + dataNum);
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("Hoke flush pool failed: ", e);
		}
	}

	private static String generatePoolKey(Method method, Object[] params) {
		StringBuilder poolKey = new StringBuilder();
		poolKey.append(method.getDeclaringClass().getName()).append(".").append(method.getName()).append("_")
				.append(method.toString().hashCode()).append("_");
		int hashCode = 0;
		for (Object obj : params) {
			hashCode = hashCode + obj.hashCode();
		}
		poolKey.append(hashCode);
		return poolKey.toString();
	}
}
