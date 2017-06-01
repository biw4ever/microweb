package com.yjz.microweb.http;

public class InstanceManagerDefault implements InstanceManager {

	@Override
	public <T> T instance(Class<T> clazz) throws InstantiationException, IllegalAccessException {
		return clazz.newInstance();
	}

}
