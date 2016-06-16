package org.axe.hoke.helper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import org.axe.util.FileUtil;
import org.axe.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hoke 存储 助手类
 * Created by CaiDongYu on 2016年6月8日 下午12:36:40.
 */
public final class HokeStorageHelper {
	private static Logger LOGGER = LoggerFactory.getLogger(HokeStorageHelper.class);
	
	public static synchronized void clear(){
		File dir = getCacheFileDir();
		if(dir.exists()){
			if(dir.isDirectory()){
				dir.delete();
			}
		}
	}
	
	public static synchronized void deleteCacheFile(String poolKey){
		try {
			File cacheFile = getCacheFile(poolKey);
			if(cacheFile.exists()){
				cacheFile.delete();
			}
		} catch (Exception e) {
			LOGGER.error("hoke storage delete cacheFile failed",e);
			throw e;
		}
	}
	
	public static synchronized void saveData(String poolKey, Object data)throws Exception{
		try {
			File cacheFile = getCacheFile(poolKey);
			BufferedWriter writer = new BufferedWriter(new FileWriter(cacheFile));
			String json = JsonUtil.toJson(data);
			writer.write(json);
			writer.close();
		} catch (Exception e) {
			LOGGER.error("hoke storage saveData failed",e);
			throw e;
		}
	}
	
	public static <T>T getData(String poolKey,Class<T> dataType){
		T data = null;
		File cacheFile = getCacheFile(poolKey);
		if(cacheFile.exists()){
			try {
				StringBuilder buf = new StringBuilder();
				BufferedReader reader = new BufferedReader(new FileReader(cacheFile));
				String line = null;
				while((line = reader.readLine()) != null){
					buf.append(line);
				}
				reader.close();
				
				data = JsonUtil.fromJson(buf.toString(), dataType);
			} catch (Exception e) {
				e.printStackTrace();
				LOGGER.error("get data failed",e);
			}
		}
		return data;
	}
	
	public static File getCacheFile(String poolKey){
		String cacheFileDirStr = HokeConfigHelper.getCacheFileDir();
		String cacheFileStr = cacheFileDirStr.endsWith("/") || cacheFileDirStr.endsWith("\\")?
									cacheFileDirStr+poolKey:
									cacheFileDirStr+"/"+poolKey;
		File cacheFile = FileUtil.createFile(cacheFileStr);
		return cacheFile;
	}
	
	public static File getCacheFileDir(){
		return new File(HokeConfigHelper.getCacheFileDir());
	}
}
