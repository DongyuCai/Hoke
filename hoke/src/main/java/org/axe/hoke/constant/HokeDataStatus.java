package org.axe.hoke.constant;

/**
 * Hoke Data 的数据状态
 * Created by CaiDongYu on 2016年6月13日 上午8:28:42.
 */
public enum HokeDataStatus {
	WAITING(0,"等待刷新"),
	FLUSHING(1,"正在刷新"),
	SUCCESS(2,"刷新成功"),
	FAILED(3,"刷新失败");
	
	public int code;
	public String desc;
	private HokeDataStatus(int code, String desc) {
		this.code = code;
		this.desc = desc;
	}
}
