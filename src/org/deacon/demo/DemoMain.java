package org.deacon.demo;

import java.io.IOException;
import java.net.UnknownHostException;

import org.deacon.DeaconService;

public class DemoMain {

	
	
	public static void main(String args[]) throws UnknownHostException, IOException{
		DemoListener myListener = new DemoListener();
		
		//DeaconService myService = new DeaconService("data.meteorserver.org",80);
		DeaconService myService = new DeaconService("home.daverea.com",4670);
		myService.joinChannel("test", 0);
		
		myService.register(myListener);
		
		System.out.println("Starting Deacon Service for: "+myService.toString());
		myService.start();
		try {
			Thread.sleep(15*1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Stopping Deacon Service");
		myService.stop();
		
	}
}
