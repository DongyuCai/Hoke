package org.axe.hoke.captain.rest;

import org.axe.annotation.ioc.Autowired;
import org.axe.annotation.ioc.Controller;
import org.axe.annotation.mvc.Request;
import org.axe.annotation.mvc.RequestParam;
import org.axe.constant.RequestMethod;
import org.axe.hoke.captain.helper.HokeCaptainHelper;

/**
 * Hoke 集群提供的代理服务
 * 任一一台Hoke机器都可以提供，
 * 接收到请求后，Hoke还是会向集群中的Captain询问，
 * Captain计算并验证得到可用机器后，告诉Hoke，
 * Hoke返回给Client端
 * 并不返回数据结果，只是代为找到数据机器的地址
 * Created by CaiDongYu on 2016年6月16日 上午9:49:36.
 */
@Controller(basePath="hoke")
public class HokeCaptainController {
	
	@Autowired
	private HokeCaptainHelper hokeCaptainHelper;
	
	@Request(value="proxy", method=RequestMethod.GET)
	public String proxy(@RequestParam("url")String url){
		return hokeCaptainHelper.askCaptainProxyUrl(url);
	}
}
