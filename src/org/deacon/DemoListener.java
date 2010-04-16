package org.deacon;

public class DemoListener implements DeaconObserver{

	@Override
	public void notify(DeaconResponse response) {
		System.out.println("Listener got: " + response.getResponse());
		
	}
	
}
