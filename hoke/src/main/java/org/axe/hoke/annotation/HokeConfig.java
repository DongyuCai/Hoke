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
	boolean lazy() default false;
	
	/**
	 * 异步数据同步间隔时间
	 * 这是个超时阈值（读yu zhi :)）
	 * 默认0，HokeThread会不停更新这个调用
	 */
	long timeOut() default 0;
	
	/**
	 * 数据缓存策略
	 * 默认使用内存存储
	 * (内存方案不代表不会使用到磁盘，内存方案会建立默认文件夹与缓存文件，缓存仅仅用于框架重启时候的数据重建)
	 */
	String cacheFile() default "";
}
