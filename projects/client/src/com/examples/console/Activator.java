package com.examples.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import com.liferay.training.service.hello.HelloService;

public class Activator implements BundleActivator {
	Random rand=new Random();
	private HelloService[] services;
	
	private int portNumber=4321;
	private boolean keepRunning=true;
	private BundleContext bundleContext;

	@Override
	public void start(BundleContext bc) throws Exception {
		bundleContext=bc;
		int c = setServices();
		for(int i=0;i<10;i++){System.out.println(services[rand.nextInt(c)].say("hello")); }
		startTCPServer(portNumber);
	}


	private void startTCPServer(int port) {
		System.out.println("starting tcp server on port "+port);
		new Thread(()->{try { runServer(port);
		} catch (Exception e) { 
			System.out.println("couldn't start TCP server");
			e.printStackTrace(); 
			}}).start();
	}


	private int setServices(int impl) throws InvalidSyntaxException {
		setServices();
		HelloService[] newServices = new HelloService[1];
		try{
			newServices[0]=services[impl];
			services=newServices;
			System.out.println("services set to service"+impl);
			return 1;
		}catch(Exception e){
			System.out.println("could not set services to service"+impl);
			return services.length;
		}
	}
	private int setServices() throws InvalidSyntaxException {
		ServiceReference<?>[] refs = bundleContext.getServiceReferences(HelloService.class.getName(), null);
		int c=0;
		HelloService[] newServices=new HelloService[refs.length];
		for(ServiceReference<?>r:refs)newServices[c++]=(HelloService)bundleContext.getService(r);
		services=newServices;
		System.out.println(c+" services set");
		return c;
	}


	public void stop(BundleContext context) throws Exception {
		System.out.println("stopping client...");
		//service = null;
	}

	public void runServer(int port) throws IOException {
		System.out.println("[Hello Client] running server on port "+port);
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(port);
		  Socket clientSocket = serverSocket.accept();
		  PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
		  out.print("_________________________________\nWelcome to the FAKE gogo shell!!\n\n\nf! ");
		  out.flush();
	      System.out.println("socket created");
	      BufferedReader in = new BufferedReader( new InputStreamReader(clientSocket.getInputStream()));
	      String line=in.readLine().trim();
	      while ( line != null && keepRunning) {
	    	  System.out.println(String.format("read line of %s chars=%s", line.length(),line));
	    	  switch(line.trim().split(" ")[0]){
	    	  case "@": setServices(); break;
	    	  case "@1": setServices(0); break;
	    	  case "@2": setServices(1); break;
	    	  case "@port": 
	    		Integer newPort=null;
	    		try{
	    			newPort=Integer.parseInt(line.trim().split(" ")[1]);
	    			if(newPort!=null)startTCPServer(newPort);
	    		}catch(Exception e){
	    			System.out.println("couldn't start new TCP server when asked this: "+line);
	    		}
	    		break;
	    	  default:
	    		  if(line.length()>0) out.println(services[rand.nextInt(services.length)].say(line));
	    		  else out.println(services[rand.nextInt(services.length)].say());
	    		  break;
	    	  }
	    	  out.print("f! ");
	          out.flush();
	          line=in.readLine().trim();
	      }
	      serverSocket.close();
	      clientSocket.close();
		}catch(Exception e){
	      serverSocket.close();
		}
	}
	
	public static void main(String[] args){
		Activator activator=new Activator();
		activator.startTCPServer(1234);
		activator.startTCPServer(1235);
		activator.startTCPServer(1236);
	}


}