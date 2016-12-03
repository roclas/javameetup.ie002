package com.example.helloWorld;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {
	private int counter=0;
	private boolean keepGoing=true;
	private Thread th0;

	@Override
	public void start(BundleContext bc) throws Exception {
		System.out.println("starting hello world Thread");
		th0 = new Thread(()->{
			while(keepGoing){
				System.out.println(counter+++" hello world !!!");
				try { Thread.sleep(500);
				} catch (Exception e) { e.printStackTrace(); }
			}
		});
		th0.start();
	}

	public void stop(BundleContext context) throws Exception {
		System.out.println("stopping client...");
		keepGoing=false;
		th0.join();
		System.out.println("stopped!!");
	}
}