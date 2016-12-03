package com.liferay.training.service.hello.impl;
import com.liferay.training.service.hello.HelloService;

public class HelloServiceImpl implements HelloService {

private int _responseCount;

	/**
	 * Returns a string response
	 */
	@Override
	public String say() {
		return "Hello ...";
	}

	@Override
	public String say(String response) {
		return "Hello ..."+response;
	}
}
