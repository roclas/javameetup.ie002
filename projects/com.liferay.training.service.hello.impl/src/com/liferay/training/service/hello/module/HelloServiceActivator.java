package com.liferay.training.service.hello.module;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.liferay.training.service.hello.HelloService;
import com.liferay.training.service.hello.impl.HelloServiceImpl;

public class HelloServiceActivator implements BundleActivator {

	private HelloService _helloService;
	@Override
	public void start(BundleContext bundleContext) throws Exception {
		System.out.println("[Hello Service] Starting Module...");

		HelloService helloService = new HelloServiceImpl();
		_helloService = helloService;
		
		bundleContext.registerService(HelloService.class.getName(), _helloService, null);
	}

	@Override
	public void stop(BundleContext arg0) throws Exception {
		System.out.println("[Hello Service] Stopping Module...");
	
	}

}
