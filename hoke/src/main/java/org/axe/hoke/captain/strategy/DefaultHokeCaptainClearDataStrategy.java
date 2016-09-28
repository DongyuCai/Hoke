package org.axe.hoke.captain.strategy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.axe.captain.bean.TeamTable;
import org.axe.hoke.captain.helper.HokeCaptainHelper;
import org.axe.hoke.captain.interface_.HokeCaptainStrategy;
import org.axe.util.JsonUtil;

/**
 * 默认的 负责清理数据的Captain的策略实现
 * Created by CaiDongYu on 2016年6月16日 上午10:09:50.
 */
public class DefaultHokeCaptainClearDataStrategy implements HokeCaptainStrategy{

	@Override
	public String doStrategy(String hostAndPoolKey) {
		
		//#根据question解析出host和poolKey
		//#根据Team表里的组员，除了host以外的，都通知清理数据，参数是poolKey
		try {
			Map<?,?> map = JsonUtil.fromJson(hostAndPoolKey, HashMap.class);
			if(map.containsKey("host") && map.containsKey("poolKey")){
				String host = String.valueOf(map.get("host"));
				String poolKey = String.valueOf(map.get("poolKey"));
				List<String> hosts = TeamTable.getTeamTableCopy();
				for(String h:hosts){
					//#谁请求的，就不用清理了，保留数据
					if(h.equals(host)) continue;
					
					//#其他的组员，都不要持有这个数据了
					try {
						HokeCaptainHelper.askMan4ClearData(h, poolKey);
					} catch (Exception e) {}
				}
			}
			return "1";
		} catch (Exception e) {
			return "0";
		}
	}
}
