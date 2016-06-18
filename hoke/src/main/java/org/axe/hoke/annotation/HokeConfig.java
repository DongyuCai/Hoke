package org.axe.hoke.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * HokeConfig 注解
 * Hoke(类注解)+HokeConfig(方法注解)才能开启Hoke服务
 * Hoke 类似 Service，被Hoke注解过的类，实例也是托管给BeanHelper
 * Hoke 提供缓存方法执行结果的服务
 * Created by CaiDongYu on 2016年6月7日 上午10:15:55.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface HokeConfig {

	/**
	 * 是否需要懒加载，
	 * 开启后首次调用方法返回结果是null，
	 * 短时间接下去调用可能依然是null，
	 * 直到方法结果被作为异步任务执行完成。
	 * 默认关闭。
	 */
	boolean lazyLoad() default false;
	
	/**
	 * 异步数据同步间隔时间，
	 * 单位是秒，
	 * 这是个超时阈值（读yu zhi :)），
	 * 默认0，HokeThread会不停更新这个调用。
	 * 需要注意的是，更新周期有可能会比这个时间长。
	 */
	long refreshSeconds() default 0;
	
	/**
	 * Hoke数据超时时间，
	 * 单位是秒，默认86400s，表示超过一天没被访问，就会移除托管，<=0表示无限制，
	 * 每次被刷入内存后这个时间会与刷入内存的时间做和，
	 * 和值就是这个数据的死亡时间，
	 * 当系统时间超过死亡时间，数据会被移除，
	 * 移除包括内存和磁盘，
	 * HokeThread守护线程也不再更新这个数据，
	 * 因为他会连PoolKey都不存在了。
	 * 说明：如果这个数据超过timeOut秒没被访问，就会被移出托管。
	 */
	long timeOut() default 86400;
}
