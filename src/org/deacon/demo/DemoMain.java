package org.deacon.demo;

import java.io.IOException;
import java.net.UnknownHostException;

import org.deacon.DeaconResponse;
import org.deacon.DeaconService;
import org.deacon.interfaces.OnPushListener;

public class DemoMain implements OnPushListener{

	public static void main(String args[]) throws UnknownHostException, IOException {
		DemoMain testMain = new DemoMain();
		testMain.run();
	}
	
	public void run() throws UnknownHostException, IOException {
		//DeaconService myService = new DeaconService("data.meteorserver.org",80);
		DeaconService myService = new DeaconService("home.daverea.com",4670);
		myService.joinChannel("10sec", 0);
		
		myService.setOnPushListener(this);
		
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

	@Override
	public void onPush(DeaconResponse response) {
		System.out.println("DemoMain.onPush on channel="+response.getChannel()+", got payload="+response.getPayload());
	}
}
