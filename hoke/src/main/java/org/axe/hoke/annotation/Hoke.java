package org.axe.hoke.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * Hoke 注解
 * Hoke 类似 Service，被Hoke注解过的类，实例也是托管给BeanHelper
 * Hoke 提供缓存方法执行结果的服务
 * Created by CaiDongYu on 2016年6月7日 上午10:15:55.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Hoke {

}
