package org.axe.hoke.captain;

import java.util.List;

import org.axe.annotation.ioc.Component;
import org.axe.captain.bean.TeamTable;
import org.axe.captain.interface_.Captain;

@Component
public class HokeCaptain implements Captain{

	@Override
	public Object answerQuestion(String poolKey) {
		return whoHasHokeData(poolKey);
	}
	
	private String whoHasHokeData(String poolKey){
		List<String> teamTable = TeamTable.getTeamTableCopy();
		//#准备做Hash预映射
		int hashCode = poolKey.hashCode();
		int len = teamTable.size();
		int index = hashCode%len;
		if(index < 0){
			index = index+len;
		}
		return teamTable.get(index);
	}

}
