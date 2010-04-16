package org.deacon;

import java.io.IOException;
import java.net.UnknownHostException;

public class DemoMain {

	
	
	public static void main(String args[]) throws UnknownHostException, IOException{
		DemoListener myListener = new DemoListener();
		
		DeaconService myService = new DeaconService("data.meteorserver.org",80,"/push/auswee/longpoll/demo");
		
		myService.register(myListener);
		
		System.out.println("Starting Deacon Service for: "+myService.toString());
		myService.start();
		try {
			Thread.sleep(10*1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Stopping Deacon Service");
		myService.stop();
		
	}
}
