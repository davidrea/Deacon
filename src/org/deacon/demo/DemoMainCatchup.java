package org.deacon.demo;

import java.io.IOException;
import java.net.UnknownHostException;

import org.deacon.DeaconError;
import org.deacon.DeaconResponse;
import org.deacon.DeaconService;
import org.deacon.interfaces.DeaconServiceObserver;

public class DemoMainCatchup implements DeaconServiceObserver{
	
	public static void main(String args[]) throws UnknownHostException, IOException {
		DemoMainCatchup testMain = new DemoMainCatchup();
		testMain.run();
	}
	
	public boolean error = false;
	
	public void run() throws UnknownHostException, IOException {
		DeaconService myService = new DeaconService("home.daverea.com",4670);
		myService.joinChannel("2sec", 3);
		
		myService.register(this);
		myService.catchUpTimeOut(10);
		
		// Start Deacon
		System.out.println("Starting Deacon Service for: "+myService.toString());
		myService.start();
		try {
			Thread.sleep(5*1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// Stop Deacon
		System.out.println("Stopping Deacon");
		myService.stop();
		try {
			Thread.sleep(5*1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// Restart Deacon
		System.out.println("Restarting Deacon");
		myService.start();
		try {
			Thread.sleep(5*1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		if(!error){
			System.out.println("Stopping Deacon Service");
			myService.stop();
		}
		else{
			System.out.println("Oops, there was an error. Move along, nothing to see...");
		}
	}

	@Override
	public void onPush(DeaconResponse response) {
		System.out.println("DemoMain.onPush on channel="+response.getChannel()+", got payload="+response.getPayload());
	}

	@Override
	public void onError(DeaconError err) {
		error = true;
		System.out.println("What the deuce, there was an ERROR: "+err.getErrorMsg());
	}

	@Override
	public void onReconnect() {
		error=false;
		System.out.println("Oh Hai!~ I'm reconnected");
	}
	
}
