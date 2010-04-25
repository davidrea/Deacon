package org.deacon.demo;

import org.deacon.DeaconObserver;
import org.deacon.DeaconResponse;

public class DemoListener implements DeaconObserver{

	@Override
	public void notify(DeaconResponse response) {
		System.out.println("Listener got: " + response.getResponse());
		
	}
	
}
