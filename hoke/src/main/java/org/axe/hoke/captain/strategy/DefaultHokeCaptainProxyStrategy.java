package org.axe.hoke.captain.strategy;

import java.util.List;

import org.axe.captain.bean.TeamTable;
import org.axe.hoke.captain.interface_.HokeCaptainStrategy;

/**
 * 默认的 Hoke 负责代理的 Captain的策略实现
 * Created by CaiDongYu on 2016年6月16日 上午10:09:50.
 */
public class DefaultHokeCaptainProxyStrategy implements HokeCaptainStrategy{

	@Override
	public String doStrategy(String url) {
		List<String> teamTable = TeamTable.getTeamTableCopy();
		//#准备做Hash预映射
		//#默认策略，去掉url后面的查询字符串
		//#剩下的执行一样的hash策略
		if(url.indexOf("?")>=0){
			url = url.substring(0,url.indexOf("?"));
		}
		int hashCode = url.hashCode();
		int len = teamTable.size();
		int index = hashCode%len;
		if(index < 0){
			index = index+len;
		}
		return teamTable.get(index);
	}
}
