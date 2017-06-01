package com.yjz.microweb.http;

public interface InstanceManager {
	
	public <T> T instance(Class<T> clazz) throws InstantiationException, IllegalAccessException;

}
