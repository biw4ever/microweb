package com.yjz.microweb.util;

public class StringUtil {
	
	private StringUtil(){}
	
	public static boolean isEmpty(String msg){
		if(null == msg || "".equals(msg)){
			return true;
		}
		return false;
	}
	
	public static boolean isNotEmpty(String msg){
		if(null != msg && !"".equals(msg)){
			return true;
		}
		return false;
	}

}
