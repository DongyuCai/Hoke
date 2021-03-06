package org.axe.hoke.core;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.axe.hoke.annotation.HokeConfig;
import org.axe.hoke.bean.HokeDataPackage;
import org.axe.hoke.bean.HokeDataPool;
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
	private static final HokeDataPool POOL;
	private static final ReentrantLock POOL_LOCK = new ReentrantLock();

	/**
	 * 等待被缓存的键
	 */
	private static final List<KeyValue<String, HokeDataPackage>> POOL_TASK_QUEE;
	private static final ReentrantLock POOL_TASK_QUEE_LOCK = new ReentrantLock();

	static {
		POOL = new HokeDataPool();
		POOL_TASK_QUEE = new ArrayList<>();
		init();
	}
	
	private static void init(){
		try {
			POOL_LOCK.lock();
			POOL_TASK_QUEE_LOCK.lock();
			
			POOL.clear();
			POOL_TASK_QUEE.clear();
			HokeStorageHelper.clear();
		} catch (Exception e) {
			LOGGER.error("HokePool init failed",e);
		} finally {
			try {
				POOL_LOCK.unlock();
			} catch (Exception e2) {}
			try {
				POOL_TASK_QUEE_LOCK.unlock();
			} catch (Exception e2) {}
		}
	}

	public static Map<String, HokeDataPackage> getPool() {
		return POOL;
	}
	
	public static Object getData(Object obj, Method method, Object[] params, MethodProxy methodProxy) throws Throwable {
		// #根据参数，计算出数据存放的key
		String poolKey = generatePoolKey(method, params);
		HokeDataPackage hokeDataPackage = POOL.get(poolKey);
		Object data = null;
		if(hokeDataPackage == null){
			try {
				POOL_LOCK.lock();
				hokeDataPackage = POOL.get(poolKey);
				if (hokeDataPackage == null) {
					hokeDataPackage = addTask2Quee(poolKey, method, obj, params, methodProxy);
					data = getNativeData(hokeDataPackage,method);
					POOL.put(poolKey, hokeDataPackage);
				}
			} catch (Exception e) {
				LOGGER.error("Hoke getData failed",e);
			} finally {
				try {
					POOL_LOCK.unlock();
				} catch (Exception e2) {}
			}
		}else{
			data = hokeDataPackage.getData();
		}
		
		return data;
	}
	
	private static Object getNativeData(HokeDataPackage hokeDataPackage, Method method) throws Throwable{
		// #缓存无法提供，如果还不支持异步加载，那就强制获取
		HokeConfig annotation = method.getAnnotation(HokeConfig.class);
		if(hokeDataPackage != null ){
			if (!annotation.lazyLoad()) {
				if(hokeDataPackage.getData() == null){
					hokeDataPackage.flushData();
				}
			}
			return hokeDataPackage.getData();
		}else{
			return null;
		}
	}
	
	private static HokeDataPackage addTask2Quee(String poolKey, Method method, Object obj, Object[] params, MethodProxy methodProxy){
		HokeConfig config = method.getAnnotation(HokeConfig.class);
		long start = 0;
		if (LOGGER.isInfoEnabled()) {
			start = System.currentTimeMillis();
			LOGGER.debug("Hoke add task try");
		}
		HokeDataPackage hokeDataPackage = new HokeDataPackage(poolKey, obj, params, method, methodProxy, config);
		try {
			POOL_TASK_QUEE_LOCK.lock();
			if (LOGGER.isInfoEnabled()) {
				long end = System.currentTimeMillis() - start;
				LOGGER.debug("Hoke add task start " + end + "ms");
			}
			POOL_TASK_QUEE.add(new KeyValue<String, HokeDataPackage>(poolKey, hokeDataPackage));
		} catch (Exception e) {
			LOGGER.error("Hoke addTask2Quee failed",e);
		} finally {
			try {
				POOL_TASK_QUEE_LOCK.unlock();
			} catch (Exception e2) {}
		}
		if (LOGGER.isInfoEnabled()) {
			long end = System.currentTimeMillis() - start;
			LOGGER.debug("Hoke add task finished " + end + "ms");
		}
		return hokeDataPackage;
	}

	/**
	 * 刷新taskQuee到Poll里
	 */
	public static void flushTaskQuee() {
		try {
			long start = 0;
			if (LOGGER.isInfoEnabled()) {
				start = System.currentTimeMillis();
				LOGGER.debug("Hoke flush task quee try");
			}
			
			if(POOL_LOCK.tryLock()){
				if (LOGGER.isInfoEnabled()) {
					long end = System.currentTimeMillis() - start;
					LOGGER.debug("Hoke flush task quee start " + end + "ms");
				}
				// ##只要抢到了锁，这段代码执行时间会较短
				if(POOL_TASK_QUEE_LOCK.tryLock()){
					for (KeyValue<String, HokeDataPackage> task : POOL_TASK_QUEE) {
						String key = task.getKey();
						if (POOL.containsKey(key))
							continue;

						POOL.put(key, task.getValue());
					}
					// #清空Task队列
					POOL_TASK_QUEE.clear();
					
					// 清理POOL中已经超时托管的数据
					// 这里主要目的不是要快，而是要节省内存
					// 因为flushTaskQuee守护线程耗时并不大
					// 所以这里详细判断后才决定要不要NEW_POOL
					if(POOL.needFullGc()){
						Map<String,HokeDataPackage> NEW_POOL = new HashMap<>();
						long now = System.currentTimeMillis();
						for (Map.Entry<String, HokeDataPackage> entry : POOL.entrySet()) {
							HokeDataPackage hokeDataPackage = entry.getValue();
							if (hokeDataPackage.getTimeOutTime() > 0 && hokeDataPackage.getTimeOutTime() < now) continue;
							
							//#挑选没有托管超时的留下
							NEW_POOL.put(hokeDataPackage.getPoolKey(), hokeDataPackage);
						}
						//#任然判断一下，是否真的有托管超时的
						if(NEW_POOL.size() < POOL.size()){
							POOL.clear();
							POOL.putAll(NEW_POOL);
						}
						NEW_POOL.clear();
						NEW_POOL = null;
						//#防止队列线程紧挨着再次抢到执行权，这里没必要再次FullGC
						POOL.setFullGc(false);
					}
					if (LOGGER.isInfoEnabled()) {
						long end = System.currentTimeMillis() - start;
						LOGGER.debug("Hoke flush task quee finished " + end + "ms");
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error("Hoke flush task quee failed: ", e);
		} finally {
			try {
				POOL_LOCK.unlock();
			} catch (Exception e2) {}
			try {
				POOL_TASK_QUEE_LOCK.unlock();
			} catch (Exception e2) {}
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
				LOGGER.debug("Hoke flush pool try");
			}
			if(POOL_LOCK.tryLock()){
				if (LOGGER.isInfoEnabled()) {
					long end = System.currentTimeMillis() - start;
					LOGGER.debug("Hoke flush pool start " + end + "ms");
				}

				// #刷数据
				
				//#刷新POOL总共耗时
				long takeTime = 0;
				// #有data数据的个数
				// TODO:这里是单线程刷新所有数据，有待改成数据线程一对一
				int dataNum = 0;
				POOL.setFullGc(false);//每次扫描前，先默认不需要FullGC
				for (Map.Entry<String, HokeDataPackage> entry : POOL.entrySet()) {
					HokeDataPackage hokeDataPackage = entry.getValue();
					long now = System.currentTimeMillis();
					// #如果托管超时，那么通知FullGC
					if (hokeDataPackage.getTimeOutTime() > 0 && hokeDataPackage.getTimeOutTime() < now) {
						POOL.setFullGc(true);
						//#这个数据不需要再刷新
						continue;
					}
					// #内存数据超时判断
					if (hokeDataPackage.getNextFlushMemTime() < now) {
						hokeDataPackage.clearData();
					}
					// #磁盘数据超时判断
					if (hokeDataPackage.getNextFlushDiskTime() < now) {
						hokeDataPackage.flushData();
					}
					if (LOGGER.isInfoEnabled()) {
						LOGGER.debug("Hoke flush [" + hokeDataPackage.getPoolKey() + "] " + (hokeDataPackage.getTakeTime()) + "ms");
						
						takeTime = takeTime+hokeDataPackage.getTakeTime();
						
						if (!hokeDataPackage.isEmpty()) {
							dataNum++;// 有数据的，记录下个数
						}
						
					}
				}
				if (LOGGER.isInfoEnabled()) {
					LOGGER.debug(
							"Hoke flush pool finished " + takeTime + "ms, pool size=" + POOL.size() + ", data size=" + dataNum);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("Hoke flush pool failed: ", e);
		} finally {
			try {
				POOL_LOCK.unlock();
			} catch (Exception e2) {}
		}
	}

	/**
	 * 移除HokeData，守护线程不在对此数据做Hoke，
	 * 释放内存、磁盘的资源，
	 * 但是当方法重新被调用时，还会重新Hoke
	 * TODO:如果数据量过多，flushPool线程会长时间持有POOL锁，导致这里删除巨慢，有待改进
	 */
	public static boolean removeHokeData(String poolKey){
		try {
			if(POOL_LOCK.tryLock()){
				if(POOL.containsKey(poolKey)){
					//#删除键值
					POOL.remove(poolKey);
					return true;
				}else{
					return false;
				}
			}else{
				return false;
			}
		} catch (Exception e) {
			LOGGER.error("Hoke removeHokeData failed",e);
			return false;
		} finally {
			try {
				//#删除磁盘文件
				HokeStorageHelper.deleteCacheFile(poolKey);
			} catch (Exception e2) {}
			
			try {
				POOL_LOCK.unlock();
			} catch (Exception e2) {}
		}
	}
	
	public static String generatePoolKey(Method method, Object[] params) {
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
