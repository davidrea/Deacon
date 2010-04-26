package org.deacon.demo;

import org.deacon.DeaconResponse;
import org.deacon.interfaces.DeaconObserver;

public class DemoListener implements DeaconObserver{

	@Override
	public void notify(DeaconResponse response) {
		System.out.println("Listener got: " + response.getResponse());
		
	}
	
}
