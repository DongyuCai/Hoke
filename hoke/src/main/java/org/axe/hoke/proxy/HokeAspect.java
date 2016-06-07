package org.axe.hoke.proxy;

import java.lang.reflect.Method;

import org.axe.annotation.aop.Aspect;
import org.axe.hoke.annotation.Hoke;
import org.axe.hoke.annotation.HokeConfig;
import org.axe.hoke.core.HokePool;
import org.axe.interface_.proxy.Proxy;
import org.axe.proxy.base.ProxyChain;

/**
 * 事务代理
 * 代理所有 @Service注解的类
 * 只增强 @Tns注解的方法
 * Created by CaiDongYu on 2016/4/19.
 */
@Aspect(Hoke.class)
public class HokeAspect implements Proxy {
//    private static final Logger LOGGER = LoggerFactory.getLogger(HokeAspect.class);


	@Override
	public Object doProxy(ProxyChain chain) throws Throwable {
		Method method = chain.getTargetMethod();
		if(method.isAnnotationPresent(HokeConfig.class)){
			//TODO:Hoke 接管，这可能导致其他的Aop切面失效！目前没有好办法
        	return HokePool.getData(chain.getTargetObject(),method,chain.getMethodParams(),chain.getMethodProxy());
        }else{
        	return chain.doProxyChain();
        }
	}
	

}
