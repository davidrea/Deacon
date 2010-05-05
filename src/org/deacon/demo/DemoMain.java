package org.deacon.demo;

import java.io.IOException;
import java.net.UnknownHostException;

import org.deacon.DeaconError;
import org.deacon.DeaconResponse;
import org.deacon.DeaconService;
import org.deacon.interfaces.DeaconServiceObserver;

public class DemoMain implements DeaconServiceObserver{
	
	public static void main(String args[]) throws UnknownHostException, IOException {
		DemoMain testMain = new DemoMain();
		testMain.run();
	}
	
	public boolean error = false;
	
	public void run() throws UnknownHostException, IOException {
		//DeaconService myService = new DeaconService("data.meteorserver.org",80);
//		DeaconService myService = new DeaconService("home2.daverea.com",4670);
		DeaconService myService = new DeaconService("home.daverea.com",4670);
		myService.joinChannel("2sec", 0);
		
		myService.register(this);
		
		System.out.println("Starting Deacon Service for: "+myService.toString());
		myService.start();
		try {
			Thread.sleep(15*1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
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
